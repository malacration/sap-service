package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.PainelIntegradoVendas
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class PainelIntegradoVendasService(
    private val sqlQueriesService: SqlQueriesService
) {
    fun fullSearchPedidos(
        dataInicial: String,
        dataFinal: String,
        cliente: String?,
        item: String?,
        vendedor: String?,
    ): NextLink<PainelIntegradoVendas>? {
        val clienteSearch = cliente?.let { "%${it}%" } ?: "%"
        val vendedorSearch = vendedor?.let { "%${it}%" } ?: "%"
        val itemSearch = item?.let { "%${it}%" } ?: "%"
        val params = listOf(
            Parameter("startDate", dataInicial),
            Parameter("finalDate", dataFinal),
            Parameter("partner", clienteSearch),
            Parameter("search", itemSearch),
            Parameter("salesPerson", vendedorSearch)
        )
        return sqlQueriesService
            .execute("pedidos-carregamento.sql", params)
            ?.tryGetNextValues<PainelIntegradoVendas>()
    }




}