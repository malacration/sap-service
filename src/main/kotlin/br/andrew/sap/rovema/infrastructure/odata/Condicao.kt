package br.andrew.sap.rovema.infrastructure.odata

enum class Condicao(val value: String) {
    GREAT_EQUAL("ge"),
    GREAT("gt"),
    EQUAL("eq"),
    STARTS_WITH("startswith");


    fun get(coluna : String, value : String) : String{
        return when(this) {
            STARTS_WITH -> "startswith(${coluna}, ${value})"
            else -> "Invalid month"
        }
    }

}