package br.andrew.sap.model.forca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class EnderecoId(private val value : String){

    init {
        val split = value.split("-")
        if(split.size != 3)
            throw IllegalArgumentException("EnderecoId deve ser no formato CODIGO-LETRA-TIPO")
    }
    val cliente : String = value.split("-")[0]
    val tipo : String = value.split("-")[1]
    val code : String = value.split("-")[2]


}