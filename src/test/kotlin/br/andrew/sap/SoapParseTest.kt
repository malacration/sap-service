package br.andrew.sap

import br.andrew.sap.infrastructure.configurations.softexpert.Envelope
import br.andrew.sap.infrastructure.configurations.softexpert.SOAPConnector
import br.andrew.sap.infrastructure.configurations.softexpert.SoftExpertEnvrioment
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import org.junit.jupiter.api.Test
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import softexpert.GetWorkflowRequestType
import softexpert.ObjectFactory
import java.lang.Boolean
import kotlin.Any
import kotlin.also

class SoapParseTest {


    fun print(payload : Any){
        val jaxbContext: JAXBContext = JAXBContext.newInstance(ObjectFactory::class.java)
        val jaxbMarshaller: Marshaller = jaxbContext.createMarshaller()
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
        jaxbMarshaller.marshal(payload,
            System.out
        )
    }

    fun print2(payload : Any){
        val mar = Jaxb2Marshaller().also {
            it.contextPath = "softexpert"
        }.createMarshaller().also {
            it.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)
        }
        mar.marshal(payload,
            System.out
        )
    }

    val service = SOAPConnector(SoftExpertEnvrioment("https://se.gruporovema.com.br/apigateway/se/ws/wf_ws.php",""))

    @Test
    fun teste(){
        val workflow = GetWorkflowRequestType().also {
            it.workflowID = "000268"
        }
        val resultado = service.getJaxElement(workflow)
        print(resultado)
    }


    @Test
    fun teste1(){
        val body = Envelope().also {
            it.body = GetWorkflowRequestType().also {
                it.workflowID = "000268"
            }
        }
        val resultado = service.getJaxElement(body)
        print2(resultado)
    }

    @Test
    fun springMarsshiln(){
        val body = GetWorkflowRequestType().also {
            it.workflowID = "000268"
        }
        val resultado = service.getJaxElement(body)
        print2(resultado)
    }

    @Test
    fun integration(){
        RequestTest()
    }
}