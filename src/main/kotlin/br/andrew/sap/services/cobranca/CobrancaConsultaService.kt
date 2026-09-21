package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAdiantamentoSap
import br.andrew.sap.model.cobranca.CobrancaCobradorSap
import br.andrew.sap.model.cobranca.CobrancaRecebimentoPeriodoSap
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.model.cobranca.CobrancaTituloSap
import br.andrew.sap.model.cobranca.CobrancaTituloVendedorSap
import br.andrew.sap.model.cobranca.CobrancaTitulosPagina
import br.andrew.sap.model.cobranca.CobrancaTitulosTotal
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class CobrancaConsultaService(val sqlQueriesService: SqlQueriesService) {

    private val formatoSap = DateTimeFormatter.BASIC_ISO_DATE
    private val formatoMesSap = DateTimeFormatter.ofPattern("yyyyMM")

    companion object {
        private const val SEM_FILTRO = "~"

        /**
         * Teto de paginas do SAP por view e por filial no totalizar. Com 3 filiais e as 2 views,
         * o pior caso e 600 idas sequenciais ao Service Layer - muito, mas sob demanda e uma vez,
         * contra o laco de "carregar tudo" pelo front, que recomeca da primeira pagina a cada
         * pagina pedida (buscarAte nao tem cursor) e custaria K²/2.
         */
        private const val TETO_PAGINAS_SAP = 100
    }

    /** Linhas ja filtradas pelas DUAS camadas (SQL + Kotlin) e se a varredura parou no teto. */
    class BuscaCombinada(val linhas: List<CobrancaTitulo>, val truncado: Boolean) {
        companion object {
            val VAZIA = BuscaCombinada(emptyList(), false)
        }
    }

    private fun statusParcelaDe(situacaoSap: String?): String = when (situacaoSap) {
        // PAGO_PARCIAL busca o mesmo balde do SAP que ABERTO (Status='O') - a distincao fina
        // fica so em passaNosFiltrosLocais, que ja compara SituacaoSap calculado.
        "ABERTO", "PAGO_PARCIAL" -> "O"
        "PAGO" -> "C"
        else -> SEM_FILTRO
    }

    /**
     * O parser de parametros do SQLQueries do SAP B1 recusa qualquer caractere nao-ASCII:
     * status='8 - EM NEGOCIACAO' responde 200, status='8 - EM NEGOCIAÇÃO' responde
     * 400 code 704 "Parameter error.". Testado contra o Service Layer - vale pra UTF-8 e pra
     * Latin-1, nao existe encoding que passe. Espaco nao e problema, so o acento.
     *
     * O apostrofo tambem fica de fora, por outro motivo: Parameter.toString() envolve o valor
     * em aspas simples e so pula isso quando o valor COMECA com uma. Um apostrofo no meio
     * ("O'Brien", "D'Avila") produz cobrador='O'Brien', que o parser recusa - a consulta falha
     * em vez de so ficar lenta. Sem ele no SQL, o nome e filtrado em Kotlin e a tela funciona.
     *
     * Rotulo de dominio ("8 - EM NEGOCIAÇÃO", "4 - LIGAÇÃO") e nome de cobrador tem acento o
     * tempo todo, entao esses filtros nao podem ir crus. Quando o valor tem acento a comparacao
     * sai do SQL e fica so em passaNosFiltrosLocais, que ja aplica exatamente o mesmo criterio -
     * custa mais paginas do SAP ate juntar o resultado, mas devolve a lista certa em vez de
     * estourar erro na cara do cobrador.
     */
    private fun soAscii(valor: String?): String? =
        valor?.takeIf { texto -> texto.all { it.code in 32..126 && it != '\'' } }

    /**
     * Prefixo ASCII do valor, pra LIKE. Quando soAscii recusa o valor, o filtro sai do SQL
     * inteiro e sobra so passaNosFiltrosLocais - que roda DEPOIS de a pagina chegar do SAP.
     * "8 - EM NEGOCIACAO" sao algumas dezenas de parcelas em mais de mil, entao o laco de
     * buscarAte varre a base de 20 em 20 ate juntar 20 aprovadas: e por isso que o drill-down
     * do dashboard demorava tanto pra abrir.
     *
     * O pedaco do valor antes do primeiro acento e ASCII puro e passa no parser. Mandar ele
     * como LIKE 'prefixo%' devolve quase exatamente o conjunto certo no proprio SAP, e a
     * igualdade exata continua sendo de passaNosFiltrosLocais - nada muda no resultado, so no
     * numero de paginas ate juntar ele.
     *
     * Prefixo com menos de 2 caracteres nao seleciona o bastante pra pagar a ida; % e _ dentro
     * dele virariam curinga de LIKE e alargariam o filtro em vez de estreitar. Nos dois casos
     * volta null e o filtro fica so no Kotlin, como era antes.
     */
    private fun prefixoLike(valor: String?): String? {
        if (valor == null || soAscii(valor) != null) return null
        val prefixo = valor.takeWhile { it.code in 32..126 && it != '%' && it != '_' && it != '\'' }
        return if (prefixo.length < 2) null else "$prefixo%"
    }

    fun listar(
        auth: User,
        filiais: List<Int>? = null,
        vendedor: Int? = null,
        cliente: String? = null,
        data: LocalDate = LocalDate.now(),
        status: String? = null,
        incluirSemStatus: Boolean? = null,
        cobrador: String? = null,
        situacao: String? = null,
        situacaoSap: String? = null,
        vencimentoDe: LocalDate? = null,
        vencimentoAte: LocalDate? = null,
        lancamentoMeses: List<YearMonth>? = null,
        semAcompanhamento: Boolean? = null,
        comAcompanhamento: Boolean? = null,
        promessaVencidaAte: LocalDate? = null,
        ocultarAvista: Boolean? = null,
        dataPagamentoDe: LocalDate? = null,
        dataPagamentoAte: LocalDate? = null,
        tipo: String? = null,
        pagina: Int = 0,
        tamanhoPagina: Int = 20,
    ): List<CobrancaTitulo> = listarComTruncamento(
        auth, filiais, vendedor, cliente, data, status, incluirSemStatus, cobrador, situacao,
        situacaoSap, vencimentoDe, vencimentoAte, lancamentoMeses, semAcompanhamento,
        comAcompanhamento, promessaVencidaAte, ocultarAvista, dataPagamentoDe, dataPagamentoAte,
        tipo, pagina, tamanhoPagina,
    ).Titulos

    /**
     * Mesma pagina do listar, mas avisando se a busca de ValorRecebidoNoPeriodo (view auxiliar,
     * teto proprio de paginas) parou incompleta - sem isso uma parcela com recebimento real
     * aparecia com esse campo nulo em silencio, e o rodape ignorava aquele pagamento sem avisar.
     */
    fun listarComTruncamento(
        auth: User,
        filiais: List<Int>? = null,
        vendedor: Int? = null,
        cliente: String? = null,
        data: LocalDate = LocalDate.now(),
        status: String? = null,
        incluirSemStatus: Boolean? = null,
        cobrador: String? = null,
        situacao: String? = null,
        situacaoSap: String? = null,
        vencimentoDe: LocalDate? = null,
        vencimentoAte: LocalDate? = null,
        lancamentoMeses: List<YearMonth>? = null,
        semAcompanhamento: Boolean? = null,
        comAcompanhamento: Boolean? = null,
        promessaVencidaAte: LocalDate? = null,
        ocultarAvista: Boolean? = null,
        dataPagamentoDe: LocalDate? = null,
        dataPagamentoAte: LocalDate? = null,
        tipo: String? = null,
        pagina: Int = 0,
        tamanhoPagina: Int = 20,
    ): CobrancaTitulosPagina {
        val busca = buscarCombinado(
            auth, filiais, vendedor, cliente, data, status, incluirSemStatus, cobrador, situacao,
            situacaoSap, vencimentoDe, vencimentoAte, lancamentoMeses, semAcompanhamento,
            comAcompanhamento, promessaVencidaAte, ocultarAvista, dataPagamentoDe, dataPagamentoAte,
            tipo, alvo = (pagina + 1) * tamanhoPagina, maxPaginasSap = Int.MAX_VALUE,
        )
        val inicio = (pagina * tamanhoPagina).coerceAtMost(busca.linhas.size)
        val fim = (inicio + tamanhoPagina).coerceAtMost(busca.linhas.size)
        return CobrancaTitulosPagina(busca.linhas.subList(inicio, fim), busca.truncado)
    }

    /**
     * Total do filtro inteiro, para a tela conferir o numero do card contra a lista.
     *
     * Roda o MESMO pipeline do listar (mesmo SQL, mesma segunda camada em Kotlin, mesma consulta
     * por filial) e so troca o recorte de pagina por uma reducao - e por isso que o total bate com
     * o que a tela mostra. Uma view agregada nova somaria tambem o que passaNosFiltrosLocais
     * descarta depois (valor com acento, mes exato, PAGO_PARCIAL) e ainda teria que manter dois
     * WHERE gigantes em sincronia.
     */
    fun totalizar(
        auth: User,
        filiais: List<Int>? = null,
        vendedor: Int? = null,
        cliente: String? = null,
        data: LocalDate = LocalDate.now(),
        status: String? = null,
        incluirSemStatus: Boolean? = null,
        cobrador: String? = null,
        situacao: String? = null,
        situacaoSap: String? = null,
        vencimentoDe: LocalDate? = null,
        vencimentoAte: LocalDate? = null,
        lancamentoMeses: List<YearMonth>? = null,
        semAcompanhamento: Boolean? = null,
        comAcompanhamento: Boolean? = null,
        promessaVencidaAte: LocalDate? = null,
        ocultarAvista: Boolean? = null,
        dataPagamentoDe: LocalDate? = null,
        dataPagamentoAte: LocalDate? = null,
        tipo: String? = null,
    ): CobrancaTitulosTotal {
        val busca = buscarCombinado(
            auth, filiais, vendedor, cliente, data, status, incluirSemStatus, cobrador, situacao,
            situacaoSap, vencimentoDe, vencimentoAte, lancamentoMeses, semAcompanhamento,
            comAcompanhamento, promessaVencidaAte, ocultarAvista, dataPagamentoDe, dataPagamentoAte,
            tipo, alvo = Int.MAX_VALUE, maxPaginasSap = TETO_PAGINAS_SAP,
        )
        // Com a janela de data de pagamento ligada, o total precisa contar CADA recebimento
        // dentro dela (ValorRecebidoNoPeriodo), nao so o mais recente de cada parcela
        // (ValorPago) - senao parcela paga duas vezes no mesmo periodo fica subcontada contra
        // o card "Recuperado", que soma por recebimento. Sem janela nenhuma pra comparar com,
        // ValorPago (o ultimo pagamento) continua sendo o unico numero que faz sentido.
        val usaJanelaDePagamento = dataPagamentoDe != null || dataPagamentoAte != null
        fun valorRecebido(titulo: CobrancaTitulo): BigDecimal? =
            if (usaJanelaDePagamento) titulo.ValorRecebidoNoPeriodo else titulo.ValorPago

        return CobrancaTitulosTotal(
            Parcelas = busca.linhas.size,
            Saldo = busca.linhas.fold(BigDecimal.ZERO) { soma, titulo -> soma.add(titulo.Saldo) },
            ValorPago = busca.linhas.fold(BigDecimal.ZERO) { soma, titulo ->
                soma.add(valorRecebido(titulo) ?: BigDecimal.ZERO)
            },
            ParcelasComPagamento = busca.linhas.count { valorRecebido(it) != null },
            Truncado = busca.truncado,
        )
    }

    private fun buscarCombinado(
        auth: User,
        filiais: List<Int>?,
        vendedor: Int?,
        cliente: String?,
        data: LocalDate,
        status: String?,
        incluirSemStatus: Boolean?,
        cobrador: String?,
        situacao: String?,
        situacaoSap: String?,
        vencimentoDe: LocalDate?,
        vencimentoAte: LocalDate?,
        lancamentoMeses: List<YearMonth>?,
        semAcompanhamento: Boolean?,
        comAcompanhamento: Boolean?,
        promessaVencidaAte: LocalDate?,
        ocultarAvista: Boolean?,
        dataPagamentoDe: LocalDate?,
        dataPagamentoAte: LocalDate?,
        tipo: String?,
        alvo: Int,
        maxPaginasSap: Int,
    ): BuscaCombinada {
        val vendedorEfetivo = CobrancaEscopo.vendedorEfetivo(auth, vendedor)

        val statusParcela = statusParcelaDe(situacaoSap)

        // A tela manda um ou vários meses (arvore por ano, multi-selecao). No SQL vai só o
        // ENVELOPE da selecao - do primeiro dia do mes mais antigo ao ultimo dia do mais novo -
        // porque lista fixa de valores nao tem precedente no parser do SQLQueries e uma consulta
        // por mes multiplicaria as chamadas ao SAP (ja e uma por filial). Selecao com buraco
        // (julho e setembro, sem agosto) entra no envelope e agosto cai em passaNosFiltrosLocais,
        // que compara o mes exato. A view tambem nao aceita YEAR()/MONTH() - ver o guarda em
        // CobrancaTitulosSqlTest.
        val mesesEscolhidos = lancamentoMeses?.distinct()?.sorted()?.takeIf { it.isNotEmpty() }
        val mesesSap = mesesEscolhidos?.map { it.format(formatoMesSap) }?.toSet()
        val lancamentoDe = mesesEscolhidos?.first()?.atDay(1)
        val lancamentoAte = mesesEscolhidos?.last()?.atEndOfMonth()

        // Valor com acento nao pode ir pro SQLQueries (ver soAscii) - vira "sem filtro" aqui e
        // passaNosFiltrosLocais faz a comparacao exata depois, com o valor original.
        // incluirSemStatus tambem desliga o filtro no SQL: la a comparacao e U_Status = :status,
        // que descartaria justamente as linhas de U_Status nulo que esse filtro quer trazer.
        val statusSql = if (incluirSemStatus == true) null else soAscii(status)
        val cobradorSql = soAscii(cobrador)
        val situacaoSql = soAscii(situacao)

        // Complemento do de cima: o que nao passa inteiro vai como prefixo. incluirSemStatus
        // desliga os dois, porque LIKE tambem descarta a linha de U_Status nulo que ele quer.
        val statusPrefixo = if (incluirSemStatus == true) null else prefixoLike(status)
        val cobradorPrefixo = prefixoLike(cobrador)
        val situacaoPrefixo = prefixoLike(situacao)

        val parametrosBase = listOf(
            Parameter("data", data.toString()),
            Parameter("vendedor", vendedorEfetivo ?: Int.MAX_VALUE),
            Parameter("vendedorIsFilter", if (vendedorEfetivo == null) Int.MAX_VALUE else -1),
            Parameter("cliente", cliente ?: SEM_FILTRO),
            Parameter("clienteIsFilter", if (cliente == null) SEM_FILTRO else ""),
            Parameter("statusParcela", statusParcela),
            Parameter("statusParcelaIsFilter", if (statusParcela == SEM_FILTRO) SEM_FILTRO else ""),
            Parameter("vencimentoDe", vencimentoDe?.toString() ?: "1900-01-01"),
            Parameter("vencimentoAte", vencimentoAte?.toString() ?: "9999-12-31"),
            Parameter("lancamentoDe", lancamentoDe?.toString() ?: "1900-01-01"),
            Parameter("lancamentoAte", lancamentoAte?.toString() ?: "9999-12-31"),
            Parameter("status", statusSql ?: SEM_FILTRO),
            Parameter("statusIsFilter", if (statusSql == null) Int.MAX_VALUE else -1),
            Parameter("statusPrefixo", statusPrefixo ?: SEM_FILTRO),
            Parameter("statusPrefixoIsFilter", if (statusPrefixo == null) Int.MAX_VALUE else -1),
            Parameter("cobrador", cobradorSql ?: SEM_FILTRO),
            Parameter("cobradorIsFilter", if (cobradorSql == null) Int.MAX_VALUE else -1),
            Parameter("cobradorPrefixo", cobradorPrefixo ?: SEM_FILTRO),
            Parameter("cobradorPrefixoIsFilter", if (cobradorPrefixo == null) Int.MAX_VALUE else -1),
            Parameter("situacao", situacaoSql ?: SEM_FILTRO),
            Parameter("situacaoIsFilter", if (situacaoSql == null) Int.MAX_VALUE else -1),
            Parameter("situacaoPrefixo", situacaoPrefixo ?: SEM_FILTRO),
            Parameter("situacaoPrefixoIsFilter", if (situacaoPrefixo == null) Int.MAX_VALUE else -1),
            Parameter("semAcompanhamentoIsFilter", if (semAcompanhamento == true) -1 else Int.MAX_VALUE),
            // Espelho do de cima: o drill-down do card "Recuperado" usa esse pra mostrar so o que
            // compoe o numero - o card faz INNER JOIN com @COB_TITULO, entao titulo que pagou sem
            // ninguem ter cobrado nao entra nele. Ligar os dois juntos devolve lista vazia (um pede
            // Code nulo, o outro pede nao-nulo) - e mutuamente exclusivo por definicao.
            Parameter("comAcompanhamentoIsFilter", if (comAcompanhamento == true) -1 else Int.MAX_VALUE),
            Parameter("promessaVencidaAte", (promessaVencidaAte ?: data).toString()),
            Parameter("promessaVencidaIsFilter", if (promessaVencidaAte == null) Int.MAX_VALUE else -1),
            // A vista = lancado e vencido no mesmo dia (DocDate = DueDate). Desligado por
            // padrao: so filtra quando o cobrador liga o toggle na tela.
            Parameter("ocultarAvistaIsFilter", if (ocultarAvista == true) -1 else Int.MAX_VALUE),
            // Data de pagamento (PR.DocDate do recebimento, vem de LEFT JOIN e pode ser nula) -
            // usa o mesmo idioma de escape em coluna nao-nula que semAcompanhamento/promessa:
            // sem isso, "desligado" comparado direto com a coluna nula sumiria com todo titulo
            // sem pagamento. E o filtro que o drill-down do card "Recuperado" usa pra recortar
            // pelo mesmo periodo (de/ate) que o dashboard esta mostrando, em vez de trazer
            // titulo pago de qualquer epoca.
            Parameter("dataPagamentoDe", (dataPagamentoDe ?: LocalDate.of(1900, 1, 1)).toString()),
            Parameter("dataPagamentoDeIsFilter", if (dataPagamentoDe == null) Int.MAX_VALUE else -1),
            Parameter("dataPagamentoAte", (dataPagamentoAte ?: LocalDate.of(9999, 12, 31)).toString()),
            Parameter("dataPagamentoAteIsFilter", if (dataPagamentoAte == null) Int.MAX_VALUE else -1),
        )

        // Uma consulta por filial escolhida. A view mantem o idioma ":filial ou :filialIsFilter"
        // (um valor so) porque lista fixa de BPLId no SQL nao tem precedente no parser do
        // SQLQueries do SAP B1 - ver o guarda em CobrancaTitulosSqlTest. Filtrar em Kotlin
        // seria pior: o laco de paginacao varreria a base toda de 20 em 20 pra descartar filial.
        var truncado = false
        val combinado = filiaisEfetivas(filiais).flatMap { filial ->
            val parametros = parametrosBase + parametrosDeFilial(filial)

            val faturas = if (tipo == CobrancaRegistro.TIPO_ADIANTAMENTO) BuscaCombinada.VAZIA else
                buscarAte<CobrancaTituloSap>("cobranca-titulos.sql", parametros, alvo, maxPaginasSap) { linhas ->
                    linhas.map { it.toDto() }
                        .filter { passaNosFiltrosLocais(it, status, incluirSemStatus, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte, mesesSap) }
                }

            val adiantamentos = if (tipo == CobrancaRegistro.TIPO_NOTA_FISCAL) BuscaCombinada.VAZIA else
                buscarAte<CobrancaAdiantamentoSap>("cobranca-titulos-adiantamento.sql", parametros, alvo, maxPaginasSap) { linhas ->
                    linhas.map { it.toDto() }
                        .filter { passaNosFiltrosLocais(it, status, incluirSemStatus, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte, mesesSap) }
                }

            truncado = truncado || faturas.truncado || adiantamentos.truncado
            faturas.linhas + adiantamentos.linhas
        }.sortedWith(compareBy({ it.DueDate }, { it.DocNum }))

        // ValorRecebidoNoPeriodo so faz sentido - e so e buscado - com a janela de pagamento
        // ligada (ver o comentario em CobrancaTitulo). Sem ela, a busca acima ja e o suficiente.
        // Com comAcompanhamento ligado (drill-down do card "Recuperado"), a soma tambem exige
        // acao ANTES de cada recebimento - a mesma exigencia do card - senao um titulo cobrado
        // SO DEPOIS de ja ter recebido inflava o total contra o card (nao so o card via
        // @COB_TITULO_L.U_Data <= data do pagamento, comAcompanhamento sozinho so exigia "tem
        // alguma acao, a qualquer momento").
        if (dataPagamentoDe != null || dataPagamentoAte != null) {
            val somenteComAcaoAntes = comAcompanhamento == true
            if (tipo != CobrancaRegistro.TIPO_ADIANTAMENTO) {
                val recebidoNf = somarRecebimentosNoPeriodoPorFiliais(
                    "cobranca-recebimentos-periodo.sql", filiais, vendedorEfetivo, cliente,
                    dataPagamentoDe, dataPagamentoAte, somenteComAcaoAntes,
                )
                truncado = truncado || recebidoNf.truncado
                combinado.filter { it.Tipo == CobrancaRegistro.TIPO_NOTA_FISCAL }
                    .forEach { it.ValorRecebidoNoPeriodo = recebidoNf.porParcela[it.DocEntry to it.InstlmntID] }
            }
            if (tipo != CobrancaRegistro.TIPO_NOTA_FISCAL) {
                val recebidoAd = somarRecebimentosNoPeriodoPorFiliais(
                    "cobranca-recebimentos-periodo-adiantamento.sql", filiais, vendedorEfetivo, cliente,
                    dataPagamentoDe, dataPagamentoAte, somenteComAcaoAntes,
                )
                truncado = truncado || recebidoAd.truncado
                combinado.filter { it.Tipo == CobrancaRegistro.TIPO_ADIANTAMENTO }
                    .forEach { it.ValorRecebidoNoPeriodo = recebidoAd.porParcela[it.DocEntry to it.InstlmntID] }
            }
        }

        return BuscaCombinada(combinado, truncado)
    }

    /** Soma por (DocEntry, InstId) e se a varredura (de QUALQUER filial) bateu no teto de paginas. */
    private class RecebimentosNoPeriodo(val porParcela: Map<Pair<Int, Int>, BigDecimal>, val truncado: Boolean)

    /**
     * Soma de TODOS os recebimentos (nao so o mais recente por parcela) dentro da janela de
     * pagamento, agrupados por (DocEntry, InstId). View separada e mais simples que a dos
     * titulos: SAP recusou (erro 701, "Invalid SQL syntax") uma subquery escalar com sum() na
     * lista de SELECT da view principal - esse parser aceita subquery como predicado (WHERE/ON),
     * nao como valor de coluna.
     *
     * Uma consulta por filial escolhida - igual buscarCombinado faz pra titulos - pra nao varrer
     * recebimento de filial/cliente/vendedor fora do filtro e gastar o teto de paginas com
     * dado que nunca ia ser usado. E igualmente importante propagar o truncado: sem isso, um
     * teto batido no meio da varredura devolvia mapa incompleto sem avisar, e uma parcela paga
     * so na pagina 101 aparecia com ValorRecebidoNoPeriodo nulo em vez de sinalizar total parcial.
     */
    private fun somarRecebimentosNoPeriodoPorFiliais(
        view: String,
        filiais: List<Int>?,
        vendedorEfetivo: Int?,
        cliente: String?,
        dataPagamentoDe: LocalDate?,
        dataPagamentoAte: LocalDate?,
        somenteComAcaoAntes: Boolean,
    ): RecebimentosNoPeriodo {
        val linhas = mutableListOf<CobrancaRecebimentoPeriodoSap>()
        var truncado = false
        filiaisEfetivas(filiais).forEach { filial ->
            val parametros = listOf(
                Parameter("dataPagamentoDe", (dataPagamentoDe ?: LocalDate.of(1900, 1, 1)).toString()),
                Parameter("dataPagamentoAte", (dataPagamentoAte ?: LocalDate.of(9999, 12, 31)).toString()),
                Parameter("acaoAntesPagamentoIsFilter", if (somenteComAcaoAntes) -1 else Int.MAX_VALUE),
                Parameter("vendedor", vendedorEfetivo ?: Int.MAX_VALUE),
                Parameter("vendedorIsFilter", if (vendedorEfetivo == null) Int.MAX_VALUE else -1),
                Parameter("cliente", cliente ?: SEM_FILTRO),
                Parameter("clienteIsFilter", if (cliente == null) SEM_FILTRO else ""),
            ) + parametrosDeFilial(filial)

            var paginaSap = sqlQueriesService.execute(view, parametros)
            var paginas = 0
            while (paginaSap != null) {
                linhas.addAll(paginaSap.tryGetValues<CobrancaRecebimentoPeriodoSap>())
                paginas++
                if (!paginaSap.hasNext()) break
                if (paginas >= TETO_PAGINAS_SAP) {
                    truncado = true
                    break
                }
                paginaSap = sqlQueriesService.nextLink(paginaSap.nextLink())
            }
        }
        val porParcela = linhas.groupingBy { it.DocEntry to it.InstId }
            .fold(BigDecimal.ZERO) { total, linha -> total.add(linha.SumApplied) }
        return RecebimentosNoPeriodo(porParcela, truncado)
    }

    // Nenhuma filial escolhida = uma consulta com o filtro desligado (null), nao consulta nenhuma.
    private fun filiaisEfetivas(filiais: List<Int>?): List<Int?> =
        filiais?.distinct()?.takeIf { it.isNotEmpty() } ?: listOf(null)

    private fun parametrosDeFilial(filial: Int?) = listOf(
        Parameter("filial", filial ?: Int.MAX_VALUE),
        Parameter("filialIsFilter", if (filial == null) Int.MAX_VALUE else -1),
    )

    /**
     * A tela montava esse combo a partir das linhas ja carregadas, o que e circular: pra filtrar
     * por um cobrador, os titulos dele precisavam ja estar entre as 20 linhas da pagina atual.
     * Quem nao estivesse simplesmente nao existia no filtro.
     */
    fun cobradores(): List<String> =
        sqlQueriesService.getAll<CobrancaCobradorSap>("cobranca-cobradores.sql")
            .mapNotNull { it.Cobrador?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()

    fun buscarTituloParaEscopo(tipo: String, docEntry: Int, instlmntId: Int): CobrancaTituloVendedorSap? {
        val view = if (tipo == CobrancaRegistro.TIPO_ADIANTAMENTO)
            "cobranca-titulo-vendedor-adiantamento.sql"
        else
            "cobranca-titulo-vendedor.sql"
        return sqlQueriesService.getAll<CobrancaTituloVendedorSap>(
            view,
            listOf(Parameter("docEntry", docEntry), Parameter("instlmntId", instlmntId)),
        ).firstOrNull()
    }

    /**
     * O teto e em PAGINAS do SAP, nao em linhas: com um filtro que so o Kotlin resolve (valor com
     * acento, mes exato), a varredura pode rejeitar quase tudo e paginar longe demais sem nunca
     * acumular linha - teto em linha nunca dispararia. O Service Layer e recurso compartilhado.
     */
    private inline fun <reified T : Any> buscarAte(
        view: String,
        parametros: List<Parameter>,
        alvo: Int,
        maxPaginas: Int = Int.MAX_VALUE,
        transformar: (List<T>) -> List<CobrancaTitulo>,
    ): BuscaCombinada {
        val acumulado = mutableListOf<CobrancaTitulo>()
        var paginaSap = sqlQueriesService.execute(view, parametros)
        var paginas = 0

        while (paginaSap != null) {
            acumulado.addAll(transformar(paginaSap.tryGetValues<T>()))
            paginas++
            if (acumulado.size >= alvo || !paginaSap.hasNext())
                return BuscaCombinada(acumulado, false)
            if (paginas >= maxPaginas)
                return BuscaCombinada(acumulado, true)
            paginaSap = sqlQueriesService.nextLink(paginaSap.nextLink())
        }
        return BuscaCombinada(acumulado, false)
    }

    private fun passaNosFiltrosLocais(
        titulo: CobrancaTitulo,
        status: String?,
        incluirSemStatus: Boolean?,
        cobrador: String?,
        situacao: String?,
        situacaoSap: String?,
        vencimentoDe: LocalDate?,
        vencimentoAte: LocalDate?,
        // Meses da selecao ja no formato do SAP ("202607"), calculados uma vez fora do laco.
        mesesSap: Set<String>?,
    ): Boolean {
        return (status == null || titulo.U_Status == status || (incluirSemStatus == true && titulo.U_Status.isNullOrBlank())) &&
            (cobrador == null || titulo.U_Cobrador == cobrador) &&
            (situacao == null || titulo.U_Situacao == situacao) &&
            (situacaoSap == null || situacaoSapCombina(titulo.SituacaoSap, situacaoSap)) &&
            (vencimentoDe == null || titulo.DueDate >= vencimentoDe.format(formatoSap)) &&
            (vencimentoAte == null || titulo.DueDate <= vencimentoAte.format(formatoSap)) &&
            (mesesSap == null || ehDeAlgumMesDeLancamento(titulo.DocDate, mesesSap))
    }

    /**
     * "ABERTO" filtrado tem que continuar trazendo PAGO_PARCIAL junto - e so uma subdivisao
     * visual/opcional de ABERTO (ver CobrancaTituloSap.toDto), entao os drill-downs que ja
     * mandam situacaoSap=ABERTO (carteira, aging, filial, etc.) nao podem perder titulo so
     * porque ele recebeu um pagamento parcial. Quem quer so o parcial pede PAGO_PARCIAL
     * explicitamente.
     */
    private fun situacaoSapCombina(situacaoDoTitulo: String, situacaoFiltrada: String): Boolean {
        if (situacaoFiltrada == "ABERTO") {
            return situacaoDoTitulo == "ABERTO" || situacaoDoTitulo == "PAGO_PARCIAL"
        }
        return situacaoDoTitulo == situacaoFiltrada
    }

    /**
     * DocDate chega do SQLQueries como "20260701", mas data do Service Layer aparece como
     * "2026-07-01" em outros contextos - so os digitos interessam, entao o separador sai antes de
     * comparar. Titulo sem DocDate fica de fora: nao da pra afirmar que e do mes pedido.
     */
    private fun ehDeAlgumMesDeLancamento(docDate: String?, mesesSap: Set<String>): Boolean {
        val digitos = docDate?.filter { it.isDigit() } ?: return false
        return digitos.take(6) in mesesSap
    }
}
