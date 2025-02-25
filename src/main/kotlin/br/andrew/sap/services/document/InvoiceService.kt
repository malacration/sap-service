package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.sap.DocEntry
import br.andrew.sap.model.bank.Payment
import br.andrew.sap.model.bank.PaymentInvoice
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.exceptions.PixPaymentException
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.sap.journal.OriginalJournal
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.journal.EntryOriginalJournal
import br.andrew.sap.services.journal.ServiceOriginalJournal
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

@Service
class InvoiceService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
                     val bussinessPlaceService: BussinessPlaceService,
                     val pixService : DynamicPixQrCodeService,
                     val bussinesPartnersService: BusinessPartnersService,
                     val incomingPaymentService: IncomingPaymentService,
                     val transactionPixService : TransactionsPixService,
                     val bankPlusService: BankPlusService
) :
        EntitiesService<Invoice>(env, restTemplate, authService), ServiceOriginalJournal {
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

    fun getInvoiceByIdPix(reference: String): Invoice {
        val url = env.host+"/b1s/v1/SQLQueries"
        val list = restT.exchange(RequestEntity
            .get("$url('invoice-by-pix-reference.sql')/List?reference='${reference}'")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), OData::class.java).body
            ?.tryGetPageValues<DocEntry>(Pageable.unpaged())
            ?.mapNotNull { getBy(it).tryGetValue<Invoice>() } ?: listOf()
        if(list.size > 1) throw Exception("Mais de uma fatura encontrada para o pix ${reference}")
        return list.firstOrNull() ?: throw Exception("Nenhuma fatura encontrada para o pix ${reference}")
    }

    fun getAllPixs(): Any {
        val url = env.host+"/b1s/v1/SQLQueries"
        return restT.exchange(RequestEntity
            .get("$url('installment-pix.sql')/List")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), Any::class.java).body!!
    }

    fun pendenteGerarPix(): List<Int> {
        val now = SimpleDateFormat("yyyy-MM-dd").format(Date())
        val url = env.host+"/b1s/v1/SQLQueries"
        return restT.exchange(RequestEntity
            .get("$url('installment-gerar-pix.sql')/List?now='${now}'")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build(), OData::class.java).body
            ?.tryGetPageValues<Installment>(Pageable.unpaged())
            ?.mapNotNull { it.DocEntry }?.toList() ?: listOf()
    }


    fun baixaPixBy(docentry : DocEntry): List<String> {
        return baixaPixBy(getBy(docentry).tryGetValue<Invoice>())
    }


    fun baixaPixBy(invoice : Invoice): List<String> {
        val conta = transactionPixService.getContaBy(invoice)
        val transactions = transactionPixService.getBy(invoice,conta)
        return transactions.filter { it.paid }.map {
            try {
                baixaPixBy(it,conta)
            }catch (t : PixPaymentException){
                t.printStackTrace()
                "erro"
            }
        }
    }

    fun baixaPixBy(transaction: Transaction, conta : ContaUzziPayPix)  : String {
        if(!transaction.paid)
            throw Exception("Transacao nao paga")
        val invoice = getInvoiceByIdPix(transaction.txId)
        val parcelaBaixar : Installment = invoice.getInstallmentBy(transaction) ?: throw Exception("Nao foi encontrato uma parcela para conciliar")
        val boletos = bankPlusService.getBoletosBy(invoice)

        val payment = Payment(transaction,conta).also {
            it.cardCode = invoice.CardCode
            it.setBPID(invoice.getBPL_IDAssignedToInvoice())
            it.paymentInvoices = listOf(
                PaymentInvoice(invoice,transaction,parcelaBaixar)
            )
        }
        incomingPaymentService.save(payment)
        boletos.filter { parcelaBaixar.getBy(it) }
            .forEach { bankPlusService.cancelarBoleto(it) }
        return "ok"
    }

    override fun getEntryOriginalJournal(jdtNum: Int): EntryOriginalJournal {
        val filter = Filter(
            Predicate("DocEntry", jdtNum, Condicao.EQUAL)
        )
        return get(filter).tryGetValues<Invoice>().first()
    }

    override fun getOriginalJournal(): OriginalJournal {
        return OriginalJournal.ttARInvoice
    }

    @Deprecated("Usar o metodo generico findById")
    fun findInvoiceById(id: Int, page : Pageable): Page<Invoice>? {
        val filter = Filter(
            Predicate("DocEntry", id,Condicao.EQUAL )
        )
        val result = get(filter,page)
        return result.tryGetPageValues<Invoice>(page)
    }

    @Deprecated("Se o metodo atribuiCentroCustoEmContasRecebeOuPagar funcionar remover esse")
    fun getCostingCodeInvoice(reference: Int, field: String): String? {
        val invoice = getById(reference).tryGetValue<Invoice>()
        return invoice?.DocumentLines?.firstOrNull()?.let { documentLine ->
            when (field) {
                "CostingCode" -> documentLine.CostingCode
                "CostingCode2" -> documentLine.CostingCode2
                else -> null
            }
        }
    }
}