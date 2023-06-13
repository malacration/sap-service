package br.andrew.sap.infrastructure.odata

class OrderBy(val propertie : Map<String, Order>) {

    constructor() : this(mapOf())
    constructor(coluna: String, order: Order) : this(mapOf(coluna to order))

    override fun toString(): String {
        if(propertie.size == 0)
            return ""
        val order = propertie.map { "${it.key} ${it.value.valor}"  }.joinToString()
        return "\$orderby=${order}"
    }
}

