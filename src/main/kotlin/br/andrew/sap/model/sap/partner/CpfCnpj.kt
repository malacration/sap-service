package br.andrew.sap.model.sap.partner

class CpfCnpj(value : String) {

    val value : String

    constructor(tax : BPFiscalTaxID) : this(
        if(tax.TaxId4 == null || tax.TaxId4!!.isBlank())
            tax.TaxId0 ?: throw Exception("CpfCnpj not found")
        else
            tax.TaxId4 ?: throw Exception("CpfCnpj not found")
    )

    init {
        this.value = value.replace("\\D".toRegex(), "")
        if(this.value.length > 14)
            throw Exception("CpfCnpj invalid, tamanho e maior que 14")
    }



    fun isCpf() : Boolean {
        return value.length == 11
    }

    fun isCnpj() : Boolean {
        return value.length == 14
    }


    fun getWithMask(): String {
        return when (value.length) {
            4,5,6 -> {
                val part1 = value.substring(0, 3)
                val part2 = value.substring(3)
                "$part1.$part2"
            }
            7,8,9 -> {
                val part1 = value.substring(0, 3)
                val part2 = value.substring(3, 6)
                val part3 = value.substring(6)
                "$part1.$part2.$part3"
            }
            10,11 -> {
                val part1 = value.substring(0, 3)
                val part2 = value.substring(3, 6)
                val part3 = value.substring(6, 9)
                val part4 = value.substring(9)
                "$part1.$part2.$part3-$part4"
            }
            12 -> {
                val part1 = value.substring(0, 2)
                val part2 = value.substring(2, 5)
                val part3 = value.substring(5, 8)
                val part4 = value.substring(8 )
                "$part1.$part2.$part3/$part4"
            }
            13,14 -> {
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

    override fun toString(): String {
        return getWithMask()
    }

    fun equals(cpf : String): Boolean {
        return CpfCnpj(cpf).value == this.value
    }
}