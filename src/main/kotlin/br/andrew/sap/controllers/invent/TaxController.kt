package br.andrew.sap.controllers.invent

import br.andrew.sap.infrastructure.configurations.security.MethodSecurityConfig
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.invent.TaxNfeService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("tax")
class TaxController(
    val documentService : InvoiceService,
    val securit : MethodSecurityConfig,
    val service : TaxNfeService,
    val bpService : BusinessPartnersService
) {

    @GetMapping("{id}",produces = arrayOf("application/pdf"))
    fun getRegistros(@PathVariable id : Int): ByteArray {
        val document = documentService.getById(id).tryGetValue<Document>()
        return service.getBoletosBy(document.getBPL_IDAssignedToInvoice().toInt(),document.docEntry!!,13).ArquivoPdf!!.toByteArray()
    }


    @GetMapping("/{id}/pdf",produces = ["application/pdf"])
    fun getPdf(@PathVariable id : Int,
               user : Authentication?
    ): ByteArray {
        val document = documentService.getById(id).tryGetValue<Document>()
        securit.acessoCliente(user,bpService.getById("'${document.CardCode}'").tryGetValue<BusinessPartner>())
        return service.onlyePdf(document.getBPL_IDAssignedToInvoice().toInt(),document.docEntry!!,13)
    }
}