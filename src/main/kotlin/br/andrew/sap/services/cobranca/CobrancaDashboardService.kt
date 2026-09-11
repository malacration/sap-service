package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAgregadoSap
import br.andrew.sap.model.cobranca.CobrancaDashboard
import br.andrew.sap.model.cobranca.CobrancaFaixa
import br.andrew.sap.model.cobranca.CobrancaMes
import br.andrew.sap.model.cobranca.CobrancaPorCobrador
import br.andrew.sap.model.cobranca.CobrancaPorFilial
import br.andrew.sap.model.cobranca.CobrancaPorStatus
import br.andrew.sap.model.cobranca.CobrancaRecuperadoDiaSap
import br.andrew.sap.model.cobranca.CobrancaRecuperadoSap
import br.andrew.sap.model.cobranca.CobrancaTrabalhadosSap
import br.andrew.sap.model.cobranca.FaixaAtraso
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Service
class CobrancaDashboardService(val sqlQueriesService: SqlQueriesService) {

    companion object {
        private const val SEM_ACOMPANHAMENTO = "Sem acompanhamento"
        private const val SEM_COBRADOR = "Sem cobrador"
        private val MESES = listOf(
            "jan", "fev", "mar", "abr", "mai", "jun",
            "jul", "ago", "set", "out", "nov", "dez",
        )
    }

