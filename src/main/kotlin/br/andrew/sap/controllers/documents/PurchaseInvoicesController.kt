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


@RestController
@RequestMapping("nf-entrada")
class PurchaseInvoicesController(val purchaseInvoiceService: PurchaseInvoiceService) {

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
}