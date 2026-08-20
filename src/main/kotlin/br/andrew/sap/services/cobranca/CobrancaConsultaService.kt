package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAdiantamentoSap
import br.andrew.sap.model.cobranca.CobrancaCobradorSap
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.model.cobranca.CobrancaTituloSap
import br.andrew.sap.model.cobranca.CobrancaTituloVendedorSap
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class CobrancaConsultaService(val sqlQueriesService: SqlQueriesService) {

    private val formatoSap = DateTimeFormatter.BASIC_ISO_DATE
    private val formatoMesSap = DateTimeFormatter.ofPattern("yyyyMM")

    companion object {
        private const val SEM_FILTRO = "~"
    }

    private fun statusParcelaDe(situacaoSap: String?): String = when (situacaoSap) {
        "ABERTO" -> "O"
        "PAGO" -> "C"
        else -> SEM_FILTRO
    }

    /**
     * O parser de parametros do SQLQueries do SAP B1 recusa qualquer caractere nao-ASCII:
     * status='8 - EM NEGOCIACAO' responde 200, status='8 - EM NEGOCIAÇÃO' responde
     * 400 code 704 "Parameter error.". Testado contra o Service Layer - vale pra UTF-8 e pra
     * Latin-1, nao existe encoding que passe. Espaco nao e problema, so o acento.
     *
     * Rotulo de dominio ("8 - EM NEGOCIAÇÃO", "4 - LIGAÇÃO") e nome de cobrador tem acento o
     * tempo todo, entao esses filtros nao podem ir crus. Quando o valor tem acento a comparacao
     * sai do SQL e fica so em passaNosFiltrosLocais, que ja aplica exatamente o mesmo criterio -
     * custa mais paginas do SAP ate juntar o resultado, mas devolve a lista certa em vez de
     * estourar erro na cara do cobrador.
     */
    private fun soAscii(valor: String?): String? =
        valor?.takeIf { texto -> texto.all { it.code in 32..126 } }

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
        promessaVencidaAte: LocalDate? = null,
        tipo: String? = null,
        pagina: Int = 0,
        tamanhoPagina: Int = 20,
    ): List<CobrancaTitulo> {
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
            Parameter("cobrador", cobradorSql ?: SEM_FILTRO),
            Parameter("cobradorIsFilter", if (cobradorSql == null) Int.MAX_VALUE else -1),
            Parameter("situacao", situacaoSql ?: SEM_FILTRO),
            Parameter("situacaoIsFilter", if (situacaoSql == null) Int.MAX_VALUE else -1),
            Parameter("semAcompanhamentoIsFilter", if (semAcompanhamento == true) -1 else Int.MAX_VALUE),
            Parameter("promessaVencidaAte", (promessaVencidaAte ?: data).toString()),
            Parameter("promessaVencidaIsFilter", if (promessaVencidaAte == null) Int.MAX_VALUE else -1),
        )

        val alvo = (pagina + 1) * tamanhoPagina

        // Uma consulta por filial escolhida. A view mantem o idioma ":filial ou :filialIsFilter"
        // (um valor so) porque lista fixa de BPLId no SQL nao tem precedente no parser do
        // SQLQueries do SAP B1 - ver o guarda em CobrancaTitulosSqlTest. Filtrar em Kotlin
        // seria pior: o laco de paginacao varreria a base toda de 20 em 20 pra descartar filial.
        val combinado = filiaisEfetivas(filiais).flatMap { filial ->
            val parametros = parametrosBase + parametrosDeFilial(filial)

            val faturas = if (tipo == CobrancaRegistro.TIPO_ADIANTAMENTO) emptyList() else
                buscarAte<CobrancaTituloSap>("cobranca-titulos.sql", parametros, alvo) { linhas ->
                    linhas.map { it.toDto() }
                        .filter { passaNosFiltrosLocais(it, status, incluirSemStatus, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte, mesesSap) }
                }

            val adiantamentos = if (tipo == CobrancaRegistro.TIPO_NOTA_FISCAL) emptyList() else
                buscarAte<CobrancaAdiantamentoSap>("cobranca-titulos-adiantamento.sql", parametros, alvo) { linhas ->
                    linhas.map { it.toDto() }
                        .filter { passaNosFiltrosLocais(it, status, incluirSemStatus, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte, mesesSap) }
                }

            faturas + adiantamentos
        }.sortedWith(compareBy({ it.DueDate }, { it.DocNum }))

        val inicio = (pagina * tamanhoPagina).coerceAtMost(combinado.size)
        val fim = (inicio + tamanhoPagina).coerceAtMost(combinado.size)
        return combinado.subList(inicio, fim)
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

    private inline fun <reified T : Any> buscarAte(
        view: String,
        parametros: List<Parameter>,
        alvo: Int,
        transformar: (List<T>) -> List<CobrancaTitulo>,
    ): List<CobrancaTitulo> {
        val acumulado = mutableListOf<CobrancaTitulo>()
        var paginaSap = sqlQueriesService.execute(view, parametros)

        while (paginaSap != null) {
            acumulado.addAll(transformar(paginaSap.tryGetValues<T>()))
            if (acumulado.size >= alvo || !paginaSap.hasNext())
                break
            paginaSap = sqlQueriesService.nextLink(paginaSap.nextLink())
        }
        return acumulado
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
            (situacaoSap == null || titulo.SituacaoSap == situacaoSap) &&
            (vencimentoDe == null || titulo.DueDate >= vencimentoDe.format(formatoSap)) &&
            (vencimentoAte == null || titulo.DueDate <= vencimentoAte.format(formatoSap)) &&
            (mesesSap == null || ehDeAlgumMesDeLancamento(titulo.DocDate, mesesSap))
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
