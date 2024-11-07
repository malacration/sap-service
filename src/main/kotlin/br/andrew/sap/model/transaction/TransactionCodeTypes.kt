package br.andrew.sap.model.transaction

enum class TransactionCodeTypes(val description : String) {
    VFET("Entrega Venda futura"),
    CanC("Cancelled"),
    VFEC("Ent. VF Conciliado");

    fun get() : TransactionCode{
        return TransactionCode(this.toString(),this.description)
    }
}