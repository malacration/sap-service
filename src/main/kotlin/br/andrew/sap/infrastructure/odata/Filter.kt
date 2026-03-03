package br.andrew.sap.infrastructure.odata

import br.andrew.sap.model.enums.Cancelled


class Filter(
    propertieImmutable : List<Predicate>,
    val defaultConector : String = "and",
    val stripPrefix: String? = null
) {
    constructor() : this(mutableListOf<Predicate>(), "and", null)
    constructor(vararg predicate: Predicate) : this(predicate.toMutableList(), "and", null)
    constructor(coluna: String, value: Any, condition : Condicao) : this(mutableListOf(Predicate(coluna,value,condition)), "and", null)
    constructor(propertieImmutable : List<Predicate>, stripPrefix: String?) : this(propertieImmutable, "and", stripPrefix)

    val propertie : MutableList<Predicate> = propertieImmutable.toMutableList()

    override fun toString(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie
            .map { predicate ->
                val (coluna, value) = normalizePredicate(predicate)
                predicate.render(coluna, value, predicate.literal)
            }
            .joinToString(" $defaultConector ")
        return "\$filter=${filtros}"
    }

    fun toSql(): String {
        if(propertie.isEmpty())
            return ""
        val filtros = propertie
            .map { predicate ->
                val (coluna, value) = normalizePredicate(predicate)
                predicate.renderSql(coluna, value)
            }
            .joinToString("&")
        return filtros
    }

    fun add(predicate: Predicate): Boolean {
        return propertie.add(predicate)
    }

    fun addAll(propertie: List<Predicate>): Filter {
        this.propertie.addAll(propertie)
        return this
    }

    fun withStripPrefix(prefix: String?): Filter {
        return Filter(this.propertie, this.defaultConector, prefix)
    }

    private fun normalizePredicate(predicate: Predicate): Pair<String, Any> {
        val coluna = stripPrefixIfNeeded(predicate.coluna)
        val value = if (predicate.literal && predicate.value is String) {
            stripPrefixIfNeeded(predicate.value as String)
        } else {
            predicate.value
        }
        return coluna to value
    }

    private fun stripPrefixIfNeeded(value: String): String {
        val prefix = stripPrefix ?: return value
        return if (value.startsWith(prefix)) value.removePrefix(prefix) else value
    }
}

class Predicate(val coluna: String, val value: Any, val condicao: Condicao, val literal : Boolean = false){
    constructor(cancelado: Cancelled, condicao: Condicao, literal : Boolean = false)
            : this(Cancelled.column,cancelado,condicao, literal)

    override fun toString(): String {
        return render()
    }

    fun toSql(): String {
        return renderSql()
    }

    fun render(coluna: String = this.coluna, value: Any = this.value, literal: Boolean = this.literal): String {
        return if(value is Int)
            condicao.get(coluna,value.toString())
        else if(value is List<*>)
            condicao.get(coluna,value)
        else if(!literal)
            condicao.get(coluna,"'$value'")
        else
            condicao.get(coluna,"$value")
    }

    fun renderSql(coluna: String = this.coluna, value: Any = this.value): String {
        return if(value is Int)
            "$coluna=$value"
        else
            "$coluna='$value'"
    }
}
