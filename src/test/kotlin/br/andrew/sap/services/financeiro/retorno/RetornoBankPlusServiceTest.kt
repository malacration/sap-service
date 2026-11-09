package br.andrew.sap.services.financeiro.retorno

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class RetornoBankPlusServiceTest {

    private val odbc: OdbcClient = mock(OdbcClient::class.java)
    private val service = RetornoBankPlusService(odbc)

    //formato real do sap-odbc: inteiro como Integer/Long, decimal como texto, data com hora
    private fun linha(nossoNumero: String, face: String, divergencia: String) = mapOf(
        "Retorno" to 4388, "Arquivo" to "CBR643.C3545862.2025112700011033.020950.RET", "DataArquivo" to "2025-11-27T00:00",
        "StatusRetorno" to 3, "Contrato" to 4, "Convenio" to "3545862", "Filial" to 4L,
        "NomeFilial" to "SUSTENNUTRI - Filial - AC", "ContaDesconto" to "2.1.3.001.07901", "ContaIof" to "5.1.1.012.00009",
        "ContaJuros" to "5.1.1.012.00006", "ContaClientes" to "1.1.2.001.00999", "Rateio" to "501", "Rateio2" to "50100403",
        "Lcm" to null, "LcmBankPlus" to 1253756, "MesmoArquivo" to null,
        "NossoNumero" to nossoNumero, "Documento" to "5673", "Cliente" to "CLI0002470", "NomeCliente" to "CLIENTE",
        "DataOcorrencia" to "2025-11-26T00:00", "Vencimento" to null,
        "Face" to face, "Iof" to "79.09", "Juros" to "242.07", "Divergencia" to divergencia,
    )

    @Test
    fun montaORetornoComOsBoletos() {
        whenever(odbc.consultar(any(), eq(mapOf("retorno" to 4388)), eq(5000))).thenReturn(QueryResponse(rows = listOf(
            linha("35458620000002643", "12876.39", ""),
            linha("35458620000003421", "1788.49", "Boleto com status Liquidado não pode ser processado"),
        )))

        val retorno = service.buscar(4388)!!

        assertTrue(retorno.processado)
        assertEquals(4, retorno.filial)
        assertEquals(1253756, retorno.lcmBankPlus)
        assertNull(retorno.lcm)
        assertEquals("501", retorno.costingCode)
        assertEquals(LocalDate.of(2025, 11, 27), retorno.dataArquivo)
        assertEquals(listOf(BigDecimal("12876.39"), BigDecimal("1788.49")), retorno.titulos.map { it.valorFace })
        assertEquals(LocalDate.of(2025, 11, 26), retorno.titulos[0].dataOcorrencia)
        //Divergence vazia vem como "" do banco: sem divergencia
        assertNull(retorno.titulos[0].divergencia)
        assertFalse(retorno.titulos[0].jaPago)
        assertTrue(retorno.titulos[1].jaPago)
    }

    @Test
    fun retornoSemOcorrenciaDeDescontoDevolveNull() {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse())
        assertNull(service.buscar(1))
    }

    @Test
    fun listaComBindDasDatasESqlSemComentario() {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = listOf(mapOf(
            "Retorno" to 4730, "Arquivo" to "CBR643.C3629394.2026022000015201.021643.RET", "DataArquivo" to "2026-02-20T00:00",
            "Contrato" to 10, "Convenio" to "3629394", "Filial" to 17, "NomeFilial" to "Cacoal", "Boletos" to 1L, "JaPagos" to 0,
            "Face" to "32851.51", "Iof" to "24.24", "Juros" to "182.33", "Lcm" to null, "LcmBankPlus" to 1305044,
        ))))

        val lista = service.listar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))

        assertEquals(1305044, lista.single().lcmBankPlus)
        assertEquals(BigDecimal("32851.51"), lista.single().totalFace)
        val sql = argumentCaptor<String>()
        val params = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(sql.capture(), params.capture(), any())
        assertEquals(mapOf("de" to "2026-02-01", "ate" to "2026-02-28"), params.firstValue)
        //o validador do sap-odbc recusa instrucao com comentario
        assertFalse(sql.firstValue.contains("--"))
        assertTrue(sql.firstValue.contains("BETWEEN :de AND :ate"))
    }
}
