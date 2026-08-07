package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAdiantamentoSap
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.model.cobranca.CobrancaTituloSap
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CobrancaConsultaService(val sqlQueriesService: SqlQueriesService) {

    private val formatoSap = DateTimeFormatter.BASIC_ISO_DATE

    companion object {
        private const val SEM_FILTRO = "~"
    }

    private fun statusParcelaDe(situacaoSap: String?): String = when (situacaoSap) {
        "ABERTO" -> "O"
        "PAGO" -> "C"
        else -> SEM_FILTRO
    }

    fun listar(
        auth: User,
        filial: Int? = null,
        vendedor: Int? = null,
        cliente: String? = null,
        data: LocalDate = LocalDate.now(),
        diasAtrasoMin: Int? = null,
        status: String? = null,
        cobrador: String? = null,
        situacao: String? = null,
        situacaoSap: String? = null,
        vencimentoDe: LocalDate? = null,
        vencimentoAte: LocalDate? = null,
        semAcompanhamento: Boolean? = null,
        promessaVencidaAte: LocalDate? = null,
        tipo: String? = null,
        pagina: Int = 0,
        tamanhoPagina: Int = 20,
    ): List<CobrancaTitulo> {
        val vendedorEfetivo = if (auth.superVendedor() == Int.MAX_VALUE) vendedor else auth.getIdInt()

        val dataEfetiva = data.minusDays((diasAtrasoMin ?: 0).coerceAtLeast(0).toLong())
        val statusParcela = statusParcelaDe(situacaoSap)

        val parametros = listOf(
            Parameter("data", dataEfetiva.toString()),
            Parameter("filial", filial ?: Int.MAX_VALUE),
            Parameter("filialIsFilter", if (filial == null) Int.MAX_VALUE else -1),
            Parameter("vendedor", vendedorEfetivo ?: Int.MAX_VALUE),
            Parameter("vendedorIsFilter", if (vendedorEfetivo == null) Int.MAX_VALUE else -1),
            Parameter("cliente", cliente ?: SEM_FILTRO),
            Parameter("clienteIsFilter", if (cliente == null) SEM_FILTRO else ""),
            Parameter("statusParcela", statusParcela),
            Parameter("statusParcelaIsFilter", if (statusParcela == SEM_FILTRO) SEM_FILTRO else ""),
            Parameter("vencimentoDe", vencimentoDe?.toString() ?: "1900-01-01"),
            Parameter("vencimentoAte", vencimentoAte?.toString() ?: "9999-12-31"),
            Parameter("status", status ?: SEM_FILTRO),
            Parameter("statusIsFilter", if (status == null) Int.MAX_VALUE else -1),
            Parameter("cobrador", cobrador ?: SEM_FILTRO),
            Parameter("cobradorIsFilter", if (cobrador == null) Int.MAX_VALUE else -1),
            Parameter("situacao", situacao ?: SEM_FILTRO),
            Parameter("situacaoIsFilter", if (situacao == null) Int.MAX_VALUE else -1),
            Parameter("semAcompanhamentoIsFilter", if (semAcompanhamento == true) -1 else Int.MAX_VALUE),
            Parameter("promessaVencidaAte", (promessaVencidaAte ?: data).toString()),
            Parameter("promessaVencidaIsFilter", if (promessaVencidaAte == null) Int.MAX_VALUE else -1),
        )

        val alvo = (pagina + 1) * tamanhoPagina

        val faturas = if (tipo == CobrancaRegistro.TIPO_ADIANTAMENTO) emptyList() else
            buscarAte<CobrancaTituloSap>("cobranca-titulos.sql", parametros, alvo) { linhas ->
                linhas.map { it.toDto() }
                    .filter { passaNosFiltrosLocais(it, diasAtrasoMin, status, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte) }
            }

        val adiantamentos = if (tipo == CobrancaRegistro.TIPO_NOTA_FISCAL) emptyList() else
            buscarAte<CobrancaAdiantamentoSap>("cobranca-titulos-adiantamento.sql", parametros, alvo) { linhas ->
                linhas.map { it.toDto() }
                    .filter { passaNosFiltrosLocais(it, diasAtrasoMin, status, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte) }
            }

        val combinado = (faturas + adiantamentos).sortedWith(compareBy({ it.DueDate }, { it.DocNum }))
        val inicio = (pagina * tamanhoPagina).coerceAtMost(combinado.size)
        val fim = (inicio + tamanhoPagina).coerceAtMost(combinado.size)
        return combinado.subList(inicio, fim)
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
        diasAtrasoMin: Int?,
        status: String?,
        cobrador: String?,
        situacao: String?,
        situacaoSap: String?,
        vencimentoDe: LocalDate?,
        vencimentoAte: LocalDate?,
    ): Boolean {
        return (diasAtrasoMin == null || titulo.DiasAtraso >= diasAtrasoMin) &&
            (status == null || titulo.U_Status == status) &&
            (cobrador == null || titulo.U_Cobrador == cobrador) &&
            (situacao == null || titulo.U_Situacao == situacao) &&
            (situacaoSap == null || titulo.SituacaoSap == situacaoSap) &&
            (vencimentoDe == null || titulo.DueDate >= vencimentoDe.format(formatoSap)) &&
            (vencimentoAte == null || titulo.DueDate <= vencimentoAte.format(formatoSap))
    }
}
