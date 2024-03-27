package br.andrew.sap.model.partner

enum class BusinessPartnerType(val cardType: String) {
    C("Cliente"),
    S("Fornecedor"),
    cCustomer("Cliente"),
    cSupplier("Fornecedor");


    fun getForSql(): String {
        return if(this == cCustomer || this == C)
            "C"
        else
            "S"
    }
}