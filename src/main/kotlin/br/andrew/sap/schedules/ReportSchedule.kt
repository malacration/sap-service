package br.andrew.sap.schedules

import br.andrew.sap.services.integracao.AgendaRelatorioService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * A cada minuto pergunta a agenda (tela Configuracoes > E-mail de inadimplencia) se e hora de enviar.
 * Dia, hora e copia vem do SAP; sem registro vale segunda 07:00. O envio roda em thread propria.
 */
@Component
@ConditionalOnProperty(value = ["report.enable"], havingValue = "true", matchIfMissing = false)
class ReportSchedule(
    val agenda: AgendaRelatorioService) {

    @Scheduled(cron = "0 * * * * *")
    fun execute() {
        agenda.verificar()
    }
}
