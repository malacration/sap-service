package br.andrew.sap.model.documents
import br.andrew.sap.services.ItemsService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = DocumentLinesDeserializer::class)
abstract class DocumentLines(var UnitPrice : String, var Quantity : String, var Usage : Int = 9) {

    var ItemCode : String? = null
    var LineNum : Int? = null
    var TaxCode : String? = null
    var DiscountPercent : Double? = null
    var U_preco_base: Double? = null
    var U_preco_negociado: Double? = null
    var WarehouseCode: String? = null
    var U_id_item_forca: String? = null
    var CostingCode: String? = null
    var CostingCode2: String? =null
    var AccountCode : String? = null

    abstract fun Duplicate() : DocumentLines;

    fun total(): Double {
        return (UnitPrice.toDouble() * Quantity.toDouble()) * (1-(DiscountPercent ?: 0.0)/100)
    }

    fun aplicaBase(itemService: ItemsService) {
        if(this is Product)
            this.U_preco_base = itemService.getPriceBase(this)
    }

    fun totalAntesDesconto() {
        TODO("Not yet implemented")
    }

    fun totalNegociado(): Double {
        return Quantity.toDouble() * (U_preco_negociado ?: 0.0)
    }

    fun presumeDesonerado(rate: Double): Double {
        return total()*rate/100
    }

    fun setDistribuicaoCusto(branch: DistribuicaoCustoByBranch) {
        CostingCode = branch.grupoEconomico
        CostingCode2 = branch.centroCusto
    }

    fun setJson(node: JsonNode){
        //setAll Properties
        val fields = DocumentLines::class.java.declaredFields
        fields.forEach {
            if(node.get(it.name) != null){
                if(it.type == Int::class.java || it.type == Integer::class.java)
                    it.set(this, node.get(it.name).asInt())
                else if(it.type == Double::class.javaPrimitiveType || it.type == Double::class.javaObjectType)
                    it.set(this, node.get(it.name).asDouble())
                else if(it.type == String::class.java)
                    it.set(this, node.get(it.name).asText())
            }else if(it.trySetAccessible()) {
                try {
                    it.set(this, null)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }
    }
}
