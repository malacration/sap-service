package br.andrew.sap.services.rdstation

import br.andrew.sap.infrastructure.configurations.rdstation.RdStationEnvrioment
import br.andrew.sap.model.documents.base.Document
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EventsService(private val envrioment : RdStationEnvrioment,
                    private val restTemplate: RestTemplate) {
    private val path = "/platform/events?event_type=conversion"
    private  val url = envrioment.host + path

    fun conversion( document: Document){
        val payload = EventPayloadPedido(document)
        val event = Event(payload)
        val request = RequestEntity
            .post(url)
            .header("Authorization","Bearer ${envrioment.getToken()}")
            .body(event)
        restTemplate
            .exchange(request,String::class.java)
    }
}

class Event(val payload : EventPayloadPedido, val event_type : String = "CONVERSION", val event_family : String = "CDP")

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class EventPayloadPedido(val email : String, val conversion_identifier : ConversionType = ConversionType.SAP_Pedido){

    var name : String? = null
    var personal_phone : String? = null
    var mobile_phone : String? = null
    var available_for_mailing = false
    var traffic_source = "ERP"

    var cf_produtos_sustennutri : String? = null

    constructor(document: Document) : this(document.CardCode+"@naotem.com"){
        name = document.cardName
        cf_produtos_sustennutri = document.DocumentLines.joinToString("\n")
    }

}

enum class ConversionType(){
    SAP_Pedido,
}