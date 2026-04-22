package br.andrew.sap.schedules

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@Profile("!test")
class AtualizaToken(
    protected val authService: AuthService,
    protected val env: SapEnvrioment) {

    val logger: Logger = LoggerFactory.getLogger(AtualizaToken::class.java)


    @EventListener(ApplicationReadyEvent::class)
    fun warmup() {
        execute()
    }

    @Scheduled(fixedDelayString = "\${sap.service.layer.session.keep-alive-ms:60000}")
    fun execute() {
        try {
            authService.keepAlive(env.getLogin())
        } catch (t : Throwable){
            logger.error(t.message, t)
        }
    }
}
