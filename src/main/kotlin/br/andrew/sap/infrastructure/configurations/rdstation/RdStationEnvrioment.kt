package br.andrew.sap.infrastructure.configurations.rdstation

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class RdStationEnvrioment(
    @Value("\${rd.host:https://api.rd.services}") val host : String,
    @Value("\${rd.client_id:windson}") val client : String,
    @Value("\${rd.client_secret:windsonSecret}") val secret : String,
    @Value("\${rd.refresh_token:windson}") var refreshToken : String,
    val restTemplate: RestTemplate
){

    private var date = Date()
    private var sessionTimeoutSecondes = 0
    private var token : String? = null

    fun isExpire(): Boolean {
        return Date().after(Date(date.time.plus((sessionTimeoutSecondes*1000)-30)))
    }

    fun refreshToken(): ResponseEntity<TokenRd> {
        val body = LoginRdStation(client,secret,refreshToken)
        val request = RequestEntity
            .post("$host/auth/token")
            .body(body)
        return restTemplate
            .exchange(request,TokenRd::class.java)

    }

    fun getToken() : String {
        if(isExpire() || token != null){
            val tokenRd = refreshToken().body?: throw Exception("Nao foi possivel pegar o token no RdStation")
            sessionTimeoutSecondes = tokenRd.expires_in
            this.token = tokenRd.access_token
        }
        return token!!
    }
}

class TokenRd(val access_token : String, val expires_in : Int, val refresh_token : String){

}

class LoginRdStation(val client_id : String, val client_secret : String, val refresh_token : String){

}