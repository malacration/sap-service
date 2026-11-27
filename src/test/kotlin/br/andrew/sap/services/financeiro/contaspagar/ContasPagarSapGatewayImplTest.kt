package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

/** Dados ficticios: CNPJs, CardCodes e valores nao correspondem a registros reais. */
class ContasPagarSapGatewayImplTest {

    private val odbc = mock<OdbcClient>()
    private val gateway = ContasPagarSapGatewayImpl(mock<BussinessPlaceService>(), odbc)

    private fun responde(vararg linhas: Map<String, Any?>) {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = linhas.toList(), rowCount = linhas.size))
    }

    @Test
    fun `fornecedor vai ao sap-odbc com o SQL do arquivo e so o CNPJ como bind`() {
        responde(mapOf("CardCode" to "FOR001"), mapOf("CardCode" to "FOR001"), mapOf("CardCode" to " "))
        assertEquals(listOf("FOR001"), gateway.fornecedoresPorCnpj("12345678000190"))
        val params = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(eq(SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_FORNECEDOR)), params.capture(), eq(1000))
        assertEquals(mapOf("cnpj" to "12345678000190"), params.firstValue)
        assertThrows<IllegalArgumentException> { gateway.fornecedoresPorCnpj("12.345.678/0001-90") }
    }

    @Test
    fun `parcelas leem decimal como texto sem perder precisao e trazem as moedas`() {
        responde(mapOf("InstlmntID" to 2, "DueDate" to "2026-11-10", "InsTotal" to "1234.5678", "PaidToDate" to "0",
            "Status" to "O", "DocCur" to "R$", "MainCurncy" to "R$ ", "WTSum" to "12.3456", "RetencaoPagamento" to "12.3456"))
        val p = gateway.parcelas(10).single()
        assertEquals(ParcelaNotaEntradaSap(2, "2026-11-10", BigDecimal("1234.5678"), BigDecimal("0"), "O", "R$", "R$",
            BigDecimal("12.3456"), BigDecimal("12.3456")), p)
        verify(odbc).consultar(eq(SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_PARCELAS)), eq(mapOf("docEntry" to 10)), eq(1000))
    }

    @Test
    fun `valor ilegivel ou ausente vira null, que nunca conta como pago`() {
        responde(mapOf("InstlmntID" to "1.5", "InsTotal" to "abc", "PaidToDate" to null, "DocCur" to ""))
        val p = gateway.parcelas(10).single()
        assertNull(p.instlmntId)
        assertNull(p.insTotal)
        assertNull(p.paidToDate)
        assertNull(p.moedaDocumento)
        assertNull(p.moedaLocal)
        assertNull(p.wtSum)
        assertNull(p.retencaoPagamentoDocumento, "sem linha P em PCH5 a subconsulta devolve null")
    }

    @Test
    fun `pagamentos filtram a parcela no SQL`() {
        responde(mapOf("DocEntry" to 500, "DocNum" to 9500, "DocDate" to "2026-11-10", "InstId" to 1, "SumApplied" to "100.1",
            "WtAppld" to "10.01"))
        assertEquals(listOf(PagamentoNotaEntradaSap(500, 9500, "2026-11-10", 1, BigDecimal("100.1"), BigDecimal("10.01"))),
            gateway.pagamentos(10, 1))
        verify(odbc).consultar(eq(SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_PAGAMENTOS)),
            eq(mapOf("docEntry" to 10, "parcela" to 1)), eq(1000))
    }

    @Test
    fun `falha do sap-odbc propaga, nao vira lista vazia`() {
        whenever(odbc.consultar(any(), any(), any())).thenThrow(OdbcException("resultado_truncado", "truncado"))
        assertThrows<OdbcException> { gateway.parcelas(10) }
        assertThrows<OdbcException> { gateway.pagamentos(10, 1) }
    }

    private fun linhaNota(docEntry: Int, filial: Int, cardCode: String, chave: String? = null) = mapOf(
        "DocEntry" to docEntry, "DocNum" to docEntry + 1000, "Serial" to 123, "SeriesStr" to " 1 ", "CardCode" to cardCode,
        "BPLId" to filial, "DocDate" to "2026-10-01", "CANCELED" to "N", "Model" to "39", "U_ChaveAcesso" to chave,
    )

    @Test
    fun `notas por numero - uma consulta por fornecedor, so numero e CardCode como bind, filial filtrada aqui`() {
        whenever(odbc.consultar(any(), eq(mapOf("numero" to 123, "cardCode" to "FOR001")), any()))
            .thenReturn(QueryResponse(rows = listOf(linhaNota(10, 2, "FOR001"), linhaNota(11, 9, "FOR001")), rowCount = 2))
        whenever(odbc.consultar(any(), eq(mapOf("numero" to 123, "cardCode" to "FOR002")), any()))
            .thenReturn(QueryResponse(rows = listOf(linhaNota(12, 4, "FOR002")), rowCount = 1))
        val notas = gateway.notasEntrada(123, listOf(2, 4), listOf("FOR001", "FOR002", "FOR001"))
        assertEquals(listOf(10, 12), notas.map { it.docEntry })
        assertEquals(NotaEntradaSap(10, 1010, 123, "1", "FOR001", 2, "2026-10-01", "N", "39", null), notas.first())
        verify(odbc).consultar(eq(SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_NOTAS_NUMERO)),
            eq(mapOf("numero" to 123, "cardCode" to "FOR001")), eq(50))
    }

    @Test
    fun `notas por chave - so a chave como bind e filial e fornecedor filtrados aqui`() {
        val chave = "1".repeat(44)
        responde(linhaNota(10, 2, "FOR001", chave), linhaNota(11, 2, "OUTRO", chave), linhaNota(12, 7, "FOR001", chave))
        val notas = gateway.notasPorChave(chave, listOf(2), listOf("FOR001"))
        assertEquals(listOf(10), notas.map { it.docEntry })
        assertEquals(chave, notas.single().chaveAcesso)
        verify(odbc).consultar(eq(SqlResource.carregar(ContasPagarSapGatewayImpl.SQL_NOTAS_CHAVE)), eq(mapOf("chave" to chave)), eq(50))
        assertThrows<IllegalArgumentException> { gateway.notasPorChave("1' OR '1", listOf(2), listOf("FOR001")) }
    }

    @Test
    fun `busca de nota exige filial e fornecedor`() {
        assertThrows<IllegalArgumentException> { gateway.notasEntrada(1, listOf(), listOf("F")) }
        assertThrows<IllegalArgumentException> { gateway.notasEntrada(1, listOf(1), listOf()) }
        assertThrows<IllegalArgumentException> { gateway.notasPorChave("1", listOf(), listOf("F")) }
    }

    @Test
    fun `filiais devolve so as que tem CNPJ de 14 digitos, sem mascara e com situacao`() {
        val places = mock<BussinessPlaceService>()
        fun place(id: Int?, cnpj: String?, disabled: String? = "tNO") = br.andrew.sap.model.sap.cadastro.BussinessPlace().apply {
            BPLID = id; BPLName = " Filial $id "; FederalTaxID = cnpj; Disabled = disabled
        }
        whenever(places.getAllBusinessPlaces()).thenReturn(listOf(
            place(3, "12.345.678/0001-90", "tYES"), place(1, "98765432000100"), place(2, null), place(null, "11111111000111"), place(4, "123"),
        ))
        assertEquals(
            listOf(FilialSap(1, "Filial 1", "98765432000100", true), FilialSap(3, "Filial 3", "12345678000190", false)),
            ContasPagarSapGatewayImpl(places, odbc).filiais(),
        )
    }
}
