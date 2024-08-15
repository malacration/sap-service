package br.andrew.sap.model.forca

import br.andrew.sap.model.sap.partner.Address
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Endereco(val rua : String,
               val numero : String,
               val bairro : String,
               val cidade : String,
               val estado : String,
               val cep : String,
               val pais : String,
               val tipoEndereco : String) {

    var complemento: String? = null
    var localidade: Int? = null

    fun getAddresse(localidade: Int?): Address {
        return Address().also {
            it.Street = rua
            it.StreetNo = numero
            it.Block = bairro
            it.City = cidade
            it.County = cidade
            it.State = estado
            it.ZipCode = cep
            it.Country = pais
            it.BuildingFloorRoom = complemento
//            it.addrType = tipoEndereco
            it.typeOfAddress = tipoEndereco
            it.U_Localidade = localidade
        }
    }
}