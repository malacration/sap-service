package br.andrew.sap.services.cadastro
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.cadastro.BussinessPlace
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.cadastro.Branch
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class BussinessPlaceService(
    val sqlQuerysServices : SqlQueriesService,
    env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<BussinessPlace>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/BusinessPlaces"
    }

    open fun getAllBusinessPlaces(): List<BussinessPlace> {
        return getAll(BussinessPlace::class.java)
    }

    fun getFilialBySalesPerson(idVendedor : Int): List<Branch> {
        return sqlQuerysServices.execute("filiais-vendedor.sql",Parameter("vendedor",idVendedor))!!.tryGetValues<Branch>()
    }

    fun getFilialByEmployee(idColaborador : Int): List<Branch> {
        return sqlQuerysServices.execute("filiais-colaborador.sql",Parameter("colaborador",idColaborador))!!.tryGetValues<Branch>()
    }
}
