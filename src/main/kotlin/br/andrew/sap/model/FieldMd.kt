package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class FieldMd(val name : String,
              val description: String,
              val tableName : String,
              val type : String = "db_Alpha", val size : Int = 2,
              val defaultValue: String = "0",
              val editSize : Int = 2,
              val mandatory: String = "tNO") {

    var ValidValuesMD : List<ValuesMd> = listOf()


}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class ValuesMd(val value : String, val description : String){

}