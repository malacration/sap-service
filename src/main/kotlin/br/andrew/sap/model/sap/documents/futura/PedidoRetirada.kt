package br.andrew.sap.model.sap.documents.futura

import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import jakarta.mail.Quota
import java.math.BigDecimal
import java.math.RoundingMode

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PedidoRetirada(
    val docEntryVendaFutura : Int,
    val itensRetirada : List<ItemRetirada>){

    fun parse(contrato: Contrato, usage: Int, docDueDate : String? = null): Quotation {
        return Quotation(
            CardCode = contrato.U_cardCode,
            DocDueDate = docDueDate,
            DocumentLines = contrato.itens.filter { base ->
                itensRetirada.map { it.itemCode }
                    .contains(base.U_itemCode) }
                .map { parse(it,usage) },
            BPL_IDAssignedToInvoice = contrato.U_filial.toString()
        ).also {
            it.salesPersonCode = contrato.U_vendedor
            it.U_venda_futura = contrato.DocEntry
            it.U_entrega_vf = 1
            if(contrato.U_valorFrete > 0){
                val proporcao = it.totalProdutos().divide(contrato.totalProdutos(), RoundingMode.HALF_DOWN)
                it.frete = BigDecimal(contrato.U_valorFrete.toString()).multiply(proporcao).setScale(2, RoundingMode.HALF_DOWN).toDouble()
            }
        }
    }

    fun parse(item: Item, usage :Int): DocumentLines {
        return Product(
            itemCode = item.U_itemCode,
            quantity = (itensRetirada
                .filter { it.itemCode == item.U_itemCode }
                .firstOrNull()?: throw Exception("Parse de item nao encontrado")
            ).quantidade.toString() ,
            unitPrice = item.U_precoNegociado.toString(),
            usage
        ).also {
            it.U_preco_negociado = item.U_precoNegociado
            it.MeasureUnit = item.U_MeasureUnit
            it.CommisionPercent = item.U_comissao
            it.DiscountPercent = 0.0
            it.U_preco_base = item.U_precoBase
            // talvez adicionar? provavel it.U_idTabela = item.U_idtabela
            it.LineTotal = null
        }
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ItemRetirada(
    val itemCode: String,
    val quantidade: Double){

}