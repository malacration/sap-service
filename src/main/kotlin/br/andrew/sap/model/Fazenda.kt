package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Fazenda(val Code : String?, val U_DescriComp : String?) {

    var U_CPFCNPJ : String? = null


    override fun toString(): String {
        return U_DescriComp?: ""
    }
}