package br.andrew.sap.controllers


import br.andrew.sap.model.SalesTaxCode
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.Session
import br.andrew.sap.model.documents.Document
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class IndexController(val env: SapEnvrioment,
                      val restTemplate: RestTemplate,
                      val authService: AuthService,
                      val salexTaxAuthoritiesService: SalesTaxAuthoritiesService,
                      val salesTaxCodeService: SalesTaxCodeService) {

    @GetMapping("/")
    fun index() : String{
        return "up"
    }

    fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    @GetMapping("teste")
    fun teste() : SalesTaxCode {
        return salesTaxCodeService.getById("5101-006").tryGetValue();
    }

    //STACode IC17BI01
    //STAType = 25 -> Tipo imposto desonerado
    //EffectiveRate -> Taxa atual aplicada
    // Exemplo de imposto com desoneração -> val
    // url = "https://localhost:50000/b1s/v1/SalesTaxCodes('5101-006')"
    //val url = "https://localhost:50000/b1s/v1/SalesTaxAuthorities(Code='IC17BI01',Type=10)"

    //Definições de imposto SalesTaxAuthorities
    @GetMapping("get")
    fun getteste() : Any{
        return salexTaxAuthoritiesService.get("IC17BI01",25)
    }


    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Teste(val Document : Document){

    }
}