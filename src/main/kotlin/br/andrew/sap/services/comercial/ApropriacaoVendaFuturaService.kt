package br.andrew.sap.services.comercial

import JournalEntry
import br.andrew.sap.model.sap.comercial.DebOrCredt
import br.andrew.sap.model.sap.comercial.InternalReconciliationOpenTransRow
import br.andrew.sap.model.sap.comercial.InternalReconciliations
import br.andrew.sap.model.sap.comercial.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.comercial.ReconciliationRow
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.base.adiantamento.DownPaymentsToDraw
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.model.transaction.UpdateTransactionCode
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.financeiro.InternalReconciliationsService
import br.andrew.sap.services.journal.JournalEntriesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Monta a nota de apropriação de adiantamento contra uma entrega de venda futura já reclassificada
 * (VFET) e reconcilia as duas, virando VFEC. Extraído de ConciliacaoVendaFuturaSchedule para ser
 * reaproveitado tanto pelo schedule automático (match exato) quanto pela baixa manual com spread
 * (BaixaSpreadVendaFuturaService), que informa créditos extras para fechar uma diferença de centavos.
 */
@Service
class ApropriacaoVendaFuturaService(
    val invoiceService: InvoiceService,
    val journalEntriesService: JournalEntriesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val batchService: BatchService,
    @Value("\${venda-futura.adiantamento-item:}") val itemConciliacaoVendaFutura: String,
    @Value("\${venda-futura.sequencia_adiantamento:-1}") val sequenceCode: Int,
    @Value("\${venda-futura.utilizacao.baixa:9}") val usage: Int,
    @Value("\${venda-futura.conta-controle:}") val contaControle: String
) {
    val logger: Logger = LoggerFactory.getLogger(ApropriacaoVendaFuturaService::class.java)

    fun conciliar(
        invoice: Invoice,
        journalReclassificado: JournalEntry,
        downPayments: List<DownPaymentsToDraw>,
        creditosExtras: List<ReconciliationRow> = listOf(),
        notaAdicionalMemo: String? = null
    ): Document {
        if (itemConciliacaoVendaFutura.isBlank() || contaControle.isBlank() || sequenceCode <= 0)
            throw Exception("Configuração de venda-futura (adiantamento-item/conta-controle/sequencia_adiantamento) ausente neste ambiente")

        val invoiceApropiacao = Invoice(
            invoice.CardCode, null,
            listOf(Product(itemConciliacaoVendaFutura, "1",
                "0",
                usage).also {
                it.U_preco_base = 1.0
            }),
            invoice.getBPL_IDAssignedToInvoice()
        ).also {
            it.downPaymentsToDraw = downPayments
            it.U_venda_futura = invoice.U_venda_futura
            it.controlAccount = contaControle
            it.SequenceCode = sequenceCode
            it.salesPersonCode = invoice.salesPersonCode
            it.journalMemo = "Apropriacao adt Com LC ${journalReclassificado.JdtNum} da entrega. NF ${invoice.docNum} | Cont ${invoice.U_venda_futura}" +
                (notaAdicionalMemo?.let { " | $it" } ?: "")
            it.U_TX_DocEntryRef = invoice.docEntry
            it.paymentGroupCode = -1
            //TODO coloocar a referencia da nf de forma estruturada.
        }

        val apropriado = invoiceService
            .save(invoiceApropiacao)
            .tryGetValue<Document>()

        val internalRecon = InternalReconciliationsBuilder(
            journalReclassificado.getReconciliationRows(DebOrCredt.Debit),
            apropriado.getReconciliationRows(DebOrCredt.Credt) + creditosExtras,
        ).build()
        try {
            val updateTransCode = UpdateTransactionCode(
                journalReclassificado.JdtNum.toString(),
                TransactionCodeTypes.VFEC
            )

            val batchList = BatchList().add(
                Triple(BatchMethod.POST, internalRecon, internalReconciliationsService)
            ).add(
                Triple(BatchMethod.PATCH, updateTransCode, journalEntriesService)
            )
            batchService.run(batchList)
        } catch (e: Exception) {
            logger.error(
                "Erro ao reconciliar apropriacao de adiantamento. {}",
                reconciliationDebug(journalReclassificado, invoice, apropriado, internalRecon)
            )
            invoiceService.cancel(apropriado.docEntry.toString())
            throw Exception("Não foi possivel realizar a reconciliação, fazendo o cancelamento da apropriação do adiantamento", e)
        }
        return apropriado
    }

    private fun reconciliationDebug(
        journalReclassificado: JournalEntry,
        invoice: Invoice,
        apropriado: Document,
        internalRecon: InternalReconciliations
    ): String {
        val linhas = internalRecon.internalReconciliationOpenTransRows
            ?.joinToString(" | ") { row -> formatRow(row) }
            ?: "sem linhas"
        return "contrato=${invoice.U_venda_futura}, cardCode=${invoice.CardCode}, " +
            "invoiceEntregaDocEntry=${invoice.docEntry}, invoiceEntregaDocNum=${invoice.docNum}, " +
            "invoiceApropriacaoDocEntry=${apropriado.docEntry}, invoiceApropriacaoDocNum=${apropriado.docNum}, " +
            "journalJdtNum=${journalReclassificado.JdtNum}, journalMemo='${journalReclassificado.memo}', linhas=[$linhas]"
    }

    private fun formatRow(row: InternalReconciliationOpenTransRow): String {
        return "tipo=${row.creditOrDebit}, transId=${row.transId}, transRowId=${row.transRowId}, " +
            "valor=${row.reconcileAmount}, parceiro=${row.shortName}"
    }
}
