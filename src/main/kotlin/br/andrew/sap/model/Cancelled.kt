package br.andrew.sap.model

enum class Cancelled {
    tNO, tYES;

    companion object {
        val column : String = "Cancelled"
    }
}