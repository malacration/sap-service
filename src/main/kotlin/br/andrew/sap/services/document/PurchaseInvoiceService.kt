package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.sap.documents.PurchaseInvoice
import br.andrew.sap.model.sap.documents.PurchaseReturns
import br.andrew.sap.model.sap.journal.OriginalJournal
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.bank.VendorPaymentService
import br.andrew.sap.services.journal.EntryOriginalJournal
import br.andrew.sap.services.journal.ServiceOriginalJournal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class PurchaseInvoiceService(env: SapEnvrioment,
                             restTemplate: RestTemplate,
                             authService: AuthService,
                             val vendorPaymentService: VendorPaymentService
) :
        EntitiesService<PurchaseInvoice>(env, restTemplate, authService), ServiceOriginalJournal {

    override fun path(): String {
        return "/b1s/v1/PurchaseInvoices"
    }

    fun findByCardCodeAndContaControle(cardCode : String, contaErrada : String): List<PurchaseInvoice> {
        return get(
            Filter(
            Predicate("CardCode",cardCode, Condicao.EQUAL),
            Predicate("ControlAccount",contaErrada, Condicao.EQUAL),
            Predicate(Cancelled.column, Cancelled.tNO, Condicao.EQUAL),
        )
        ).tryGetValues<PurchaseInvoice>()
    }

    fun duplicaNotaComPagamentosNotTransactionSafe(nota : PurchaseInvoice, contaCorreta : String){
        val pagamentos = vendorPaymentService
            .getPaymentBy(nota.docEntry.toString())
            .tryGetValues<Payment>()
        pagamentos.forEach{
            vendorPaymentService.cancel(it.docEntry.toString())
        }
        cancel(nota.docEntry.toString())
        val newNota = save(nota.duplicate().also { it.controlAccount = contaCorreta })
            .tryGetValue<PurchaseInvoice>()
        pagamentos.forEach { vendorPaymentService.save(it.duplicateFor(newNota)) }
    }

    override fun getEntryOriginalJournal(jdtNum: Int): EntryOriginalJournal {
        val filter = Filter(
            Predicate("DocEntry", jdtNum, Condicao.EQUAL)
        )
        return get(filter).tryGetValues<PurchaseInvoice>().first()
    }

    override fun getOriginalJournal(): OriginalJournal {
        return OriginalJournal.ttAPInvoice
    }
}