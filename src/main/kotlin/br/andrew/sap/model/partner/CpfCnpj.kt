package br.andrew.sap.model.partner

class CpfCnpj(value : String) {

    val value = value.replace("\\D".toRegex(), "")

    fun isCpf() : Boolean {
        return value.length == 11
    }

    fun isCnpj() : Boolean {
        return value.length == 14
    }
}