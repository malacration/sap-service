package br.andrew.sap.infrastructure.odata

class Parameter(val key : String, val value : Any) {

    override fun toString(): String {
        return if(value is String && !value.toString().startsWith("'"))
            "$key='$value'"
        else
            "$key=$value"
    }
}