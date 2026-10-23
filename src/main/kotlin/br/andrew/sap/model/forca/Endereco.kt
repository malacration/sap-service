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
    //AbsId do municipio (OCNT), que e o que o SAP guarda em County. A integracao do forca de
    //vendas so manda o nome da cidade e continua caindo nele
    var codigoMunicipio: String? = null

    fun getAddresse(localidade: Int?): Address {
        return Address().also {
            it.Street = rua
            it.StreetNo = numero
            it.Block = bairro
            it.City = cidade
            it.County = codigoMunicipio?.takeIf { codigo -> codigo.isNotBlank() } ?: cidade
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