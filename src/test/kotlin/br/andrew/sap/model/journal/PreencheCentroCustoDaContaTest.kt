package br.andrew.sap.model.journal

import JournalEntry
import JournalEntryLines
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Regra: linha na conta de apuracao dos custos de producao (4.9.1.001.00001) ou na de desvio
 * de producao (4.2.1.001.00007), na filial 2 e sem centro de custo, recebe 500/50000205.
 * A 4.2.1.001.00007 exige regra de reparticao: enquanto a dimensao fica vazia o SAP recusa
 * qualquer update no lancamento. O SAP devolve a dimensao vazia ora como "" ora como null, e
 * as demais linhas do lancamento nao podem ser tocadas.
 */
class PreencheCentroCustoDaContaTest {

    private val contas = listOf("4.9.1.001.00001", "4.2.1.001.00007")

    private fun linha(accountCode: String, filial: Int, costingCode: String? = null, costingCode2: String? = null) =
        JournalEntryLines(accountCode, 0.0, 100.0, filial, costingCode, costingCode2)

    @Test
    fun `preenche apenas a linha das contas alvo e deixa as outras intactas`() {
        val outra = linha("1.1.3.007.00300", 2)
        val lancamento = JournalEntry(listOf(linha("4.9.1.001.00001", 2), outra), "Apuracao")

        assertTrue(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))

        assertEquals("500", lancamento.journalEntryLines[0].costingCode)
        assertEquals("50000205", lancamento.journalEntryLines[0].costingCode2)
        assertEquals(null, outra.costingCode)
        assertEquals(null, outra.costingCode2)
    }

    @Test
    fun `preenche a conta de desvio de producao que bloqueia o update no sap`() {
        val lancamento = JournalEntry(listOf(linha("4.2.1.001.00007", 2)), "Ordem de producao")

        assertTrue(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))
        assertEquals("500", lancamento.journalEntryLines[0].costingCode)
        assertEquals("50000205", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `trata dimensao vazia vinda do sap como ausente`() {
        val lancamento = JournalEntry(listOf(linha("4.9.1.001.00001", 2, "", "")), "Apuracao")

        assertTrue(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))
        assertEquals("500", lancamento.journalEntryLines[0].costingCode)
    }

    @Test
    fun `preenche so a dimensao vazia e preserva a que ja tinha valor`() {
        val lancamento = JournalEntry(listOf(linha("4.9.1.001.00001", 2, "F98", "")), "Apuracao")

        assertTrue(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))
        assertEquals("F98", lancamento.journalEntryLines[0].costingCode)
        assertEquals("50000205", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `nao sobrescreve centro de custo ja preenchido`() {
        val lancamento = JournalEntry(listOf(linha("4.9.1.001.00001", 2, "F98", "FAZ00304")), "Apuracao")

        assertFalse(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))
        assertEquals("F98", lancamento.journalEntryLines[0].costingCode)
        assertEquals("FAZ00304", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `ignora as contas alvo em outra filial`() {
        val lancamento = JournalEntry(listOf(linha("4.9.1.001.00001", 9)), "Apuracao")

        assertFalse(lancamento.preencheCentroCustoDasContas(contas, 2, "500", "50000205"))
        assertEquals(null, lancamento.journalEntryLines[0].costingCode)
    }
}
