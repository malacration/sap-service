package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.Version
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.UserFieldsMDService
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class IndexController(val env: SapEnvrioment,
                      val restTemplate: RestTemplate,
                      val authService: AuthService,
                      val version : Version,
                      val salexTaxAuthoritiesService: SalesTaxAuthoritiesService,
                      val service: UserFieldsMDService) {

    @GetMapping("/")
    fun index() : Version{
        return version
    }

    @GetMapping("teste")
    fun teste() : Any {
        val predicates = listOf(Predicate("Name","Boleto",Condicao.STARTS_WITH))
        return  service.get(Filter(predicates))
    }
}