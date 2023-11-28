package br.andrew.sap.controllers


import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.User
import br.andrew.sap.model.Version
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.documents.DocumentReport
import br.andrew.sap.model.documents.base.Document
import br.andrew.sap.model.documents.base.Product
import br.andrew.sap.model.partner.BPFiscalTaxID
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.CpfCnpj
import br.andrew.sap.services.*
import br.andrew.sap.services.bank.*
import br.andrew.sap.services.document.QuotationsService
import br.andrew.sap.services.softexpert.DocumentExpertService
import br.andrew.sap.services.softexpert.WorkFlowService
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*

@RestController
@RequestMapping("assinatura")
class Pedido4AssinaturaController(val service : QuotationsService,
                                  val businessPartnerService: BusinessPartnersService,
                                  val paymentMethodService : WizardPaymentMethodService,
                                  val templateEngine : TemplateEngine,
                                  val businessPlaceService: BussinessPlaceService) {

    @GetMapping("/{id}/html")
    fun html(@PathVariable id : String) : String {
        service.getById("${id}").tryGetValue<Document>().let {
            return html(report(it))
        }
    }

    fun html(p : DocumentReport): String {
        return this.templateEngine.process("pedido-4-assinatura",
            Context().also {
                it.setVariable("p", p)
            })
    }

    @GetMapping("/{id}/pdf", produces = ["application/pdf"])
    fun pdf(@PathVariable id : String) : ByteArray {
        service.getById("${id}").tryGetValue<Document>().let {
            return pdf(it)
        }
    }
    fun pdf(order : Document) : ByteArray {
        return pdf(report(order))
    }

    fun pdf(report: DocumentReport): ByteArray {
        html(report).let {
            return PdfService().htmlToPdf(it)
        }
    }

    fun report(order : Document): DocumentReport {
        val businessPartner = businessPartnerService.getById("'${order.CardCode}'").tryGetValue<BusinessPartner>()
        val place = businessPlaceService.getById("${order.getBPL_IDAssignedToInvoice()}").tryGetValue<BussinessPlace>()
        val paymentMethod =
            if (order.paymentMethod != null  && order.paymentMethod != "")
                paymentMethodService.getById("'${order.paymentMethod}'").tryGetValue()
            else
                PaymentMethod("").also { it.Description = "" }
        return DocumentReport(order, businessPartner, place, paymentMethod)
    }
}
