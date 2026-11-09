package br.andrew.sap.model.agenda

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Agenda de envio de um relatorio por e-mail, guardada no SAP como UDO `AGENDA_RELATORIO`
 * (/b1s/v1/AGENDA_RELATORIO). `Code` identifica o relatorio (hoje so INADIMPLENCIA).
 * Tudo e alfanumerico no SAP: numerico sem size vira SMALLINT e a lista de dias/e-mails nao cabe em numero.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class AgendaRelatorio(
    var Code: String? = null,
    var Name: String? = null,
    var U_Ativo: String? = "Y",
    var U_Dias: String? = null,
    var U_Hora: String? = null,
    var U_Copia: String? = null,
    var U_UltimoEnvio: String? = null,
    var U_UltimoStatus: String? = null,
)

/** Agenda ja interpretada. `dias` segue ISO: 1 = segunda ... 7 = domingo. */
data class AgendaConfig(
    val ativo: Boolean = true,
    val dias: Set<Int> = setOf(1),
    val hora: LocalTime = LocalTime.of(7, 0),
    val copia: List<String> = listOf(),
    val ultimoEnvio: LocalDateTime? = null,
    val ultimoStatus: String? = null,
) {

    /**
     * Dispara no dia marcado, a partir da hora e ate [JANELA_MINUTOS] depois, uma vez por dia. A janela
     * existe para um reinicio do servidor as 7h01 nao perder o envio, sem mandar relatorio da manha as 15h.
     */
    fun deveEnviar(agora: LocalDateTime): Boolean {
        if (!ativo || agora.dayOfWeek.value !in dias) return false
        val inicio = agora.toLocalDate().atTime(hora)
        if (agora.isBefore(inicio) || !agora.isBefore(inicio.plusMinutes(JANELA_MINUTOS))) return false
        return ultimoEnvio?.toLocalDate() != agora.toLocalDate()
    }

    fun paraEntidade(codigo: String) = AgendaRelatorio(
        Code = codigo,
        Name = codigo,
        U_Ativo = if (ativo) "Y" else "N",
        U_Dias = dias.sorted().joinToString(","),
        U_Hora = hora.toString(),
        U_Copia = copia.joinToString(","),
        U_UltimoEnvio = ultimoEnvio?.truncatedTo(ChronoUnit.SECONDS)?.toString() ?: "",
        U_UltimoStatus = ultimoStatus ?: "",
    )

    companion object {
        const val JANELA_MINUTOS = 60L
        private val HORA = Regex("^([01]\\d|2[0-3]):[0-5]\\d$")
        private val ULTIMA_HORA = LocalTime.of(23, 0)
        private val EMAIL = Regex("^[^@\\s,;]+@[^@\\s,;]+\\.[^@\\s,;]+$")

        fun emailValido(email: String) = EMAIL.matches(email.trim())

        fun de(e: AgendaRelatorio) = AgendaConfig(
            ativo = e.U_Ativo != "N",
            dias = e.U_Dias.orEmpty().split(",").mapNotNull { it.trim().toIntOrNull() }.toSet(),
            hora = e.U_Hora?.takeIf { it.isNotBlank() }?.let(LocalTime::parse) ?: LocalTime.of(7, 0),
            copia = e.U_Copia.orEmpty().split(",").map { it.trim() }.filter { it.isNotEmpty() },
            ultimoEnvio = e.U_UltimoEnvio?.takeIf { it.isNotBlank() }?.let(LocalDateTime::parse),
            ultimoStatus = e.U_UltimoStatus?.takeIf { it.isNotBlank() },
        )

        /** Valida o que o usuario digitou; devolve a mensagem de erro ou null se estiver tudo certo. */
        fun validar(dias: List<Int>, hora: String, copia: List<String>): String? {
            if (dias.isEmpty() || dias.any { it !in 1..7 }) return "Escolha ao menos um dia da semana."
            // U_Hora tem 5 posicoes: "07:00:30" passaria no parse e estouraria no SAP
            if (!HORA.matches(hora)) return "Hora invalida, use HH:mm."
            // a janela de 60 minutos nao atravessa a meia-noite (o dia do envio e o da hora marcada)
            if (LocalTime.parse(hora) > ULTIMA_HORA) return "Use uma hora ate 23:00: a janela de envio nao passa da meia-noite."
            copia.firstOrNull { !EMAIL.matches(it.trim()) }?.let { return "E-mail invalido em copia: $it" }
            // U_Copia tem 254 posicoes no SAP
            if (copia.joinToString(",") { it.trim() }.length > 254) return "Lista de e-mails em copia grande demais (maximo 254 caracteres)."
            return null
        }
    }
}
