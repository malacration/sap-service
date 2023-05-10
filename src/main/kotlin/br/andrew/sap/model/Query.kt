package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Query(val sqlCode: String, val sqlName: String, sqlText: String){

    val sqlText = sqlText.replace("\t"," ").replace("\n", " ").replace("\r", " ")

    init{
        val pattern = ":(\\w+)".toRegex()
        pattern.findAll(sqlText).forEach { matchResult ->
            val value = matchResult.groupValues[1]
        }
    }

}