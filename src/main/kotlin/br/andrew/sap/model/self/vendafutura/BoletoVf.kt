package br.andrew.sap.model.self.vendafutura

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BoletoVf {
    var DocNum : String? = ""
    var DocDueDate : String? = ""
    var DocTotal : String? = ""
    var devolucao : String? = ""
    var DocStatus : String? = ""
}