package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
class SalesTaxCode(val code : String, val salesTaxCodes_Lines : List<SalesTaxCodeLine>)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)

class SalesTaxCodeLine(val STCCode : String, val STACode: String, val STAType : Int)