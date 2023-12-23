package br.andrew.sap.model.serasa.retorno

import java.util.*

class Protestos(val entrada : String) : RetornoSerasa(){

    val subTipo : String = entrada.substring(4, 6)
    val dataInicial : Date = getDate(entrada.substring(6, 6+8))
    val dataFinal : Date = getDate(entrada.substring(14, 14+8))
    val total : String = entrada.substring(24, 33)
    val valor : Double = getDouble(entrada.substring(33, 46))
    val origem : String = entrada.substring(46, 76)

    init {
        if(subTipo != "00")
            throw Exception("Subtipo nao implementado para Protestos")
    }

}