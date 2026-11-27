package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.bank.IncomingPaymentService
import br.andrew.sap.services.bank.PaymentTermsTypesService
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.AccountsReceivableService
import br.andrew.sap.services.documents.CreditNotesService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.uzzipay.DynamicPixQrCodeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.infrastructure.odata.Parameter
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.argThat
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertDoesNotThrow
import br.andrew.sap.model.enums.Cancelled
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDate
import br.andrew.sap.model.exceptions.NotFoundException
import br.andrew.sap.model.sap.sistema.ErrorMsg
import br.andrew.sap.model.sap.sistema.SapError
import br.andrew.sap.model.sap.sistema.SapMessage
import com.fasterxml.jackson.core.JsonProcessingException
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import br.andrew.sap.model.sap.sistema.Session
import org.junit.jupiter.api.Assertions.assertFalse
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

/**
 * O adiantamento (boleto) do contrato sai com a condicao e a forma do pedido de origem - as mesmas
 * da minuta. Antes o SAP preenchia com as do cadastro do cliente (contrato 713: pedido "60" na
 * conta 1199, boleto "150" na conta 126455).
 */
class DownPaymentServiceCondicaoFormaTest {

    private val orderService = mock(OrdersService::class.java)
    private val sql = mock(SqlQueriesService::class.java)
    private val service = novoService("FORMA-CONFIG")

    private fun novoService(formaConfig: String) = spy(DownPaymentService(
        mock(SapEnvrioment::class.java),
        sql,
        orderService,
        mock(PaymentTermsTypesService::class.java),
        mock(BankPlusService::class.java),
        mock(AccountsReceivableService::class.java),
        mock(IncomingPaymentService::class.java),
        mock(CreditNotesService::class.java),
        mock(BatchService::class.java),
        mock(JournalEntriesService::class.java),
        mock(DynamicPixQrCodeService::class.java),
        mock(BusinessPartnersService::class.java),
        "VF-ITEM",
        66,
        formaConfig,
        mock(RestTemplate::class.java),
        mock(AuthService::class.java)
    ))

    private val contrato = Contrato(99, "C0001", mutableListOf(Item("A", "A", 10.0, 1.0, 10.0, 0.0, 1.0, "UN")), 1, "Cliente", 2)
        .also { it.DocEntry = 713 }
    private val parcela = PaymentDueDates(BigDecimal("100.00"), LocalDate.of(2027, 1, 16))

    private fun pedido(condicao: Int?, forma: String?) =
        OrderSales("C0001", "2026-11-16", listOf()).also { it.paymentGroupCode = condicao; it.paymentMethod = forma }

    @Test
    fun `usa condicao e forma do pedido`() {
        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(13, "BB-RC-BOL-1199"))

