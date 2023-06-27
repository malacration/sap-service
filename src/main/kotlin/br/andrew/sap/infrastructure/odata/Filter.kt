package br.andrew.sap.infrastructure.odata

import br.andrew.sap.model.Cancelled


class Filter(val propertie : List<Predicate>) {
    constructor() : this(listOf())
    //constructor(predicate: Predicate) : this(listOf(predicate))
    constructor(vararg predicate: Predicate) : this(predicate.toList())

    override fun toString(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie.map { it.toString() }.joinToString(" and ")
        return "\$filter=${filtros}"
    }
}

class Predicate(val coluna: String, val value: Any, val condicao: Condicao){
    constructor(cancelado: Cancelled,condicao: Condicao) : this(Cancelled.column,cancelado,condicao)

    override fun toString(): String {
        return if(value is Int)
            condicao.get(coluna,value.toString())
        else if(value is List<*>)
            condicao.get(coluna,value)
        else
            condicao.get(coluna,"'${value.toString()}'")
    }
}