package br.andrew.sap.infrastructure.configurations.uzzipay

import br.andrew.sap.services.BussinessPlaceService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class UzziPayBusinessPlaceInitializer(
    private val envrioment: UzziPayEnvrioment,
    private val bussinessPlaceService: BussinessPlaceService
) {
    private val logger = LoggerFactory.getLogger(UzziPayBusinessPlaceInitializer::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun loadBusinessPlaces() {
        val contasComFilial = envrioment.contas.filter { conta ->
            if (conta.idFilial == null) {
                logger.warn("Conta UzziPay sem idFilial configurado para a chave {}", conta.chavePix)
                return@filter false
            }
            true
        }

        if (contasComFilial.isEmpty()) {
            return
        }

        val businessPlacesById = bussinessPlaceService
            .getAllBusinessPlaces()
            .associateBy { it.BPLID }

        contasComFilial.forEach { conta ->
            val idFilial = conta.idFilial ?: return@forEach
            conta.businessPlace = businessPlacesById[idFilial]
            if (conta.businessPlace == null) {
                logger.warn("BusinessPlace não encontrada para a filial {}", idFilial)
            }
        }
    }
}
