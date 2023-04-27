package br.andrew.sap.model

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PrecoUnitarioComDesoneracaoTest {


    @Test
    fun teste(){

        val precoAlvo = BigDecimal("93.65");
        val imposto = SalesTaxAuthorities(0,
                17.5,
                0.0,
                0.0,
                100.0)
        val valorEsperado = BigDecimal("113.5152");
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }

    @Test
    fun baseReduzida(){
        val precoAlvo = BigDecimal("93.65");
        val imposto = SalesTaxAuthorities(0,
                18.0,
                0.0,
                0.0,
                0.00)

        val valorEsperado = BigDecimal("93.65");
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }


    @Test
    fun desoneradoDesconto(){
        val precoAlvo = BigDecimal("93.65");
        val imposto = SalesTaxAuthorities(0,
                18.0,
                0.0,
                0.0,
                0.00)

        val valorEsperado = BigDecimal("93.65");
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }

    @Test
    fun desoneradoComDesconto(){
        val precoAlvo = BigDecimal("100");
        val imposto = SalesTaxAuthorities(0,
                17.5,
                0.0,
                0.0,
                100.00)
        val desconto = BigDecimal("10")

        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto,desconto)
        Assertions.assertEquals("134.6803", resultado.toString())
    }
}