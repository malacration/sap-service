package br.andrew.sap.model.dto

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ContratoDto(
    @JsonProperty("AR_CONTRATO_FUTURO")
    val contrato : Contrato,
    @JsonProperty("Orders")
    val order: OrderSales,
    val SalesPersons : SalePerson,
) {


    companion object{
        fun getJoins(filter : Filter) : Filter {
            return Filter(
                Predicate("AR_CONTRATO_FUTURO/U_orderDocEntry","Orders/DocEntry",Condicao.EQUAL,true),
                Predicate("AR_CONTRATO_FUTURO/U_vendedor","SalesPersons/SalesEmployeeCode",Condicao.EQUAL, true),
                //TODO filter abaixo é do agreggation
//                Predicate("AR_CONTRATO_FUTURO/DocEntry","AR_CONTRATO_FUTURO/AR_CF_LINHACollection/DocEntry",Condicao.EQUAL, true),
            ).addAll(filter.propertie)
        }
    }
}