package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LineTaxJurisdiction {
    var JurisdictionType : Int? = null
    var JurisdictionCode : String? = null
    var TaxAmount : Double? = null
    var TaxRate : Double? = null
}
