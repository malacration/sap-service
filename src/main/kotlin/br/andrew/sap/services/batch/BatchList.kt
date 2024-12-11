package br.andrew.sap.services.batch

import br.andrew.sap.services.abstracts.EntitiesService

class BatchList : Collection<Triple<BatchMethod,Any, EntitiesService<*>>>{
    private val content : MutableList<Triple<BatchMethod,Any, EntitiesService<*>>> = mutableListOf()

    fun add(method : BatchMethod, payLoad : Any, service : EntitiesService<*>): BatchList {
        content.add(Triple(method,payLoad,service))
        return this
    }

    override val size: Int
        get() = content.size

    override fun isEmpty(): Boolean {
        return content.isEmpty()
    }

    override fun iterator(): Iterator<Triple<BatchMethod,Any, EntitiesService<*>>> {
        return content.iterator()
    }

    override fun containsAll(elements: Collection<Triple<BatchMethod,Any, EntitiesService<*>>>): Boolean {
        return content.containsAll(elements)
    }

    override fun contains(element: Triple<BatchMethod,Any, EntitiesService<*>>): Boolean {
        return content.contains(element)
    }

}