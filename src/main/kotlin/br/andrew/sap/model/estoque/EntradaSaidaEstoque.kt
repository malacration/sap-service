package br.andrew.sap.model.estoque

import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class EntradaSaidaEstoque(private val BPLId : String, val DocumentLines : List<Produto>, val JournalMemo : String? = null) {

    constructor(BPLId : String, produto : Produto, JournalMemo: String? = null) : this(BPLId, listOf(produto),JournalMemo)

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPLId
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Produto(val ItemCode: String, val Quantity : String, val WarehouseCode : String, val UnitPrice : String? = null){

    var AccountCode: String? = null

    //TODO um dia refatorar o atribuidor de centro de custo, ele deve conter outra dimensao chamada "AREA"
    var CostingCode: String? = "500"
    var CostingCode2: String? = "50000205"
    var BatchNumbers: List<BatchStock> = listOf()
}


