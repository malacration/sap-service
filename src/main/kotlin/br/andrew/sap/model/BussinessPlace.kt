package br.andrew.sap.model

import br.andrew.sap.model.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class BussinessPlace {

    @JsonProperty("BPLID")
    var BPLID: Int? = null
    @JsonProperty("BPLName")
    var BPLName: String? = null
    var DefaultCustomerID: String? = null
    var DefaultVendorID: String? = null
    var DefaultWarehouseID: String? = null
    var FederalTaxID : String? = null

    fun cnpjSemMascara() : String {
        return CpfCnpj(FederalTaxID ?: throw Exception("Erro, filial $BPLName sem cnpj")).value
    }
}
