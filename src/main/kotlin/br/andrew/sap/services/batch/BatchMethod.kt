package br.andrew.sap.services.batch

import br.andrew.sap.services.abstracts.EntitiesService
import okio.Path

enum class BatchMethod {
        POST,PATCH,DELETE,CANCEL,PUT,CLOSE;

    // POST cria (sem id na URL); CANCEL/CLOSE sao acoes sem corpo; DELETE tambem vai sem corpo.
    fun temCorpo(): Boolean = this == POST || this == PATCH || this == PUT

    fun getHttp(service: EntitiesService<*>, id : BatchId? = null): String {
        if(this != POST && id == null)
            throw Exception("Nao e possivel dar um PATH em um documento sem informar o ID")

        return if(this == POST)
            "${this} ${service.path()}"
        else if(this == CANCEL) //Nao sei se esse metodo funciona
            "POST ${service.path()}(${id!!.getId()})/Cancel"
        else if(this == CLOSE)
            "POST ${service.path()}(${id!!.getId()})/Close"
        else
            "${this} ${service.path()}(${id!!.getId()})"
    }
}
