package br.andrew.sap.model.cobranca

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class CobrancaDominio(
    var Code: String? = null,
    var U_Tipo: String? = null,
    var U_Codigo: String? = null,
    var U_Descricao: String? = null,
    var U_Ordem: Int? = null,
    var U_Ativo: String? = "Y"
) {
    companion object {
        fun code(tipo: String, codigo: String) = "$tipo-$codigo"
    }

    @get:JsonIgnore
    val rotulo: String
        get() = "$U_Codigo - $U_Descricao"
}
