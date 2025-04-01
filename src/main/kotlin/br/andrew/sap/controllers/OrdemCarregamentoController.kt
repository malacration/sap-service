package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.authentication.User
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

// br.andrew.sap.controllers/OrdemCarregamentoController.kt
@RestController
@RequestMapping("ordem-carregamento")
class OrdemCarregamentoController(
    val service: OrdemCarregamentoService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    val logger = LoggerFactory.getLogger(OrdemCarregamentoController::class.java)

    @GetMapping("")
    fun get(page: Pageable): ResponseEntity<Page<OrdemCarregamento>> {
        val contratos = service.get(
            Filter(),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<OrdemCarregamento>(page)
        return ResponseEntity.ok(contratos)
    }

    @GetMapping("all")
    fun getAll(page : Pageable): ResponseEntity<Page<OrdemCarregamento>> {
        val contratos = service.get(Filter(),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<OrdemCarregamento>(page)
        return ResponseEntity.ok(contratos)
    }

    @PostMapping("criar")
    fun createOrdemCarregamento(@RequestBody pedido: OrdemCarregamento, auth: Authentication): OrdemCarregamento {
        logger.info("Ordem recebida: $pedido")
        logger.info("Cabeçalho: U_orderCode=${pedido.orderCode}, U_ordemName=${pedido.ordemName}, U_pesoTotal=${pedido.pesoTotal}")
        logger.info("Linhas: ${pedido.linhas}")
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
                OrderBy(mapOf("U_orderCode" to Order.DESC))
            ).tryGetValues<OrdemCarregamento>()
                .firstOrNull()
        } catch (e: Exception) {
            println("Erro ao buscar todas as ordens: ${e.message}")
            null
        }
    }
}