package br.andrew.sap.model.partner

class CpfCnpj(value : String) {

    val value = value.replace("\\D".toRegex(), "")

    fun isCpf() : Boolean {
        return value.length == 11
    }

    fun isCnpj() : Boolean {
        return value.length == 14
    }

    fun getWithMask(): String {
        return when (value.length) {
            11 -> { // CPF
                val part1 = value.substring(0, 3)
                val part2 = value.substring(3, 6)
                val part3 = value.substring(6, 9)
                val part4 = value.substring(9)
                "$part1.$part2.$part3-$part4"
            }
            14 -> { // CNPJ
                val part1 = value.substring(0, 2)
                val part2 = value.substring(2, 5)
                val part3 = value.substring(5, 8)
                val part4 = value.substring(8, 12)
                val part5 = value.substring(12)
                "$part1.$part2.$part3/$part4-$part5"
            }
            else -> value
        }
    }
}