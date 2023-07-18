package br.andrew.sap.model.bankplus


class Boleto {
    var id : Int? = null
    var nossoNumero : String? = null
    var notaFiscal : Int? = null
    var numeroDaParcela : Int? = null
    var tipoDeDocumento : String? = null
    var codigoDoParceiro : String? = null
    var nomeDoParceiro : String? = null
    var valorDoBoleto : Double? = null
    var dataDoVencimento : String? = null
    var valorDoDesconto : Double? = null
    var valorDeAbatimento : Double? = null
    var valorDeJuros : Double? = null
    var valorDeMulta : Double? = null
    var linhaDigitavel : String? = null
    var statusId : Int? = null
    var situacaoId : Int? = null
    var linkUrl : String? = null
    var statusDescricao : String? = null
    var tipoCobranca : String? = null
}