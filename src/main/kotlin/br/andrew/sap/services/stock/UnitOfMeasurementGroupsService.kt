package br.andrew.sap.services.stock

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.stock.UnitOfMeasurementGroups
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class UnitOfMeasurementGroupsService(env: SapEnvrioment,
                                     restTemplate: RestTemplate,
                                     authService: AuthService) :
    EntitiesService<CreditNotes>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/UnitOfMeasurementGroups"
    }

    @Cacheable("unidade-grupo")
    fun getByIdCacheable(id : Int): UnitOfMeasurementGroups {
        return getById(id).tryGetValue<UnitOfMeasurementGroups>()
    }
}