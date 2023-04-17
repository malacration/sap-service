package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class FieldMd(val name : String,
              val description: String,
              val tableName : String,
              val type : DbType = DbType.db_Alpha,
              val size : Int = 15,
              val defaultValue: String = "0",
              val editSize : Int = 2,
              val mandatory: String = "tNO") {

    var ValidValuesMD : List<ValuesMd> = listOf()
    val subType : String? = if(type == DbType.db_Float) "st_Measurement" else null

}

enum class DbType {
    db_Alpha,
    db_Memo, //Observacao
    db_Numeric,
    db_Float,
    db_Date;
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class ValuesMd(val value : String, val description : String){

}