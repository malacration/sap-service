package br.andrew.sap.schedules

import br.andrew.sap.services.security.acesso.RegrasAcessoCache
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Confere a cada minuto se ha uma versao nova das regras de acesso no SAP.
 *
 * A conferencia roda numa thread PROPRIA, nao na do scheduler do app: o RestTemplate compartilhado
 * nao tem timeout e o scheduler tem uma thread so, entao um SAP travado aqui pararia o pix e o
 * keep-alive da sessao. Se a rodada anterior ainda nao terminou, esta e pulada.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(name = ["acesso.regras.fonte"], havingValue = "sap")
class RegrasAcessoRefreshSchedule(private val cache: RegrasAcessoCache) {

    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "regras-acesso-refresh").also { it.isDaemon = true }
    }
    private val emAndamento = AtomicBoolean(false)

    @Scheduled(fixedDelayString = "\${acesso.regras.refresh-ms:60000}")
    fun agendar() {
        if (!emAndamento.compareAndSet(false, true)) return
        executor.execute {
            try {
                cache.conferir()
            } finally {
                emAndamento.set(false)
            }
        }
    }
}
