package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.CreditNotes
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DocumentTotalLiquidoTest {

    private fun documento() = CreditNotes("C001", "2026-10-01", listOf(Product("ITEM", "1", "200")), "1")

    @Test
    fun `desconto percentual de cabecalho reduz o total liquido, nao o bruto`() {
        val doc = documento().also { it.discountPercent = 50.0 }
        assertEquals(100.0, doc.totalLiquido())
        //total() continua bruto: aplicaDescontoDesonerado calcula o percentual em cima dele
        assertEquals(200.0, doc.total())
    }

    @Test
    fun `desconto em valor vale quando nao ha percentual`() {
        val doc = documento().also { it.totalDiscount = "30" }
        assertEquals(170.0, doc.totalLiquido())
    }

    @Test
    fun `frete nao entra no desconto de cabecalho`() {
        val doc = documento().also {
            it.discountPercent = 50.0
            it.documentAdditionalExpenses.add(AdditionalExpenses.frete(40.0))
        }
        assertEquals(140.0, doc.totalLiquido())
    }

    @Test
    fun `sem desconto o liquido e igual ao bruto`() {
        val doc = documento()
        assertEquals(doc.total(), doc.totalLiquido())
    }
}
