package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnore

class ContactOpaque(entrada : String) {

    val contato : String =
        if(entrada.split("@").size > 1) {
            opaqueEmail(entrada)
        }
        else
            opaqueTelefone(entrada)

    private val trueValue = entrada

    fun opaqueEmail(entrada: String): String {
        return "^(.)(.*)(.{3})@(.+)$".toRegex().replace(entrada) {
            val firstChar = it.groupValues[1]
            val obscuredPart = it.groupValues[2].replace(".".toRegex(), "*")
            val lastThreeChars = it.groupValues[3]
            val domain = it.groupValues[4]
            "$firstChar$obscuredPart$lastThreeChars@$domain"
        }
    }

    fun opaqueTelefone(entrada: String): String {
        val digitos = entrada.replace("\\D+".toRegex(),"")
        val matchResult = "^(\\d{3})(\\d+)(\\d{3})\$".toRegex().find(digitos)
        return matchResult?.let {
            val firstThreeDigits = it.groupValues[1]
            val middleDigits = it.groupValues[2].replace("\\d".toRegex(), "*")
            val lastThreeDigits = it.groupValues[3]
            "$firstThreeDigits$middleDigits$lastThreeDigits"
        } ?: ""
    }

    @JsonIgnore
    fun getTrueValue(): String {
        return trueValue
    }

}
