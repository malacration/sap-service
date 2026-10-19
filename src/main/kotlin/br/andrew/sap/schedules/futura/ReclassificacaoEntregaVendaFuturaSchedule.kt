package br.andrew.sap.schedules.futura

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.comercial.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.financeiro.InternalReconciliationsService
import br.andrew.sap.services.comercial.EstornoReclassificacaoVendaFuturaService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import br.andrew.sap.model.sap.documents.base.DocumentLines


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
    val estornoService : EstornoReclassificacaoVendaFuturaService,
    val odbc : OdbcClient,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
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
        // try por nota: uma entrega que falha (inclusive barrada pela SBO_SP_VALIDACAO_VENDA_FUTURA)
        // nao pode impedir as demais de serem reclassificadas.
        inoviceService.get(entregasFilter).tryGetValues<Invoice>().forEach { invoice ->
            try {
                reclassificar(invoice)
            } catch (e: Exception) {
                logger.error("Erro ao reclassificar a entrega NF ${invoice.docNum} do contrato [${invoice.U_venda_futura}]", e)
            }
        }
    }

    private fun reclassificar(invoice: Invoice) {
        val decisao = DecisaoReclassificacao.de(reclassificacoesAtivas(invoice))
        val journalEntrie = when (decisao) {
            is DecisaoReclassificacao.Pular -> {
                logger.warn("Entrega NF ${invoice.docNum} do contrato [${invoice.U_venda_futura}] aberta, mas ja " +
                    "reclassificada e conciliada no lancamento ${decisao.transId}. Nao sera reclassificada de novo.")
                return
            }
            is DecisaoReclassificacao.Reutilizar ->
                journalEntriesService.getById(decisao.transId).tryGetValue<JournalEntry>()
            DecisaoReclassificacao.Criar ->
                journalEntriesService.save(novaReclassificacao(invoice)).tryGetValue<JournalEntry>()
        }
        internalReconciliationsService.save(
            InternalReconciliationsBuilder(invoice,journalEntrie).build()
        )
    }

    private fun reclassificacoesAtivas(invoice: Invoice): List<ReclassificacaoAtiva> {
        val params = mapOf(
            "referencia" to (invoice.docNum ?: throw Exception("Nota sem DocNum")),
            "memo" to memoReclassificacaoEntrega(invoice),
        )
        return odbc.consultar(SqlResource.carregar("odbc/reclassificacao-entrega-ativa.sql"), params, 100).rows.map {
            ReclassificacaoAtiva(it["TransId"].toString().toBigDecimal().intValueExact(), it["TransCode"].toString())
        }
    }

    private fun novaReclassificacao(invoice: Invoice): JournalEntry {
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
        return JournalEntry(listOf(cred,deb),memoReclassificacaoEntrega(invoice)).also {
            it.TransactionCode = TransactionCodeTypes.VFET.toString()
            it.Reference = invoice.docNum
        }
    }

    @Scheduled(fixedDelay = 10, timeUnit = TimeUnit.MINUTES)
    fun estorno() {
        val filter = Filter(
            Predicate("U_venda_futura",0,Condicao.GREAT),
            Predicate("U_conciliar_automatico",'1',Condicao.EQUAL),
            Predicate("DocDate", "2024-11-05", Condicao.GREAT)
        )

        // try por devolucao: a que falha fica com U_conciliar_automatico = 1 e e tentada de novo na
        // proxima execucao, sem travar as demais.
        creditnotesservice.get(filter).tryGetValues<Invoice>().forEach { devolucao ->
            try {
                if(!devolucao.DocumentLines.any {
                    it.BaseType != 13 ||
                    it.Usage == utilizacaoBaixa ||
                    it.ItemCode == this.itemConciliacaoVendaFutura}
                ) {
                    estornoService.estornar(devolucao, estornoService.baseInvoiceEntriesFromDevolucao(devolucao))
                }
                creditnotesservice.update("{ \"U_conciliar_automatico\" : \"0\"}",devolucao.docEntry.toString())
            } catch (e: Exception) {
                logger.error("Erro ao estornar a devolucao NF ${devolucao.docNum} do contrato [${devolucao.U_venda_futura}]", e)
            }
        }
    }
}