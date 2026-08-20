package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Registro de que uma acao saiu do historico de cobranca, guardado em @COB_TITULO_LOG.
 *
 * O historico e a prova do que foi combinado com o cliente e a remocao nao tem desfazer - o SAP
 * nao versiona UDT. Guardar o conteudo apagado aqui e o que permite reconstruir a linha depois de
 * uma remocao por engano; log de aplicacao nao serve pra isso (fica no console de quem rodou, gira
 * por tamanho e nao acompanha a base numa restauracao).
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaRemocaoLog(
    var Code: String? = null,
    // Code do registro em @COB_TITULO (ex.: NF-7503-1) e a linha que saiu de lá.
    var U_Registro: String? = null,
    var U_LineId: Int? = null,
    // Quem apagou e quando.
    var U_RemovidoPor: String? = null,
    var U_RemovidoPorId: String? = null,
    var U_RemovidoEm: String? = null,
    var U_RemovidoHora: String? = null,
    // Conteudo da linha apagada, campo por campo.
    var U_Autor: String? = null,
    var U_AutorId: String? = null,
    var U_Data: String? = null,
    var U_Hora: String? = null,
    var U_Status: String? = null,
    var U_Acao: String? = null,
    var U_Situacao: String? = null,
    var U_Ocorrencia: String? = null,
    var U_Observacao: String? = null,
    var U_DataPromessa: String? = null,
) {
    companion object {
        private val formatoCode = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

        /**
         * Code = registro + linha + instante. A linha só pode ser removida uma vez, então registro
         * e linha já quase bastam; o instante evita colisão com uma remoção de linha reaproveitada
         * pelo SAP e mantém o Code legível na consulta.
         */
        fun de(registro: String, linha: CobrancaHistorico, quem: String, quemId: String, agora: LocalDateTime) =
            CobrancaRemocaoLog(
                Code = "$registro-${linha.LineId}-${agora.format(formatoCode)}",
                U_Registro = registro,
                U_LineId = linha.LineId,
                U_RemovidoPor = quem,
                U_RemovidoPorId = quemId,
                U_RemovidoEm = agora.toLocalDate().toString(),
                U_RemovidoHora = agora.format(DateTimeFormatter.ofPattern("HH:mm")),
                U_Autor = linha.U_Usuario,
                U_AutorId = linha.U_UsuarioId,
                U_Data = linha.U_Data,
                U_Hora = linha.U_Hora,
                U_Status = linha.U_Status,
                U_Acao = linha.U_Acao,
                U_Situacao = linha.U_Situacao,
                U_Ocorrencia = linha.U_Ocorrencia,
                U_Observacao = linha.U_Observacao,
                U_DataPromessa = linha.U_DataPromessa,
            )

        fun de(registro: String, linha: CobrancaHistorico, quem: String, quemId: String) =
            de(registro, linha, quem, quemId, LocalDateTime.now())
    }
}
