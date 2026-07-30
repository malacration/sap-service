package br.andrew.sap.model.sap.sistema
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class Session(val sessionId : String,
              val version : String,
              val sessionTimeout : Int,
              val date : Date = Date()) {

    //cookies extras de roteamento (ex.: ROUTEID) devolvidos no login por um
    //load balancer com sticky session na frente do Service Layer. Sem
    //reenviar isso, cada request pode cair num node que nao conhece o
    //B1SESSION e o Service Layer derruba a conexao (502/no response).
    var routeCookie : String? = null

    fun cookieHeader() : String {
        return if(routeCookie.isNullOrBlank()) "B1SESSION=$sessionId" else "B1SESSION=$sessionId; $routeCookie"
    }

    override fun toString(): String {
        return sessionId
    }

    fun isExpire(): Boolean {
        val plusDate = sessionTimeout*60*1000-(30*1000)
        return Date().after(Date(date.time.plus(plusDate)))
    }

}