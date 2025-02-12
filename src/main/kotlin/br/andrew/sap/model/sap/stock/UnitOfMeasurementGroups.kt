package br.andrew.sap.model.sap.stock

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UnitOfMeasurementGroups {
    var AbsEntry: Int? = null
    var Code: String? = null
    var Name: String? = null
    var BaseUoM: Int? = null
    var UoMGroupDefinitionCollection: List<UoMGroupDefinition> = listOf()

}