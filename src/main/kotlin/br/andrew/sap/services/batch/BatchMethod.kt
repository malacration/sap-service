package br.andrew.sap.services.batch

import br.andrew.sap.services.abstracts.EntitiesService
import okio.Path

enum class BatchMethod {
        POST,PATCH,CANCEL,PUT;

    fun getHttp(service: EntitiesService<*>, id : BatchId? = null): String {
        if(this != POST && id == null)
            throw Exception("Nao e possivel dar um PATH em um documento sem informar o ID")

        return if(this == POST)
            "${this} ${service.path()}"
        else if(this == CANCEL) //Nao sei se esse metodo funciona
            "POST ${service.path()}(${id!!.getId()})/Cancel"
        else
            "${this} ${service.path()}(${id!!.getId()})"
    }
}