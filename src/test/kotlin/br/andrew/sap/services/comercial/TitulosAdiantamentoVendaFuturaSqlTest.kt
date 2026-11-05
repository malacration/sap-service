package br.andrew.sap.services.comercial

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

class TitulosAdiantamentoVendaFuturaSqlTest {

    private val arquivo = "src/main/resources/odbc/titulos-inadimplencia-vendedor.sql"
    private val sql = Files.readString(Path.of(arquivo)).replace(Regex("\\s+"), " ")
    private val ramos = sql.split("UNION ALL")

    @Test
    fun `tem um braco de nota e um de adiantamento, e o adiantamento so de contrato de venda futura`() {
        assertEquals(2, ramos.size)
        assertTrue(ramos[0].contains("FROM OINV NS"))
        assertTrue(ramos[1].contains("FROM ODPI T0"))
        assertTrue(ramos[1].contains("INNER JOIN \"@AR_CONTRATO_FUTURO\" VF ON VF.\"DocEntry\" = T0.\"U_venda_futura\""))
        // na nota o contrato so informa o numero: LEFT JOIN, nunca corta a nota sem contrato
        assertTrue(ramos[0].contains("LEFT JOIN \"@AR_CONTRATO_FUTURO\" VF ON VF.\"DocEntry\" = NS.\"U_venda_futura\""))
        assertFalse(ramos[0].contains("INNER JOIN \"@AR_CONTRATO_FUTURO\""))
    }

    @Test
    fun `os dois bracos cortam igual - parcela aberta, vencida, do vendedor e nas mesmas filiais`() {
        ramos.forEach { ramo ->
            listOf("P.\"Status\" = 'O'", "P.\"DueDate\" <= :data", "V.\"SlpCode\" = :slpCode", "\"BPLId\" IN (2,4,11,17,18,12)", "P.\"InsTotal\" > 0")
                .forEach { assertTrue(ramo.contains(it), "braco perdeu: $it") }
        }
    }

    @Test
    fun `parcela negativa, como a NF de bonificacao, nao e divida`() {
        assertFalse(sql.contains("<> 0"))
    }

    @Test
    fun `adiantamento cancelado ou ja encerrado nao entra`() {
        assertTrue(ramos[1].contains("T0.\"CANCELED\" = 'N'"))
        assertTrue(ramos[1].contains("T0.\"DocStatus\" = 'O'"))
        // adiantamento de reconhecimento de devolucao (U_TX_DocEntryRef) nao e cobranca
        assertTrue(ramos[1].contains("IFNULL(T0.\"U_TX_DocEntryRef\", 0) = 0"))
    }

    @Test
    fun `junta a UDT de cobranca pelo Tipo de cada braco`() {
        assertTrue(ramos[0].contains("C.\"U_Tipo\" = 'NF'"))
        assertTrue(ramos[1].contains("C.\"U_Tipo\" = 'AD'"))
    }

    @Test
    fun `o que viaja ao sap-odbc nao leva comentario`() {
        assertFalse(SqlResource.carregar("odbc/titulos-inadimplencia-vendedor.sql").contains("--"))
    }

    @Test
    fun `junta nota e adiantamento, converte o retorno do odbc e ordena por cliente`() {
        val odbc = mock<OdbcClient>()
        whenever(odbc.consultar(any(), any(), eq(ParcelasAbertoService.MAX_LINHAS))).thenReturn(
            QueryResponse(rows = listOf(
                linha("CLI2", "NF", 506, 1),
                linha("CLI1", "AD", 77, 2, pago = "30"),
                linha("CLI1", "NF", 400, 1),
            ))
        )

        val parcelas = ParcelasAbertoService(odbc).getAllBySql(151)

        assertEquals(listOf("CLI1-400-1", "CLI1-77-2", "CLI2-506-1"), parcelas.map { "${it.CardCode}-${it.Serial}-${it.InstlmntID}" })
        val ad = parcelas[1]
        assertEquals("Fatura", ad.tipo)
        assertEquals("NF", parcelas[0].tipo)
        assertEquals(BigDecimal("70.00"), ad.saldo)
        assertEquals("02/03/2026", ad.getdueDateFormated())

        val params = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(any(), params.capture(), any())
        assertEquals(151, params.firstValue["slpCode"])
        assertTrue(params.firstValue["data"].toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }

    @Test
    fun `ordena pelo vencimento, o mais vencido primeiro, e traz o numero do contrato`() {
        val odbc = mock<OdbcClient>()
        whenever(odbc.consultar(any(), any(), eq(ParcelasAbertoService.MAX_LINHAS))).thenReturn(
            QueryResponse(rows = listOf(
                linha("CLI1", "NF", 1, 1, venc = "2026-10-20"),
                linha("CLI9", "AD", 2, 1, venc = "2026-09-01", contrato = 55),
                linha("CLI5", "NF", 3, 1, venc = "2026-10-05"),
            ))
        )

        val parcelas = ParcelasAbertoService(odbc).getAllBySql(151)

        assertEquals(listOf("20260901", "20261005", "20261020"), parcelas.map { it.DueDate })
        assertEquals("55", parcelas[0].contrato)
        assertEquals(null, parcelas[1].contrato)
    }

    private fun linha(cliente: String, tipo: String, serial: Int, parcela: Int, pago: String = "0", venc: String = "2026-03-02", contrato: Int? = null) = mapOf<String, Any?>(
        "BPLName" to "Filial", "CardCode" to cliente, "CardName" to "Nome", "SlpName" to "Vend",
        "Tipo" to tipo, "Serial" to serial, "InstlmntID" to parcela, "InsTotal" to "100.00",
        "DueDate" to venc + "T00:00", "Contrato" to contrato, "DocTotal" to "100.00", "PaidToDate" to pago,
        "U_StatusCobranca" to null, "U_AgenteCobrador" to null,
    )
}
