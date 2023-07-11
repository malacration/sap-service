package br.andrew.sap.model.uzzipay

class Payer(
    val document : String,
    val name : String,
    val email : String,
    val address : String,
    val city : String,
    val state : String,
    val zipCode : String
){
    init {
        if(document.isBlank())
            throw Exception("Documento do cliente nao pode ser vazio")
    }
}