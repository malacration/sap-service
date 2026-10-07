package br.andrew.sap.services.journal.importacao

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Uma importacao de lancamentos, em @LC_IMPORTACAO. Vai no mesmo changeset dos lancamentos: a
 * linha existe se, e somente se, a importacao entrou. Isso responde "quem importou" (no SAP o
 * UserSign do lancamento e a conta de servico) e e o que impede reimportar o mesmo arquivo.
 *
 * Classe comum, nao data class: o SapGenericException poe toString() na mensagem que vai para o
 * navegador e para o Telegram.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ImportacaoLancamentoLog(
    var Code: String? = null,
    var Name: String? = null,
    var U_Usuario: String? = null,
    var U_UsuarioId: String? = null,
    var U_Data: String? = null,
    var U_Hora: String? = null,
    var U_Arquivo: String? = null,
    var U_Hash: String? = null,
    var U_Linhas: Int? = null,
    var U_Total: BigDecimal? = null,
    var U_Forcado: String? = null,
    //preenchido depois do commit, por PATCH de melhor esforco: o numero so existe na resposta
    var U_Lancamentos: String? = null,
) {
    companion object {
        private val formatoCode = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

        /** Code = instante + sufixo aleatorio: ordena por data no historico e nao colide. */
        fun novo(
            usuario: String, usuarioId: String, arquivo: String?, hash: String,
            linhas: Int, total: BigDecimal, forcado: Boolean, agora: LocalDateTime = LocalDateTime.now(),
        ): ImportacaoLancamentoLog {
            val code = "${agora.format(formatoCode)}-${UUID.randomUUID().toString().take(8)}"
            return ImportacaoLancamentoLog(
                Code = code,
                Name = code,
                U_Usuario = usuario.take(100),
                U_UsuarioId = usuarioId.take(100),
                U_Data = agora.toLocalDate().toString(),
                U_Hora = agora.format(DateTimeFormatter.ofPattern("HH:mm")),
                U_Arquivo = arquivo?.take(254),
                U_Hash = hash,
                U_Linhas = linhas,
                U_Total = total,
                U_Forcado = if (forcado) "Y" else "N",
            )
        }
    }
}
