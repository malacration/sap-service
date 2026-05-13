package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.sap.BussinessPlace

class Payer(
    val document : String,
    val name : String,
    val email : String,
    address : String,
    city : String,
    state : String,
    zipCode : String,
    fallback : BussinessPlace? = null
){
    val address : String = resolveAddress(address, fallback)
    val city : String = resolveCity(city, fallback)
    val state : String = resolveState(state, fallback)
    val zipCode : String = resolveZipCode(zipCode, fallback)

    init {
        if(document.isBlank())
            throw Exception("Documento do cliente nao pode ser vazio")
    }

    fun getCepNumber(): String {
        return zipCode
    }

    companion object {
        private fun sanitizeDigits(value: String?): String {
            return value?.replace(Regex("\\D"), "") ?: ""
        }

        private fun firstNotBlank(vararg values: String?): String? {
            return values.firstOrNull { !it.isNullOrBlank() }?.trim()
        }

        private fun resolveAddress(address: String, fallback: BussinessPlace?): String {
            return firstNotBlank(address, fallback?.Street, fallback?.Block, fallback?.BPLName) ?: "Sem Nome"
        }

        private fun resolveCity(city: String, fallback: BussinessPlace?): String {
            return firstNotBlank(city, fallback?.City, fallback?.County) ?: "Sem Cidade"
        }

        private fun resolveState(state: String, fallback: BussinessPlace?): String {
            return firstNotBlank(state, fallback?.State) ?: "Sem Estado"
        }

        private fun resolveZipCode(zipCode: String, fallback: BussinessPlace?): String {
            val current = sanitizeDigits(zipCode)
            if (current.length == 8) {
                return current
            }

            val fallbackZipCode = sanitizeDigits(fallback?.ZipCode)
            if (fallbackZipCode.length == 8) {
                return fallbackZipCode
            }

            return current.ifBlank { fallbackZipCode }
        }
    }
}
