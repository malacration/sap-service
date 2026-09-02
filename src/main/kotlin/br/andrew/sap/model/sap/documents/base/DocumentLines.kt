package br.andrew.sap.model.sap.documents.base
import br.andrew.sap.model.sistema.Comissao
import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.DocumentTypes
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
    var U_ORD_CARREGAMENTO : Int? = null

    var BaseType : Int? = null
    var BaseEntry : Int? = null
    var BaseLine : Int? = null
    var SalUnitMsr : String? = null
    var FatherType : String? = null

    var BatchNumbers: List<BatchStock> = listOf()
    //Anotar so a propriedade deixa o @JsonProperty no campo e o getter getCFOPCode() vira uma
    //SEGUNDA propriedade implicita ("Cfopcode", depois do UpperCamelCase). O payload saia com
    //"CFOPCode" e "Cfopcode", e o Service Layer rejeita a segunda com "Internal error (-5002)".
    //Fixar o nome nos dois acessores mantem uma unica propriedade.
    @get:JsonProperty("CFOPCode")
    @set:JsonProperty("CFOPCode")
    var CFOPCode : String? = null

    // Impostos ja calculados pelo SAP nesta linha. Populado no deserializer
    // (o setJson por reflexao so trata escalares). Nunca volta para o SAP: o Service Layer
    // responde "Internal error (-5002)" ao receber LineTaxJurisdictions num PATCH.
    //
    // Precisa ser @get:JsonIgnore. Propriedade Kotlin que comeca com maiuscula quebra o
    // pareamento do Jackson: a anotacao sem use-site cai no campo ("LineTaxJurisdictions"),
    // mas o getter tem nome implicito "lineTaxJurisdictions" - nomes diferentes, o Jackson
    // nao liga os dois, e o getter vira propriedade sozinho carregando o @JsonIgnore do campo.
    @get:JsonIgnore
    var LineTaxJurisdictions : List<LineTaxJurisdiction> = listOf()


    @JsonIgnore
    var valorDesonerado : BigDecimal = BigDecimal(0)

    @JsonIgnore
    var resto: BigDecimal = BigDecimal(0)

    // Valor liquido da linha (LineTotal - imposto desonerado). Preenchido SOMENTE no fluxo
    // de /entregas (Document.preencheDesonerado). Fica null nos demais fluxos e, por
    // @JsonInclude(NON_EMPTY), e omitido do payload de PATCH do SAP - senao o Service Layer
    // rejeitaria esse campo nao-SAP nos updates dos schedules de desonerado.
    @JsonProperty("LineTotalDesonerado")
    var lineTotalDesonerado : Double? = null

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


    /**
     * Valor que o desonerado tem que fazer o liquido da linha voltar a ser: o preco negociado
     * quando existe, senao o preco da propria linha.
     *
     * Regra unica de proposito: quando o "U_preco_negociado" era lido cru no calculo do total
     * esperado, pedido com o campo zerado gerava desconto de 100% e o SAP recusava com
     * "(7) Desconto nao permitido" (DocNum 65506).
     */
    @JsonIgnore
    fun precoAlvo(): BigDecimal {
        return if((U_preco_negociado ?: 0.0) <= 0.0)
            BigDecimal(UnitPrice)
        else
            BigDecimal(U_preco_negociado!!)
    }

    @JsonIgnore
    fun totalNegociado(): BigDecimal {
        return BigDecimal(Quantity).multiply(BigDecimal((U_preco_negociado ?: 0.0).toString()))
    }

    @JsonIgnore
    fun presumeDesonerado(rate: Double): Double {
        return total().toDouble()*rate/100
    }

    // Calcula o valor liquido da linha: subtrai do LineTotal o TaxAmount ja calculado pelo
    // SAP nas jurisdicoes cujo JurisdictionType e um imposto desonerado. Sem imposto a
    // reduzir, retorna o proprio LineTotal. Nao e getter (nao serializa sozinho) - o
    // resultado e guardado em lineTotalDesonerado apenas no fluxo de /entregas.
    fun calcularLineTotalDesonerado(desoneradoIds: List<Int>): Double? {
        val total = LineTotal ?: return null
        if (desoneradoIds.isEmpty())
            return total
        val imposto = LineTaxJurisdictions
            .filter { desoneradoIds.contains(it.JurisdictionType) }
            .sumOf { it.TaxAmount ?: 0.0 }
        return BigDecimal.valueOf(total)
            .minus(BigDecimal.valueOf(imposto))
            .setScale(2, RoundingMode.HALF_UP)
            .toDouble()
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

    fun toInvoice(baseType: DocumentTypes) {
        this.BaseType = baseType.value
        this.BaseLine = this.LineNum
        this.BaseEntry = this.DocEntry
        this.DocEntry = null
        this.BatchNumbers.forEach {
            it.BaseLineNumber = this.BaseLine
            it.ItemCode = this.ItemCode
            it.toInvoice()
        }
    }
}
