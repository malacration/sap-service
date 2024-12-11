package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.ClosableEntitiesService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrdersService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
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
}