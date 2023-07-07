package br.andrew.sap.services.document

import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.RequestQrCodeBuilder
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class InvoiceService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
                     val bussinessPlaceService: BussinessPlaceService,
                     val pixService : DynamicPixQrCodeService,
                     val bussinesPartnersService: BusinessPartnersService) :
        EntitiesService<Invoice>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/Invoices"
    }

    fun createPix(docEntry: Int){
        val invoice = this.getById(docEntry).tryGetValue<Invoice>()
        val bussinessPlace = bussinessPlaceService
            .getById(invoice.getBPL_IDAssignedToInvoice())
            .tryGetValue<BussinessPlace>()
        val partner = bussinesPartnersService.getById("'${invoice.CardCode}'").tryGetValue<BusinessPartner>()
        val requestes = RequestQrCodeBuilder(partner,bussinessPlace,invoice).build()
        requestes.forEach {
            val chave = pixService.genereateFor(it)
            invoice.setPix(it,chave.toString())
            println("Criando pix para parcela ${it.externalIdentifier} no valor de ${it.getAmount()}")
        }
    }
}