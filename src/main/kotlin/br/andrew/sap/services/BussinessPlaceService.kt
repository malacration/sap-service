package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Branch
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BussinessPlaceService(
    val sqlQuerysServices : SqlQueriesService,
    env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<BussinessPlace>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/BusinessPlaces"
    }

    fun getFilialBy(idVendedor : Int): List<Branch> {
        return sqlQuerysServices.execute("filiais-vendedor.sql",Parameter("vendedor",idVendedor))!!.tryGetValues<Branch>()
    }
}