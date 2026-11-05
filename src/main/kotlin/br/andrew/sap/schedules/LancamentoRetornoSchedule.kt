package br.andrew.sap.schedules

import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Rotina automatica do Lancamento de Retorno (LCM do desconto de duplicatas). Ligar e desligar e pela tela (LANC_RET_CFG);
 * esta flag so diz em qual instancia a rotina roda. Ligar em UMA instancia so: a trava contra
 * lancamento duplo vale dentro de uma instancia.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(value = ["jobs.lancamento-retorno"], havingValue = "true", matchIfMissing = false)
class LancamentoRetornoSchedule(val service: LancamentoRetornoService) {

    private val log = LoggerFactory.getLogger(LancamentoRetornoSchedule::class.java)

    @Scheduled(fixedDelayString = "\${lancamento-retorno.intervalo-ms:1800000}", initialDelay = 60000)
    fun execute() {
        try {
            val lancados = service.lancarAutomatico()
            if (lancados > 0) log.info("Lancamento de Retorno automatico: $lancados LCM(s) gerado(s)")
        } catch (e: Exception) {
            log.error("Falha na rotina do Lancamento de Retorno", e)
        }
    }
}
