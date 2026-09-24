package br.andrew.sap.model.journal

import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * No fluxo de pagamento e recebimento o rateio do lancamento e copiado da nota. O SAP devolve
 * dimensao nao preenchida ora como null ora como string vazia, e o filtro original so checava
 * CostingCode2 - uma linha com centro e sem grupo era escolhida e estourava NPE no '!!'.
 */
class CopiaRateioDaNotaTest {

    private fun nota(vararg linhas: Product) = Document("CLI001", null, linhas.toList(), "2")

    private fun produto(costingCode: String?, costingCode2: String?) =
        Product("ITEM01", "1", "10").also {
            it.CostingCode = costingCode
            it.CostingCode2 = costingCode2
        }

    private fun lancamento() = JournalEntry(
        listOf(
            JournalEntryLines("1.1.1.002.00001", 100.0, 0.0, 2),
            JournalEntryLines("4.1.1.001.00001", 0.0, 100.0, 2)
        ),
        "Pagamento"
    )

    @Test
    fun `copia o rateio da primeira linha completa da nota`() {
        val lc = lancamento()
        lc.costingCodes(nota(produto(null, null), produto("500", "50000201")))

        lc.journalEntryLines.forEach {
            assertEquals("500", it.costingCode)
            assertEquals("50000201", it.costingCode2)
        }
    }

    @Test
    fun `linha com centro mas sem grupo nao e usada como origem do rateio`() {
        assertThrows<Exception> { lancamento().costingCodes(nota(produto(null, "50000201"))) }
    }

    @Test
    fun `dimensao vazia na nota nao conta como rateio`() {
        //copiar "" marcaria o lancamento como processado sem rateio: o NON_EMPTY omite o campo
        assertThrows<Exception> { lancamento().costingCodes(nota(produto("", ""))) }
    }

    @Test
    fun `nota sem rateio nenhum continua lancando excecao`() {
        assertThrows<Exception> { lancamento().costingCodes(nota(produto(null, null))) }
    }
}
