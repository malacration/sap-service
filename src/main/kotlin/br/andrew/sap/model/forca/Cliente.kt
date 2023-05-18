package br.andrew.sap.model.forca

import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.BusinessPartnerType
import br.andrew.sap.model.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Cliente(
        val nome : String,
        val cpfCnpj : String,
        val endereco : Endereco,
        val idCliente : String){

    var telefone : String? = null
    val email : String? = null
    val idVendedor : String? = null
    val obscadastral : String? = null
    var ierg : String? = null

    @JsonIgnore
    @JsonIgnoreProperties
    fun getBusinessPartner() : BusinessPartner {
        return BusinessPartner(nome, BusinessPartnerType.C).also {
            it.setCpfCnpj(CpfCnpj(cpfCnpj),ierg)
            it.setAddresse(endereco.getAddresse())
            it.u_id_forca = idCliente
            it.phone1 = telefone
            it.emailAddress = email
            it.salesPersonCode = idVendedor?.toInt() ?: -1
            it.freeText = obscadastral;
            it.u_fazer_fluxo_prazo = "1"
        }
    }
}