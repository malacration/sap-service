package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.*
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.bank.PaymentInvoice
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.documents.Installment
import br.andrew.sap.model.documents.Invoice
import br.andrew.sap.model.exceptions.PixPaymentException
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BankPlusService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService){


    fun aplicaDesonerado(order : Document): Document {
        order.productsByTax().forEach{
            val taxCodeDesonerado = taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines.firstOrNull { it.STAType == 25 || it.STAType == 28 }
            if(taxCodeDesonerado != null) {
                var taxParam = taxAuthoritiesService.get(taxCodeDesonerado)
                    .tryGetValue<SalesTaxAuthorities>()
                it.value.forEach { p ->
                    p.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p, taxParam).toString()
                }
            }
        }
        order.u_pedido_update = "0"
        return order
    }
}