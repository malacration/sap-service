package br.andrew.sap.model.documents

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Product(val ItemCode : String, val Quantity : String, var UnitPrice : String){

    var LineNum : Int? = null;
    var TaxCode : String? = null
    var discountPercent : Double = 0.0

}