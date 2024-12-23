package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Installment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class InstallmentTests {

    @Test
    fun test() {
        val date = Date.from(GregorianCalendar(1900, 0, 1).toInstant())
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val test = Installment(date, 0.0)
        assertEquals("1900-01-01", test.dueDate.toString())
    }
}
