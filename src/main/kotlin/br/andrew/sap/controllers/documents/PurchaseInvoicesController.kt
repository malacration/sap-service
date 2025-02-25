package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.document.PurchaseInvoiceService
import br.andrew.sap.services.document.PurchaseInvoiceforSoftExpert
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import kotlin.math.log
import org.slf4j.MDC
import org.springframework.security.core.Authentication


@RestController
@RequestMapping("nf-entrada")
class PurchaseInvoicesController(val purchaseInvoiceService: PurchaseInvoiceService, val itemService : ItemsService) {
    val logger = LoggerFactory.getLogger(PurchaseInvoicesController::class.java)

    @GetMapping("duplicate/{id}")
    fun duplicate(@PathVariable id : String) : Any{
        val duplicado =  purchaseInvoiceService.getById("'$id'").tryGetValue<PurchaseInvoice>().duplicate()
        return purchaseInvoiceService.save(duplicado)
    }

    @GetMapping("notas/{id}")
    fun notaEntrada(@PathVariable id : Int) : Any{
        return purchaseInvoiceService.getById(id).tryGetValue<PurchaseInvoice>()
    }

    @GetMapping("cancel/{id}")
    fun cancel(@PathVariable id : String) : OData? {
        return purchaseInvoiceService.cancel(id)
    }

    @GetMapping("cancel-duplicate/{cardCode}")
    fun cancelAndDuplicate(@PathVariable cardCode : String) : OData? {
        //TODO terminar essa logica
        val predicate = Predicate("CardCode",cardCode,Condicao.EQUAL)
        return purchaseInvoiceService.get(Filter(mutableListOf(predicate)))
    }


    @GetMapping("entrada/{id}")
    fun nota(@PathVariable id : Int) : Any{
        return purchaseInvoiceService.getById(id)
    }

    @PostMapping("criar")
    fun criarNotaFiscalEntrada(@RequestBody notaFiscal: PurchaseInvoice, auth: Authentication): 
  
  {
        try {
            val document = PurchaseInvoiceforSoftExpert().prepareToSave(notaFiscal, itemService, auth)
            purchaseInvoiceService.save(document).tryGetValue<PurchaseInvoice>()
            return RespostaSoftExpert("Documento recebido no SAP com sucesso!")
        } catch (e: Exception) {
            val mdc = if(MDC.get("X-B3-TraceId") == null) "" else MDC.get("X-B3-TraceId")
            val msg = "Erro ao criar a nota fiscal de entrada [$mdc]: ${e.message}"
            logger.error(msg,e)
            return RespostaSoftExpert(msg)
        }
    }
}

class RespostaSoftExpert(val resultado: String){
}