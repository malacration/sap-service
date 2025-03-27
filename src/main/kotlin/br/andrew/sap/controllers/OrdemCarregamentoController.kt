package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.OrdemCarregamento
import br.andrew.sap.model.sap.partner.ReferenciaComercial
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.OrdemCarregamentoService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ordem-carregamento")
class OrdemCarregamentoController(
    val service: OrdemCarregamentoService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    val logger = LoggerFactory.getLogger(OrdemCarregamentoController::class.java)

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

    @PostMapping("criar")
    fun createOrdemCarregamento(@RequestBody pedido: OrdemCarregamento, auth: Authentication): OrdemCarregamento {
        return service.save(pedido).tryGetValue<OrdemCarregamento>().also {
            try {
                applicationEventPublisher.publishEvent(it)
            } catch (e: Exception) {
                logger.error(e.message, e)
            }
        }
    }

    @GetMapping("search-last-code")
    fun getLastOrdemCode(): OrdemCarregamento? {
        return try {
            service.get(
                Filter(),
                OrderBy(mapOf("U_ordemCode" to Order.DESC))
            ).tryGetValues<OrdemCarregamento>()
                .firstOrNull()
        } catch (e: Exception) {
            println("Erro ao buscar todas as ordens: ${e.message}")
            null
        }
    }
}