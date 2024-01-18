package br.andrew.sap.schedules

import br.andrew.sap.events.listener.TelegramListener
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.SerasaService
import br.andrew.sap.services.TelegramRequestService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*


@Component
@ConditionalOnProperty(value = ["serasa-schedule"], havingValue = "true", matchIfMissing = false)
class SerasaSchedule(
    val service : SerasaService,
    val telegramService : TelegramRequestService,
    val bpService : BusinessPartnersService) {

    val logger: Logger = LoggerFactory.getLogger(SerasaSchedule::class.java)

    @Scheduled(fixedDelay = 5000)
    fun execute() {
        val bps = service.getParceirosParaAtualizar()
        bps.forEach {
            try {
                val bp = bpService.getById("'$it'").tryGetValue<BusinessPartner>()
                telegramService.send("Execucao do serasa: cpf: ${bp.getCpfCnpj().value}")
                service.atualizaSerasa(bp.getCpfCnpj())
                bpService.atualizaDataSerasa(Date(),bp)
            }catch (e : Exception){
                telegramService.send("Erro ao executar serasa! ${e.message}")
                logger.error("Erro ao executar serasa! ${e.message}",e)
            }
        }
    }
}