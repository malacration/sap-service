package br.andrew.sap.controllers


import br.andrew.sap.infrastructure.configurations.softexpert.SOAPConnector
import br.andrew.sap.model.Version
import br.andrew.sap.services.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import softexpert.GetWorkflowRequestType
import softexpert.GetWorkflowResponseType
import softexpert.ObjectFactory
import java.util.*


@RestController
class IndexController(val version : Version, val serasaService: SerasaService, val soapService : SOAPConnector) {

    @GetMapping("/")
    fun index() : Version{
        return version
    }

    @GetMapping("/test")
    fun test() : Any? {
        return newWorkflowEditDataClient().get(soapService)
    }

}


class newWorkflowEditDataClient : WebServiceGatewaySupport(){

    fun get(soap : SOAPConnector) : GetWorkflowResponseType {
        ObjectFactory().createGetWorkflowRequestType()
        val request = GetWorkflowRequestType().also {
            it.workflowID = "12"
        }
        return soap.callWebService(request) as GetWorkflowResponseType
    }

}