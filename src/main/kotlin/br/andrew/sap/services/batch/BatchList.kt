package br.andrew.sap.services.batch

import br.andrew.sap.services.abstracts.EntitiesService

/**
 * Lista de operacoes de um changeset. Guarda [BatchItem] pra suportar header por requisicao,
 * mas mantem as sobrecargas com Triple: e assim que os servicos existentes montam o lote.
 */
class BatchList : Collection<BatchItem>{
    private val content : MutableList<BatchItem> = mutableListOf()

    fun add(item : BatchItem): BatchList {
        content.add(item)
        return this
    }

    fun add(triple : Triple<BatchMethod, Any, EntitiesService<*>>): BatchList {
        return add(BatchItem(triple.first, triple.second, triple.third))
    }

    fun add(method : BatchMethod, payLoad : Any, service : EntitiesService<*>): BatchList {
        return add(BatchItem(method, payLoad, service))
    }

    // Header por requisicao - so quem precisa passa (ex.: B1S-ReplaceCollectionsOnPatch).
    fun add(method : BatchMethod, payLoad : Any, service : EntitiesService<*>, headers : Map<String,String>): BatchList {
        return add(BatchItem(method, payLoad, service, headers))
    }

    override val size: Int
        get() = content.size

    override fun isEmpty(): Boolean {
        return content.isEmpty()
    }

    override fun iterator(): Iterator<BatchItem> {
        return content.iterator()
    }

    override fun containsAll(elements: Collection<BatchItem>): Boolean {
        return content.containsAll(elements)
    }

    override fun contains(element: BatchItem): Boolean {
        return content.contains(element)
    }

    fun addAll(triple: List<Triple<BatchMethod, Any, EntitiesService<*>>>): BatchList {
        triple.forEach {
            this.add(it)
        }
        return this
    }

}
