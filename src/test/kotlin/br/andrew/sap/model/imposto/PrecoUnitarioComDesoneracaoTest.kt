package br.andrew.sap.model.imposto

import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.services.ComissaoServiceMock
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.math.RoundingMode

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
    fun semImpostoComDesconto(){
        val precoAlvo = BigDecimal("100")
        val imposto = SalesTaxAuthorities(0,
            17.5,
            0.0,
            0.0,
            0.0)
        val valorEsperado = BigDecimal("111.1111")
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto, BigDecimal(10))

        Assertions.assertEquals(valorEsperado, resultado)
    }

    @Test
    fun disconto(){
        val precoAlvo = BigDecimal("131.6700")
        val imposto = SalesTaxAuthorities(0,
            17.5,
            0.0,
            0.0,
            100.0)
        val valorEsperado = BigDecimal("168.0000").setScale(4)
        val desconto = BigDecimal("5")
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto,desconto)
        var desonerado = resultado.multiply(BigDecimal("0.175"))
        Assertions.assertEquals(valorEsperado, resultado)
        Assertions.assertEquals(precoAlvo.setScale(2),
            resultado.minus(desonerado)
                .multiply(BigDecimal("0.95"))
                .setScale(2,RoundingMode.HALF_DOWN))
    }

    @Test
    fun baseReduzida(){
        val precoAlvo = BigDecimal("93.65")
        val imposto = SalesTaxAuthorities(0,
                18.0,
                0.0,
                0.0,
                0.00)

        val valorEsperado = BigDecimal("93.6500")
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

        val valorEsperado = BigDecimal("93.6500")
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
        Assertions.assertEquals("134.6801", resultado.toString())
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
        Assertions.assertEquals(1699.74,orderBase.total())
        Assertions.assertEquals(297.4545,desonerado)
        val liquido = orderBase.total()-desonerado
        Assertions.assertEquals(1402.2855,liquido)
    }

    @Test
    fun desoneradoBase60(){
        val precoAlvo = BigDecimal("100")
        val imposto = SalesTaxAuthorities(0,
            12.0,
            0.0,
            0.0,
            60.00)

        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        val rate = imposto.valorImposto(resultado)
        Assertions.assertEquals("0.0720", imposto.rateBaseOutro().toString())
        Assertions.assertEquals("107.7586", resultado.toString())
        Assertions.assertEquals("7.76", rate.toString())
    }


    @Test
    fun desoneradoArredondamento(){
        val precoAlvo = BigDecimal("93.50")
        val imposto = SalesTaxAuthorities(0,
            19.5,
            0.0,
            0.0,
            100.00)
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals("0.1950", imposto.rateBaseOutro().toString())
        val valorDosProdutos = resultado.multiply(BigDecimal("50"))
        val valorDaDesoneracaoDoIcms = valorDosProdutos.multiply(imposto.rateBaseOutro())
        Assertions.assertEquals("4675.00", valorDosProdutos.minus(valorDaDesoneracaoDoIcms).setScale(2,RoundingMode.HALF_UP).toString())
        Assertions.assertEquals("116.1491", resultado.toString())
    }


    @Test
    fun testeDesoneradoValoresGrande(){
        val precoAlvo = BigDecimal("935000.00")
        val imposto = SalesTaxAuthorities(0,
            19.5,
            0.0,
            0.0,
            100.00)
        val resultado = PrecoUnitarioComDesoneracao().calculaPreco(precoAlvo,imposto)
        Assertions.assertEquals("0.1950", imposto.rateBaseOutro().toString())
        Assertions.assertEquals("1161490.6832", resultado.toString())
        val valorDaDesoneracaoDoIcms = resultado.multiply(imposto.rateBaseOutro()).setScale(2,RoundingMode.HALF_UP)
        Assertions.assertEquals("226490.68", valorDaDesoneracaoDoIcms.toString())
        Assertions.assertEquals("935000.00", resultado.minus(valorDaDesoneracaoDoIcms).setScale(2,RoundingMode.HALF_UP).toString())
        Assertions.assertEquals("1161490.6832", resultado.toString())
    }
}