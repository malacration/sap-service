package br.andrew.sap.model.estoque

import br.andrew.sap.model.sap.price.ItemPrice
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Item(val itemCode : String) {

    var itemPrices : List<ItemPrice> = emptyList()
    var ItemWarehouseInfoCollection : List<ItemWarehouseInfo>? = null

}
