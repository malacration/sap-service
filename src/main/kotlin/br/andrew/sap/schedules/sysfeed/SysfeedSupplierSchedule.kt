package br.andrew.sap.schedules.sysfeed

import br.andrew.sap.services.sysfeed.SysfeedSupplierService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["sysfeed.fornecedor.enable"], havingValue = "true", matchIfMissing = false)
class SysfeedSupplierSchedule(
    private val service: SysfeedSupplierService
) {
    private val logger = LoggerFactory.getLogger(SysfeedSupplierSchedule::class.java)

    @Scheduled(fixedDelayString = "\${sysfeed.fornecedor.interval-ms:60000}")
    fun execute() {
        try {
            val result = service.executePending()
            logger.info("Sysfeed fornecedores executado. enviados={}, erros={}", result.sent, result.errors)
        } catch (e: Exception) {
            logger.error("Erro ao executar schedule de fornecedores Sysfeed", e)
        }
    }
}
