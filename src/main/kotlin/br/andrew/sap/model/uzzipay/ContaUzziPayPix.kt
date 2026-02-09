package br.andrew.sap.model.uzzipay



class ContaUzziPayPix {
    lateinit var cnpj : String
    lateinit var tokenJwt : String
    lateinit var privateKey : String
    lateinit var chavePix : String
    lateinit var consulta : String
    lateinit var contaBanco : String
    var transitoria : String? = null
    var idFilialTransitoria : String? = null

    fun getAccountForPayment() : String{
        return transitoria ?: contaBanco
    }
}

