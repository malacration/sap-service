package br.andrew.sap.services.rdstation

import br.andrew.sap.infrastructure.configurations.rdstation.RdStationEnvrioment
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.BusinessPartner
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class EventsService(private val envrioment : RdStationEnvrioment,
                    private val restTemplate: RestTemplate) {
    private val path = "/platform/events?event_type=conversion"
    private  val url = envrioment.host + path

    fun conversion(document: Document, businessPartner: BusinessPartner, salePerson: SalePerson): ResponseEntity<String> {
        val payload = EventPayloadPedido(document,businessPartner,salePerson)
        val event = Event(payload)
        val request = RequestEntity
            .post(url)
            .header("Authorization","Bearer ${envrioment.getToken()}")
            .body(event)
        return restTemplate.exchange(request,String::class.java)
    }
}

class Event(val payload : EventPayloadPedido, val event_type : String = "CONVERSION", val event_family : String = "CDP")

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class EventPayloadPedido(val email : String, val conversion_identifier : ConversionType = ConversionType.SAP_Pedido){

    var name : String? = null
    var personal_phone : String? = null
    var mobile_phone : String? = null
    var available_for_mailing = false
    var traffic_source = "ERP"

    var cf_produtos_sustennutri : String? = null
    var company_name : String? = null

    var cf_data_criacao : String? = null
    var cf_representante_vendedor : String? = null
    var cf_observacoes : String? = null
    var cf_numero_pedido : String? = null
    var cf_nome_cliente : String? = null
    var cf_data_da_venda_sap : String? = null


    constructor(document: Document, businessPartner: BusinessPartner, salePerson: SalePerson) : this(document.CardCode+"@naotem.com"){
        if(document.salesPersonCode != salePerson.SalesEmployeeCode)
            throw Exception("Nao e permitido utilizar um Vendedor distindo do documento")
        if(document.CardCode != businessPartner.cardCode)
            throw Exception("Nao e permitido utilizar um Telefone se o parceiro e distinto do documento")

        name = document.cardName
        cf_produtos_sustennutri = document.DocumentLines.joinToString("\n")

        cf_data_criacao = document.docDate
        cf_representante_vendedor = salePerson.SalesEmployeeName
        cf_observacoes = document.comments
        mobile_phone = businessPartner.phone1
        cf_numero_pedido = document.docNum
        cf_nome_cliente = document.cardName
        cf_data_da_venda_sap = document.docDate

        company_name = listOf(cf_numero_pedido, cf_nome_cliente, cf_representante_vendedor, cf_data_da_venda_sap).joinToString(" - ", "", "")
    }

}

enum class ConversionType(){
    SAP_Pedido,
}