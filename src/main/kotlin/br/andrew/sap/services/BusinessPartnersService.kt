package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.infrastructure.toInt
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.partner.*
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.*

@Service
class BusinessPartnersService(
    val sqlQueriesService : SqlQueriesService,
    env: SapEnvrioment,
    restTemplate: RestTemplate,
     authService: AuthService) :
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
            it.bpBranchAssignment = listOf(BPBranchAssignment().also {
                it.BPCode = cardCode
                it.bplid = idBusinesPlace
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

    fun atualizaDataSerasa(date : Date,bp: BusinessPartner): OData? {
        val strDate = SimpleDateFormat("yyyy-MM-dd").format(date)
        return update("{" +
                " \"U_dataSerasa\" : \"$strDate\" "+
                "}","'${bp.cardCode}'")
    }

    fun getByCpfCnpj(cpfCnpj: String, type : BusinessPartnerType): BusinessPartner {
        val url = env.host+"/b1s/v1/"
        var uri = "${url}SQLQueries('parceiro.sql')/List?valor='${CpfCnpj(cpfCnpj).getWithMask()}'&type='${type.getForSql()}'"
        val request = RequestEntity
            .get(uri)
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        val odata = restT.exchange(request, OData::class.java)
            .body?.tryGetValues<BusinessPartner>() ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")
        return getById(
            "'${odata.firstOrNull()?.cardCode ?: throw Exception("O ${CpfCnpj(cpfCnpj).getWithMask()} não foi encontrado")}'"
        )
            .tryGetValue<BusinessPartner>()
    }

    //TODO melhorar depois
    fun fullSearchText(fullText: String, auth: Authentication): NextLink<BusinessPartnerSlin> {
        val request = RequestEntity
            .get(env.host+"/b1s/v1/sml.svc/CLIENTE_VENDEDORParameters(searchText='$fullText',vendedor=${auth.principal})/CLIENTE_VENDEDOR")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        return restT.exchange(request, OData::class.java).body!!.tryGetNextValues()
    }

    fun fullSearchTextFallBack(fullText: String, user: User): NextLink<BusinessPartnerSlin> {
        if(fullText.startsWith("SQLQueries('parceiro-full-search-text.sql')"))
            return sqlQueriesService.nextLink(fullText)!!.tryGetNextValues()
        val busca = if(fullText.toDoubleOrNull() == null )  fullText.replace("*","%") else CpfCnpj(fullText).getWithMask();
        val parametros = listOf(
            Parameter("superVendedor",user.superVendedor()),
            Parameter("valor","'%${busca}%'"),
            Parameter("vendedor",user.principal))
        return sqlQueriesService
            .execute("parceiro-full-search-text.sql", parametros)!!
            .tryGetNextValues()
    }

    fun searchLocalidade(): List<BusinessPartner> {
        return sqlQueriesService.execute("search-localidade.sql")!!.tryGetValues()
    }

    fun normalizeAddressName(bp: BusinessPartner) {
        bp.getAddresses().forEach {
            it.addressName = it.normalize()
        }
        val novo = BusinessPartner().also {
            it.setAddresses(bp.getAddresses())
        }
        update(novo,"'${bp.cardCode}'")

    }

    fun findAllBySalePerson(idSalesPerson: Int, page: Pageable) : Page<BusinessPartner> {
        val filter = Filter((mutableListOf(Predicate("SalesPersonCode",idSalesPerson, Condicao.EQUAL))))
        return  get(filter).tryGetPageValues<BusinessPartner>(page)
    }

    fun modificarVendedor(businessPartners: BusinessPartner, idSalesPerson: Int) : BusinessPartner {
        return businessPartners.also {
            val json =
                "{ \n" +
                        "                    \"SalesPersonCode\" : \"${idSalesPerson}\"\n" +
                        "                }"
            this.update(json, "'${it.cardCode}'")
        }
    }

    fun findBusinessPartnersBySalesPersonCode(salesPersonCode: Int, page : Pageable): Page<BusinessPartner>? {
        val filter = Filter(
            Predicate("SalesPersonCode", salesPersonCode,Condicao.EQUAL )
        )
        val result = get(filter,page)
        return result.tryGetPageValues<BusinessPartner>(page)
    }
}