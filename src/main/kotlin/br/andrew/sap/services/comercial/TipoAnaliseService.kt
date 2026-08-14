package br.andrew.sap.services.comercial
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.romaneio.TipoAnalise
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class TipoAnaliseService(restTemplate: RestTemplate,
                         env: SapEnvrioment,
                         authService : AuthService) : EntitiesService<TipoAnalise>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/PECU_UDO_REGR"
    }
}