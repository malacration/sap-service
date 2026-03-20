package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
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
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.journal.OriginalJournal
import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.dto.InvoicePixUpdatePayload
import br.andrew.sap.model.dto.InstallmentPixConsulta
import br.andrew.sap.model.dto.InstallmentPixResumo
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.Transaction
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import br.andrew.sap.schedules.AutoApprovalPaymentCondition
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.BussinessPlaceService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.journal.EntryOriginalJournal
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.journal.ServiceOriginalJournal
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import br.andrew.sap.services.uzzipay.TransactionsPixService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*

@Service
class InvoiceService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService,
                     val bussinessPlaceService: BussinessPlaceService,
                     val pixService : DynamicPixQrCodeService,
                     val bussinesPartnersService: BusinessPartnersService,
                     val incomingPaymentService: IncomingPaymentService,
                     val transactionPixService : TransactionsPixService,
                     val bankPlusService: BankPlusService,
                     val batchService: BatchService,
                     val journalEntriesService: JournalEntriesService,
                     val sqlQueriesService: SqlQueriesService
) :
        EntitiesService<Document>(env, restTemplate, authService), ServiceOriginalJournal {

    override fun path(): String {
        return "/b1s/v1/Invoices"
    }
    val logger: Logger = LoggerFactory.getLogger(AutoApprovalPaymentCondition::class.java)


    fun createPix(docEntry : Int, parcelas : Int, jurosMoraPercent: Double = 0.0): List<Installment> {
        return createPix(docEntry,listOf(parcelas), jurosMoraPercent)
    }

    fun createPix(invoice: Invoice, parcelas : Int, jurosMoraPercent: Double = 0.0): List<Installment> {
        return createPix(invoice,listOf(parcelas), jurosMoraPercent)
    }

    fun createPix(docEntry: Int, parcelas : List<Int> = listOf(), jurosMoraPercent: Double = 0.0): List<Installment> {
        val invoice = this.getById(docEntry).tryGetValue<Invoice>()
        return createPix(invoice,parcelas,jurosMoraPercent)
    }

    fun createPix(invoice: Invoice, parcela : List<Int> = listOf(), jurosMoraPercent: Double = 0.0): List<Installment> {
        val partner = bussinesPartnersService.getById("'${invoice.CardCode}'").tryGetValue<BusinessPartner>()
        val builder = RequestPixDueDateSemContaBuilder(partner,invoice,parcela,jurosMoraPercent)
        val requestes = builder.build()
        val parcelasSolicitadas = builder.parcelasSolicitadas()
        if (requestes.isEmpty()) {
            return parcelasSolicitadas
        }
        val installmentsAtualizadas = requestes.mapNotNull { invoice.setPix(it, pixService.genereateFor(it)) }
        updatePixInstallments(invoice.docEntry ?: throw Exception("DocEntry da invoice nao pode ser nulo"), installmentsAtualizadas)
        return parcelasSolicitadas
    }

    fun updatePixInstallments(docEntry: Int, installments: List<Installment>) {
        if(installments.isEmpty()) {
            return
        }
        val payload = InvoicePixUpdatePayload.from(installments)
        this.update(payload, docEntry.toString())
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

    fun getAllPixs(): List<InstallmentPixResumo> {
        return sqlQueriesService.getAll("installment-pix.sql")
    }

    fun getPixsGeradosParaConsulta(
        dataReferencia: LocalDateTime
    ): List<InstallmentPixConsulta> {
        val agora = dataReferencia.truncatedTo(MINUTES).toString()
        return sqlQueriesService.getAll(
            "installment-pix-consulta.sql",
            listOf(Parameter("now", agora))
        )
    }

    fun pendenteGerarPix(): List<Int> {
        val now = SimpleDateFormat("yyyy-MM-dd").format(Date())
        return sqlQueriesService.getAll<Installment>(
            "installment-gerar-pix.sql",
            listOf(Parameter("now", now))
        ).mapNotNull { it.DocEntry }
    }


    fun baixaPixBy(docentry : DocEntry): List<Transaction> {
        return baixaPixBy(getBy(docentry).tryGetValue<Invoice>())
    }


    fun baixaPixBy(invoice : Invoice): List<Transaction> {
        val conta = transactionPixService.getContaBy(invoice)
        val transactions = transactionPixService.getBy(invoice,conta)
        return transactions.filter { it.paid }.map { baixaPixBy(it,conta) }
    }

    fun baixaPixBy(transaction: Transaction, conta : ContaUzziPayPix)  : Transaction {
        if(!transaction.paid)
            throw Exception("O Pagamento não foi realizado ainda")
        val invoice = getInvoiceByIdPix(transaction.txId)
        val parcelaBaixar : Installment = invoice.getInstallmentBy(transaction) ?: throw Exception("Nao foi encontrato uma parcela para conciliar")
        val boletos = try {
            bankPlusService.getBoletosBy(invoice)
        } catch (e : Exception){
            logger.warn(e.message,e)
            null
        }
        val payment = Payment(transaction,conta).also {
            it.cardCode = invoice.CardCode
            it.setBPID(invoice.getBPL_IDAssignedToInvoice())
            it.paymentInvoices = listOf(
                PaymentInvoice(invoice,transaction,parcelaBaixar)
            )
        }
        val batchList = BatchList()
        batchList.add(BatchMethod.POST, payment, incomingPaymentService)

        if (conta.hasTransitoryAccount()) {
            val filialTransitoria = conta.idFilialTransitoria
                ?.toIntOrNull() ?: throw Exception("Lancamento sem filial transitoria")
            val valor = transaction.receivedAmount ?: throw Exception("Transacao sem valor recebido")
            val deb = JournalEntryLines(conta.contaContabilBanco, valor, 0.0, filialTransitoria)
            val cred = JournalEntryLines(conta.transitoria!!, 0.0, valor, filialTransitoria)
            val entry = JournalEntry(listOf(deb, cred), "Transferencia Pix transitoria ${transaction.txId} - INV ${invoice.docNum}").also {
                it.Reference = transaction.txId
            }
            batchList.add(BatchMethod.POST, entry, journalEntriesService)
        }
        batchService.run(batchList)
        if(boletos != null){
            boletos.filter { parcelaBaixar.getBy(it) }
            .forEach { bankPlusService.cancelarBoleto(it) }
        }
        return transaction
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
