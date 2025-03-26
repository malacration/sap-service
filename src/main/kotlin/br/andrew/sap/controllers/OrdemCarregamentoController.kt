package br.andrew.sap.controllers

import br.andrew.sap.controllers.documents.QuotationsController
import br.andrew.sap.controllers.documents.RespostaSoftExpert
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.OrdemCarregamento
import br.andrew.sap.services.*
import br.andrew.sap.services.document.DocumentForAngular
import br.andrew.sap.services.document.PurchaseInvoiceforSoftExpert
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("ordem-carregamento")
class OrdemCarregamentoController(
    val service : BusinessPartnersService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    val logger = LoggerFactory.getLogger(QuotationsController::class.java)

    @GetMapping()
    fun get(): OData {
        return service.get()
    }

//    @PostMapping("criar")
//    fun createOrdemCarregamento(@RequestBody pedido : OrdemCarregamento, auth : Authentication): OrdemCarregamento {
//        return service.save(OrdemCarregamento).tryGetValue<OrdemCarregamento>().also {
//            try{
//                applicationEventPublisher.publishEvent(it)
//            }catch (e : Exception){
//                logger.error(e.message,e)
//            }
//        }
//    }
}