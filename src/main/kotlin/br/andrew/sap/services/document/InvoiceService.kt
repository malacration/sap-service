package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.Installment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

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
        val requestes = RequestPixDueDateSemContaBuilder(partner,bussinessPlace,invoice).build()
        requestes.forEach {
            invoice.setPix(it,pixService.genereateFor(it))
        }
        this.update(invoice,invoice.docEntry.toString())
    }

    fun getByIdPix(docEntry: Int){
        val invoice = this.getById(docEntry).tryGetValue<Invoice>()
        val bussinessPlace = bussinessPlaceService
            .getById(invoice.getBPL_IDAssignedToInvoice())
            .tryGetValue<BussinessPlace>()
        val partner = bussinesPartnersService.getById("'${invoice.CardCode}'").tryGetValue<BusinessPartner>()
        val requestes = RequestPixDueDateSemContaBuilder(partner,bussinessPlace,invoice).build()
        requestes.forEach {
            invoice.setPix(it,pixService.genereateFor(it))
        }
        this.update(invoice,invoice.docEntry.toString())
    }

    fun getAllPixs(): Any {
        val url = env.host+"/b1s/v1/SQLQueries"
        return restTemplate.exchange(RequestEntity
            .get("$url('installment-pix.sql')/List")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), Any::class.java).body!!
    }

    fun pendenteGerarPix(): List<Int> {
        val now = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val url = env.host+"/b1s/v1/SQLQueries"
        return restTemplate.exchange(RequestEntity
            .get("$url('installment-gerar-pix.sql')/List?now='${now}'")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), OData::class.java).body
            ?.tryGetPageValues<Installment>()
            ?.mapNotNull { it.DocEntry }?.toList() ?: listOf()
    }
}