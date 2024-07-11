package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.CurrencyRate
import br.andrew.sap.model.Session
import br.andrew.sap.model.envrioments.SapEnvrioment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.text.SimpleDateFormat
import java.util.Date

@Service
class CompanyService(val restTemplate : RestTemplate,
                     val authService: AuthService,
                     val env: SapEnvrioment) {

    fun session() : Session {
        return authService.getToken(env.getLogin())
    }

    fun getCurrencyRate(): String? {
        val getPath = "/b1s/v1/SBOBobService_GetCurrencyRate"
        val entry = CurrencyRate("USD").also { it.Date = SimpleDateFormat("yyyy-MM-dd").format(Date()) }
        val request = RequestEntity
            .post(env.host+getPath)
            .header("cookie","B1SESSION=${session().sessionId}")
            .body(entry)
        return restTemplate.exchange(request, String::class.java).body
    }

    fun setCurrencyRate(currencyRates: List<CurrencyRate>){
        currencyRates.forEach{setCurrencyRate(it)}
    }

    fun setCurrencyRate(currencyRate: CurrencyRate): String? {
        val getPath = "/b1s/v1/SBOBobService_SetCurrencyRate"
        val request = RequestEntity
            .post(env.host+getPath)
            .header("cookie","B1SESSION=${session().sessionId}")
            .body(currencyRate)
        return restTemplate.exchange(request, String::class.java).body
    }
}