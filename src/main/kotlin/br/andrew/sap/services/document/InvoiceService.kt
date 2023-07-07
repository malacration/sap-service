package br.andrew.sap.services.document

import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import br.andrew.sap.services.uzzipay.DynamicQrCodeService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class InvoiceService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
                     val taxCodeService: SalesTaxCodeService,
                     val qrCodeService: DynamicQrCodeService,
                     val taxAuthoritiesService: SalesTaxAuthoritiesService) :
        EntitiesService<Invoice>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/Invoices"
    }

    fun createPix(invoice: Invoice){


    }
}