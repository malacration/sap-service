package br.andrew.sap.model.documents

import br.andrew.sap.model.WarehouseDefault
import br.andrew.sap.services.ItemsService
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
open class Document(val CardCode : String,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                    val DocDueDate : String?,
                    val DocumentLines : List<Product>,
                    private val BPL_IDAssignedToInvoice : String) {

    var comments: String? = null
    var docDate :String? = null
    var salesPersonCode: Int = -1
    var paymentGroupCode: String? = null
    var docEntry : Int? = null
    var docNum : String? = null
    var paymentMethod : String? = null
    var discountPercent : Double = 0.0
    var COGSCostingCode : String? = null
    var COGSCostingCode2 : String? = null
    var ControlAccount : String? = null
    var documentInstallments : List<Installment>? = null
    var journalMemo : String? = null
    var u_pedido_update : String? = "0";

    @JsonProperty("U_Id_Pedido_Forca")
    var u_id_pedido_forca: String? = null
    var cardName: String? = null
    var OpeningRemarks: String? = null


    var documentAdditionalExpenses : List<AdditionalExpenses> = emptyList()
    var frete: Double? = null
        set(valor){

        field = valor
    }

    @JsonProperty("BPL_IDAssignedToInvoice")
    fun getBPL_IDAssignedToInvoice(): String {
        return BPL_IDAssignedToInvoice;
    }

    fun productsByTax(): Map<String, List<Product>> {
        return this.DocumentLines
                .filter { it.taxCode != null && it.taxCode!!.isNotEmpty() }
                .groupBy { it.taxCode!! }
    }

    fun aplicaBase(itemService: ItemsService){
        this.DocumentLines.forEach { it.aplicaBase(itemService) }
    }

    fun usaBrenchDefaultWarehouse(branchs : List<WarehouseDefault>){
        branchs.firstOrNull{ it.BPLID == BPL_IDAssignedToInvoice }
                ?.also { usaBrenchDefaultWarehouse(it) }

    }
    fun usaBrenchDefaultWarehouse(default : WarehouseDefault){
        if(default.defaultWarehouseID != null)
            DocumentLines
                    .filter { it.warehouseCode == null}
                    .forEach { it.warehouseCode = default.defaultWarehouseID}
    }

    fun isAvista(): Boolean {
        return paymentGroupCode == "-1"
    }

    fun isCalculaDesonaerado(): Boolean {
        return u_pedido_update == "1"
    }

    fun total() : Double {
        return DocumentLines.sumOf { it.total() }.plus(totalDespesaAdicional());
    }

    fun totalNegociado() : Double {
        return DocumentLines.sumOf { it.totalNegociado() }
    }

    fun totalDespesaAdicional(): Double {
        return documentAdditionalExpenses.sumOf { it.lineTotalSys }
    }

    fun presumeDesonerado(rate : Double) : Double {
        return DocumentLines.sumOf { it.presumeDesonerado(rate) }
    }

    override fun toString(): String {
        return "Document(CardCode='$CardCode', Branch='$BPL_IDAssignedToInvoice', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }


}

