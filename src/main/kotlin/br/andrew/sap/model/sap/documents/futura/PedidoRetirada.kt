package br.andrew.sap.model.sap.documents.futura

import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.math.BigDecimal
import java.math.RoundingMode

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class PedidoRetirada(
    val docEntryVendaFutura : Int,
    val itensRetirada : List<ItemRetirada>){

    fun parse(contrato: Contrato, usage: Int, docDueDate : String? = null, order : Document): Quotation {
        val itemOriginal = order.DocumentLines?.get(0)
        return Quotation(
            CardCode = contrato.U_cardCode,
            DocDueDate = docDueDate,
            DocumentLines = contrato.itens.filter { base ->
                itensRetirada.any{
                    it.itemCode == base.U_itemCode && it.LineId == base.LineId
                }
            }.map { parse(it,usage,itemOriginal) },
            BPL_IDAssignedToInvoice = contrato.U_filial.toString()
        ).also {
            it.salesPersonCode = contrato.U_vendedor
            it.U_venda_futura = contrato.DocEntry
            it.Incoterms = order.Incoterms
            it.U_entrega_vf = 1
            it.journalMemo = "Entrega de mercadoria ref a contrato Nº ${contrato.DocEntry}"
            it.comments = it.journalMemo
            if(contrato.U_valorFrete > 0){
                val proporcao = it.totalProdutos().divide(contrato.totalProdutos(), RoundingMode.HALF_DOWN)
                it.frete = BigDecimal(contrato.U_valorFrete.toString()).multiply(proporcao).setScale(2, RoundingMode.HALF_DOWN).toDouble()
            }
        }
    }

    fun parse(itemContrato: Item, usage :Int, lineOriginal : DocumentLines?): DocumentLines {
        return Product(
            itemCode = itemContrato.U_itemCode,
            quantity = (itensRetirada
                .filter { it.itemCode == itemContrato.U_itemCode && it.LineId == itemContrato.LineId }
                .firstOrNull()?: throw Exception("Parse de item nao encontrado")
            ).quantidade.toString() ,
            unitPrice = itemContrato.U_precoNegociado.toString(),
            usage
        ).also {
            it.U_preco_negociado = itemContrato.U_precoNegociado
            it.MeasureUnit = itemContrato.U_MeasureUnit
            it.CommisionPercent = itemContrato.U_comissao
            it.DiscountPercent = 0.0
            it.U_preco_base = itemContrato.U_precoBase
            it.U_idTabela = lineOriginal?.U_idTabela
            it.CostingCode = lineOriginal?.CostingCode
            it.CostingCode2 = lineOriginal?.CostingCode2
            it.LineTotal = null
        }
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ItemRetirada(
    val itemCode: String,
    val quantidade: Double,
    val LineId : Int){
}