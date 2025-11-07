package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.PainelIntegradoVendas
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service

@Service
class PainelIntegradoVendasService(
    private val sqlQueriesService: SqlQueriesService
) {
    fun searchExpeditionOrders(
        dataInicial: String,
        dataFinal: String,
        filial: String,
        cliente: String?,
        item: String?,
        vendedor: String?,
        agrupador: String?,
        localidade:String?,
        incoterms:String?,
    ): NextLink<PainelIntegradoVendas> {

        val clienteSearch   = cliente?.let   { "%$it%" } ?: "%"
        val itemSearch      = item?.let      { "%$it%" } ?: "%"
        val vendedorSearch  = vendedor?.let  { "%$it%" } ?: "%"
        val localidaSearch  = localidade?.let{ "%$it%" } ?: "%"
        val incotermsSearch = incoterms?.let { "%$it%" } ?: "%"

        val params = listOf(
            Parameter("startDate",   dataInicial),
            Parameter("finalDate",   dataFinal),
            Parameter("branch",      filial),
            Parameter("partner",     clienteSearch),
            Parameter("search",      itemSearch),
            Parameter("salesPerson", vendedorSearch),
            Parameter("localidade",  localidaSearch),
            Parameter("incoterms",   incotermsSearch)
        )

        val sqlFile = when (agrupador) {
            "cliente"  -> "pedidos-agrupado-cliente-carregamento.sql"
            "item"     -> "pedidos-agrupado-item-carregamento.sql"
            "vendedor" -> "pedidos-agrupado-vendedor-carregamento.sql"
            else       -> "pedidos-sem-agrupamento-carregamento.sql"
        }

        val all = sqlQueriesService.getAll<PainelIntegradoVendas>(sqlFile, params)
        return NextLink(content = all, nextLink = "")
    }
}
