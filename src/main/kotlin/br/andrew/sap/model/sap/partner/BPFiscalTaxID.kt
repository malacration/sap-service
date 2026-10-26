package br.andrew.sap.model.sap.partner

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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
    //A UpperCamelCase do JsonNaming transforma o getter getBPCode em "Bpcode": no POST o Service
    //Layer recusava ("Property 'Bpcode' of 'BPFiscalTaxID' is invalid") e, na leitura, o BPCode
    //vindo do SAP nunca era preenchido. Nulo fica fora do JSON - no cadastro novo ainda nao ha CardCode
    @get:JsonProperty("BPCode") @set:JsonProperty("BPCode")
    var BPCode : String? = null

    override fun toString(): String {
        return "$TaxId4 $TaxId0"
    }
}
