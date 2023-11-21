package br.andrew.sap.infrastructure.configurations.softexpert

import jakarta.xml.bind.JAXBElement
import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement
import jakarta.xml.soap.SOAPException
import jakarta.xml.soap.SOAPMessage
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import org.springframework.ws.WebServiceMessage
import org.springframework.ws.client.core.WebServiceMessageCallback
import org.springframework.ws.client.core.support.WebServiceGatewaySupport
import org.springframework.ws.soap.SoapMessage
import javax.xml.namespace.QName
import kotlin.Any
import kotlin.String
import kotlin.Throws
import kotlin.also


class SOAPConnector(val envrioment : SoftExpertEnvrioment) : WebServiceGatewaySupport() {

    init {
        val marshaller = Jaxb2Marshaller().also {
            it.setPackagesToScan("softexpert")
//            it.marshal(JAXBElement<VSM>(QName("uri", "local"), VSM::class.java, vsm), System.out)
        }
        this.setDefaultUri(envrioment.host)
        this.setMarshaller(marshaller)
        this.setUnmarshaller(marshaller)
    }

    private val callback = SoapAddAuthorizationCallBack(
        envrioment.token
    )
    fun callWebService(request: Any?): Any? {
//        val messageFactory: MessageFactory = MessageFactory.newInstance()
//
//        val soapMessage: SOAPMessage = messageFactory.createMessage()
//        val soapPart = soapMessage.soapPart
//
//        val xmlns = "?"
//        val envelope = soapPart.envelope
//        envelope.addNamespaceDeclaration("urn", xmlns)
//
//        val soapBody: SOAPBody = envelope.body
//        val bodyElem: SOAPElement = soapBody.addChildElement("HelpDesk_QueryList_Service", "urn")
//
//        val body = teste(request)
//        soapMessage.saveChanges();
//        soapMessage.writeTo(System.out);
//
//
//        val tesete = SaajSoapMessageFactory().createWebServiceMessage()
//
////        val soapHeader: SOAPHeader = envelope.header
////        val authInfoElem: SOAPElement = soapHeader.addChildElement("AuthenticationInfo", "urn")
////        createElementAndSetText(authInfoElem, "userName", username)
////        createElementAndSetText(authInfoElem, "password", password)

        return getWebServiceTemplate().marshalSendAndReceive(Envelop())
//        return getWebServiceTemplate().marshalSendAndReceive(Envelop(),this.callback)
    }

    @Throws(SOAPException::class)
    private fun createSoapEnvelope(soapMessage: SOAPMessage) {
        val soapPart = soapMessage.soapPart


        // SOAP Envelope
        val envelope = soapPart.envelope
        envelope.addNamespaceDeclaration("soap", "http://example.com/SMX/example")
        val soapBody = envelope.body
    }

    inline fun <reified T> getJaxElement(teste : T): JAXBElement<T> {
        val name = QName("urn:workflow", "urn:getWorkflow")
        return JAXBElement(name, T::class.java, teste)
    }

    fun doWithMessage(message: WebServiceMessage?) {
        if(message is SoapMessage){
            message.soapHeader.addHeaderElement(QName("Autorization", envrioment.token))
        }
    }

}

class SoapAddAuthorizationCallBack(val token : String) : WebServiceMessageCallback{

    override fun doWithMessage(message: WebServiceMessage?) {
        if(message is SoapMessage){
            message.soapHeader.addHeaderElement(QName("Autorization", token))
//            val soapHeader: SoapHeader = message.soapHeader
//            val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
//            transformer.transform(StringSource(token), soapHeader.result)
        }

    }

}



@XmlRootElement(name = "TYPES")
@XmlAccessorType(XmlAccessType.FIELD)
class Envelop() {

    @XmlElement(name = "TYPE")
    var body : Any? = null
}