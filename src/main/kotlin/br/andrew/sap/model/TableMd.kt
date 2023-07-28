package br.andrew.sap.model

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class TableMd(val tableName : String,
              val tableDescription: String,
              val tableType : TbType) {

    val archivable : YesNo = YesNo.tNO

}

enum class TbType {
    bott_MasterData,
    bott_NoObject,
    bott_Document,
    bott_DocumentLines,
    bott_MasterDataLines
}