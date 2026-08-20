package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaHistorico(
    val U_Data: String,
    val U_Usuario: String,
    val U_Cobrador: String,
    // Quem registrou, por identidade e nao por nome (User.id: SalesPersonCode ou EmployeeID).
    // Nulo nas linhas gravadas antes do campo existir - ver ehAutor em CobrancaService.
    val U_UsuarioId: String? = null,
    val U_Status: String? = null,
    val U_Acao: String? = null,
    val U_Situacao: String? = null,
    val U_Ocorrencia: String? = null,
    val U_Observacao: String? = null,
    val U_Hora: String? = null,
    // Data prometida NESTA acao (o cabecalho guarda a vigente). Nulo quando a acao nao prometeu
    // nada e nas linhas gravadas antes do campo existir.
    val U_DataPromessa: String? = null,
) {
    var LineId: Int? = null
}
