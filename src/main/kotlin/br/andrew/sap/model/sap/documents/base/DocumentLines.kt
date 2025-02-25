package br.andrew.sap.model.sap.documents.base
import br.andrew.sap.model.Comissao
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
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
abstract class DocumentLines(
    var UnitPrice : String,
    var Quantity : String, var Usage : Int? = 9) {

    var DocEntry: Int? = null
    var ItemDescription: String? = null
    var CommisionPercent: Double? = null
    @JsonProperty("U_idTabela")
    var U_idTabela : Int? = null
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
    var OnHand : Int? = null
    var LineTotal : Double? = null
    var PriceUnit : Int? = null
    var U_LBR_Destinacao : String? = null

    var BaseType : Int? = null
    var BaseEntry : Int? = null
    var BaseLine : Int? = null
    var SalUnitMsr : String? = null


    @JsonIgnore
    var valorDesonerado : BigDecimal = BigDecimal(0)

    @JsonIgnore
    var resto: BigDecimal = BigDecimal(0)

    abstract fun Duplicate() : DocumentLines


    @JsonIgnore
    fun total(): BigDecimal {
        val desconto = BigDecimal(1 -(DiscountPercent ?: 0.0)/100)
        return BigDecimal(UnitPrice.toDouble())
            .setScale(4,RoundingMode.HALF_DOWN)
            .multiply(BigDecimal(Quantity).multiply(desconto))
            .setScale(2,RoundingMode.HALF_UP)
    }

    fun aplicaBase(precoBase: Double, idTabela: Int, comissao: Comissao): DocumentLines {
        if(this is Product)
            this.U_preco_base = precoBase
        this.U_idTabela = idTabela
        this.CommisionPercent = comissao.U_porcentagem
        return this
    }


    @JsonIgnore
    fun totalNegociado(): BigDecimal {
        return BigDecimal(Quantity).multiply(BigDecimal((U_preco_negociado ?: 0.0).toString()))
    }

    @JsonIgnore
    fun presumeDesonerado(rate: Double): Double {
        return total().toDouble()*rate/100
    }

    fun setDistribuicaoCusto(custoByBranch: DistribuicaoCustoByBranch) {
        CostingCode = custoByBranch.grupoEconomico
        CostingCode2 = custoByBranch.centroCusto
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
                    e.printStackTrace()
                }
            }
        }
    }

    fun atualizaPrecoBase(itemService: ItemsService) : DocumentLines {
        if(this.ItemCode != null  && PriceList != null) {
            U_idTabela = PriceList
            U_preco_base = itemService.getPriceBase(this.ItemCode!!, PriceList!!)
        }
        return this
    }
}
