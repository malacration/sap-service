package br.andrew.sap.listener

import br.andrew.sap.model.sap.ErrorMsg
import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.sap.SapMessage
import br.andrew.sap.model.exceptions.PixPaymentException
import br.andrew.sap.model.exceptions.SapGenericException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher

@SpringBootTest
class SapErrorListenerTests {

    @Autowired
    val eventPublisher: ApplicationEventPublisher? = null

    @Test
    fun testaProducaoMsgErro(){
        val error = SapError(ErrorMsg("", SapMessage("666","teste de integração msg erro - windson")))
        eventPublisher!!.publishEvent(SapGenericException(error))
    }

    @Test
    fun testeLinkedPayment(){
        val error = SapError(ErrorMsg("", SapMessage("666","teste de integração - linkedPayment - windson")))
        eventPublisher!!.publishEvent(PixPaymentException(error))
    }
}