package br.andrew.sap.infrastructure.odata

enum class Condicao(val value: String) {
    GREAT_EQUAL("ge"),
    GREAT("gt"),
    EQUAL("eq"),
    NOT_EQUAL("ne"),
    LESS("lt"),
    LESS_EQUAL("le"),
    STARTS_WITH("startswith"),
    CONTAINS("contains"),
    IN("in");


    fun get(coluna : String, value : String) : String{
        return when(this) {
            STARTS_WITH -> "startswith(${coluna}, ${value})"
            EQUAL -> "${coluna} eq ${value}"
            NOT_EQUAL -> "${coluna} ne ${value}"
            CONTAINS -> "contains(${coluna}, ${value})"
            GREAT -> "${coluna} gt ${value}"
            GREAT_EQUAL -> "${coluna} ge ${value}"
            IN -> "${coluna} eq ${value}"
            LESS -> "${coluna} ${this.value} ${value}"
            LESS_EQUAL -> "${coluna} ${this.value} ${value}"
            else -> "Tipo de dado é invalido para a condição"
        }
    }

    fun get(coluna: String, value: List<*>): String {
        if(value.size == 0 )
            return ""
        return when(this) {
            IN -> {
                if(value.get(0) is Int)
                    return "("+value.map { EQUAL.get(coluna,"${it}") }
                        .joinToString(" or ")+")"
                "("+value.map { EQUAL.get(coluna,"'${it}'") }
                        .joinToString(" or ")+")"
            }
            else -> "Tipo de dado é invalido para a condição"
        }
    }

}