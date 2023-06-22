package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.Version
import br.andrew.sap.model.documents.PurchaseInvoice
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.DummyService
import br.andrew.sap.services.bank.VendorPaymentService
import br.andrew.sap.services.document.PurchaseInvoiceService
import br.andrew.sap.services.structs.QuerysServices
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class IndexController(val env: SapEnvrioment,
                      val restTemplate: RestTemplate,
                      val authService: AuthService,
                      val version : Version,
                      val purcharseInvoiceService : PurchaseInvoiceService,
                      val vendorPaymentService: VendorPaymentService,
                      val queryService : QuerysServices,
                      val service: DummyService) {

    @GetMapping("/")
    fun index() : Version{
        return version
    }

    @GetMapping("teste")
    fun teste() : Any {
        val cardCode = "FOR0000507"//FOR0000759,FOR0000509,FOR0000116,FOR0000378
        val contaErrada = "2.1.1.001.00001"
        val contaCorreta = "2.1.1.001.03000"

        val notas = purcharseInvoiceService.get(Filter(
            Predicate("CardCode",cardCode,Condicao.EQUAL),
            Predicate("ControlAccount",contaErrada,Condicao.EQUAL)
        )).tryGetValues<PurchaseInvoice>()


        //TODO fazer uma view para salvar no service layer e localizar os pagamentos
        //return notas.map{ it.duplicate().also { it.controlAccount = contaCorreta } }.forEach {
        //    purcharseInvoiceService.save(it)
        //}

        return notas.filter { it.docEntry != null }.map { vendorPaymentService.getPaymentBy(it.docEntry.toString()) }
    }
}