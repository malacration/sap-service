import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JournalEntryServiceTest {

    private val allJournalEntryLines = listOf(
        JournalEntryLines("4.1.1.001.00001", 0.0, 0.0, 1), // Despesa (resultado)
        JournalEntryLines("5.1.1.001.00001", 0.0, 0.0, 1), // Custo (resultado)
        JournalEntryLines("2.1.1.001.00001", 0.0, 0.0, 1), // Passivo
        JournalEntryLines("6.1.1.001.00003", 0.0, 0.0, 1), // Não resultado
        JournalEntryLines("1.1.1.001.00001", 0.0, 0.0, 1, "costingCode") // Ativo com costingCode
    )

    @Test
    fun `Deve retornar true quando houver pelo menos uma linha de conta resultado`() {
        val journalEntry = JournalEntry(allJournalEntryLines, "memo")
        assertEquals(true, journalEntry.hasContaResultado())
    }

    @Test
    fun `Deve retornar false quando não houver nenhuma linha de conta resultado`() {
        val journalEntry = JournalEntry(
            allJournalEntryLines.filter { it.AccountCode.startsWith("1") || it.AccountCode.startsWith("2") },
            "memo"
        )
        assertEquals(false, journalEntry.hasContaResultado())
    }

    @Test
    fun `Deve retornar true quando houver múltiplas linhas de conta resultado`() {
        val journalEntry = JournalEntry(
            allJournalEntryLines.filter { it.AccountCode.startsWith("3") || it.AccountCode.startsWith("4") || it.AccountCode.startsWith("5") },
            "memo"
        )
        assertEquals(true, journalEntry.hasContaResultado())
    }
}
