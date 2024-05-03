package br.andrew.sap.integration

import br.andrew.sap.infrastructure.odata.Parameter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ParameterTest {


    @Test
    fun test(){
        val p = Parameter("windson","jose")
        Assertions.assertEquals("windson=jose",p.toString())
    }
}