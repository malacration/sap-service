package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.MotoristaContrato
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Localidade
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.model.sap.partner.CpfCnpj
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class LocalidadeService(val sqlQueriesService : SqlQueriesService, env : SapEnvrioment,
                        restTemplate: RestTemplate,
                        authService: AuthService)
    : EntitiesService<Localidade>(env, restTemplate,authService) {
    override fun path(): String {
        return "/b1s/v1/Locais"
    }

    fun fullSearchTextFallBack(fullText: String, user: User): NextLink<Localidade> {
        if(fullText.startsWith("SQLQueries('localidade-search.sql')"))
            return sqlQueriesService.nextLink(fullText)!!.tryGetNextValues()
        val busca = if(fullText.toDoubleOrNull() == null )  fullText.replace("*","%") else CpfCnpj(fullText).getWithMask();
        val parametros = listOf(
            Parameter("valor","'%${busca}%'"),
        )
        return sqlQueriesService
            .execute("localidade-search.sql", parametros)!!
            .tryGetNextValues()
    }
}