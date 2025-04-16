package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.controllers.documents.OrderSalesController
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.WarehouseDefault
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.CpfCnpj
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.ClosableEntitiesService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrdersService(val sqlQueriesService : SqlQueriesService, env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService, val warehouseDefault: List<WarehouseDefault>) :
        EntitiesService<Document>(env, restTemplate, authService), ClosableEntitiesService<OrderSales> {
    override fun path(): String {
        return "/b1s/v1/Orders"
    }

    fun getByContrato(contrato: Contrato, page : Pageable = Pageable.unpaged()): Page<Document> {
        return getByContrato(
            contrato.DocEntry ?: throw Exception("O id do contrato nao pode sere nulo"),
            page
        )
    }

    fun getByContrato(idContrato: Int, page : Pageable = Pageable.unpaged()): Page<Document> {
        val filter = Filter(mutableListOf(
            Predicate("U_venda_futura", idContrato, Condicao.EQUAL),
            Predicate("DocumentStatus", DocumentStatus.bost_Open,Condicao.EQUAL),
        ))
        return get(filter).tryGetPageValues<Document>(page)
    }

    fun fullSearch(filter: OrderSalesController.OrderSalesFilter): NextLink<OrderSales> {
        val dataInicial = if (filter.dataInicial.isNotEmpty()) filter.dataInicial else "2000-01-01"
        val dataFinal = if (filter.dataFinal.isNotEmpty()) filter.dataFinal else "2099-12-31"
        val localidade = if (filter.localidade.isNotEmpty()) filter.localidade else "%"
        val filial = if (filter.filial.isNotEmpty()) filter.filial else "%"
        val skip = filter.skip ?: 0

        val parametros = listOf(
            Parameter("startDate", dataInicial),
            Parameter("finalDate", dataFinal),
            Parameter("localidade", localidade),
            Parameter("filial", filial),
            Parameter("skip", skip.toString())
        )
        val result = sqlQueriesService
            .execute("search-pedido-venda-carregamento.sql", parametros)!!
            .tryGetNextValues<OrderSales>()

        result.content.forEach { order ->
            order.DocumentLines.forEach { line ->
                if (line.WarehouseCode == null) {
                    val warehouse = warehouseDefault.find { it.BPLID == filial }?.defaultWarehouseID
                    line.WarehouseCode = warehouse ?: "01"
                }
            }
        }
        return result
    }
}