package br.andrew.sap.schedules.futura

import JournalEntry
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.adiantamento.ApropriacaoAdiantamento
import br.andrew.sap.model.transaction.TransactionCodeTypes
import br.andrew.sap.services.comercial.ApropriacaoVendaFuturaService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
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
@ConditionalOnProperty(value = [
    "venda-futura.adiantamento-item",
    "venda-futura.conta-controle"], matchIfMissing = false)
class ConciliacaoVendaFuturaSchedule(
    val adiantamentoService : DownPaymentService,
    val journalEntriesService : JournalEntriesService,
    val inoviceService : InvoiceService,
    val apropriacaoVendaFuturaService: ApropriacaoVendaFuturaService,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>) {

    val logger: Logger = LoggerFactory.getLogger(ConciliacaoVendaFuturaSchedule::class.java)


    @Scheduled(fixedDelay = 15, timeUnit = TimeUnit.MINUTES)
    fun execute() {
        val filterReclassificacaoEntrega = Filter(
            Predicate("TransactionCode", TransactionCodeTypes.VFET, Condicao.EQUAL),
            Predicate("TaxDate", "2024-12-04", Condicao.GREAT),
        )

        journalEntriesService.getAll(JournalEntry::class.java,filterReclassificacaoEntrega)
            .filter { filiais.contains(it.getFilial())}
            .forEach { journalReclassificado ->
                try {
                    val ref = journalReclassificado.Reference
                        ?.toIntOrNull()
                        ?: throw Exception("Nao tem numero de referencia. ${journalReclassificado.JdtNum}")
                    val invoiceFilter = Filter(
                        Predicate("U_venda_futura", 0, Condicao.GREAT),
                        Predicate("DocNum", ref, Condicao.EQUAL)
                    )
                    inoviceService.get(invoiceFilter).tryGetValues<Invoice>().forEach { invoice ->
                        val adiantamentos = adiantamentoService.adiantamentosAbertos(invoice)
                        val adiantamentosDisponiveis = ApropriacaoAdiantamento(invoice, adiantamentos).get()
                        if (adiantamentosDisponiveis.isNotEmpty()) {
                            apropriacaoVendaFuturaService.conciliar(invoice, journalReclassificado, adiantamentosDisponiveis)
                        }
                    }
                }catch (e : Exception){
                    logger.error("Erro no processamento da conciliação da venda futura! OJDT ${journalReclassificado.JdtNum}",e)
                }
        }
    }
}
