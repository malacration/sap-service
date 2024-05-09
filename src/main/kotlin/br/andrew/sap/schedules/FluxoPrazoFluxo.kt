package br.andrew.sap.schedules

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.AtualizacaoCadastralService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.TelegramRequestService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
@ConditionalOnProperty(value = ["org.quartz.enable"], havingValue = "true", matchIfMissing = false)
class FluxoPrazoFluxo(
    val atualizaCadastroService : AtualizacaoCadastralService,
    val telegramRequestService: TelegramRequestService,
    val businessPartnersService: BusinessPartnersService) {

    val logger: Logger = LoggerFactory.getLogger(FluxoPrazoFluxo::class.java)


    @Scheduled(fixedDelay = 300000)
    fun execute() {
        val predicados = mutableListOf(
            Predicate("U_fazer_fluxo_prazo", "1", Condicao.EQUAL)
        )
        businessPartnersService
            .get(Filter(predicados)).tryGetValues<BusinessPartner>()
            .forEach {
                try {
                    atualizaCadastroService.atualizaCardCode(it)
                } catch (e: Exception) {
                    "Erro ao executar fluxo de prazo para ${it.cardCode} - ${it.cardName}".also {
                        logger.error(it, e)
                        telegramRequestService.send(it)
                    }
                }
            }
    }
}