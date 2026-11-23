package br.andrew.sap.controllers.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.services.comercial.FreteContratoService
import br.andrew.sap.services.documents.OrdersService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

/**
 * Contrato com frete e sem localidade nasce assim quando o schedule nao consegue resolver o
 * destino. A troca resolve pelo pedido original em vez de travar; contrato sem frete nao trava.
 */
class TrocaLocalidadeContratoTest {

    private val orderService = mock<OrdersService>()
    private val freteContratoService = mock<FreteContratoService>()
    private val controller = ContratoVendaFuturaController(mock(), mock(), mock(), mock(), mock(), mock(),
        mock(), mock(), mock(), orderService, mock(), mock(), mock(), mock(), 9, mock(),
        freteContratoService, mock(), mock())

    private fun contrato(frete: Double, localidade: Int? = null) =
        Contrato(99, "C0001", mutableListOf(Item("A", "A", 10.0, 1.0, 10.0, 0.0, 1.0, "UN")), 1, "Cliente", 2, frete).also {
            it.DocEntry = 390
            it.U_Localidade = localidade
        }

    private fun pedidoComEndereco() {
        val pedido = OrderSales("C0001", "2026-10-08", listOf()).also { it.shipToCode = "FAZENDA" }
        whenever(orderService.getById(99)).thenReturn(OData(linkedMapOf("value" to OData().mapper.writeValueAsString(pedido))))
    }

    @Test
    fun `contrato sem frete e sem localidade nao trava`() {
        val contrato = contrato(frete = 0.0)

        controller.garanteLocalidadeDoFrete(contrato)

        assertEquals(null, contrato.U_Localidade)
        verifyNoInteractions(orderService, freteContratoService)
    }

    @Test
    fun `contrato com frete e localidade segue como antes`() {
        controller.garanteLocalidadeDoFrete(contrato(frete = 200.0, localidade = 20))

        verifyNoInteractions(orderService, freteContratoService)
    }

    @Test
    fun `contrato com frete sem localidade resolve pelo endereco do pedido`() {
        pedidoComEndereco()
        whenever(freteContratoService.localidadeDoEndereco("C0001", "FAZENDA", AddresType.bo_ShipTo)).thenReturn(20)
        whenever(freteContratoService.regiaoVigente(2)).thenReturn(Regiao().also { it.Code = "VIGENTE" })
        val contrato = contrato(frete = 200.0)

        controller.garanteLocalidadeDoFrete(contrato)

        assertEquals(20, contrato.U_Localidade)
        assertEquals("VIGENTE", contrato.U_RegiaoCode)
    }

    @Test
    fun `endereco sem localidade falha com a causa real`() {
        pedidoComEndereco()
        whenever(freteContratoService.localidadeDoEndereco("C0001", "FAZENDA", AddresType.bo_ShipTo))
            .thenThrow(RuntimeException("O endereco de entrega 'FAZENDA' do cliente C0001 nao possui localidade cadastrada"))

        val erro = assertThrows<Exception> { controller.garanteLocalidadeDoFrete(contrato(frete = 200.0)) }

        assertTrue(erro.message!!.contains("nao possui localidade cadastrada"), erro.message)
    }

    @Test
    fun `filial sem regiao ativa falha com a causa real`() {
        pedidoComEndereco()
        whenever(freteContratoService.localidadeDoEndereco("C0001", "FAZENDA", AddresType.bo_ShipTo)).thenReturn(20)
        whenever(freteContratoService.regiaoVigente(2))
            .thenThrow(RuntimeException("Nao existe regiao de frete ativa para a filial 2"))
        val contrato = contrato(frete = 200.0)

        val erro = assertThrows<Exception> { controller.garanteLocalidadeDoFrete(contrato) }

        assertTrue(erro.message!!.contains("Nao existe regiao de frete ativa"), erro.message)
        assertEquals(null, contrato.U_Localidade)
    }
}
