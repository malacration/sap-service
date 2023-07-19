package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class Product(itemCode : String, quantity : String, unitPrice : String, usage : Int = 9)
    : DocumentLines(unitPrice, quantity, usage){

    init {
        this.ItemCode = itemCode
    }
    override fun Duplicate(): Product {
        return Product(ItemCode!!,Quantity,UnitPrice,Usage).also {
            it.TaxCode = TaxCode
            it.AccountCode = AccountCode
        }
    }
}