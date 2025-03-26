package br.andrew.sap.model

import br.andrew.sap.model.payment.HandlePaymentTermsLines
import br.andrew.sap.model.payment.PaymentDueDates
import br.andrew.sap.model.payment.PaymentTermsLines
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.Exception

class HandlePaymentTermsLinesTestes {


    @Test
    fun paymentTermsEmpty(){
        val inputDate = LocalDate.of(2024, 8, 9)
        val expectedDate = LocalDate.of(2024, 8, 9)
        val resultado = HandlePaymentTermsLines(listOf()).calculaVencimentos("100.0",inputDate)
        Assertions.assertEquals(1, resultado.size)
        Assertions.assertEquals(expectedDate, resultado[0].dueDate)
        Assertions.assertEquals(BigDecimal("100.00"), resultado[0].value)
    }
    @Test
    fun parcelamentoFeliz(){
        val lista = listOf(
            PaymentTermsLines(1,0,10,"100.0"),
        )
        val inputDate = LocalDate.of(2024, 8, 9)
        val expectedDate = LocalDate.of(2024, 8, 19)
        val resultado = HandlePaymentTermsLines(lista).calculaVencimentos("100.0",inputDate)
        Assertions.assertEquals(1, resultado.size)
        Assertions.assertEquals(expectedDate, resultado[0].dueDate)
    }

    @Test
    fun `test calculaVencimentos with multiple payment terms`() {
        val total = 1000.0
        val dataInicial = LocalDate.of(2024, 8, 9)
        val paymentTerms = listOf(
            PaymentTermsLines(CTGCode = 1, InstMonth = 0, InstDays = 10, "50.0"),
            PaymentTermsLines(CTGCode = 2, InstMonth = 1, InstDays = 10, InstPrcnt = "25.0"),
            PaymentTermsLines(CTGCode = 3, InstMonth = 2, InstDays = 10, InstPrcnt = "25.0")
        )

        // Datas de vencimento esperadas
        val expectedDueDates = listOf(
            PaymentDueDates("500.00", LocalDate.of(2024, 8, 19)),
            PaymentDueDates("250.00", LocalDate.of(2024, 9, 19)),
            PaymentDueDates("250.00", LocalDate.of(2024, 10, 19))
        )

        // Execução do método
        val actualDueDates = HandlePaymentTermsLines(paymentTerms).calculaVencimentos(total.toString(), dataInicial)

        // Verificações
        Assertions.assertEquals(expectedDueDates.size, actualDueDates.size)
        expectedDueDates.forEachIndexed { index, expected ->
            val actual = actualDueDates[index]
            Assertions.assertEquals(expected.value, actual.value)
            Assertions.assertEquals(expected.dueDate, actual.dueDate)
        }
    }

    @Test
    fun `test calculaVencimentos with floating point precision issues`() {
        // Dados de entrada
        val total = 1000.0
        val dataInicial = LocalDate.of(2024, 8, 9)
        val paymentTerms = listOf(
            PaymentTermsLines(CTGCode = 1, InstMonth = 0, InstDays = 10, InstPrcnt = "33.333"),
            PaymentTermsLines(CTGCode = 2, InstMonth = 1, InstDays = 10, InstPrcnt = "33.333"),
            PaymentTermsLines(CTGCode = 3, InstMonth = 2, InstDays = 10, InstPrcnt = "33.334")
        )

        // Datas de vencimento esperadas
        val expectedDueDates = listOf(
            PaymentDueDates(333.33, LocalDate.of(2024, 8, 19)), // 10 dias após a data inicial
            PaymentDueDates(333.33, LocalDate.of(2024, 9, 19)), // 1 mês e 10 dias após a data inicial
            PaymentDueDates(333.34, LocalDate.of(2024, 10, 19)) // 2 meses e 10 dias após a data inicial
        )

        // Execução do método
        val actualDueDates = HandlePaymentTermsLines(paymentTerms).calculaVencimentos(total.toString(), dataInicial)

        // Verificações
        Assertions.assertEquals(expectedDueDates.size, actualDueDates.size)
        expectedDueDates.forEachIndexed { index, expected ->
            val actual = actualDueDates[index]
            Assertions.assertEquals(expected.value, actual.value) // Tolerância para questões de precisão
            Assertions.assertEquals(expected.dueDate, actual.dueDate)
        }
    }

