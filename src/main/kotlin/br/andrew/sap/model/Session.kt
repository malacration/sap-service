package br.andrew.sap.model

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
class Session(val sessionId : String,
              val version : String,
              val sessionTimeout : Int,
              val date : Date = Date()) {

    override fun toString(): String {
        return sessionId
    }

    fun isExpire(): Boolean {
        return Date().after(Date(date.time.plus((sessionTimeout*1000)-30)))
    }

    fun expireDate(): Date {
        return Date(date.time.plus((sessionTimeout*1000)))
    }

}