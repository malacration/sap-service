package br.andrew.sap.controllers.documents

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.services.document.PurchaseInvoiceService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.document.PurchaseInvoiceforSoftExpert
import org.slf4j.MDC
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*


@RestController
@RequestMapping("nf-entrada")
class PurchaseInvoicesController(val purchaseInvoiceService: PurchaseInvoiceService, val itemService : ItemsService) {

    @GetMapping("duplicate/{id}")
    fun duplicate(@PathVariable id : String) : Any{
        val duplicado =  purchaseInvoiceService.getById("'$id'").tryGetValue<PurchaseInvoice>().duplicate()
        return purchaseInvoiceService.save(duplicado)
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

    @PostMapping("criar")
    fun criarNotaFiscalEntrada(@RequestBody notaFiscal: PurchaseInvoice, auth: Authentication): RespostaSoftExpert {
        try {
            val document = PurchaseInvoiceforSoftExpert().prepareToSave(notaFiscal, itemService, auth)
            purchaseInvoiceService.save(document).tryGetValue<PurchaseInvoice>()
            return RespostaSoftExpert("Deu tudo certo")
        } catch (e: Exception) {
            return RespostaSoftExpert("Erro ao criar a nota fiscal de entrada: ${e.message}, ")
            val mdc = if(MDC.get("X-B3-TraceId") == null) "" else MDC.get("X-B3-TraceId")
            throw RuntimeException("Erro ao criar a nota fiscal de entrada [$mdc]: ${e.message}",e)

        }
    }
}

class RespostaSoftExpert(val mensagem : String){
}