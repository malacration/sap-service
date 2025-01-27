package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Produto(
    val ItemCode : String,
    val ItemName : String,
    val InventoryUOM : String,
    var ItemWarehouseInfoCollection : List<Deposito>
    ) {

    var defaultWareHouse : String? = null
        set(value) {
            field = value
            if(ItemWarehouseInfoCollection.any { it.WarehouseCode == field }) {
                val found = ItemWarehouseInfoCollection.first { it.WarehouseCode == field }
                ItemWarehouseInfoCollection = listOf(found)
                if(!::custoMateriaPrimaCurrency.isInitialized)
                    custoMateriaPrimaCurrency = found.StandardAveragePrice
            }
        }
    lateinit var kgsPorUnidade : BigDecimal
    lateinit var custoGgf : BigDecimal
    lateinit var custoMateriaPrimaCurrency : BigDecimal


    val descricao : String = ItemName
    val unidadeMedida : String = InventoryUOM
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Deposito(
    val WarehouseCode : String,
    val StandardAveragePrice : BigDecimal,
    val InStock : Double,
    val Committed : Double,
    val Ordered : Double,
){

}