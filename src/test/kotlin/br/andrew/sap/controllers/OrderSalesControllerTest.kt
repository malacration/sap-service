package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sovis.PedidoVenda
import br.andrew.sap.services.OrdersService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired


//@ActiveProfiles("test")
//@SpringBootTest
class OrderSalesControllerTest {

    @Autowired
    var controller : OrderSalesController? = null

    @Autowired
    var ordersService: OrdersService? = null

    fun test(){
        val json = "{\"dataEntraga\":\"2023-04-18\",\"observacao\":\"PRAZO 120 DIAS \\nPRECO 110 ,00\\nDESCONTO 0,00\\nFRETE 7,69\\nROTEIRO .. SAIR DA BR 364 ENTRER SENTIDO CACAULANDIA R0 140 SEGUIR POR 15 KM ENTRAR A DIREITA NA LINHA C. 25  SEGUIR POR 7 KM LADO EAQUERDO  PROCURAR POR DANIEL .\",\"idCliente\":\"CLI0002970\",\"desconto\":0,\"produtos\":{\"precoUnitario\":110,\"idProduto\":\"PAC0000122\",\"desconto\":0,\"valorTabela\":110,\"quantidade\":30},\"idCondicaoPagamento\":19,\"frete\":7.69,\"idEmpresa\":2,\"tipoPedido\":9,\"codVendedor\":32,\"idPedido\":29,\"idFormaPagamento\":\"BB-RC-BOL-1199\"}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val pedidoBase = mapper.readValue(json, jacksonTypeRef<PedidoVenda>())
        val orderBase = pedidoBase.getOrder()
        val odata = Mockito.mock(OData::class.java)

        Assertions.assertEquals("29",orderBase.u_id_pedido_forca)
        Mockito.`when`(ordersService!!.save(orderBase)).thenReturn( odata )
        val retornoController = controller!!.save(pedidoBase)
//        Assertions.assertEquals("29",retornoController.u_id_pedido_forca)
    }
}