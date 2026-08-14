package br.andrew.sap.services.romaneio

import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.romaneio.RomaneioFazendaInsumo
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RomaneioEntradaInsumoService(restTemplate: RestTemplate,
                                   env: SapEnvrioment,
                                   authService : AuthService) : EntitiesService<RomaneioFazendaInsumo>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/PECU_UDO_REGR"
    }
}