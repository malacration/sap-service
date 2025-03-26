package br.andrew.sap.model.sap.partner

import br.andrew.sap.model.sap.DocEntry
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OrdemCarregamento(){
    var DocEntry: Int? = null
    var U_ordemName: String? = null
    var U_docEntryPedido: Int? = null
    var U_docNumPedido: Int? = null
    var U_codItemPedido: String? = null
    var U_nameItemPedido: String? = null
    var U_quantidadePedido: Int? = null
    var U_unidadeMedidaPedido: String? = null
    var U_codRegiao: Int? = null
    var U_nomeRegiao: String? = null
    var U_pesoPedido: Double? = null
}