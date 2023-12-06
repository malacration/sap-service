package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.ParcelasAberto
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.partner.BPBranchAssignment
import br.andrew.sap.model.partner.BusinessPartnerType
import br.andrew.sap.model.partner.CpfCnpj
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate

@Service
class BusinessPartnersService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<BusinessPartner>(env, restTemplate, authService) {
    override fun path(): String {
        return "/b1s/v1/BusinessPartners"
    }

    fun addPaymentMethod(cardCode : String, idFormaPagamento: String): OData? {
        val bp : BusinessPartner = BusinessPartner().also {
            it.setBPPaymentMethods(listOf(PaymentMethod(idFormaPagamento)))
        }
        return update(bp,"'${cardCode}'")
    }

    fun addBusinesPlace(cardCode : String, idBusinesPlace: String): OData? {
        val bp : BusinessPartner = BusinessPartner().also {
            it.BPBranchAssignment = listOf(BPBranchAssignment().also {
                it.BPCode = cardCode
                it.BPLID = idBusinesPlace
                it.DisabledForBP = Cancelled.tNO
            })
        }
        return update(bp,"'${bp.cardCode}'")
    }

    fun atualizaKey(key: String, bp: BusinessPartner): OData? {
        return update("{" +
                " \"U_keyUpdate\" : \"$key\", " +
                " \"U_fazer_fluxo_prazo\" : 0 "+
                "}","'${bp.cardCode}'")
    }

    fun getByCpfCnpj(cpfCnpj: String, type : BusinessPartnerType): BusinessPartner {
        val url = env.host+"/b1s/v1/"
        var uri = "${url}SQLQueries('parceiro.sql')/List?valor='${CpfCnpj(cpfCnpj).getWithMask()}'&type='${type.getForSql()}'"
        val request = RequestEntity
            .get(uri)
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        val odata = restTemplate.exchange(request, OData::class.java)
            .body?.tryGetValues<BusinessPartner>() ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")
        return getById(
            "'${odata.firstOrNull()?.cardCode ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")}'"
        )
            .tryGetValue<BusinessPartner>()
    }
}