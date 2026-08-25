package br.andrew.sap.model.sap.price

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class PriceList(val priceListNo : Int , val priceListName : String = ""){
    //UDF alfanumerico (db_Alpha, linkedUDO "comissao" - ver ComissaoConfiguration):
    //guarda o Code da comissao como texto, que pode nao ser numerico (ex.: "2,5")
    var U_tipoComissao : String? = null
}