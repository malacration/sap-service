package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Installment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class InstallmentTests {

    @Test
    fun test(){
        val date = Date.from(GregorianCalendar(1900, 0, 1).toInstant())
        val test = Installment(date,0.0)
        Assertions.assertEquals("1900-01-01",test.dueDate)
    }
}