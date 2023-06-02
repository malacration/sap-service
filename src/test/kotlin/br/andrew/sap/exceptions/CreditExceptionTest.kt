package br.andrew.sap.exceptions

import br.andrew.sap.infrastructure.configurations.EventPublisherSingleton
import br.andrew.sap.model.ErrorMsg
import br.andrew.sap.model.SapError
import br.andrew.sap.model.SapMessage
import br.andrew.sap.model.exceptions.CreditException
import br.andrew.sap.model.exceptions.LinkedPaymentMethodException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.context.ApplicationEventPublisher

class CreditExceptionTest {
    @Test
    fun construtor(){
        val locationEntrada = "https://localhost:6666/b1s/v1/Drafts(5099)"
        val sapErro = SapError(ErrorMsg("code", SapMessage("","")))
        val excption = CreditException(sapErro,locationEntrada)
        Assertions.assertEquals("5099",excption.idLocation)
    }

    @Test
    fun construtorLocationNull(){
        val sapErro = SapError(ErrorMsg("code", SapMessage("","")))
        val excption = CreditException(sapErro,null)
        Assertions.assertEquals("",excption.idLocation)
    }

    @Test
    fun linkPaymentException(){
        EventPublisherSingleton.instance = mock(ApplicationEventPublisher::class.java);
        val msg = "Linked payment method BB-RC-BOL-1199 is inactive or is no longer linked with business partner CLI0003204"
        val sapErro = SapError(ErrorMsg("code", SapMessage("",msg)))
        val exception = sapErro.getError()
        Assertions.assertTrue(exception is LinkedPaymentMethodException)
    }
}