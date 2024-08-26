package br.andrew.sap.model.sap.documents

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

    //TODO não achei onde fica esse propriedade
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

    fun getQuotationVendaFutura(pedidoRetirada: PedidoRetirada) : Quotation{
        val itens = pedidoRetirada.itensRetirada
        val docLines : List<DocumentLines> = itens.map {  retirada ->
            val item = this.DocumentLines
                .firstOrNull{ it.ItemCode == retirada.itemCode } ?: throw Exception("Erro ao selecionar item para retirada")
            item.Quantity = retirada.quantity.toString()
            item.LineTotal = null
            item
        }
        //TODO adicionar DATA de entrega
        return Quotation(CardCode,DocDueDate,docLines, getBPL_IDAssignedToInvoice()).also {
            it.U_venda_futura = pedidoRetirada.docEntryVendaFutura
        }
    }
}
//data de entrega - ORDRdocduedate
