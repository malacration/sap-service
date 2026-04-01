package br.andrew.sap.model.uzzipay

import br.andrew.sap.model.sap.BussinessPlace
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

class PayerTests {

    @Test
    fun test(){
        val erro = assertThrows<Exception> {
            Payer("","","","","","","")
        }
        Assertions.assertEquals("Documento do cliente nao pode ser vazio",erro.message)
    }

    @Test
    fun deveManterSomenteNumerosNoZipCode() {
        val payer = Payer(
            "12345678901",
            "Cliente Teste",
            "cliente@teste.com",
            "Rua A",
            "Ji-Parana",
            "RO",
            "76.835-500"
        )
        Assertions.assertEquals("76835500", payer.zipCode)
    }

    @Test
    fun deveUsarZipCodeDaFilialQuandoCepDoParceiroForInvalido() {
        val payer = Payer(
            "12345678901",
            "Cliente Teste",
            "cliente@teste.com",
            "",
            "",
            "",
            "76.835-50",
            BussinessPlace().also {
                it.Street = "Av. Filial"
                it.City = "Porto Velho"
                it.State = "RO"
                it.ZipCode = "76.835-500"
            }
        )

        Assertions.assertEquals("Av. Filial", payer.address)
        Assertions.assertEquals("Porto Velho", payer.city)
        Assertions.assertEquals("RO", payer.state)
        Assertions.assertEquals("76835500", payer.zipCode)
    }
}
