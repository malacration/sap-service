package br.andrew.sap.model.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BPFiscalTaxID() {

    constructor(cpfCnpj: CpfCnpj) : this(){
        if (cpfCnpj.isCpf()) {
            TaxId4 = cpfCnpj.getWithMask()
        } else {
            TaxId0 = cpfCnpj.getWithMask()
        }
    }
    var TaxId0 : String? = null
    var TaxId4 : String? = null
}