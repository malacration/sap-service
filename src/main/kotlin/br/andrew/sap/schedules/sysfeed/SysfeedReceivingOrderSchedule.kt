package br.andrew.sap.schedules.sysfeed

import br.andrew.sap.services.sysfeed.SysfeedReceivingOrderService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicBoolean

@Component
@Profile("!test")
@ConditionalOnProperty(value = ["sysfeed.recebimento.enable"], havingValue = "true", matchIfMissing = false)
class SysfeedReceivingOrderSchedule(
    private val service: SysfeedReceivingOrderService,
    @Value("\${sysfeed.recebimento.enable:false}") private val enabled: Boolean
) {
    private val logger = LoggerFactory.getLogger(SysfeedReceivingOrderSchedule::class.java)
    private val running = AtomicBoolean(false)

    @Scheduled(fixedDelayString = "\${sysfeed.recebimento.interval-ms:60000}")
    fun execute() {
        if (!enabled || !running.compareAndSet(false, true)) return
        try {
            val result = service.executePending()
            logger.info("Sysfeed recebimento executado. enviados={}, duplicados={}, erros={}", result.sent, result.duplicated, result.errors)
        } finally {
            running.set(false)
        }
    }
}
