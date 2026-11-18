package br.andrew.sap.services.logistica

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

/** PIX pago do pedido: a nota da ordem abate o saldo inteiro, senao a SBO_SP_VALIDACAO_FINANCEIRA recusa. */
class CarregamentoPixPagoTest {

    private val odbc: OdbcClient = mock(OdbcClient::class.java)
    private val service = CarregamentoService(
        mock(br.andrew.sap.services.batch.BatchService::class.java),
        mock(br.andrew.sap.services.documents.OrdersService::class.java),
        mock(br.andrew.sap.services.abstracts.SqlQueriesService::class.java),
        odbc,
        mock(br.andrew.sap.services.fiscal.TaxNfeService::class.java),
        mock(br.andrew.sap.model.sistema.SapEnvrioment::class.java),
        mock(org.springframework.web.client.RestTemplate::class.java),
        mock(br.andrew.sap.services.security.AuthService::class.java),
    )

    private fun linha(pedido: Int, docEntry: Int, saldo: String) =
        mapOf("Pedido" to pedido, "DocEntry" to docEntry, "DocNum" to docEntry + 100, "Saldo" to saldo)

    @Test
    fun `agrupa os PIX por pedido e abate o saldo inteiro de cada um`() {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = listOf(
            linha(80533, 6450, "3240.000000"),
            linha(80533, 6460, "100.5"),
            linha(80421, 6470, "50"),
        )))

        val pix = service.pixPagosPorPedido(listOf(80533, 80421, 80473))

        assertEquals(setOf(80533, 80421), pix.keys)
        assertEquals(listOf(6450, 6460), pix[80533]!!.map { it.docEntry })
        assertEquals(listOf(6550, 6560), pix[80533]!!.map { it.docNum })
        assertEquals(0, BigDecimal("3240").compareTo(pix[80533]!![0].saldo))
        assertEquals(0, BigDecimal("100.5").compareTo(pix[80533]!![1].saldo))
        assertEquals(null, pix[80473])
    }

    @Test
    fun `uma consulta so para todos os pedidos da ordem`() {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse())

        service.pixPagosPorPedido(listOf(1, 2, 3))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(odbc, org.mockito.kotlin.times(1)).consultar(any(), params.capture(), any())
        assertEquals(mapOf("pedidos" to listOf(1, 2, 3)), params.firstValue)
    }

    @Test
    fun `ordem sem pedidos nao consulta o banco`() {
        assertTrue(service.pixPagosPorPedido(emptyList()).isEmpty())
        verifyNoInteractions(odbc)
    }

    @Test
    fun `abatimento vai para o SAP com DocEntry e valor bruto`() {
        val json = ObjectMapper().writeValueAsString(CarregamentoService.abatimentoDoPix(PixPagoDoPedido(6450, 6450, BigDecimal("3240.00"))))

        assertTrue(json.contains("\"DocEntry\":6450"), json)
        assertTrue(json.contains("\"GrossAmountToDraw\":3240.00"), json)
    }

    private val trava = "Pedido %s: 400 - (7) Pedido com PIX pago: a nota tem que usar o adiantamento 6450 no valor total do saldo (R$ 3240,00)."
    private val pixDoPedido = mapOf(80473 to listOf(PixPagoDoPedido(14894, 6450, BigDecimal("3240"))))
    private val descricoes = mapOf(80421 to "Pedido 80421", 80473 to "Pedido 80473", 80533 to "Pedido 80533")

    @Test
    fun `erro da trava passa a citar o pedido dono do adiantamento`() {
        val corrigido = CarregamentoService.comPedidoDoPix(trava.format(80533), pixDoPedido, descricoes)

        assertEquals(trava.format(80473), corrigido)
    }

    @Test
    fun `adiantamento sem dono conhecido perde o prefixo que nao e confiavel`() {
        val corrigido = CarregamentoService.comPedidoDoPix(trava.format(80533), emptyMap(), descricoes)

        assertEquals(trava.format(80533).removePrefix("Pedido 80533: "), corrigido)
    }

    @Test
    fun `outro erro do SAP volta como veio`() {
        val outro = "Pedido 80533: 400 - Lote insuficiente"

        assertEquals(outro, CarregamentoService.comPedidoDoPix(outro, pixDoPedido, descricoes))
    }

    @Test
    fun `a consulta segue os criterios da trava de PIX pago`() {
        val sql = Files.readString(Path.of("src/main/resources/odbc/carregamento-pix-pago.sql"))

        listOf(
            "\"U_TX_DocTypeRef\" = 17", "\"U_TX_DocEntryRef\" IN (:pedidos)", "\"CANCELED\" = 'N'",
            "\"DocStatus\" = 'C'", "NOT EXISTS", "\"BaseType\" = 203", "\"Saldo\" > 0.01",
        ).forEach { assertTrue(sql.contains(it), "faltou: $it") }
    }
}
