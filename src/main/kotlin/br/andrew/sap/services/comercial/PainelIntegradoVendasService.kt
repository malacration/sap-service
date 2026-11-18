package br.andrew.sap.services.comercial
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.comercial.PainelIntegradoVendas
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.logistica.CarregamentoService
import br.andrew.sap.services.logistica.CarregamentoUtilizacoes
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PainelIntegradoVendasService(
    private val sqlQueriesService: SqlQueriesService,
    private val odbc: OdbcClient,
    private val utilizacoes: CarregamentoUtilizacoes,
) {
    fun searchExpeditionOrders(
        dataInicial: String,
        dataFinal: String,
        filial: Int,
        cliente: String?,
        item: String?,
        vendedor: Int?,
        agrupador: String?,
        localidade:String?,
        incoterms:String?,
        tipo: String? = null,
        sinaisFrete: List<Int> = listOf(-1, 0, 1),
    ): NextLink<PainelIntegradoVendas> {

        val clienteSearch   = cliente?.let   { "%$it%" } ?: "%"
        val itemSearch      = item?.let      { "%$it%" } ?: "%"
        val localidaSearch  = localidade?.let{ "%$it%" } ?: "%"
        val incotermsSearch = incoterms?.let { "%$it%" } ?: "%"

        val params = listOf(
            Parameter("startDate",   dataInicial),
            Parameter("finalDate",   dataFinal),
            Parameter("branch",      filial.toString()),
            Parameter("partner",     clienteSearch),
            Parameter("search",      itemSearch),
            Parameter("localidade",  localidaSearch),
            Parameter("incoterms",   incotermsSearch)
        ) + parametrosVendedor(vendedor)

        val all = if (tipo == "nf-venda-futura") emptyList() else pedidos(agrupador, parametrosOdbc(params, filial, utilizacoes.lista, sinaisFrete))
        val faturas = if (tipo == "pedido") emptyList() else faturasDeVendaFutura(params)
        return NextLink(content = all + agrupar(faturas, agrupador), nextLink = "")
    }

    /*
     * Pedidos pelo sap-odbc (odbc/painel-carregamento-pedidos*.sql) para filtrar pelas mesmas utilizacoes
     * da busca da ordem de carregamento. O SQLQueries paginava sem teto; aqui acima de 10000 linhas o
     * OdbcClient recusa (resultado_truncado) em vez de mostrar recorte.
     */
    private fun pedidos(agrupador: String?, params: Map<String, Any?>): List<PainelIntegradoVendas> {
        val sqlFile = when (agrupador) {
            "cliente"  -> "odbc/painel-carregamento-pedidos-por-cliente.sql"
            "item"     -> "odbc/painel-carregamento-pedidos-por-item.sql"
            "vendedor" -> "odbc/painel-carregamento-pedidos-por-vendedor.sql"
            else       -> "odbc/painel-carregamento-pedidos.sql"
        }
        return odbc.consultar(SqlResource.carregar(sqlFile), params, 10000).rows
            .map { OData(LinkedHashMap(it)).tryGetValue<PainelIntegradoVendas>() }
    }

    /*
     * NF de venda futura (fatura de reserva) com saldo a entregar, no formato do painel: cada linha
     * da fatura vira uma linha por ordem Aberta que reservou parte dela (Quantity = retirada) e uma
     * do saldo livre (OpenQty menos as reservas). A view traz uma linha por retirada com os valores
     * crus porque o SQLQueries nao aceita conta no SELECT.
     */
    private fun faturasDeVendaFutura(params: List<Parameter>): List<PainelIntegradoVendas> =
        linhasDeFatura(sqlQueriesService.getAll("faturas-reserva-carregamento.sql", params))

    companion object {
        const val TIPO_FATURA = "NF venda futura"

        /** Codigo exato do vendedor, no mesmo esquema de CarregamentoService.searchPedidos (sem vendedor = todos). */
        internal fun parametrosVendedor(vendedor: Int?): List<Parameter> = listOf(
            Parameter("vendedor", vendedor ?: -2),
            Parameter("vendedorDisable", if (vendedor == null) -2 else Int.MAX_VALUE),
        )

        /** JDBC sem conversao implicita: filial vai como Int. sinaisFrete: ver CarregamentoService.sinaisFrete. */
        internal fun parametrosOdbc(params: List<Parameter>, filial: Int, utilizacoes: List<Int>, sinaisFrete: List<Int>): Map<String, Any?> =
            params.associate { it.key to it.value } + mapOf(
                "branch" to filial,
                "utilizacoes" to utilizacoes,
                "sinaisFrete" to sinaisFrete,
            )

        internal fun linhasDeFatura(rows: List<Map<String, Any?>>): List<PainelIntegradoVendas> =
            rows.groupBy { it["DocEntry"].toString() to it["BaseLine"].toString() }
                .values.flatMap { retiradas ->
                    val linha = retiradas.first()
                    val porOrdem = retiradas.filter { it["OrdemAberta"] != null }
                        .groupBy { it["OrdemAberta"].toString() }
                        .mapValues { (_, daOrdem) -> daOrdem.sumOf { decimal(it["Retirada"]) } }
                    val livre = decimal(linha["OpenQty"]) - porOrdem.values.fold(BigDecimal.ZERO, BigDecimal::add)
                    porOrdem.map { (ordem, quantidade) -> linhaFatura(linha, quantidade, ordem) } +
                        listOfNotNull(if (livre.signum() > 0) linhaFatura(linha, livre, null) else null)
                }

        private fun linhaFatura(linha: Map<String, Any?>, quantidade: BigDecimal, ordem: String?) = PainelIntegradoVendas(
            DocDate = linha["DocDate"]?.toString(),
            CardCode = linha["CardCode"]?.toString(),
            CardName = linha["CardName"]?.toString(),
            SlpCode = linha["SlpCode"]?.toString(),
            SlpName = linha["SlpName"]?.toString(),
            ItemCode = linha["ItemCode"].toString(),
            Description = linha["Description"]?.toString(),
            Usage = (linha["Usage"] as? Number)?.toInt(),
            DistribSum = null,
            Quantity = quantidade,
            OnHand = decimal(linha["OnHand"]),
            Name = CarregamentoService.localidade(linha),
            Incoterms = linha["Incoterms"]?.toString(),
            EstoqueMinimo = linha["EstoqueMinimo"]?.let { decimal(it) },
            EmOrdemDeCarregamento = ordem,
            DocEntry = (linha["DocEntry"] as? Number)?.toInt(),
            Tipo = TIPO_FATURA,
            WhsCode = linha["WhsCode"]?.toString(),
        )

        //Mesmas chaves do GROUP BY das views agrupadas; a quantidade soma e o estoque soma uma vez por deposito.
        internal fun agrupar(linhas: List<PainelIntegradoVendas>, agrupador: String?): List<PainelIntegradoVendas> {
            val chave: (PainelIntegradoVendas) -> List<Any?> = when (agrupador) {
                "cliente" -> { it -> listOf(it.CardCode, it.ItemCode, it.EmOrdemDeCarregamento) }
                "item" -> { it -> listOf(it.ItemCode, it.EmOrdemDeCarregamento) }
                "vendedor" -> { it -> listOf(it.SlpCode, it.ItemCode, it.EmOrdemDeCarregamento) }
                else -> return linhas
            }
            return linhas.groupBy(chave).values.map { grupo ->
                val primeira = grupo.first()
                PainelIntegradoVendas(
                    DocDate = null,
                    CardCode = primeira.CardCode.takeIf { agrupador == "cliente" },
                    CardName = primeira.CardName.takeIf { agrupador == "cliente" },
                    SlpCode = primeira.SlpCode.takeIf { agrupador == "vendedor" },
                    SlpName = primeira.SlpName.takeIf { agrupador == "vendedor" },
                    ItemCode = primeira.ItemCode,
                    Description = primeira.Description,
                    Usage = null,
                    DistribSum = null,
                    Quantity = grupo.sumOf { it.Quantity ?: BigDecimal.ZERO },
                    OnHand = grupo.distinctBy { it.WhsCode }.sumOf { it.OnHand },
                    Name = null,
                    Incoterms = null,
                    EstoqueMinimo = primeira.EstoqueMinimo,
                    EmOrdemDeCarregamento = primeira.EmOrdemDeCarregamento,
                    Tipo = TIPO_FATURA,
                )
            }
        }

        private fun decimal(valor: Any?) = BigDecimal(valor?.toString() ?: "0")
    }
}
