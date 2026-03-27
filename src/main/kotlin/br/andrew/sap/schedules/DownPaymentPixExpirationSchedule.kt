package br.andrew.sap.schedules

import br.andrew.sap.model.sap.documents.DownPayment
import br.andrew.sap.services.document.DownPaymentService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnProperty(value = ["pix.schedule.enable"], havingValue = "true", matchIfMissing = false)
class DownPaymentPixExpirationSchedule(
    private val downPaymentService: DownPaymentService
) {
    private val logger: Logger = LoggerFactory.getLogger(DownPaymentPixExpirationSchedule::class.java)

    @Scheduled(fixedDelayString = "\${pix.schedule.adiantamento-expirado-delay-minutes:30}", timeUnit = TimeUnit.MINUTES)
    fun execute() {
        val agora = LocalDateTime.now().truncatedTo(MINUTES)
        downPaymentService.getPixsVencidos(agora.toString())
            .groupBy { it.docEntry }
            .forEach { (docEntry, installments) ->
                if(docEntry == null) {
                    return@forEach
                }
                try {
                    processaAdiantamento(docEntry, installments.mapNotNull { it.installmentId }.toSet(), agora)
                } catch (t: Throwable) {
                    logger.error("Erro ao cancelar pix vencido do adiantamento {}", docEntry, t)
                }
            }
    }

    private fun processaAdiantamento(docEntry: Int, installmentsIds: Set<Int>, agora: LocalDateTime) {
        val downPayment = downPaymentService.getById(docEntry).tryGetValue<DownPayment>()
        val parcelasVencidas = downPayment.documentInstallments
            ?.filter { it.InstallmentId != null && installmentsIds.contains(it.InstallmentId) }
            ?.filter {
                !it.U_pix_reference.isNullOrBlank() && (
                    !it.isPixValido() ||
                        it.getPixConsultarAte()?.truncatedTo(MINUTES)?.let { limite -> !limite.isAfter(agora) } == true
                    )
            }
            ?: listOf()

        if(parcelasVencidas.isEmpty()) {
            return
        }

        downPaymentService.cancel(
            (downPayment.docEntry ?: throw Exception("DocEntry do adiantamento nao pode ser nulo")).toString()
        )
    }
}
