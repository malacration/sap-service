import br.andrew.sap.model.uzzipay.Payer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception

class JournalEntriesServiceTest {

    @Test
    fun `Deve filtrar linhas válidas corretamente`() {
        val journalEntry = JournalEntry(
            listOf(
                JournalEntryLines("4.1.1.001.00001", 0.0, 0.0, 1),
                JournalEntryLines("5.1.1.001.00001", 0.0, 0.0, 1),
                JournalEntryLines("2.1.1.001.00001", 0.0, 0.0, 1),
                JournalEntryLines("6.1.1.001.00003", 0.0, 0.0, 1),
                JournalEntryLines("1.1.1.001.00001", 0.0, 0.0, 1, "costingCode"),
            ),
            "memo"
        )

        val validLines = journalEntry.journalEntryLines.filter {
            it.isContaResultado() && it.costingCode.isNullOrEmpty() && it.costingCode2.isNullOrEmpty()
        }

        assertEquals(2, validLines.size)
    }

    @Test
    fun `string vazia`() {
        val erro = assertThrows<Exception> {
            assertEquals(false, JournalEntryLines("", 0.0, 0.0, 1)
                .isContaResultado().toString())
        }
    }

    @Test
    fun `string comeca com espaco`() {
        assertEquals(true, JournalEntryLines(" 3.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }

    @Test
    fun `deve ser falso ativo`() {
        assertEquals(false, JournalEntryLines("1.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }

    @Test
    fun `deve ser falso pasivo`() {
        assertEquals(false, JournalEntryLines("2.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }

    @Test
    fun `deve ser true resultado`() {
        assertEquals(true, JournalEntryLines("3.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }

    @Test
    fun `deve ser true despesa`() {
        assertEquals(true, JournalEntryLines("4.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }

    @Test
    fun `deve ser true custo`() {
        assertEquals(true, JournalEntryLines("5.1.1.001.00001", 0.0, 0.0, 1)
            .isContaResultado())
    }
}