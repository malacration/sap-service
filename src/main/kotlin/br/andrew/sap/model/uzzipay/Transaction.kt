package br.andrew.sap.model.uzzipay

class Transaction(val txId : String) {
    var paid : Boolean = false
    var receivedAmount : Double? = null
    var originalAmount : Double? = null
    var paymentDate : String? = null
    var paymentType : String? = null
}