package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.sap.BussinessPlace


class ContaUzziPayPix {
    lateinit var cnpj : String
    var idFilial : Int? = null
    var tokenJwt : String? = null
    lateinit var privateKey : String
    lateinit var chavePix : String
    lateinit var consulta : String
    lateinit var contaContabilBanco : String
    var transitoria : String? = null
    var idFilialTransitoria : String? = null
    var bussinessPlace : BussinessPlace? = null

    val contaForPayment: String
        get() = getAccountForPayment()

    fun getAccountForPayment() : String{
        return transitoria?.takeIf { it.isNotBlank() } ?: contaContabilBanco
    }

    fun hasTransitoryAccount(): Boolean {
        if (transitoria.isNullOrBlank()) {
            return false
        }
        if (idFilialTransitoria.isNullOrBlank()) {
            throw Exception("Conta transitoria configurada sem idFilialTransitoria")
        }
        return true
    }
}
