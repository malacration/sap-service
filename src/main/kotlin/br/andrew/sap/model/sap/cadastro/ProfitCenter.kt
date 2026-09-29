package br.andrew.sap.model.sap.cadastro

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ProfitCenter(
    val CenterCode: String,
    val CenterName: String?,
    val InWhichDimension: Int?,
    val Active: YesNo?
) {

    @JsonIgnore
    fun isGrupoEconomico(): Boolean {
        return InWhichDimension == DIMENSAO_GRUPO_ECONOMICO
    }

    @JsonIgnore
    fun isCentroCusto(): Boolean {
        return InWhichDimension == DIMENSAO_CENTRO_CUSTO
    }

    companion object {
        const val DIMENSAO_GRUPO_ECONOMICO = 1
        const val DIMENSAO_CENTRO_CUSTO = 2
    }
}
