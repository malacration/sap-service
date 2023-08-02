package br.andrew.sap.model

import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.services.ComissaoServiceMock
import br.andrew.sap.services.ItemsService
import br.andrew.sap.services.pricing.ComissaoService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal

class PrecoUnitarioComDesoneracaoTest {


    @Test
    fun teste(){
        val precoAlvo = BigDecimal("93.65")
        val imposto = SalesTaxAuthorities(0,
                17.5,
                0.0,
                0.0,
                100.0)
        val valorEsperado = BigDecimal("113.5152")
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }

    @Test
    fun baseReduzida(){
        val precoAlvo = BigDecimal("93.65")
        val imposto = SalesTaxAuthorities(0,
                18.0,
                0.0,
                0.0,
                0.00)

        val valorEsperado = BigDecimal("93.65")
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }


    @Test
    fun desoneradoDesconto(){
        val precoAlvo = BigDecimal("93.65")
        val imposto = SalesTaxAuthorities(0,
                18.0,
                0.0,
                0.0,
                0.00)

        val valorEsperado = BigDecimal("93.65")
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals(valorEsperado, resultado)
    }

    @Test
    fun desoneradoComDesconto(){
        val precoAlvo = BigDecimal("100")
        val imposto = SalesTaxAuthorities(0,
                17.5,
                0.0,
                0.0,
                100.00)
        val desconto = BigDecimal("10")

        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto,desconto)
        Assertions.assertEquals("134.6803", resultado.toString())
    }


    val json = "{\"dataEntraga\":\"2023-05-08\",\"observacao\":\"CLIENTE RETIRA.\",\"idCliente\":\"CLI0000297\",\"desconto\":0,\"produtos\":[{\"precoUnitario\":77.83,\"idProduto\":\"PAC0000069\",\"desconto\":8.0023641,\"valorTabela\":84.6,\"quantidade\":6},{\"precoUnitario\":93.53,\"idProduto\":\"PAC0000138\",\"desconto\":7.0093458,\"valorTabela\":100.58,\"quantidade\":10}],\"idCondicaoPagamento\":-2,\"frete\":0,\"idEmpresa\":11,\"tipoPedido\":9,\"codVendedor\":55,\"idPedido\":234,\"idFormaPagamento\":\"AVISTA\"}"
    @Test
    fun pedidoDesonerado(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val pedidoBase = mapper.readValue(json, jacksonTypeRef<PedidoVenda>()).also { it.produtos.forEach { it.listapreco = 5 } }
        val orderBase = pedidoBase.getOrder(mock(ItemsService::class.java), ComissaoServiceMock.get())

        val imposto = SalesTaxAuthorities(0,
                17.5,
                0.0,
                0.0,
                100.00)
        for (documentLine in orderBase.DocumentLines) {
            documentLine.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(documentLine,imposto).toString()
            println(documentLine.UnitPrice)
        }
        val desonerado = imposto.taxValueOutros(orderBase.total())
        Assertions.assertEquals(1699.7355167157484,orderBase.total())
        Assertions.assertEquals(297.45371542525595,desonerado)
        val liquido = orderBase.total()-desonerado
        Assertions.assertEquals(1402.2818012904925,liquido)
    }
}