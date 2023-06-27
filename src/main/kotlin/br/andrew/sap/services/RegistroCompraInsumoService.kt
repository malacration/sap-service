package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.RegistroCompraInsumo
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RegistroCompraInsumoService(
        env : SapEnvrioment,
        restTemplate: RestTemplate,
        authService: AuthService) : EntitiesService<RegistroCompraInsumo>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/PECU_UDO_RCIS"
    }

    fun getByPn(codPn : String): OData {
        return get(Filter(
                listOf(
                        Predicate("U_CodParceiroNegocio",codPn,Condicao.EQUAL)
                )));
    }
}