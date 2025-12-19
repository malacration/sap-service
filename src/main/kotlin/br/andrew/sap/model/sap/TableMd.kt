package br.andrew.sap.model.sap

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class TableMd(tableName : String,
              val tableDescription: String,
              val tableType : TbType
) {
    init {
        if(tableName.length > 19)
            throw Exception("Nome da tabela é muito grande")
    }

    val archivable : YesNo = YesNo.tNO
    val tableName = tableName.uppercase()
}

enum class TbType {
    bott_MasterData,
    bott_NoObject,
    bott_Document,
    bott_DocumentLines,
    bott_MasterDataLines
}