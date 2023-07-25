package br.andrew.sap.model.documents
import br.andrew.sap.model.Comissao
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonDeserialize(using = DocumentLinesDeserializer::class)
abstract class DocumentLines(var UnitPrice : String, var Quantity : String, var Usage : Int = 9) {

    var Commission: Double? = null
    var u_idTabela : Int? = null
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

    abstract fun Duplicate() : DocumentLines

    fun total(): Double {
        return (UnitPrice.toDouble() * Quantity.toDouble()) * (1-(DiscountPercent ?: 0.0)/100)
    }

    fun aplicaBase(precoBase: Double, idTabela: Int, comissao: Comissao) {
        if(this is Product)
            this.U_preco_base = precoBase
        this.u_idTabela = idTabela
        this.Commission = comissao.U_porcentagem
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
        val fields = this::class.memberProperties
        fields.forEach {
            if(node.get(it.name) != null && it is KMutableProperty<*> && node.get(it.name)?.asText() ?: "" != "null"){
                if(it.returnType == Int::class || it.returnType == Integer::class.java || it.javaField!!.type == Integer::class.javaObjectType || it.javaField!!.type == Integer::class.javaPrimitiveType)
                    it.setter.call(this, node.get(it.name).asInt())
                else if(it.returnType == Double::class.javaPrimitiveType || it.javaField!!.type == Double::class.javaObjectType || it.javaField!!.type == Double::class.javaPrimitiveType)
                    it.setter.call(this, node.get(it.name).asDouble())
                else if(it.returnType == String::class || it.javaField!!.type == String::class.java)
                    it.setter.call(this, node.get(it.name).asText())
            }else if(node.get(it.name)?.asText() ?: "" == "null" && it is KMutableProperty<*>) {
                try {
                    it.setter.call(this, null)
                } catch (e: Exception) {
                    println(e.message)
                }
            }
        }
    }
}
