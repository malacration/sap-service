package br.andrew.sap.model.sap.documents.base
import br.andrew.sap.model.Comissao
import br.andrew.sap.model.sap.price.PriceList
import br.andrew.sap.services.ItemsService
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(using = DocumentLinesDeserializer::class)
abstract class DocumentLines(var UnitPrice : String, var Quantity : String, var Usage : Int = 9) {

    var ItemDescription: String? = null
    var CommisionPercent: Double? = null
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
    var MeasureUnit : String? = null
    var PriceList : Int? = null
    var ListName : String? = null

    @JsonIgnore
    var valorDesonerado : BigDecimal = BigDecimal(0)

    abstract fun Duplicate() : DocumentLines

    fun total(): BigDecimal {
        val desconto = BigDecimal(1 -(DiscountPercent ?: 0.0)/100)
        return BigDecimal(UnitPrice.toDouble()).setScale(4,RoundingMode.UP).multiply(BigDecimal(Quantity).multiply(desconto))
    }

    fun aplicaBase(precoBase: Double, idTabela: Int, comissao: Comissao) {
        if(this is Product)
            this.U_preco_base = precoBase
        this.u_idTabela = idTabela
        this.CommisionPercent = comissao.U_porcentagem
    }

    fun totalAntesDesconto() {
        TODO("Not yet implemented")
    }

    fun totalNegociado(): Double {
        return Quantity.toDouble() * (U_preco_negociado ?: 0.0)
    }

    fun presumeDesonerado(rate: Double): Double {
        return total().toDouble()*rate/100
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

    fun atualizaPrecoBase(itemService: ItemsService) {
        if(this.ItemCode != null  && PriceList != null) {
            u_idTabela = PriceList
            U_preco_base = itemService.getPriceBase(this.ItemCode!!, PriceList!!)
        }
    }
}
