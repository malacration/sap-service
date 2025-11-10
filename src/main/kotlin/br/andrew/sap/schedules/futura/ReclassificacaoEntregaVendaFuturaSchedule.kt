package br.andrew.sap.schedules.futura

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.invent.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = ["venda-futura.conta-controle"], matchIfMissing = false)
class ReclassificacaoEntregaVendaFuturaSchedule(
    protected val authService: AuthService,
    val bankplus : BankPlusService,
    val batchService: BatchService,
    val journalEntriesService : JournalEntriesService,
    val creditnotesservice : CreditNotesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService : InvoiceService,
    val adiantamentoService : DownPaymentService,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.sequencia_adiantamento}") val sequenceCode : Int,
    @Value("\${venda-futura.conta-adiantamento}") val contaAdiantamento : String,
    @Value("\${venda-futura.adiantamento-item:none}") val vfItemAdiantamento : String,
    @Value("\${venda-futura.adiantamento-item}") val itemConciliacaoVendaFutura : String,
    @Value("\${venda-futura.utilizacao.baixa:79}") val utilizacaoBaixa : Int,
    @Value("\${venda-futura.conta-controle}") val contaControleRedutoraPassivo : String) {

    val logger: Logger = LoggerFactory.getLogger(ReclassificacaoEntregaVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    fun execute() {
        val entregasFilter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("DocDate", "2024-11-05", Condicao.GREAT),
            Predicate("U_entrega_vf", "1", Condicao.EQUAL),
            Predicate("U_conciliar_automatico", "1", Condicao.EQUAL),
            Predicate("DocumentStatus",DocumentStatus.bost_Open,Condicao.EQUAL),
            Predicate("BPL_IDAssignedToInvoice", filiais, Condicao.IN),
        )
        inoviceService.get(entregasFilter).tryGetValues<Invoice>().forEach { invoice ->
            val filial = invoice.getBPL_IDAssignedToInvoice()
            val docTotal = invoice.DocTotal?.toDoubleOrNull() ?: throw Exception("valor Documento nao e valido")
            val cred = JournalEntryLines(
                invoice.controlAccount?: throw Exception("Lancamento sem conta controle"),
                0.0,docTotal,
                filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")).also {
                it.ShortName = invoice.CardCode
            }
            val deb = JournalEntryLines(contaControleRedutoraPassivo,docTotal,0.0,filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")).also {
                it.ShortName = invoice.CardCode
            }

            val journalEntrie = journalEntriesService
                .saveOrRecouverReference(
                    JournalEntry(listOf(cred,deb),"Reclassificação entrega. Mercadoria venda futura [${invoice.U_venda_futura}]. NF Num ${invoice.docNum}").also {
                        it.TransactionCode = TransactionCodeTypes.VFET.toString()
                        it.Reference = invoice.docNum
                    })
            internalReconciliationsService.save(
                InternalReconciliationsBuilder(invoice,journalEntrie).build()
            )
        }
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    fun estorno() {
        val filter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("U_conciliar_automatico",'1',Condicao.EQUAL),
            Predicate("DocDate", "2024-11-05", Condicao.GREAT)
        )

        creditnotesservice.get(filter).tryGetValues<Invoice>().forEach { devolucao ->
            if(!devolucao.DocumentLines.any {
                it.BaseType != 13 ||
                it.Usage == utilizacaoBaixa ||
                it.ItemCode == this.itemConciliacaoVendaFutura}
            ) {
                val apropriacoes = creditnotesservice.getById(devolucao.docEntry.toString())
                    .tryGetValue<Invoice>().DocumentLines
                    .filter { it.BaseType == DocumentTypes.oInvoices.value && it.BaseEntry != null }
                    .map { it.BaseEntry }
                    .flatMap {
                        val filter = Filter(
                            Predicate("U_TX_DocEntryRef", it!!, Condicao.EQUAL),
                            Predicate("SequenceCode", sequenceCode, Condicao.EQUAL),
                        )
                        this.inoviceService.get(filter).tryGetValues<Invoice>()
                    }

                val filial = devolucao.getBPL_IDAssignedToInvoice()
                val docTotal = devolucao.DocTotal?.toDoubleOrNull() ?: throw Exception("valor Documento nao e valido")

                val deb = JournalEntryLines(
                    devolucao.controlAccount ?: throw Exception("Lancamento sem conta controle"),
                    docTotal, 0.0,
                    filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")
                ).also {
                    it.ShortName = devolucao.CardCode
                }
                val cred = if (apropriacoes.isNotEmpty())
                    JournalEntryLines(
                        contaAdiantamento,
                        0.0,
                        docTotal,
                        filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")
                    )
                else
                    JournalEntryLines(
                        contaControleRedutoraPassivo,
                        0.0,
                        docTotal,
                        filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")
                    ).also {
                        it.ShortName = devolucao.CardCode
                    }

                val journalEntrie = journalEntriesService
                    .saveOrRecouverReference(
                        JournalEntry(
                            listOf(cred, deb),
                            "Reclassificação Devolução venda futura [${devolucao.U_venda_futura}]. NF Num ${devolucao.docNum}"
                        ).also {
                            it.TransactionCode = TransactionCodeTypes.VFDV.toString()
                            it.Reference = devolucao.docNum
                        })

                //Teve apropriacao entao fazer estorno do adiantamento
                if (apropriacoes.isNotEmpty()) {
                    val cred = JournalEntryLines(
                        devolucao.controlAccount ?: throw Exception("Lancamento sem conta controle"),
                        0.0, docTotal,
                        filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")
                    ).also {
                        it.ShortName = devolucao.CardCode
                    }
                    val deb = JournalEntryLines(
                        contaAdiantamento,
                        docTotal,
                        0.0,
                        filial.toIntOrNull() ?: throw Exception("Lancamento sem filial")
                    )
                    val memo =
                        "Baixa adiantamento com devolucao. venda futura [${devolucao.U_venda_futura}]. NF Num ${apropriacoes.first().docNum}"
                    val devolucaoApropriacao = journalEntriesService
                        .saveOrRecouverReference(JournalEntry(listOf(cred, deb), memo).also {
                            it.Reference = apropriacoes.first().docNum
                            it.TransactionCode = TransactionCodeTypes.AROU.toString()
                        })

                    //----- adiantamento ----

                    if (vfItemAdiantamento == "none")
                        throw Exception("O parametro [venda-futura.adiantamento-item] nao pode ser $vfItemAdiantamento")
                    val linhas = listOf(Product(vfItemAdiantamento, "1", docTotal.toString()))
                    val adiantamento = DownPayment(
                        devolucao.CardCode,
                        null,
                        linhas,
                        filial
                    )
                    adiantamento.U_venda_futura = devolucao.U_venda_futura;
                    adiantamento.journalMemo =
                        "Reconhecimento de adiantamento de devolucao da VF [${devolucao.U_venda_futura}]. NF Num ${devolucao.docNum}"
                    adiantamento.comments = adiantamento.journalMemo
                    adiantamento.U_TX_DocEntryRef = devolucao.docEntry

                    var adiantamentoSalvo =
                        adiantamentoService.get(Filter("U_TX_DocEntryRef", devolucao.docEntry!!, Condicao.EQUAL))
                            .tryGetValues<DownPayment>().firstOrNull()
                    if (adiantamentoSalvo == null)
                        adiantamentoSalvo = adiantamentoService.save(adiantamento).tryGetValue<DownPayment>()

                    internalReconciliationsService.save(
                        InternalReconciliationsBuilder(
                            adiantamentoSalvo,
                            devolucaoApropriacao
                        ).build()
                    )
                }
            }
            creditnotesservice.update("{ \"U_conciliar_automatico\" : \"0\"}",devolucao.docEntry.toString())
        }
    }
}