package br.andrew.sap.services.abstracts

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

class SqlQueriesServiceTest {

    val rt = RestTemplate()

    @Test
    fun test(){
        val authService = AuthService("",rt)
        val envrioment = SapEnvrioment("","","","")
        val sq = SqlQueriesService(envrioment,rt ,authService)

        Assertions.assertEquals("windson='windson'",sq.getParameter(listOf(Parameter("windson","windson"))))

        Assertions.assertEquals(
            "windson='windson'&bruno='jose'",
            sq.getParameter(listOf(
                Parameter("windson","windson"),
                Parameter("bruno","jose"),
            )))

    }
}