    @Test
    fun `testeDaErroPqNaoSoma100%`() {
        val paymentTerms = listOf(
            PaymentTermsLines(CTGCode = 1, InstMonth = 0, InstDays = 10, InstPrcnt = "33.333"),
        )
        val erro = assertThrows<Exception> {
            HandlePaymentTermsLines(paymentTerms)
        }
    }

    @Test
    fun `test calculaVencimentos with corrected floating point percentages`() {
        // Dados de entrada
        val total = "1000.0"
        val dataInicial = LocalDate.of(2024, 8, 9)
        val paymentTerms = listOf(
            PaymentTermsLines(CTGCode = 1, InstMonth = 0, InstDays = 10, InstPrcnt = "16.666700"),
            PaymentTermsLines(CTGCode = 2, InstMonth = 1, InstDays = 10, InstPrcnt = "16.666700"),
            PaymentTermsLines(CTGCode = 3, InstMonth = 2, InstDays = 10, InstPrcnt = "16.666700"),
            PaymentTermsLines(CTGCode = 4, InstMonth = 3, InstDays = 10, InstPrcnt = "16.666700"),
            PaymentTermsLines(CTGCode = 5, InstMonth = 4, InstDays = 10, InstPrcnt = "16.666700"),
            PaymentTermsLines(CTGCode = 6, InstMonth = 5, InstDays = 10, InstPrcnt = "16.666500")
        )
        //4
        // Datas de vencimento esperadas
        val expectedDueDates = listOf(
            PaymentDueDates(166.67, LocalDate.of(2024, 8, 19)),  // 10 dias após a data inicial
            PaymentDueDates(166.67, LocalDate.of(2024, 9, 19)),  // 1 mês e 10 dias após a data inicial
            PaymentDueDates(166.67, LocalDate.of(2024, 10, 19)), // 2 meses e 10 dias após a data inicial
            PaymentDueDates(166.67, LocalDate.of(2024, 11, 19)), // 3 meses e 10 dias após a data inicial
            PaymentDueDates(166.66, LocalDate.of(2024, 12, 19)), // 4 meses e 10 dias após a data inicial
            PaymentDueDates(166.66, LocalDate.of(2025, 1, 19))   // 5 meses e 10 dias após a data inicial
        )

        // Execução do método
        val actualDueDates = HandlePaymentTermsLines(paymentTerms).calculaVencimentos(total.toString(), dataInicial)

        // Verificações
        Assertions.assertEquals(expectedDueDates.size, actualDueDates.size)
        expectedDueDates.forEachIndexed { index, expected ->
            val actual = actualDueDates[index]
            Assertions.assertEquals(expected.value, actual.value) // Tolerância para questões de precisão
            Assertions.assertEquals(expected.dueDate, actual.dueDate)
        }
    }

    @Test
    fun `testaComPrimos`() {
        val total = "177.32"
        val dataInicial = LocalDate.of(2024, 8, 9)
        val paymentTerms = listOf(
            PaymentTermsLines(1,0,0, "50.00"),
            PaymentTermsLines(1, 0,10,"50.00"),
        )
        val expectedDueDates = listOf(
            PaymentDueDates(88.66, LocalDate.of(2024, 8, 9)),
            PaymentDueDates(88.66, LocalDate.of(2024, 8, 19)),
        )

        // Execução do método
        val actualDueDates = HandlePaymentTermsLines(paymentTerms).calculaVencimentos(total.toString(), dataInicial)

        // Verificações
        Assertions.assertEquals(expectedDueDates.size, actualDueDates.size)
        expectedDueDates.forEachIndexed { index, expected ->
            val actual = actualDueDates[index]
            Assertions.assertEquals(expected.value, actual.value)
            Assertions.assertEquals(expected.dueDate, actual.dueDate)
        }
    }


    @Test
    fun paymentAdicionaCarencia(){
        val inputDate = LocalDate.of(2024, 8, 9)
        val expectedDate = LocalDate.of(2024, 8, 16)
        val resultado = HandlePaymentTermsLines(listOf()).calculaVencimentos("100.0",inputDate,7)
        Assertions.assertEquals(1, resultado.size)
        Assertions.assertEquals(expectedDate, resultado[0].dueDate)
        Assertions.assertEquals(BigDecimal("100.00"), resultado[0].value)
    }
}