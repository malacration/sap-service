package br.andrew.sap.model.acesso

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Uma versao completa das regras de acesso, gravada no SAP como linha do UDO `ACESSO_REGRAS`
 * (/b1s/v1/ACESSO_REGRAS). A versao em vigor e a de maior `Code`.
 *
 * `Code` e o numero da versao com zeros a esquerda ("000012"). Como o Code e a chave da tabela, um
 * POST com um Code que ja existe falha: e isso que faz duas gravacoes simultaneas nao se
 * sobreporem. `U_IdEscrita` identifica cada tentativa de gravacao, para distinguir "a resposta se
 * perdeu mas a linha e nossa" de "outra pessoa gravou esta versao primeiro".
 *
 * Classe comum, com toString proprio, de proposito: o SapGenericException coloca o toString da
 * entidade na mensagem de erro, que vai para o navegador e para o Telegram - com uma data class
 * seria o documento inteiro das regras.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class AcessoRegrasVersao(
    var Code: String? = null,
    var Name: String? = null,
    var U_Documento: String? = null,
    var U_IdEscrita: String? = null,
    var U_Usuario: String? = null,
    var U_UsuarioId: String? = null,
    var U_Data: String? = null,
    var U_Hora: String? = null,
    var U_Origem: String? = null,
    var U_Comentario: String? = null,
    var U_Resumo: String? = null,
) {
    fun numeroDaVersao(): Int = Code?.toIntOrNull() ?: 0

    override fun toString(): String = "AcessoRegrasVersao(Code=$Code)"

    companion object {
        /** Numero da versao -> Code do SAP (seis digitos, para a ordem alfabetica bater com a numerica). */
        fun codigo(versao: Int): String = versao.toString().padStart(6, '0')
    }
}
