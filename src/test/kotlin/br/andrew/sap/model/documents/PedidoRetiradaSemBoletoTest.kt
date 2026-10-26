package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.ItemRetirada
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Retirada lancada antes de os boletos do contrato existirem. Quem barra e o faturamento
 * (SBO_SP_VALIDACAO_VENDA_FUTURA no sap-sql); a cotacao sai normalmente.
 */
class PedidoRetiradaSemBoletoTest {

    private fun contrato(): Contrato {
        val item = Item("A", "", 10.0, 100.0, 10.0, 0.0, 0.0, "KG")
        item.LineId = 0
        return Contrato(1, "C0001", mutableListOf(item), 1, "", 2, 0.0).also { it.DocEntry = 161 }
    }

    private fun cotacao(numerosBoletos: List<String>, vencimento: String = "2026-10-20") =
        PedidoRetirada(161, listOf(ItemRetirada("A", 10.0, 0)))
            .parse(contrato(), 17, vencimento, Document("C0001", null, listOf(Product("A", "1", "1", 0)), "2"), numerosBoletos)

    @Test
    fun `sem boleto a observacao cita so o contrato, sem 'nao encontrados'`() {
        val cotacao = cotacao(listOf())

        assertEquals(
            "Faturamento referente a entrega de mercadorias do contrato Nº 161, com referencia aos boletos do contrato.",
            cotacao.ClosingRemarks,
        )
        assertEquals("2026-10-20", cotacao.DocDueDate)
        assertEquals(1, cotacao.U_entrega_vf)
        assertEquals(161, cotacao.U_venda_futura)
    }

    @Test
    fun `com boleto a observacao lista os numeros`() {
        assertEquals(
            "Faturamento referente a entrega de mercadorias do contrato Nº 161 com referencia aos boletos numero 123, 456.",
            cotacao(listOf("123", " ", "456", "123")).ClosingRemarks,
        )
    }
}
