package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Installment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

    @Test
    fun pixValido_deveSerFalso_quandoNaoTemReferencia() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_due_date = LocalDate.now().toString()
        }
        assertFalse(installment.isPixValido())
    }

    @Test
    fun pixValido_deveSerFalso_quandoNaoTemData() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
        }
        assertFalse(installment.isPixValido())
    }

    @Test
    fun pixValido_deveSerFalso_quandoDataInvalida() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "data-invalida"
        }
        assertFalse(installment.isPixValido())
    }

    @Test
    fun pixValido_deveSerFalso_quandoDataVencida() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = LocalDate.now().minusDays(1).toString()
        }
        assertFalse(installment.isPixValido())
    }

    @Test
    fun pixValido_deveSerVerdadeiro_quandoDataHoje() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = LocalDate.now().toString()
        }
        assertTrue(installment.isPixValido())
    }

    @Test
    fun pixValido_deveSerVerdadeiro_quandoDataFutura() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = LocalDate.now().plusDays(1).toString()
        }
        assertTrue(installment.isPixValido())
    }
}
