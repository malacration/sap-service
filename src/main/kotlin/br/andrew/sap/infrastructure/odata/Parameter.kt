package br.andrew.sap.infrastructure.odata

class Parameter(val key : String, val value : Any) {

    override fun toString(): String {
        return "$key=$value"
    }
}