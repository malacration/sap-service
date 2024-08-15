package br.andrew.sap.model.forca

import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.CpfCnpj
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Cliente(val nome : String, val cpfCnpj : String){

    var endereco : Endereco? = null
    var idCliente : String? = null
    var telefone : String? = null
    val email : String? = null
    val idVendedor : String? = null
    val obscadastral : String? = null
    var ierg : String? = null
    var dtnasc : String? = null
    var localidade : Int? = null


    @JsonIgnore
    @JsonIgnoreProperties
    fun getBusinessPartner() : BusinessPartner {
        return BusinessPartner(nome, BusinessPartnerType.C).also {
            it.setCpfCnpj(CpfCnpj(cpfCnpj),ierg)
            if(endereco != null)
                it.setAddresse(endereco!!.getAddresse(localidade))
            it.u_id_forca = idCliente
            it.phone1 = telefone
            it.emailAddress = email
            it.salesPersonCode = idVendedor?.toInt() ?: -1
            it.freeText = obscadastral
            it.U_fazer_fluxo_prazo = "1"
            it.U_Rov_Data_Nascimento = if(dtnasc?.trim().isNullOrEmpty()) null else dtnasc
        }
    }


    @JsonIgnore
    @JsonIgnoreProperties
    fun getUpdateBusinessPartner(cardCode : String) : BusinessPartner {
        return BusinessPartner(nome, BusinessPartnerType.C).also {
            it.cardCode = cardCode
            it.series = null
            it.U_fazer_fluxo_prazo = null
            it.cardType = null
            it.u_id_forca = idCliente
            it.phone1 = telefone
            it.emailAddress = email
            it.freeText = obscadastral
            it.U_Rov_Data_Nascimento = if(dtnasc?.trim().isNullOrEmpty()) null else dtnasc
        }
    }

    fun getAddresForUpdate(): Address? {
        return endereco?.getAddresse(localidade)
    }
}