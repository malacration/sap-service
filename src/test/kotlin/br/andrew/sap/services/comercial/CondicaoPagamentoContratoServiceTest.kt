package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.comercial.PrazoPagamentoDto
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.model.payment.PaymentTermsTypes
import br.andrew.sap.services.stock.ItemsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.math.BigDecimal

class CondicaoPagamentoContratoServiceTest {

    private val contratoService = mock<ContratoVendaFuturaService>()
    private val orderService = mock<OrdersService>()
    private val prazoService = mock<PrazoPagamentoService>()
    private val itemService = mock<ItemsService>()
    private val paymentTermsService = mock<PaymentTermsTypesService>()
    private val service = CondicaoPagamentoContratoService(contratoService, orderService, prazoService, itemService, paymentTermsService)

    /** Tabela 3, condicao 15: 5% de desconto e 2% de juros. */
    private val prazo = PrazoPagamentoDto("15", "30 dias", "C1", "3", U_desconto = 5.0, U_juros = 2.0)

    @Test
    fun `preco com condicao aplica desconto, juros e desconto do vendedor`() {
        //100 x 0,95 x 1,02 x 0,90
        assertEquals(BigDecimal("87.21"), CondicaoPagamentoContratoService.precoComCondicao(100.0, prazo, 10.0))
    }

    @Test
    fun `sanitiza contrato legado com a condicao do pedido original`() {
        whenever(contratoService.getById(1)).thenReturn(odata(contrato(condicao = null)))
        whenever(orderService.getById(99)).thenReturn(odata(pedido(condicao = 15)))

        val resultado = service.sanitiza(1)

        assertEquals(15, resultado.U_condicaoPagamento)
        verify(contratoService).update(eq(mapOf("U_condicaoPagamento" to 15)), eq("1"))
    }

    @Test
    fun `sanitiza contrato de pedido a vista com condicao -1`() {
        whenever(contratoService.getById(1)).thenReturn(odata(contrato(condicao = null)))
        whenever(orderService.getById(99)).thenReturn(odata(pedido(condicao = -1)))

        assertEquals(-1, service.sanitiza(1).U_condicaoPagamento)
        verify(contratoService).update(eq(mapOf("U_condicaoPagamento" to -1)), eq("1"))
    }

    @Test
    fun `troca de contrato a vista usa a condicao -1 da tabela`() {
        whenever(prazoService.getByTabelaParaContrato(3)).thenReturn(listOf(PrazoPagamentoDto("-1", "A vista", "C1", "3", U_desconto = 5.0)))
        whenever(itemService.getPriceBase("NOVO", 3)).thenReturn(100.0)

        //100 x 0,95 x 0,90
        assertDoesNotThrow { service.validaPrecosDaTroca(contrato(condicao = -1), troca(precoNegociado = 85.5)) }
    }

    @Test
    fun `sanitiza nao mexe em contrato que ja tem condicao`() {
        whenever(contratoService.getById(1)).thenReturn(odata(contrato(condicao = 7)))

        assertEquals(7, service.sanitiza(1).U_condicaoPagamento)

        verifyNoInteractions(orderService)
        verify(contratoService, never()).update(any(), any())
    }

    @Test
    fun `sanitiza recusa pedido original sem condicao`() {
        whenever(contratoService.getById(1)).thenReturn(odata(contrato(condicao = null)))
        whenever(orderService.getById(99)).thenReturn(odata(pedido(condicao = null)))

        assertThrows<Exception> { service.sanitiza(1) }
        verify(contratoService, never()).update(any(), any())
    }

    @Test
    fun `troca com preco da condicao do contrato passa`() {
        prepara()
        assertDoesNotThrow { service.validaPrecosDaTroca(contrato(condicao = 15), troca(precoNegociado = 87.21)) }
    }

    @Test
    fun `troca sem o desconto da condicao e recusada`() {
        prepara()
        //100 x 0,90 so com o desconto do vendedor - o bug da tela de troca
        val erro = assertThrows<Exception> {
            service.validaPrecosDaTroca(contrato(condicao = 15), troca(precoNegociado = 90.0))
        }
        assertTrue(erro.message!!.contains("divergente"), erro.message)
    }

    @Test
    fun `troca com tabela sem a condicao do contrato e recusada`() {
        prepara()
        val erro = assertThrows<Exception> {
            service.validaPrecosDaTroca(contrato(condicao = 20), troca(precoNegociado = 87.21))
        }
        assertTrue(erro.message!!.contains("nao esta disponivel"), erro.message)
    }

    @Test
    fun `troca de contrato sem condicao e recusada`() {
        val erro = assertThrows<Exception> {
            service.validaPrecosDaTroca(contrato(condicao = null), troca(precoNegociado = 87.21))
        }
        assertTrue(erro.message!!.contains("nao possui condicao"), erro.message)
    }

    @Test
    fun `nome da condicao a vista nao consulta o SAP`() {
        assertEquals("À vista", service.nome(-1))
        verifyNoInteractions(paymentTermsService)
    }

    @Test
    fun `nome da condicao vem do cadastro de condicoes`() {
        whenever(paymentTermsService.getById(76)).thenReturn(odata(PaymentTermsTypes().also { it.PaymentTermsGroupName = "VF 3X" }))
        assertEquals("VF 3X", service.preencheNome(contrato(condicao = 76)).CondicaoPagamentoNome)
    }

    @Test
    fun `falha ao buscar o nome nao derruba o contrato`() {
        whenever(paymentTermsService.getById(76)).thenThrow(RuntimeException("SAP fora"))
        assertEquals(null, service.preencheNome(contrato(condicao = 76)).CondicaoPagamentoNome)
    }

    private fun prepara() {
        whenever(prazoService.getByTabelaParaContrato(3)).thenReturn(listOf(prazo))
        whenever(itemService.getPriceBase("NOVO", 3)).thenReturn(100.0)
    }

    private fun contrato(condicao: Int?) =
        Contrato(99, "C0001", mutableListOf(Item("A", "A", 10.0, 1.0, 10.0, 0.0, 1.0, "UN")), 1, "Cliente", 2).also {
            it.DocEntry = 1
            it.U_condicaoPagamento = condicao
        }

    private fun pedido(condicao: Int?) = OrderSales("C0001", "2026-10-08", listOf()).also {
        it.paymentGroupCode = condicao
        it.docNum = "1234"
    }

    private fun troca(precoNegociado: Double) = PedidoTroca("1", listOf(), listOf(
        Product("NOVO", "1", "3").also {
            it.PriceList = 3
            it.U_preco_negociado = precoNegociado
            it.DiscountPercent = 10.0
        }
    ))

    private fun odata(value: Any): OData {
        return OData(linkedMapOf("value" to OData().mapper.writeValueAsString(value)))
    }
}
