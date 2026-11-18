package br.andrew.sap.services.logistica

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** A tela decide os botoes de boleto por nota: EhBoleto vem de boleto.ids, que so o back conhece. */
class CarregamentoNotasStatusTest {

    private val ids = listOf("BB-RC-BOL-1199", "SC-RC-BOL-17609")

    @Test
    fun `marca como boleto so a forma que esta em boleto ids`() {
        val linhas = listOf(
            mapOf("DocEntry" to 1, "DocType" to 13, "FormaPagamento" to "BB-RC-BOL-1199"),
            mapOf("DocEntry" to 2, "DocType" to 13, "FormaPagamento" to "pix-vencimento"),
            mapOf("DocEntry" to 3, "DocType" to 13, "FormaPagamento" to null),
        )

        val r = CarregamentoService.marcaBoleto(linhas, ids)

        assertEquals(listOf(true, false, false), r.map { it["EhBoleto"] })
        assertEquals("BB-RC-BOL-1199", r[0]["FormaPagamento"])
    }

    @Test
    fun `entrega de venda futura nunca e boleto`() {
        val r = CarregamentoService.marcaBoleto(
            listOf(mapOf("DocEntry" to 9, "DocType" to 15, "FormaPagamento" to "BB-RC-BOL-1199")), ids)
        assertEquals(false, r.single()["EhBoleto"])
    }
}
