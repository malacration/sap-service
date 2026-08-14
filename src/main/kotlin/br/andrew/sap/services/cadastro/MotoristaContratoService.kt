package br.andrew.sap.services.cadastro
import br.andrew.sap.model.cadastro.MotoristaContrato
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class MotoristaContratoService(env : SapEnvrioment,
                               restTemplate: RestTemplate,
                               authService: AuthService)
    : EntitiesService<MotoristaContrato>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/AMFS_UDO_MTRT"
    }
}