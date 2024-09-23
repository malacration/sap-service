package br.andrew.sap.model.sap.documents

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.ItemRetirada
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class OrderSales(CardCode: String,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYY-MM-dd", timezone = "UTC")
                 DocDueDate: String?,
                 DocumentLines: List<DocumentLines>,
                 BPL_IDAssignedToInvoice: String)
    : Document(CardCode, DocDueDate, DocumentLines, BPL_IDAssignedToInvoice) {

    var header : String? = null

    override fun toString(): String {
        return "OrderSales(CardCode='$CardCode', Branch='${getBPL_IDAssignedToInvoice()}', docEntry=$docEntry, docNum=$docNum, pedido_forca=$u_id_pedido_forca)"
    }


    fun duplicate() : OrderSales {
        val produtos = DocumentLines.map { it.Duplicate() }
        return OrderSales(CardCode,DocDueDate,produtos, getBPL_IDAssignedToInvoice()).also {
            it.model = model
            it.documentInstallments = this.documentInstallments
            it.journalMemo = this.journalMemo
            it.docDate = this.docDate
            it.controlAccount = this.controlAccount
        }
    }

    fun getQuotationVendaFutura(pedidoRetirada: PedidoRetirada, utilizacao : Int) : Quotation{
        val docLines : List<DocumentLines> = pedidoRetirada.itensRetirada.map {  retirada ->
            val item = this.DocumentLines
                .firstOrNull{ it.ItemCode == retirada.itemCode } ?: throw Exception("Erro ao selecionar item para retirada")
            Product(item.ItemCode ?: throw Exception("nao e um produto"),
                    retirada.quantity.toString(),
                    item.UnitPrice,
                    utilizacao
            ).also {
                it.DiscountPercent = item.DiscountPercent
                it.U_preco_negociado = item.U_preco_negociado
                it.U_preco_base = item.U_preco_base
                it.U_id_item_forca = item.U_id_item_forca
                it.u_idTabela = item.u_idTabela
                it.u_venda_futura = item.u_venda_futura
                it.LineTotal = null
            }
        }
        return Quotation(CardCode,DocDueDate,docLines, getBPL_IDAssignedToInvoice()).also {
            it.U_venda_futura = pedidoRetirada.docEntryVendaFutura
        }.also {
            if(this.totalDespesaAdicional().compareTo(BigDecimal.ZERO) > 0){
                val proporcao = it.totalProdutos().divide(this.totalProdutos(),RoundingMode.HALF_DOWN)
                it.documentAdditionalExpenses = this.documentAdditionalExpenses.map {
                    val lineTotal = BigDecimal(it.LineTotal.toString()).multiply(proporcao).setScale(2,RoundingMode.HALF_DOWN).toDouble()
                    AdditionalExpenses(it.expenseCode,lineTotal)
                }.toMutableList()
            }


        }
    }
}
//data de entrega - ORDRdocduedate
