package br.andrew.sap.model.journal

import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Recebimento por conta (ex.: JdtNum 1507641, filial 4) nao tem NF para herdar a dimensao, e o SAP
 * recusa o PATCH em conta de resultado sem rateio: o lancamento voltava a cada rodada do job.
 */
class PreencheCentroCustoPadraoDaFilialTest {

    private val padrao = mapOf("4" to DistribuicaoCustoByBranch("4", "501", "50100403"))

    private fun linha(accountCode: String, filial: Int, costingCode: String? = null, costingCode2: String? = null) =
        JournalEntryLines(accountCode, 0.0, 100.0, filial, costingCode, costingCode2)

    @Test
    fun `preenche conta de resultado sem dimensao e preserva as contas de balanco`() {
        val balanco = linha("1.1.2.001.00999", 4)
        val resultado = linha("3.1.3.001.00003", 4).also { it.Line_ID = 1 }
        val lancamento = JournalEntry(listOf(balanco, resultado), "Recebimento")

        val preenchidos = lancamento.preencheCentroCustoPadraoDaFilial(padrao)

        assertEquals(listOf("linha 1 grupo ->501", "linha 1 centro ->50100403"), preenchidos)
        assertEquals("501", resultado.costingCode)
        assertEquals("50100403", resultado.costingCode2)
        assertNull(balanco.costingCode)
        assertNull(balanco.costingCode2)
    }

    @Test
    fun `preenche cada dimensao em separado`() {
        val resultado = linha("3.1.3.001.00003", 4, "502", "")
        val lancamento = JournalEntry(listOf(resultado), "Recebimento")

        lancamento.preencheCentroCustoPadraoDaFilial(padrao)

        assertEquals("502", resultado.costingCode)
        assertEquals("50100403", resultado.costingCode2)
    }

    @Test
    fun `nao mexe em dimensao ja preenchida`() {
        val resultado = linha("3.1.3.001.00003", 4, "502", "50200403")
        val lancamento = JournalEntry(listOf(resultado), "Recebimento")

        assertTrue(lancamento.preencheCentroCustoPadraoDaFilial(padrao).isEmpty())
        assertEquals("502", resultado.costingCode)
        assertEquals("50200403", resultado.costingCode2)
    }

    @Test
    fun `conta mal formada em outra linha nao impede o preenchimento`() {
        val resultado = linha("3.1.3.001.00003", 4)
        val lancamento = JournalEntry(listOf(linha("", 4), resultado), "Recebimento")

        lancamento.preencheCentroCustoPadraoDaFilial(padrao)

        assertEquals("501", resultado.costingCode)
        assertEquals("50100403", resultado.costingCode2)
    }

    @Test
    fun `conta mal formada antes da conta de resultado nao esconde a linha de resultado`() {
        val lancamento = JournalEntry(listOf(linha("", 4), linha("3.1.3.001.00003", 4)), "Recebimento")

        assertTrue(lancamento.hasContaResultado())
    }

    @Test
    fun `nao altera filial sem padrao configurado`() {
        val resultado = linha("3.1.3.001.00003", 9)
        val lancamento = JournalEntry(listOf(resultado), "Recebimento")

        assertTrue(lancamento.preencheCentroCustoPadraoDaFilial(padrao).isEmpty())
        assertNull(resultado.costingCode)
    }
}
