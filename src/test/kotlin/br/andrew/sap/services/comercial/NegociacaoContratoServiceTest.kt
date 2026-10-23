package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.payment.PaymentTermsLines
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.bank.WizardPaymentMethodService
import br.andrew.sap.services.documents.OrdersService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Dados da negociacao da minuta do contrato: forma e parcelamento saem do pedido de origem, a mesma
 * fonte que a emissao de boletos usa (createAdiantamentoBycontrato) - a minuta mostra quantos boletos vao sair.
 */
class NegociacaoContratoServiceTest {

    private val contratoService = mock<ContratoVendaFuturaService>()
    private val orderService = mock<OrdersService>()
    private val paymentTermsService = mock<PaymentTermsTypesService>()
    private val formas = mock<WizardPaymentMethodService>()
    private val condicoes = mock<CondicaoPagamentoContratoService>()
    private val service = NegociacaoContratoService(contratoService, orderService, paymentTermsService, formas, condicoes)

    private fun odata(value: Any) = OData(linkedMapOf("value" to OData().mapper.writeValueAsString(value)))

    private fun preparar(condicaoPedido: Int?, forma: String?, condicaoContrato: Int? = null) {
        val contrato = Contrato(99, "C0001", mutableListOf(Item("A", "A", 10.0, 1.0, 10.0, 0.0, 1.0, "UN")), 1, "Cliente", 2)
            .also { it.DocEntry = 1; it.U_condicaoPagamento = condicaoContrato }
        val pedido = OrderSales("C0001", "2026-10-08", listOf()).also { it.paymentGroupCode = condicaoPedido; it.paymentMethod = forma }
        whenever(contratoService.getById(1)).doReturn(odata(contrato))
        whenever(orderService.getById(99)).doReturn(odata(pedido))
    }

    private fun parcelas(n: Int) = (1..n).map { PaymentTermsLines(15, 0, 30 * it, (100.0 / n).toString()) }

    @Test
    fun `forma e parcelamento vem do pedido de origem`() {
        preparar(condicaoPedido = 15, forma = "BB-RC-BOL-1199")
        whenever(paymentTermsService.getParcelas(15)).doReturn(parcelas(3))
        whenever(condicoes.nome(15)).doReturn("30/60/90")
        whenever(formas.getById("'BB-RC-BOL-1199'")).doReturn(odata(PaymentMethod("BB-RC-BOL-1199").also { it.Description = "BOLETO" }))

        val negociacao = service.negociacao(1)

        assertEquals("BB-RC-BOL-1199", negociacao.formaPagamentoCodigo)
        assertEquals("BOLETO", negociacao.formaPagamento)
        assertEquals(15, negociacao.condicaoPagamentoCodigo)
        assertEquals("30/60/90", negociacao.condicaoPagamento)
        assertEquals(3, negociacao.quantidadeParcelas)
    }

    @Test
    fun `a vista e uma parcela sem consultar as linhas`() {
        preparar(condicaoPedido = -1, forma = "AVISTA")
        whenever(formas.getById(any<String>())).doThrow(RuntimeException("sem SL"))

        val negociacao = service.negociacao(1)

        assertEquals(1, negociacao.quantidadeParcelas)
        //sem descricao, mostra o codigo em vez de deixar vazio
        assertEquals("AVISTA", negociacao.formaPagamento)
    }

    @Test
    fun `condicao sem linhas conta como pagamento unico`() {
        preparar(condicaoPedido = 8, forma = null)
        whenever(paymentTermsService.getParcelas(8)).doReturn(listOf())

        val negociacao = service.negociacao(1)

        assertEquals(1, negociacao.quantidadeParcelas)
        assertEquals(null, negociacao.formaPagamento)
    }

    /** A emissao dos boletos exige a condicao do pedido; a minuta nao usa a do contrato no lugar dela. */
    @Test
    fun `pedido sem condicao fica sem parcelamento, mesmo com condicao no contrato`() {
        preparar(condicaoPedido = null, forma = "AVISTA", condicaoContrato = 15)

        val negociacao = service.negociacao(1)

        assertEquals(null, negociacao.quantidadeParcelas)
        assertEquals(null, negociacao.condicaoPagamentoCodigo)
        org.mockito.kotlin.verifyNoInteractions(paymentTermsService)
    }
}
