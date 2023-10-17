package br.andrew.sap.model.entity

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class FieldMd(val name : String,
              val description: String?,
              tableName : String,
              val type : DbType = DbType.db_Alpha,
              val mandatory: YesNo = YesNo.tNO) {

    var size : Int? = type.size
    var ValidValuesMD : List<ValuesMd> = listOf()
    val subType : String? = if(type == DbType.db_Float) "st_Measurement" else null
    var defaultValue: String? = null
    var editSize : Int? = null

    var linkedUDO : String? = null
    var tableName = tableName.uppercase()

}

enum class DbType(val size : Int?){
    db_Alpha(254),
    db_Memo(null), //Observacao
    db_Numeric(null),
    db_Float(null),
    db_Date(null);
}

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class ValuesMd(val value : String, val description : String)