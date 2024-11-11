package br.andrew.sap.model.sap

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class Session(val sessionId : String,
              val version : String,
              val sessionTimeout : Int,
              val date : Date = Date()) {

    override fun toString(): String {
        return sessionId
    }

    fun isExpire(): Boolean {
        val plusDate = sessionTimeout*60*1000-(30*1000)
        return Date().after(Date(date.time.plus(plusDate)))
    }

}