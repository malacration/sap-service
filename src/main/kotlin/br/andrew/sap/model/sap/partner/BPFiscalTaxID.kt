package br.andrew.sap.model.sap.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BPFiscalTaxID() {

    constructor(cpfCnpj: CpfCnpj, ierg: String? = null) : this(){
        if (cpfCnpj.isCpf()) {
            TaxId4 = cpfCnpj.getWithMask()
        } else {
            TaxId0 = cpfCnpj.getWithMask()
            TaxId1 = ierg
        }
    }
    var TaxId1 : String? = null
    var TaxId0 : String? = null
    var TaxId4 : String? = null

    override fun toString(): String {
        return "$TaxId4 $TaxId0"
    }
}