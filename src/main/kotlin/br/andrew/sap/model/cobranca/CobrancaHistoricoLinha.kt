package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Linha do historico como a TELA ve. Existe separada de [CobrancaHistorico] porque essa vai e
 * volta do SAP: qualquer campo nosso a mais no JSON faz o Service Layer recusar o PATCH com
 * "Property is invalid".
 *
 * PodeRemover e decidido aqui, no backend, e nao comparando usuario no Angular: com login via
 * Keycloak o token da tela nao carrega o User.id do SAP (o `jti` e um id de token qualquer e o
 * `sub` e o UUID do Keycloak), entao a tela nao tem como reproduzir a regra de autoria.
 */
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class CobrancaHistoricoLinha(
    val LineId: Int?,
    val U_Data: String,
    val U_Hora: String?,
    val U_Usuario: String,
    val U_Cobrador: String,
    val U_Status: String?,
    val U_Acao: String?,
    val U_Situacao: String?,
    val U_Ocorrencia: String?,
    val U_Observacao: String?,
    val U_DataPromessa: String?,
    val PodeRemover: Boolean,
) {
    companion object {
        fun de(linha: CobrancaHistorico, podeRemover: Boolean) = CobrancaHistoricoLinha(
            LineId = linha.LineId,
            U_Data = linha.U_Data,
            U_Hora = linha.U_Hora,
            U_Usuario = linha.U_Usuario,
            U_Cobrador = linha.U_Cobrador,
            U_Status = linha.U_Status,
            U_Acao = linha.U_Acao,
            U_Situacao = linha.U_Situacao,
            U_Ocorrencia = linha.U_Ocorrencia,
            U_Observacao = linha.U_Observacao,
            U_DataPromessa = linha.U_DataPromessa,
            PodeRemover = podeRemover,
        )
    }
}
