package br.andrew.sap.model.journal

import JournalEntry
import JournalEntryLines
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Lancamentos antigos apontam para regras de reparticao que depois foram inativadas no SAP.
 * O Service Layer recusa QUALQUER update nesses lancamentos, mesmo um PATCH que so mexe no UDF
 * de controle - reproduzido no JdtNum 2310:
 * "-5002 540000088 - Inactive distribution rule: 50000102".
 * Nos casos reais (2310 e 3802) o grupo economico seguia valido e so o centro de custo tinha
 * sido inativado, por isso cada dimensao e avaliada em separado.
 */
class SubstituiRateioInativoTest {

    private val inativosGrupo = setOf("Centr_z")
    private val inativosCentro = setOf("50000102", "50000105", "Centr_z2")
    private val padrao = mapOf("2" to DistribuicaoCustoByBranch("2", "500", "50000205"))

    private fun linha(accountCode: String, filial: Int, costingCode: String?, costingCode2: String?) =
        JournalEntryLines(accountCode, 0.0, 100.0, filial, costingCode, costingCode2)

    @Test
    fun `troca so o centro inativo e preserva o grupo valido`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.1.001.00005", 2, "500", "50000102")),
            "REF REEMBOLSO CX"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isNotEmpty())
        assertEquals("500", lancamento.journalEntryLines[0].costingCode)
        assertEquals("50000205", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `descreve a troca com valor antigo e novo para o log`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.1.001.00005", 2, "500", "50000102").also { it.Line_ID = 1 }),
            "REF REEMBOLSO CX"
        )

        val trocas = lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao)

        assertEquals(listOf("linha 1 centro 50000102->50000205"), trocas)
    }

    @Test
    fun `troca so o grupo inativo e preserva o centro valido`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.1.001.00005", 2, "Centr_z", "50000202")),
            "Lancamento"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isNotEmpty())
        assertEquals("500", lancamento.journalEntryLines[0].costingCode)
        assertEquals("50000202", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `mexe apenas na linha com rateio inativo`() {
        val valida = linha("1.1.1.002.00001", 2, "500", "50000202")
        val lancamento = JournalEntry(
            listOf(valida, linha("1.1.1.001.00005", 2, "500", "50000102")),
            "REF REEMBOLSO CX"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isNotEmpty())
        assertEquals("50000202", valida.costingCode2)
        assertEquals("50000205", lancamento.journalEntryLines[1].costingCode2)
    }

    @Test
    fun `nao altera quando o rateio esta ativo`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.1.002.00001", 2, "500", "50000202")),
            "Lancamento"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isEmpty())
        assertEquals("50000202", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `nao altera filial sem padrao configurado`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.1.001.00005", 9, "500", "50000102")),
            "Lancamento"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isEmpty())
        assertEquals("50000102", lancamento.journalEntryLines[0].costingCode2)
    }

    @Test
    fun `linha sem rateio fica para a regra das contas`() {
        val lancamento = JournalEntry(
            listOf(linha("1.1.3.004.00300", 2, "", "")),
            "Saida de mercadorias"
        )

        assertTrue(lancamento.substituiRateioInativo(inativosGrupo, inativosCentro, padrao).isEmpty())
        assertEquals("", lancamento.journalEntryLines[0].costingCode2)
    }
}
