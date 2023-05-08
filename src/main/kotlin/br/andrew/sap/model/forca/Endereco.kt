package br.andrew.sap.model.forca

import br.andrew.sap.model.partner.Address

class Endereco(val rua : String,
               val numero : String,
               val bairro : String,
               val cidade : String,
               val estado : String,
               val cep : String,
               val pais : String) {

    fun getAddresse(): Address {
        return Address().also {
            it.Street = rua
            it.StreetNo = numero
            it.Block = bairro
            it.City = cidade
            it.State = estado
            it.ZipCode = cep
            it.Country = pais
        }
    }
}