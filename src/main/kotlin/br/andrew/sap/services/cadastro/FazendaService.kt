package br.andrew.sap.services.cadastro
import br.andrew.sap.model.cadastro.Fazenda
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class FazendaService(restTemplate: RestTemplate,
                     env: SapEnvrioment,
                     authService : AuthService) : EntitiesService<Fazenda>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AGRI_UDO_UNPF"
    }

}