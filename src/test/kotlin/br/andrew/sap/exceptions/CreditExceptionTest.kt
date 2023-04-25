package br.andrew.sap.exceptions

import br.andrew.sap.model.ErrorMsg
import br.andrew.sap.model.SapError
import br.andrew.sap.model.SapMessage
import br.andrew.sap.model.exceptions.CreditException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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
}