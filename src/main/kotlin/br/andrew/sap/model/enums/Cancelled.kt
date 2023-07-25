package br.andrew.sap.model.enums

enum class Cancelled {
    tNO, tYES;

    companion object {
        val column : String = "Cancelled"
    }
}