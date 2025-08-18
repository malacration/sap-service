package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Produto(
    val ItemCode : String,
    @get:JsonIgnore
    val ItemName : String,
    @get:JsonIgnore
    val InventoryUOM : String,
    @get:JsonIgnore
    var ItemWarehouseInfoCollection : List<Deposito>
    ) {


    var QuantidadeNaReceita: BigDecimal = BigDecimal(1)

    var defaultWareHouse : String? = null
        set(value) {
            field = value
            if(ItemWarehouseInfoCollection.any { it.WarehouseCode == field }) {
                val found = ItemWarehouseInfoCollection.first { it.WarehouseCode == field }
                ItemWarehouseInfoCollection = listOf(found)
                custoMateriaPrimaCurrency = found.StandardAveragePrice
            }
        }

    var kgsPorUnidade : BigDecimal? = null
    var custoGgf : BigDecimal? = null
    var custoMateriaPrimaCurrency : BigDecimal? = null

    val descricao : String = ItemName
    val unidadeMedida : String = InventoryUOM
    var ingredientes : List<Produto> = listOf()
        set(value) {
            field = value.groupBy { it.ItemCode }
                .map { (itemCode, produtos) ->
                    produtos.reduce { acc, produto ->
                        acc.apply { QuantidadeNaReceita += produto.QuantidadeNaReceita }
                    }
                }
        }

    var pai : String? = null

    var UoMGroupEntry : Int? = null
    var InventoryUoMEntry : Int? = null

    @JsonIgnore
    override fun toString(): String {
        return "$ItemCode - $descricao"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Produto

        return ItemCode == other.ItemCode
    }

    override fun hashCode(): Int {
        return ItemCode.hashCode()
    }


}