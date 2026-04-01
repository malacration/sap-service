package br.andrew.sap.model.uzzipay

enum class Type(val type: String) {
    TaxId("TaxId"),
    Email("Email"),
    PhoneNumber("PhoneNumber"),
    EVP("EVP")
}