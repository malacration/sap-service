package br.andrew.sap.services

import br.andrew.sap.model.Comissao
import br.andrew.sap.services.document.DesoneradoService
import br.andrew.sap.services.pricing.ComissaoService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class DesoneradoServiceTest {

    @Autowired
    var desoneradoService: DesoneradoService? = null

    @Test
    fun test(){
        Assertions.assertTrue(desoneradoService!!.allImpotos.containsAll(listOf(10,20,30)))
    }
}