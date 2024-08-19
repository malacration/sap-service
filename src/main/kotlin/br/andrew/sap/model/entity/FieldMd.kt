package br.andrew.sap.model.entity

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class FieldMd(val name : String,
              val description: String?,
              tableName : String,
              val type : DbType? = DbType.db_Alpha,
              val mandatory: YesNo = YesNo.tNO) {

    var size : Int? = type?.size
    var ValidValuesMD : List<ValuesMd> = listOf()
    var subType : String? = if(type == DbType.db_Float) "st_Measurement" else null
    var defaultValue: String? = null
    var editSize : Int? = null
    @JsonProperty("FieldID")
    val FieldID : String? = null
    var linkedUDO : String? = null
    var LinkedSystemObject : LinkedSystemObject? = null
    var LinkedTable : String? = null
    var tableName = tableName.uppercase()

}

enum class DbType(val size : Int?){
    db_Alpha(254),
    db_Memo(null), //Observacao
    db_Numeric(null),
    db_Float(null),
    db_Date(null);
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class ValuesMd(val value : String, val description : String)