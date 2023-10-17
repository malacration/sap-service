package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.partner.Person
import br.andrew.sap.model.partner.ReferenciaComercial
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ReferenciaComercialService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<ReferenciaComercial>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/refComercial"
    }

    fun getByCardCode(cardCode: String): ReferenciaComercial? {
        val filter = Filter("U_cardCode",cardCode,Condicao.EQUAL)
        return get(filter).tryGetValues<ReferenciaComercial>().firstOrNull()
    }
}