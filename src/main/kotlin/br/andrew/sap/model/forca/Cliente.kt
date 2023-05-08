package br.andrew.sap.model.forca

import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.BusinessPartnerType
import br.andrew.sap.model.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

class Cliente(
        val nome : String,
        val cpfCnpj : String,
        val endereco : Endereco,
        val idCliente : String){

    var telefone : String? = null
    val email : String? = null

    @JsonIgnore
    @JsonIgnoreProperties
    fun getBusinessPartner() : BusinessPartner {
        return BusinessPartner(nome, BusinessPartnerType.C).also {
            it.setCpfCnpj(CpfCnpj(cpfCnpj))
            it.setAddresse(endereco.getAddresse())
        }
    }
}