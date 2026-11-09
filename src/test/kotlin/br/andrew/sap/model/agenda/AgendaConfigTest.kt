package br.andrew.sap.model.agenda

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime

class AgendaConfigTest {

    // 2026-10-26 e segunda-feira
    private val segunda7h = LocalDateTime.of(2026, 10, 26, 7, 0)
    private val padrao = AgendaConfig()

    @Test
    fun `padrao reproduz o cron antigo - segunda as 7h`() {
        assertTrue(padrao.deveEnviar(segunda7h))
        assertFalse(padrao.deveEnviar(segunda7h.minusMinutes(1)))
        assertFalse(padrao.deveEnviar(segunda7h.plusDays(1)))
    }

    @Test
    fun `reinicio logo depois da hora ainda envia, mas fora da janela nao`() {
        assertTrue(padrao.deveEnviar(segunda7h.plusMinutes(59)))
        assertFalse(padrao.deveEnviar(segunda7h.plusMinutes(60)))
        assertFalse(padrao.deveEnviar(segunda7h.withHour(15)))
    }

    @Test
    fun `envia uma vez por dia`() {
        val enviado = padrao.copy(ultimoEnvio = segunda7h)
        assertFalse(enviado.deveEnviar(segunda7h.plusMinutes(1)))
        assertTrue(enviado.copy(ultimoEnvio = segunda7h.minusDays(7)).deveEnviar(segunda7h))
    }

    @Test
    fun `desligado nunca envia e varios dias valem`() {
        assertFalse(padrao.copy(ativo = false).deveEnviar(segunda7h))
        val segEQui = padrao.copy(dias = setOf(1, 4), hora = LocalTime.of(8, 30))
        assertTrue(segEQui.deveEnviar(segunda7h.withHour(8).withMinute(30)))
        assertTrue(segEQui.deveEnviar(segunda7h.plusDays(3).withHour(8).withMinute(31)))
        assertFalse(segEQui.deveEnviar(segunda7h.plusDays(2).withHour(8).withMinute(30)))
    }

    @Test
    fun `ida e volta pela entidade do SAP preserva a agenda`() {
        val c = AgendaConfig(false, setOf(4, 1), LocalTime.of(9, 5), listOf("a@x.com", "b@x.com"), segunda7h.plusNanos(123_000_000), "ok")
        val e = c.paraEntidade("INADIMPLENCIA")
        assertEquals("1,4", e.U_Dias)
        assertEquals("N", e.U_Ativo)
        assertEquals("09:05", e.U_Hora)
        assertTrue(e.U_UltimoEnvio!!.length <= 25, "campo U_UltimoEnvio tem 25 posicoes")
        assertEquals(c.copy(ultimoEnvio = segunda7h), AgendaConfig.de(e))
    }

    @Test
    fun `entidade vazia vira a agenda padrao`() {
        assertEquals(AgendaConfig(), AgendaConfig.de(AgendaRelatorio(U_Ativo = "Y", U_UltimoEnvio = "", U_Hora = "", U_Dias = "1")))
    }

    @Test
    fun `validacao recusa dia, hora e e-mail invalidos`() {
        assertNull(AgendaConfig.validar(listOf(1, 4), "07:00", listOf("a@x.com")))
        assertNotNull(AgendaConfig.validar(listOf(), "07:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(8), "07:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "7h", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "25:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "07:00", listOf("sem-arroba")))
        assertNotNull(AgendaConfig.validar(listOf(1), "07:00", listOf("a@x.com b@x.com")))
        assertNotNull(AgendaConfig.validar(listOf(1), "07:00", List(30) { "pessoa$it@empresa.com.br" }))
    }

    @Test
    fun `hora com segundos ou fora de HH-mm e recusada e depois das 23h tambem`() {
        assertNull(AgendaConfig.validar(listOf(1), "07:00", listOf()))
        assertNull(AgendaConfig.validar(listOf(1), "23:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "07:00:30", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "7:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "24:00", listOf()))
        assertNotNull(AgendaConfig.validar(listOf(1), "23:30", listOf()))
    }

    @Test
    fun `PATCH parcial do estado nao carrega a configuracao`() {
        val json = com.fasterxml.jackson.databind.ObjectMapper()
            .writeValueAsString(AgendaRelatorio(U_Ativo = null, U_UltimoEnvio = "2026-10-26T07:00:00", U_UltimoStatus = "ok"))
        assertTrue(json.contains("U_UltimoEnvio") && json.contains("U_UltimoStatus"))
        listOf("U_Ativo", "U_Dias", "U_Hora", "U_Copia").forEach { assertFalse(json.contains(it), "$it nao devia ir") }
    }
}
