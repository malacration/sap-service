package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.SapEnvrioment
import br.andrew.sap.model.Session
import br.andrew.sap.model.documents.Document
import br.andrew.sap.model.documents.OrderSales
import br.andrew.sap.model.documents.Product
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.AuthService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.RequestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import java.util.*


@RestController
class IndexController(val env: SapEnvrioment,
                      val restTemplate: RestTemplate,
                      val authService: AuthService) {

    @GetMapping("/")
    fun index() : String{
        return "up"
    }

    fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    @GetMapping("teste")
    fun teste() : Any{
        val url = "https://localhost:50000/b1s/v1/InvoicesService_GetApprovalTemplates"
        val products = listOf(Product("PAC0000118","1.0","4.0"))
        val documento = OrderSales(
                "CLI0000863",
                Date(),
                products,
                "2",
                "9")
        val request = RequestEntity
                .post(url)
                .header("cookie","B1SESSION=${session().sessionId}")
                .body(Teste(documento))
        return restTemplate.exchange(request, OData::class.java).body!!
    }

    @GetMapping("get")
    fun getteste() : Any{
        val url = "https://localhost:50000/b1s/v1/SalesTaxInvoices"
        val request = RequestEntity
                .get(url)
                .header("cookie","B1SESSION=${session().sessionId}")
                .build()
        return restTemplate.exchange(request, OData::class.java).body!!
    }


    @JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Teste(val Document : Document){

    }
}