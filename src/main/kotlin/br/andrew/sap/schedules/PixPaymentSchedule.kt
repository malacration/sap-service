package br.andrew.sap.schedules

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.services.document.DownPaymentService
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
    private val downPaymentService: DownPaymentService,
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
                    processaInvoice(docEntry, installments.mapNotNull { it.installmentId }.toSet(), agora)
                } catch (t: Throwable) {
                    logger.error("Erro ao consultar pix do documento {}", docEntry, t)
                }
            }
        downPaymentService.getPixsGeradosParaConsulta(agora.truncatedTo(java.time.temporal.ChronoUnit.MINUTES).toString())
            .groupBy { it.docEntry }
            .forEach { (docEntry, installments) ->
                if(docEntry == null) {
                    return@forEach
                }
                try {
                    processaAdiantamento(docEntry, installments.mapNotNull { it.installmentId }.toSet(), agora)
                } catch (t: Throwable) {
                    logger.error("Erro ao consultar pix do adiantamento {}", docEntry, t)
                }
            }
    }

    private fun processaInvoice(docEntry: Int, installmentsIds: Set<Int>, agora: LocalDateTime) {
        val invoice = invoiceService.getById(docEntry).tryGetValue<Invoice>()
        val parcelasElegiveis = invoice.documentInstallments
            ?.filter { it.InstallmentId != null && installmentsIds.contains(it.InstallmentId) }
            ?.filter { it.podeConsultarPixPagamento(agora) }
            ?: listOf()

        if(parcelasElegiveis.isEmpty()) {
            return
        }

        parcelasElegiveis.forEach { it.registrarConsultaPix(consultaIntervaloMinutos, agora) }
        invoiceService.updatePixInstallments(
            invoice.docEntry ?: throw Exception("DocEntry da invoice nao pode ser nulo"),
            parcelasElegiveis
        )
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

    private fun processaAdiantamento(docEntry: Int, installmentsIds: Set<Int>, agora: LocalDateTime) {
        val downPayment = downPaymentService.getById(docEntry).tryGetValue<DownPayment>()
        val parcelasElegiveis = downPayment.documentInstallments
            ?.filter { it.InstallmentId != null && installmentsIds.contains(it.InstallmentId) }
            ?.filter { it.podeConsultarPixPagamento(agora) }
            ?: listOf()

        if(parcelasElegiveis.isEmpty()) {
            return
        }

        parcelasElegiveis.forEach { it.registrarConsultaPix(consultaIntervaloMinutos, agora) }
        downPaymentService.updatePixInstallments(
            downPayment.docEntry ?: throw Exception("DocEntry do adiantamento nao pode ser nulo"),
            parcelasElegiveis
        )
        parcelasElegiveis.forEach { installment ->
            try {
                pixPaymentVerificationService.verificaPixEhBaixa(downPayment, installment)
            } catch (t: Throwable) {
                logger.error(
                    "Erro ao verificar pix {} da parcela {} do adiantamento {}",
                    installment.U_pix_reference,
                    installment.InstallmentId,
                    docEntry,
                    t
                )
            }
        }
    }
}
