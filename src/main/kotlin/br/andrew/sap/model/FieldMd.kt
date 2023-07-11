package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class FieldMd(val name : String,
              val description: String,
              val tableName : String,
              val type : DbType = DbType.db_Alpha,
              val mandatory: String = "tNO") {

    var size : Int? = 15
    var ValidValuesMD : List<ValuesMd> = listOf()
    val subType : String? = if(type == DbType.db_Float) "st_Measurement" else null
    var defaultValue: String? = null
    var editSize : Int? = null

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
class ValuesMd(val value : String, val description : String)