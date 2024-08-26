package br.andrew.sap.services.address

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.sap.address.State
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.romaneio.TipoAnalise
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class StateService(env : SapEnvrioment,
                   restTemplate: RestTemplate,
                   authService: AuthService
) : EntitiesService<State>(env,restTemplate, authService)  {


    override fun path(): String {
        return "/b1s/v1/States"
    }

    @Cacheable("states")
    fun getAllCached(filter: Filter = Filter()): List<State>{
        return super.getAll(filter)
    }

}