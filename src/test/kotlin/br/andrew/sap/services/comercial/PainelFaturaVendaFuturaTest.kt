package br.andrew.sap.services.comercial

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class PainelFaturaVendaFuturaTest {

    private fun row(baseLine: Int, item: String, openQty: String, retirada: String?, ordemAberta: Int?) = mapOf(
        "DocEntry" to 256990, "BaseLine" to baseLine, "CardCode" to "CLI0006303", "SlpCode" to 7,
        "ItemCode" to item, "OpenQty" to openQty, "OnHand" to "95",
        "Retirada" to retirada, "OrdemAberta" to ordemAberta,
    )

    private val rows = listOf(
        row(0, "PAC0000108", "20", "5", 11),
        row(0, "PAC0000108", "20", "10", null),
        row(1, "PAC0000174", "180", null, null),
    )

    @Test
    fun `linha vira reserva por ordem aberta mais saldo livre`() {
        val linhas = PainelIntegradoVendasService.linhasDeFatura(rows)
            .associate { (it.ItemCode to it.EmOrdemDeCarregamento) to it.Quantity }

        assertEquals(mapOf(
            ("PAC0000108" to "11") to BigDecimal("5"),
            ("PAC0000108" to null) to BigDecimal("15"),
            ("PAC0000174" to null) to BigDecimal("180"),
        ), linhas)
    }

    @Test
    fun `agrupado por item soma o estoque uma vez por deposito`() {
        val linhas = listOf(
            row(0, "PAC0000108", "20", null, null) + ("WhsCode" to "500.01") + ("OnHand" to "10"),
            row(1, "PAC0000108", "5", null, null) + ("WhsCode" to "500.01") + ("OnHand" to "10"),
            row(2, "PAC0000108", "7", null, null) + ("WhsCode" to "600.01") + ("OnHand" to "50"),
        )
        val agrupado = PainelIntegradoVendasService.agrupar(PainelIntegradoVendasService.linhasDeFatura(linhas), "item").single()

        assertEquals(BigDecimal("60"), agrupado.OnHand)
        assertEquals(BigDecimal("32"), agrupado.Quantity)
    }

    @Test
    fun `agrupado por cliente soma por item e ordem`() {
        val agrupado = PainelIntegradoVendasService.agrupar(
            PainelIntegradoVendasService.linhasDeFatura(rows + row(2, "PAC0000174", "20", null, null)), "cliente")
            .associate { (it.ItemCode to it.EmOrdemDeCarregamento) to it.Quantity }

        assertEquals(BigDecimal("200"), agrupado["PAC0000174" to null])
        assertEquals(3, agrupado.size)
    }
}
