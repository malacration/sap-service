package br.andrew.sap.services.document

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.bank.VendorPaymentService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DownPaymentService(env: SapEnvrioment,
                         @Value("\${adiantamento.itemcode:none}")
                         val itemAdiantamento : String,
                             restTemplate: RestTemplate,
                             authService: AuthService,
                             val vendorPaymentService: VendorPaymentService
) :
        EntitiesService<Document>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/DownPayments"
    }

    fun adiantamentosVendaFutura(document: Document, contrato: Contrato, paymentInfo: PaymentDueDates): Document {
        //TODO colocar item de adiantamento como parametro?
        val linhas = listOf(Product("FIN0000002",paymentInfo.value.toString(),"1"))
        val adiantamento = DownPayment(
            document.CardCode,
            paymentInfo.dueDate.toString(),
            linhas,
            document.getBPL_IDAssignedToInvoice())
        adiantamento.U_venda_futura = contrato.DocEntry;
        return save(adiantamento).tryGetValue<Document>()
    }
}