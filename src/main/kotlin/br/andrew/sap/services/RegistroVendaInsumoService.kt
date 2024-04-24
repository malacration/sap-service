package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.romaneio.RegistroInsumo
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class RegistroVendaInsumoService(
    env : SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService) : EntitiesService<RegistroInsumo>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/AGRI_UDO_CTVD"
    }

    fun getByPn(codPn : String): OData {
        return get(Filter(
                mutableListOf(
                        Predicate("U_CodParceiroNegocio",codPn,Condicao.EQUAL)
                )))
    }

    fun getByItemAndPn(itemCode : String, cardCode : String): OData {
        return get(Filter(
            mutableListOf(
                Predicate("U_CodParceiroNegocio",cardCode,Condicao.EQUAL),
                Predicate("U_CodItem",itemCode,Condicao.EQUAL)
            )))
    }
}