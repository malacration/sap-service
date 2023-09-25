package br.andrew.sap.infrastructure.odata

import br.andrew.sap.model.enums.Cancelled


class Filter(val propertie : List<Predicate>) {
    constructor() : this(listOf())
    //constructor(predicate: Predicate) : this(listOf(predicate))
    constructor(vararg predicate: Predicate) : this(predicate.toList())
    constructor(coluna: String, value: Any, condition : Condicao) : this(listOf(Predicate(coluna,value,condition)))

    override fun toString(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie.map { it.toString() }.joinToString(" and ")
        return "\$filter=${filtros}"
    }

    fun toSql(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie.map { it.toSql() }.joinToString("&")
        return filtros
    }
}

class Predicate(val coluna: String, val value: Any, val condicao: Condicao){
    constructor(cancelado: Cancelled, condicao: Condicao) : this(Cancelled.column,cancelado,condicao)

    override fun toString(): String {
        return if(value is Int)
            condicao.get(coluna,value.toString())
        else if(value is List<*>)
            condicao.get(coluna,value)
        else
            condicao.get(coluna,"'$value'")
    }

    fun toSql(): String {
        return if(value is Int)
            "$coluna=$value"
        else
            "$coluna='$value'"
    }
}