package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.PaymentMethod
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class BusinessPartnersService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<BusinessPartner>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/BusinessPartners"
    }

    fun addPaymentMethod(cardCode : String, idFormaPagamento: String): OData? {
        val bp : BusinessPartner = BusinessPartner().also { it.cardCode = cardCode }
                .also { it.setBPPaymentMethods(listOf(PaymentMethod(idFormaPagamento)))  }
        return update(bp,"'${bp.cardCode}'")
    }

    fun addBusinesPlace(cardCode : String, idBusinesPlace: String): OData? {
        val bp : BusinessPartner = BusinessPartner().also {
            it.cardCode = cardCode
            it.BPLID = idBusinesPlace
            it.DisabledForBP = "tNO"
        }
        return update(bp,"'${bp.cardCode}'")
    }
}