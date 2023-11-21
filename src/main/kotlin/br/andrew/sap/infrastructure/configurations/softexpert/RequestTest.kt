package br.andrew.sap.infrastructure.configurations.softexpert

import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.Marshaller
import jakarta.xml.soap.MessageFactory
import jakarta.xml.soap.SOAPConstants
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.web.client.RestTemplate
import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import softexpert.GetWorkflowRequestType
import softexpert.GetWorkflowResponseType
import softexpert.ObjectFactory
import java.lang.Boolean
import javax.xml.namespace.QName


internal class RequestTest : WebServiceGatewaySupport() {

    init {
        val marshaller = Jaxb2Marshaller().also {
            it.setPackagesToScan("softexpert")
            MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL)
        }
        // this package must match the package in the <generatePackage> specified in
        // pom.xml
        marshaller.setMarshallerProperties(
            mapOf(
                Marshaller.JAXB_FORMATTED_OUTPUT to true
            )
        );

        this.setDefaultUri("https://se.gruporovema.com.br/se/ws/wf_ws.php?wsdl");
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);


//        val jaxbContext: JAXBContext = JAXBContext.newInstance(request!!::class.java)
//        val jaxbMarshaller: Marshaller = jaxbContext.createMarshaller()
//        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)

        val payload = ObjectFactory().createGetWorkflowRequestType().also {
            it.workflowID = "1"
        }
        val name = QName("urn:workflow", "Envelope")
        val request = JAXBElement(name, GetWorkflowRequestType::class.java, payload)

        webServiceTemplate
            .marshalSendAndReceive(
                "https://se.gruporovema.com.br/se/ws/wf_ws.php?wsdl", payload,
            ) as GetWorkflowResponseType
    }
}