        assertEquals(13, adiantamento.paymentGroupCode)
        assertEquals("BB-RC-BOL-1199", adiantamento.paymentMethod)
    }

    @Test
    fun `pedido sem forma cai na forma configurada`() {
        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(13, " "))

        assertEquals("FORMA-CONFIG", adiantamento.paymentMethod)
    }

    @Test
    fun `sem pedido informado busca o pedido do contrato - troca`() {
        val odata = OData(linkedMapOf("value" to OData().mapper.writeValueAsString(pedido(13, "BB-RC-BOL-1199"))))
        whenever(orderService.getById(99)).doReturn(odata)

        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela)

        assertEquals(13, adiantamento.paymentGroupCode)
        assertEquals("BB-RC-BOL-1199", adiantamento.paymentMethod)
    }

    @Test
    fun `pedido sem condicao usa a do contrato, nao a do cadastro do cliente`() {
        contrato.U_condicaoPagamento = 13

        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(null, "BB-RC-BOL-1199"))

        assertEquals(13, adiantamento.paymentGroupCode)
    }

    @Test
    fun `ultimo boleto ignora o devolvido - estorno de PIX expirado nao desliga do contrato`() {
        fun boleto(entry: Int, vencimento: String) =
            Document("C0001", vencimento, listOf(), "2").also { it.docEntry = entry }
        doReturn(listOf(boleto(2, "2027-03-10T00:00:00Z"), boleto(1, "2027-01-16T00:00:00Z")))
            .whenever(service).getByContratoVendaFutura(eq(713), any())
        val devolvido = OData(linkedMapOf("value" to """[{"DocEntry":900}]"""))
        whenever(sql.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenAnswer { if ((it.arguments[1] as Parameter).value == 2) devolvido else null }

        assertEquals("2027-01-16", service.getLastInstallment(713))
    }

    private fun boleto(entry: Int, vencimento: String, cancelado: Boolean = false) =
        Document("C0001", vencimento, listOf(), "2").also {
            it.docEntry = entry
            it.Cancelled = if (cancelado) Cancelled.tYES else Cancelled.tNO
        }

    private fun stubBoletos(vararg boletos: Document) =
        doReturn(boletos.toList()).whenever(service).getByContratoVendaFutura(eq(713), any())

    private fun stubDevolvidos(vararg entries: Int) {
        val devolvido = OData(linkedMapOf("value" to """[{"DocEntry":900}]"""))
        whenever(sql.execute(eq("devolucao-adiantamento.sql"), any<Parameter>()))
            .thenAnswer { if ((it.arguments[1] as Parameter).value in entries.toList()) devolvido else null }
    }

    @Test
    fun `ultimo boleto ignora o cancelado sem consultar devolucao`() {
        stubBoletos(boleto(3, "2027-05-10T00:00:00Z", cancelado = true), boleto(1, "2027-01-16T00:00:00Z"))
        stubDevolvidos()

        assertEquals("2027-01-16", service.getLastInstallment(713))
        verify(sql, times(0)).execute(eq("devolucao-adiantamento.sql"), argThat<Parameter> { key == "docEntry" && value == 3 })
        verify(sql, times(1)).execute(eq("devolucao-adiantamento.sql"), argThat<Parameter> { key == "docEntry" && value == 1 })
    }

    @Test
    fun `todos cancelados - sem ultimo boleto`() {
        stubBoletos(boleto(3, "2027-05-10T00:00:00Z", cancelado = true), boleto(1, "2027-01-16T00:00:00Z", cancelado = true))
        stubDevolvidos()

        assertNull(service.getLastInstallment(713))
        verify(sql, never()).execute(eq("devolucao-adiantamento.sql"), any<Parameter>())
    }

    @Test
    fun `cancelado, devolvido e valido - usa o valido`() {
        stubBoletos(
            boleto(3, "2027-05-10T00:00:00Z", cancelado = true),
            boleto(2, "2027-03-10T00:00:00Z"),
            boleto(1, "2027-01-16T00:00:00Z"))
        stubDevolvidos(2)

        assertEquals("2027-01-16", service.getLastInstallment(713))
        verify(sql, never()).execute(eq("devolucao-adiantamento.sql"), argThat<Parameter> { key == "docEntry" && value == 3 })
    }

    @Test
    fun `GET do pedido falha - usa condicao do contrato e forma configurada`() {
        contrato.U_condicaoPagamento = 21
        //4xx do SAP convertido por getById; checked para o Mockito (getById sem @Throws), dai o thenAnswer
        whenever(orderService.getById(99)).thenAnswer { throw NotFoundException(SapError(ErrorMsg("-2028", SapMessage("pt-br", "SAP 404")))) }

        val adiantamento = assertDoesNotThrow { service.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        assertEquals(21, adiantamento.paymentGroupCode)
        assertEquals("FORMA-CONFIG", adiantamento.paymentMethod)
    }

    @Test
    fun `GET do pedido falha e forma configurada none - forma nula`() {
        contrato.U_condicaoPagamento = 21
        //5xx / I-O no RestTemplate
        whenever(orderService.getById(99)).thenThrow(ResourceAccessException("SAP fora do ar"))

        val adiantamento = assertDoesNotThrow { novoService("none").adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        assertEquals(21, adiantamento.paymentGroupCode)
        assertNull(adiantamento.paymentMethod)
    }

    @Test
    fun `GET do pedido com 5xx - usa condicao do contrato e forma configurada`() {
        contrato.U_condicaoPagamento = 21
        whenever(orderService.getById(99)).thenThrow(HttpServerErrorException(HttpStatus.BAD_GATEWAY))

        val adiantamento = assertDoesNotThrow { service.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        assertEquals(21, adiantamento.paymentGroupCode)
        assertEquals("FORMA-CONFIG", adiantamento.paymentMethod)
    }

    @Test
    fun `GET do pedido com erro generico propaga - sem fallback`() {
        contrato.U_condicaoPagamento = 21
        val erro = RuntimeException("bug inesperado")
        whenever(orderService.getById(99)).thenThrow(erro)

        val lancado = assertThrows<RuntimeException> { service.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        assertSame(erro, lancado)
    }

    @Test
    fun `GET do pedido com resposta malformada propaga - sem fallback`() {
        contrato.U_condicaoPagamento = 21
        whenever(orderService.getById(99)).doReturn(OData(linkedMapOf("value" to "{nao e json")))

        assertThrows<JsonProcessingException> { service.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }
    }

    @Test
    fun `GET do pedido com RestClientException generica propaga - sem fallback`() {
        contrato.U_condicaoPagamento = 21
        //ex.: RestTemplate nao conseguiu extrair o corpo de um 200 - nao e erro HTTP nem I/O
        val erro = RestClientException("could not extract response")
        whenever(orderService.getById(99)).thenThrow(erro)

        val lancado = assertThrows<RestClientException> { service.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        assertSame(erro, lancado)
    }

    @Test
    fun `SAP responde 200 com JSON malformado - falha propaga pelo OrdersService real`() {
        contrato.U_condicaoPagamento = 21
        //caminho real: OrdersService.getById -> EntitiesService.exchangeWithValidSession -> RestTemplate.
        //MockRestServiceServer substitui o request factory: nenhuma requisicao sai da JVM.
        val restTemplate = RestTemplate()
        val server = MockRestServiceServer.bindTo(restTemplate).build()
        val auth = mock(AuthService::class.java)
        whenever(auth.executeWithValidSession<Any>(any(), any())).thenAnswer {
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as (Session) -> Any).invoke(Session("sess-1", "1000", 30))
        }
        val env = SapEnvrioment("http://sap.test", "user", "password", "companyDB")
        val ordersReal = OrdersService(mock(SqlQueriesService::class.java), env, restTemplate, auth)
        server.expect(requestTo(env.host + ordersReal.path() + "(99)"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("{\"DocEntry\": 99, \"PaymentGroupCode\": ", MediaType.APPLICATION_JSON))
        val serviceReal = DownPaymentService(
            env, sql, ordersReal,
            mock(PaymentTermsTypesService::class.java), mock(BankPlusService::class.java),
            mock(AccountsReceivableService::class.java), mock(IncomingPaymentService::class.java),
            mock(CreditNotesService::class.java), mock(BatchService::class.java),
            mock(JournalEntriesService::class.java), mock(DynamicPixQrCodeService::class.java),
            mock(BusinessPartnersService::class.java),
            "VF-ITEM", 66, "FORMA-CONFIG", restTemplate, auth)

        val lancado = assertThrows<RestClientException> { serviceReal.adiantamentosVendaFuturaWithoutSave(contrato, parcela) }

        //nao e erro HTTP nem I/O: por isso buscaPedidoOrigem nao cai no fallback
        assertFalse(lancado is RestClientResponseException)
        assertFalse(lancado is ResourceAccessException)
        server.verify()
    }

    @Test
    fun `pedido informado nao busca o pedido no SAP`() {
        whenever(orderService.getById(99)).thenThrow(RuntimeException("nao deveria ser chamado"))

        val adiantamento = service.adiantamentosVendaFuturaWithoutSave(contrato, parcela, pedido(13, "BB-RC-BOL-1199"))

        assertEquals(13, adiantamento.paymentGroupCode)
        assertEquals("BB-RC-BOL-1199", adiantamento.paymentMethod)
        verify(orderService, never()).getById(any<Int>())
        verify(orderService, never()).getById(any<String>())
    }
}