    @Cacheable(
        "cobranca-dashboard",
        key = "'resumo-' + #auth.getIdInt() + '-' + #filiais + '-' + #vendedor + '-' + #cobrador + '-' + #de + '-' + #ate + '-' + #hoje",
    )
    fun resumo(
        auth: User,
        filiais: List<Int>? = null,
        vendedor: Int? = null,
        cobrador: String? = null,
        de: LocalDate,
        ate: LocalDate,
        hoje: LocalDate = LocalDate.now(),
    ): CobrancaDashboard {
        val escopo = escopo(auth, vendedor)
        val recorteDeFilial = filiais?.distinct()?.takeIf { it.isNotEmpty() }?.toSet()

        val faixas = mutableListOf<CobrancaFaixa>()
        val carteira = mutableListOf<CobrancaAgregadoSap>()
        for (faixa in FaixaAtraso.values()) {
            val linhas = carteiraDaFaixa(faixa, hoje, escopo)
                .daFilial(recorteDeFilial) { it.BPLId }
                .doCobrador(cobrador) { it.U_Cobrador }
            carteira.addAll(linhas)
            faixas.add(
                CobrancaFaixa(
                    Faixa = faixa.rotulo,
                    Saldo = linhas.somaSaldo(),
                    Parcelas = linhas.somaParcelas(),
                    DiasMin = faixa.diasMin,
                    DiasMax = faixa.diasMax,
                )
            )
        }

        val recuperado = buscarAgregado<CobrancaRecuperadoSap>(
            "cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql",
            escopo + listOf(Parameter("de", de.toString()), Parameter("ate", ate.toString())),
        ).daFilial(recorteDeFilial) { it.BPLId }.doCobrador(cobrador) { it.U_Cobrador }

        val diasDoPeriodo = ChronoUnit.DAYS.between(de, ate) + 1
        val ateAnterior = de.minusDays(1)
        val deAnterior = ateAnterior.minusDays(diasDoPeriodo - 1)
        val recuperadoAnterior = buscarAgregado<CobrancaRecuperadoSap>(
            "cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql",
            escopo + listOf(Parameter("de", deAnterior.toString()), Parameter("ate", ateAnterior.toString())),
        ).daFilial(recorteDeFilial) { it.BPLId }.doCobrador(cobrador) { it.U_Cobrador }

        // "Ninguem tocou" e ausencia de registro, logo ausencia de cobrador: escolher um
        // cobrador zera o card em vez de repetir o numero da empresa inteira dentro do recorte.
        val semAcao = if (cobrador != null) emptyList() else buscarAgregado<CobrancaAgregadoSap>(
            "cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql",
            escopo + listOf(Parameter("data", hoje.toString())),
        ).daFilial(recorteDeFilial) { it.BPLId }

        val promessas = buscarAgregado<CobrancaAgregadoSap>(
            "cobranca-promessa-vencida.sql", "cobranca-promessa-vencida-adiantamento.sql",
            escopo + listOf(Parameter("data", hoje.toString())),
        ).daFilial(recorteDeFilial) { it.BPLId }.doCobrador(cobrador) { it.U_Cobrador }

        val trabalhados = buscarAgregado<CobrancaTrabalhadosSap>(
            "cobranca-trabalhados.sql", "cobranca-trabalhados-adiantamento.sql",
            escopo + listOf(Parameter("de", de.toString()), Parameter("ate", ate.toString())),
        ).daFilial(recorteDeFilial) { it.BPLId }.doCobrador(cobrador) { it.U_Usuario }

        return CobrancaDashboard(
            De = de.toString(),
            Ate = ate.toString(),
            DeAnterior = deAnterior.toString(),
            AteAnterior = ateAnterior.toString(),
            CarteiraSaldo = carteira.somaSaldo(),
            CarteiraParcelas = carteira.somaParcelas(),
            Recuperado = recuperado.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.recuperado()) },
            RecuperadoAnterior = recuperadoAnterior.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.recuperado()) },
            RecuperadoDocumentos = recuperado.sumOf { it.documentos() },
            SemAcaoSaldo = semAcao.somaSaldo(),
            SemAcaoParcelas = semAcao.somaParcelas(),
            PromessaVencidaSaldo = promessas.somaSaldo(),
            PromessaVencidaParcelas = promessas.somaParcelas(),
            Faixas = faixas,
            PorFilial = porFilial(carteira),
            PorStatus = porStatus(carteira),
            PorCobrador = porCobrador(recuperado, trabalhados, promessas),
        )
    }

    @Cacheable(
        "cobranca-dashboard",
        key = "'evolucao-' + #auth.getIdInt() + '-' + #filiais + '-' + #vendedor + '-' + #cobrador + '-' + #meses + '-' + #hoje",
    )
    fun evolucao(
        auth: User,
        filiais: List<Int>? = null,
        vendedor: Int? = null,
        cobrador: String? = null,
        meses: Int = 6,
        hoje: LocalDate = LocalDate.now(),
    ): List<CobrancaMes> {
        val quantidade = meses.coerceIn(1, 24)
        val primeiroMes = YearMonth.from(hoje).minusMonths((quantidade - 1).toLong())

        val dias = buscarAgregado<CobrancaRecuperadoDiaSap>(
            "cobranca-recuperado-diario.sql", "cobranca-recuperado-diario-adiantamento.sql",
            escopo(auth, vendedor) + listOf(
                Parameter("de", primeiroMes.atDay(1).toString()),
                Parameter("ate", hoje.toString()),
            ),
        ).daFilial(filiais?.distinct()?.takeIf { it.isNotEmpty() }?.toSet()) { it.BPLId }
            .doCobrador(cobrador) { it.U_Cobrador }

        val porMes = dias.groupBy { YearMonth.from(LocalDate.parse(it.DocDate, DateTimeFormatter.BASIC_ISO_DATE)) }

        return (0 until quantidade).map { indice ->
            val mes = primeiroMes.plusMonths(indice.toLong())
            val linhas = porMes[mes] ?: emptyList()
            CobrancaMes(
                Mes = mes.toString(),
                Rotulo = "${MESES[mes.monthValue - 1]}/${mes.year % 100}",
                Recuperado = linhas.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.recuperado()) },
                Documentos = linhas.map { it.DocEntry }.distinct().size,
            )
        }
    }

    /**
     * O filtro de filial NAO vai pro SQL: o recorte e feito em Kotlin, pelo BPLId que toda view
     * do dashboard devolve. Uma passada por filial escolhida custava 18 consultas ao SAP por
     * filial - com as 18 da empresa marcadas, mais de 300 chamadas sequenciais pra montar uma
     * tela. Assim o custo fica constante: 18 consultas, escolha o usuario uma filial ou todas.
     *
     * De quebra some o multiplicador: filial e lista que vem da requisicao, entao sem isso uma
     * chamada com centenas de IDs (inclusive inexistentes) inundava o Service Layer, que e
     * recurso compartilhado. Agora 500 IDs na URL custam o mesmo que um.
     *
     * Os parametros continuam sendo enviados, sempre "sem filtro", porque as views mantem o
     * idioma ":filial ou :filialIsFilter" - e o teste que exige filtro opcional de filial e
     * vendedor em toda view do dashboard continua valendo.
     */
    private fun escopo(auth: User, vendedor: Int?): List<Parameter> {
        val vendedorEfetivo = CobrancaEscopo.vendedorEfetivo(auth, vendedor)
        return listOf(
            Parameter("filial", Int.MAX_VALUE),
            Parameter("filialIsFilter", Int.MAX_VALUE),
            Parameter("vendedor", vendedorEfetivo ?: Int.MAX_VALUE),
            Parameter("vendedorIsFilter", if (vendedorEfetivo == null) Int.MAX_VALUE else -1),
        )
    }

    private fun carteiraDaFaixa(faixa: FaixaAtraso, hoje: LocalDate, escopo: List<Parameter>): List<CobrancaAgregadoSap> {
        return buscarAgregado(
            "cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql",
            escopo + listOf(
                Parameter("vencimentoDe", faixa.vencimentoDe(hoje).toString()),
                Parameter("vencimentoAte", faixa.vencimentoAte(hoje).toString()),
            ),
        )
    }

    private inline fun <reified T : Any> buscarAgregado(
        viewNotaFiscal: String,
        viewAdiantamento: String,
        parametros: List<Parameter>,
    ): List<T> {
        return sqlQueriesService.getAll<T>(viewNotaFiscal, parametros) +
            sqlQueriesService.getAll<T>(viewAdiantamento, parametros)
    }

    private fun porFilial(carteira: List<CobrancaAgregadoSap>): List<CobrancaPorFilial> {
        return carteira.groupBy { it.BPLId }
            .map { (bplId, linhas) ->
                CobrancaPorFilial(
                    BPLId = bplId,
                    BPLName = linhas.firstOrNull { it.BPLName != null }?.BPLName,
                    Saldo = linhas.somaSaldo(),
                    Parcelas = linhas.somaParcelas(),
                )
            }
            .sortedByDescending { it.Saldo }
    }

    private fun porStatus(carteira: List<CobrancaAgregadoSap>): List<CobrancaPorStatus> {
        return carteira.groupBy { it.U_Status?.trim().orEmpty().ifEmpty { SEM_ACOMPANHAMENTO } }
            .map { (status, linhas) ->
                CobrancaPorStatus(
                    Status = status,
                    Saldo = linhas.somaSaldo(),
                    Parcelas = linhas.somaParcelas(),
                )
            }
            .sortedByDescending { it.Saldo }
    }

    private fun porCobrador(
        recuperado: List<CobrancaRecuperadoSap>,
        trabalhados: List<CobrancaTrabalhadosSap>,
        promessas: List<CobrancaAgregadoSap>,
    ): List<CobrancaPorCobrador> {
        val recuperadoPorCobrador = recuperado.groupBy { it.U_Cobrador.rotuloCobrador() }
        val trabalhadosPorCobrador = trabalhados.groupBy { it.U_Usuario.rotuloCobrador() }
        val promessasPorCobrador = promessas.groupBy { it.U_Cobrador.rotuloCobrador() }

        val cobradores = recuperadoPorCobrador.keys + trabalhadosPorCobrador.keys + promessasPorCobrador.keys
        return cobradores.map { cobrador ->
            CobrancaPorCobrador(
                Cobrador = cobrador,
                Recuperado = recuperadoPorCobrador[cobrador].orEmpty()
                    .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.recuperado()) },
                Documentos = recuperadoPorCobrador[cobrador].orEmpty().sumOf { it.documentos() },
                TitulosTrabalhados = trabalhadosPorCobrador[cobrador].orEmpty().sumOf { it.titulos() },
                ParcelasComPromessaVencida = promessasPorCobrador[cobrador].orEmpty().somaParcelas(),
            )
        }.sortedByDescending { it.Recuperado }
    }

    /**
     * Filtro de cobrador fica em Kotlin de proposito. No SQL ele esbarraria no parser do
     * SQLQueries, que recusa nao-ASCII - e nome de cobrador tem acento o tempo todo (ver
     * soAscii em CobrancaConsultaService). Aqui o volume e de agregado, algumas dezenas de
     * linhas por view, entao filtrar depois nao custa paginacao extra como custa na lista.
     */
    /**
     * Recorte de filial, tambem em Kotlin - ver escopo(). Nulo e "todas": nenhuma filial
     * escolhida nao vira lista vazia, senao o dashboard voltaria zerado em vez de completo.
     */
    private fun <T> List<T>.daFilial(filiais: Set<Int>?, bplId: (T) -> Int?): List<T> =
        if (filiais == null) this else filter { bplId(it) in filiais }

    private fun <T> List<T>.doCobrador(cobrador: String?, nome: (T) -> String?): List<T> =
        if (cobrador == null) this else filter { nome(it)?.trim() == cobrador }

    private fun String?.rotuloCobrador(): String = this?.trim().orEmpty().ifEmpty { SEM_COBRADOR }

    private fun List<CobrancaAgregadoSap>.somaSaldo(): BigDecimal =
        fold(BigDecimal.ZERO) { acc, linha -> acc.add(linha.saldo()) }

    private fun List<CobrancaAgregadoSap>.somaParcelas(): Int = sumOf { it.parcelas() }
}
