package br.andrew.sap.model

import br.andrew.sap.model.price.ItemPrice
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Item(val itemCode : String) {

    var itemPrices : List<ItemPrice> = emptyList()

}

