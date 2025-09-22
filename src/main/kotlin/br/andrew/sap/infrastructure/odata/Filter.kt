package br.andrew.sap.infrastructure.odata

import br.andrew.sap.model.enums.Cancelled


class Filter(propertieImmutable : List<Predicate>, val defaultConector : String = "and") {
    constructor() : this(mutableListOf<Predicate>())
    constructor(vararg predicate: Predicate) : this(predicate.toMutableList())
    constructor(coluna: String, value: Any, condition : Condicao) : this(mutableListOf(Predicate(coluna,value,condition)))

    val propertie : MutableList<Predicate> = propertieImmutable.toMutableList()

    override fun toString(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie.map { it.toString() }.joinToString(" $defaultConector ")
        return "\$filter=${filtros}"
    }

    fun toSql(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie.map { it.toSql() }.joinToString("&")
        return filtros
    }

    fun add(predicate: Predicate): Boolean {
        return propertie.add(predicate)
    }

    fun addAll(propertie: List<Predicate>): Filter {
        this.propertie.addAll(propertie)
        return this
    }
}

class Predicate(val coluna: String, val value: Any, val condicao: Condicao, val literal : Boolean = false){
    constructor(cancelado: Cancelled, condicao: Condicao, literal : Boolean = false)
            : this(Cancelled.column,cancelado,condicao, literal)

    override fun toString(): String {
        return if(value is Int)
            condicao.get(coluna,value.toString())
        else if(value is List<*>)
            condicao.get(coluna,value)
        else if(!literal)
            condicao.get(coluna,"'$value'")
        else
            condicao.get(coluna,"$value")
    }

    fun toSql(): String {
        return if(value is Int)
            "$coluna=$value"
        else
            "$coluna='$value'"
    }
}