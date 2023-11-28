package br.andrew.sap.controllers


import br.andrew.sap.model.BussinessPlace
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.thymeleaf.TemplateEngine
import java.util.*


@RestController
class IndexController(val version : Version,
                      val paymentMethodService : WizardPaymentMethodService,
                      val templateEngine : TemplateEngine){

    @GetMapping("/")
    fun index() : Version{
        return version
    }
}
