package br.andrew.sap.services.futura

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.InternalReconciliationsBuilder
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.InternalReconciliationsService
import br.andrew.sap.services.document.CreditNotesService
import br.andrew.sap.services.document.DownPaymentService
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.journal.JournalEntriesService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Reversão da reclassificação de uma devolução de venda futura: lançamento contábil VFDV e,
 * quando houve apropriação de adiantamento na entrega, o estorno do adiantamento com a
 * respectiva reconciliação.
 *
 * A lógica vinha embutida em [ReclassificacaoEntregaVendaFuturaSchedule.estorno]. Foi
 * extraída para cá para poder ser reusada de forma síncrona pelo endpoint que vincula uma
 * devolução avulsa a um contrato ([br.andrew.sap.controllers.ContratoVendaFuturaController]).
 *
 * As apropriações de adiantamento a estornar são localizadas a partir das notas de saída
 * base ([baseInvoiceEntries]): no fluxo do schedule, essas notas vêm das linhas da própria
 * devolução (nota copiada, `BaseType == oInvoices`); no fluxo avulso, vêm da nota de saída
 * que o usuário informou explicitamente.
 */
@Service
class EstornoReclassificacaoVendaFuturaService(
    val journalEntriesService: JournalEntriesService,
    val internalReconciliationsService: InternalReconciliationsService,
    val inoviceService: InvoiceService,
    val creditnotesservice: CreditNotesService,
    val adiantamentoService: DownPaymentService,
    @Value("\${venda-futura.sequencia_adiantamento:-1}") val sequenceCode: Int,
    @Value("\${venda-futura.conta-adiantamento:none}") val contaAdiantamento: String,
    @Value("\${venda-futura.adiantamento-item:none}") val vfItemAdiantamento: String,
    @Value("\${venda-futura.conta-controle:none}") val contaControleRedutoraPassivo: String,
) {

    val logger = LoggerFactory.getLogger(EstornoReclassificacaoVendaFuturaService::class.java)

    /**
     * Localiza as notas de saída base a partir das linhas da devolução copiada
     * (`BaseType == oInvoices`). Usado pelo schedule, onde a devolução é derivada da nota.
     */
    fun baseInvoiceEntriesFromDevolucao(devolucao: Invoice): List<Int> {
        return creditnotesservice.getById(devolucao.docEntry.toString())
            .tryGetValue<Invoice>().DocumentLines
            .filter { it.BaseType == DocumentTypes.oInvoices.value && it.BaseEntry != null }
            .mapNotNull { it.BaseEntry }
    }

    /**
     * Faz a reversão (VFDV) da [devolucao]. [baseInvoiceEntries] são os DocEntry das notas de
     * saída que originaram a entrega — usados para achar as apropriações de adiantamento a
     * estornar. Não altera `U_conciliar_automatico` (responsabilidade do chamador).
     *
     * Retorna o lançamento VFDV criado/recuperado, para que o chamador possa conciliá-lo com
     * a reclassificação inicial quando fizer sentido (fluxo avulso do controller).
     */
    /**
     * Apropriações de adiantamento vinculadas às notas de saída [baseInvoiceEntries]. Se não
     * estiver vazio, a reclassificação da entrega já foi apropriada (transcode VFEC).
     */
    fun apropriacoes(baseInvoiceEntries: List<Int>): List<Invoice> {
        return baseInvoiceEntries.flatMap { baseEntry ->
            val filter = Filter(
                Predicate("U_TX_DocEntryRef", baseEntry, Condicao.EQUAL),
                Predicate("SequenceCode", sequenceCode, Condicao.EQUAL),
            )
            this.inoviceService.get(filter).tryGetValues<Invoice>()
        }
    }

    fun estornar(devolucao: Invoice, baseInvoiceEntries: List<Int>): JournalEntry {
        val apropriacoes = apropriacoes(baseInvoiceEntries)

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

        val vfdv = journalEntriesService
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
            adiantamento.U_venda_futura = devolucao.U_venda_futura
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
        return vfdv
    }
}
