package br.andrew.sap.services.comercial
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.comercial.PainelIntegradoVendas
import br.andrew.sap.model.comercial.PainelTotais
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.logistica.CarregamentoUtilizacoes
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import br.andrew.sap.services.odbc.consultarPagina
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class PainelIntegradoVendasService(
    private val odbc: OdbcClient,
    private val utilizacoes: CarregamentoUtilizacoes,
) {
    private val base by lazy { SqlResource.carregar("odbc/painel-carregamento-linhas.sql") }

    /*
     * Pedidos e NF de venda futura pelo sap-odbc (base odbc/painel-carregamento-linhas.sql mais uma cauda por
     * agrupador), paginados no banco para filtrar pelas mesmas utilizacoes da busca da ordem de carregamento.
     * Nos modos agrupados a pagina e por grupo, entao pode ter mais linhas que o tamanho. Acima de 10000 linhas
     * o OdbcClient recusa (resultado_truncado) em vez de mostrar recorte.
     */
    fun pedidos(
        dataInicial: String,
        dataFinal: String,
        filial: List<Int>,
        cliente: String?,
        item: String?,
        vendedor: Int?,
        agrupador: String?,
        localidade: String?,
        incoterms: String?,
        tipo: String? = null,
        sinaisFrete: List<Int> = listOf(-1, 0, 1),
        emOrdem: String? = null,
        page: Pageable,
    ): Page<PainelIntegradoVendas> {
        val params = parametros(dataInicial, dataFinal, filial, cliente, item, vendedor, localidade, incoterms, tipo, sinaisFrete, emOrdem)
        val (cauda, maxRows) = when (agrupador) {
            "cliente"  -> "odbc/painel-carregamento-pedidos-por-cliente.sql" to MAX_LINHAS
            "item"     -> "odbc/painel-carregamento-pedidos-por-item.sql" to MAX_LINHAS
            "vendedor" -> "odbc/painel-carregamento-pedidos-por-vendedor.sql" to MAX_LINHAS
            else       -> "odbc/painel-carregamento-pedidos.sql" to null
        }
        return odbc.consultarPagina(sql(cauda), params, page, MAX_PAGINA, maxRows)
            .map { OData(LinkedHashMap(it + ("Name" to CarregamentoService.localidade(it)))).tryGetValue<PainelIntegradoVendas>() }
    }

    fun totais(
        dataInicial: String,
        dataFinal: String,
        filial: List<Int>,
        cliente: String?,
        item: String?,
        vendedor: Int?,
        localidade: String?,
        incoterms: String?,
        tipo: String? = null,
        sinaisFrete: List<Int> = listOf(-1, 0, 1),
        emOrdem: String? = null,
    ): PainelTotais {
        val params = parametros(dataInicial, dataFinal, filial, cliente, item, vendedor, localidade, incoterms, tipo, sinaisFrete, emOrdem)
        val linha = odbc.consultar(sql("odbc/painel-carregamento-pedidos-totais.sql"), params, 1).rows.first()
        fun n(chave: String) = linha[chave].toString().toBigDecimal().toLong()
        return PainelTotais(n("Linhas"), n("Pedidos"), n("NotasVendaFutura"))
    }

    private fun sql(cauda: String) = base + "\n" + SqlResource.carregar(cauda)

    private fun parametros(
        dataInicial: String, dataFinal: String, filial: List<Int>, cliente: String?, item: String?, vendedor: Int?,
        localidade: String?, incoterms: String?, tipo: String?, sinaisFrete: List<Int>, emOrdem: String?,
    ): Map<String, Any?> {
        val params = listOf(
            Parameter("startDate",   dataInicial),
            Parameter("finalDate",   dataFinal),
            Parameter("partner",     cliente?.let    { "%$it%" } ?: "%"),
            Parameter("search",      item?.let       { "%$it%" } ?: "%"),
            Parameter("localidade",  localidade?.let { "%$it%" } ?: "%"),
            Parameter("incoterms",   incoterms?.let  { "%$it%" } ?: "%"),
        ) + parametrosVendedor(vendedor)
        return parametrosOdbc(params, filial, utilizacoes.lista, sinaisFrete, tipo.orEmpty(), emOrdem?.ifEmpty { null } ?: "todos")
    }

    companion object {
        const val MAX_PAGINA = 200
        private const val MAX_LINHAS = 10_000

        /** Codigo exato do vendedor, no mesmo esquema de CarregamentoService.searchPedidos (sem vendedor = todos). */
        internal fun parametrosVendedor(vendedor: Int?): List<Parameter> = listOf(
            Parameter("vendedor", vendedor ?: -2),
            Parameter("vendedorDisable", if (vendedor == null) -2 else Int.MAX_VALUE),
        )

        /** JDBC sem conversao implicita: filiais vao como lista de Int (IN (:branch)). sinaisFrete: ver CarregamentoService.sinaisFrete. */
        internal fun parametrosOdbc(params: List<Parameter>, filial: List<Int>, utilizacoes: List<Int>, sinaisFrete: List<Int>,
                                    tipo: String = "", emOrdem: String = "todos"): Map<String, Any?> =
            params.associate { it.key to it.value } + mapOf(
                "branch" to filial,
                "utilizacoes" to utilizacoes,
                "sinaisFrete" to sinaisFrete,
                "tipo" to tipo,
                "emOrdem" to emOrdem,
            )
    }
}
