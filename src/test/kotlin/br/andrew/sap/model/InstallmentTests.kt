package br.andrew.sap.model

import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.model.sap.documents.base.PixControleData
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
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

    @Test
    fun pixValido_deveSerVerdadeiro_quandoDataComHoraZ() {
        val installment = Installment(LocalDate.now().plusDays(20), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "3026-02-20T00:00:00Z"
        }
        assertTrue(installment.isPixValido())
    }

    private fun noFuso(ano: Int, mes: Int, dia: Int, hora: Int, minuto: Int): OffsetDateTime =
        LocalDateTime.of(ano, mes, dia, hora, minuto).atZone(PixControleData.ZONA).toOffsetDateTime()

    @Test
    fun sanitizarControleConsultaPix_deveLimparUltimaConsultaERegistrarLimite() {
        val referencia = noFuso(2026, 3, 19, 8, 0)
        val installment = Installment(LocalDate.of(2026, 3, 20), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "2026-03-20"
            it.U_pix_proxima_consulta_em = "2026-03-19T10:00:00"
        }

        installment.sanitizarControleConsultaPix(referencia)

        val proximaEsperada = PixControleData.formatar(referencia.truncatedTo(ChronoUnit.MINUTES))
        val limiteEsperado = PixControleData.formatar(
            LocalDate.of(2026, 3, 20).atTime(LocalTime.MAX).atZone(PixControleData.ZONA).toOffsetDateTime()
        )
        assertEquals(proximaEsperada, installment.U_pix_proxima_consulta_em)
        assertEquals(limiteEsperado, installment.U_pix_consultar_ate)
        // o limite (consultar_ate) carrega offset explicito
        assertTrue(installment.U_pix_consultar_ate!!.startsWith("2026-03-20T23:59:59"))
    }

    @Test
    fun podeConsultarPixPagamento_deveRespeitarIntervaloEDataLimite() {
        val installment = Installment(LocalDate.of(2026, 3, 20), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_due_date = "2026-03-20"
            it.sanitizarControleConsultaPix(noFuso(2026, 3, 19, 8, 0))
            it.U_pix_proxima_consulta_em = PixControleData.formatar(noFuso(2026, 3, 19, 10, 15))
        }

        assertFalse(installment.podeConsultarPixPagamento(noFuso(2026, 3, 19, 10, 10)))
        assertTrue(installment.podeConsultarPixPagamento(noFuso(2026, 3, 19, 10, 15)))
        assertFalse(installment.podeConsultarPixPagamento(noFuso(2026, 3, 21, 0, 0)))
    }

    @Test
    fun jurosSimples_deveSerZero_quandoNaoEstaEmAtraso() {
        val installment = Installment(LocalDate.of(2026, 2, 20), 1000.0)
        val juros = installment.calcularJurosSimplesPorDia(0.17, LocalDate.of(2026, 2, 20))
        assertEquals(0.0, juros)
    }

    @Test
    fun jurosSimples_deveCalcularPorDiasReais_comArredondamentoParaBaixo() {
        val installment = Installment(LocalDate.of(2026, 1, 27), 2149.80)
        assertEquals(8, installment.diasAtraso(LocalDate.of(2026, 2, 4)))
        val juros = installment.calcularJurosSimplesPorDia(0.166666, LocalDate.of(2026, 2, 4))
        assertEquals(28.66, juros)
    }

    @Test
    fun isPixValido_usaConsultarAte_eToleraValorLegadoNaive() {
        // valor legado sem offset (formato antigo ja persistido no SAP)
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_consultar_ate = LocalDate.now().atTime(LocalTime.MAX).toString()
        }
        assertTrue(installment.isPixValido())
    }

    @Test
    fun isPixValido_deveSerFalso_quandoConsultarAteJaPassou() {
        val installment = Installment(LocalDate.now(), 0.0).also {
            it.U_pix_reference = "ref-123"
            it.U_pix_consultar_ate = PixControleData.formatar(noFuso(2020, 1, 1, 0, 0))
        }
        assertFalse(installment.isPixValido())
    }

    @Test
    fun consultarAte_deveSerTimezoneAware_aposGeracao() {
        val installment = Installment(LocalDate.of(2026, 3, 20), 0.0).also {
            it.U_pix_due_date = "2026-03-20"
            it.sanitizarControleConsultaPix(noFuso(2026, 3, 19, 8, 0))
        }
        // valor deve ser um OffsetDateTime valido (offset explicito) e fazer round-trip no formato
        val valor = installment.U_pix_consultar_ate!!
        assertEquals(valor, PixControleData.formatar(OffsetDateTime.parse(valor)))
    }
}
