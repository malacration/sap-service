package br.andrew.sap.schedules

import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.services.document.InvoiceService
import br.andrew.sap.services.uzzipay.PixPaymentVerificationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty(value = ["pix.schedule.enable"], havingValue = "true", matchIfMissing = false)
class PixPaymentSchedule(
    private val invoiceService: InvoiceService,
    private val pixPaymentVerificationService: PixPaymentVerificationService,
    @Value("\${pix.schedule.consulta-interval-minutes:60}") private val consultaIntervaloMinutos: Long
) {

    private val logger: Logger = LoggerFactory.getLogger(PixPaymentSchedule::class.java)

    @Scheduled(fixedDelayString = "\${pix.schedule.delay-minutes:5}", timeUnit = TimeUnit.MINUTES)
    fun execute() {
        val agora = LocalDateTime.now()
        invoiceService.getPixsGeradosParaConsulta(agora)
            .groupBy { it.docEntry }
            .forEach { (docEntry, installments) ->
                if(docEntry == null) {
                    return@forEach
                }
                try {
                    processaDocumento(docEntry, installments.mapNotNull { it.installmentId }.toSet(), agora)
                } catch (t: Throwable) {
                    logger.error("Erro ao consultar pix do documento {}", docEntry, t)
                }
            }
    }

    private fun processaDocumento(docEntry: Int, installmentsIds: Set<Int>, agora: LocalDateTime) {
        val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()
        val parcelasElegiveis = invoice.documentInstallments
            ?.filter { it.InstallmentId != null && installmentsIds.contains(it.InstallmentId) }
            ?.filter { it.podeConsultarPixPagamento(agora) }
            ?: listOf()

        if(parcelasElegiveis.isEmpty()) {
            return
        }

        parcelasElegiveis.forEach { it.registrarConsultaPix(consultaIntervaloMinutos, agora) }
        invoiceService.update(invoice, invoice.docEntry.toString())
        parcelasElegiveis.forEach { installment ->
            try {
                pixPaymentVerificationService.verificaPixEhBaixa(invoice, installment)
            } catch (t: Throwable) {
                logger.error(
                    "Erro ao verificar pix {} da parcela {} do documento {}",
                    installment.U_pix_reference,
                    installment.InstallmentId,
                    docEntry,
                    t
                )
            }
        }
    }
}
