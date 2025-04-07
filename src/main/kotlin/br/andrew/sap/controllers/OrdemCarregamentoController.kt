package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sap.partner.OrdemCarregamento
import br.andrew.sap.services.OrdemCarregamentoService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ordem-carregamento")
class OrdemCarregamentoController(
    val service: OrdemCarregamentoService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    val logger = LoggerFactory.getLogger(OrdemCarregamentoController::class.java)

    @GetMapping("")
    fun get(page: Pageable): ResponseEntity<Page<OrdemCarregamento>> {
        val ordens = service.get(
            Filter(),
            OrderBy(mapOf("U_dataCriacao" to Order.DESC, "DocEntry" to Order.DESC)),
            page
        ).tryGetPageValues<OrdemCarregamento>(page)
        logger.info("Returning ${ordens.totalElements} items")
        return ResponseEntity.ok(ordens)
    }

    @GetMapping("/{docEntry}")
    fun getById(@PathVariable docEntry: Int): ResponseEntity<OrdemCarregamento> {
        val ordem = service.get(Filter("DocEntry", docEntry, Condicao.EQUAL)) // Passando como Int diretamente
            .tryGetValues<OrdemCarregamento>()
            .firstOrNull() ?: throw Exception("Ordem com DocEntry $docEntry não encontrada")
        return ResponseEntity.ok(ordem)
    }

    @PostMapping("criar")
    fun createOrdemCarregamento(@RequestBody pedido: OrdemCarregamento): OrdemCarregamento {
        logger.info("Ordem recebida: $pedido")
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
            ).tryGetValues<OrdemCarregamento>().firstOrNull()
        } catch (e: Exception) {
            logger.error("Erro ao buscar todas as ordens: ${e.message}")
            null
        }
    }
}