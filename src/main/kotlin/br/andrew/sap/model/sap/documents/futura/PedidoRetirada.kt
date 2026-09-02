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
    val itensRetirada : List<ItemRetirada>,
    //Endereco de entrega escolhido na tela. Nulo cai no primeiro bo_ShipTo do cliente, que era o
    //comportamento antigo (o SAP aplicava o endereco padrao) - mantido para nao quebrar chamador
    //que ainda nao manda o campo.
    val shipToCode : String? = null){

    fun parse(
        contrato: Contrato,
        usage: Int,
        docDueDate : String? = null,
        order : Document,
        numerosBoletos: List<String> = listOf(),
        entregasFaturadas : List<Document> = listOf(),
        //Endereco que a validacao de regiao resolveu e aprovou. Vai no shipToCode para o SAP
        //entregar exatamente onde foi validado: sem ele, cliente antigo que nao manda endereco
        //deixava o SAP aplicar o padrao DELE, que nao e necessariamente o que foi conferido.
        enderecoValidado : String? = null
    ): Quotation {
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
            it.Incoterms = order.TaxExtension?.Incoterms
            it.U_entrega_vf = 1
            it.paymentGroupCode = -1
            it.journalMemo = "Entrega de mercadoria ref a contrato Nº ${contrato.DocEntry}"
            it.comments = it.journalMemo
            it.ClosingRemarks = observacaoFooter(contrato, numerosBoletos)
            it.shipToCode = enderecoValidado ?: shipToCode
            it.frete = freteResidual(contrato, it.quantidadeProdutos(), entregasFaturadas)
        }
    }

    /**
     * Frete da retirada rateado sobre o SALDO do contrato, nao sobre o contrato inteiro:
     *
     *     frete = (valorFrete do contrato - frete ja faturado) * QUANTIDADE da retirada
     *             / (quantidade do contrato - quantidade ja faturada)
     *
     * A base e QUANTIDADE DE ITENS, nao valor: o frete e calculado multiplicando pela quantidade
     * (ver Regiao.calcularFrete), entao retirar 1 item de 100 leva 1% do frete, independente de
     * aquele item ser o mais caro ou o mais barato do contrato.
     *
     * Isso faz a retirada absorver o desvio das notas anteriores - se uma nota saiu com frete a
     * maior ou a menor, o residual encolhe/aumenta e as proximas se ajustam sozinhas. Na ultima
     * retirada a base da retirada iguala a base residual, entao o frete dela e exatamente o
     * residual e o somatorio do contrato sempre fecha no valorFrete contratado.
     *
     * Multiplica antes de dividir e arredonda uma unica vez no fim, na mesma ordem de operacoes
     * da SBO_SP_VALIDACAO_VENDA_FUTURA, que revalida esse valor no faturamento. Dividir primeiro
     * nao serve: divide(divisor, RoundingMode) devolve o resultado na escala do dividendo (2
     * casas, vindo do setScale de totalProdutos), o que arredondava a proporcao e errava o frete
     * em ate 0,5% do valor do contrato.
     *
     * Residual nao positivo (contrato ja cobrou todo o frete, ou cobrou a maior) devolve null:
     * a nota sai sem despesa de frete e nada trava - nao se lanca despesa adicional negativa.
     */
    fun freteResidual(contrato: Contrato, quantidadeRetirada: BigDecimal, entregasFaturadas: List<Document>): Double? {
        if(contrato.U_valorFrete <= 0)
            return null

        val freteJaFaturado = entregasFaturadas.fold(BigDecimal.ZERO) { acc, doc ->
            acc.plus(doc.freteDespesaAdicional().multiply(BigDecimal(doc.sinalNoContrato())))
        }
        val quantidadeJaFaturada = entregasFaturadas.fold(BigDecimal.ZERO) { acc, doc ->
            acc.plus(doc.quantidadeProdutos().multiply(BigDecimal(doc.sinalNoContrato())))
        }

        val freteResidual = BigDecimal(contrato.U_valorFrete.toString()).minus(freteJaFaturado)
        val quantidadeResidual = BigDecimal(contrato.quantidadeTotal().toString()).minus(quantidadeJaFaturada)

        if(freteResidual.signum() <= 0 || quantidadeResidual.signum() <= 0)
            return null

        return freteResidual
            .multiply(quantidadeRetirada)
            .divide(quantidadeResidual, 2, RoundingMode.HALF_DOWN)
            .toDouble()
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

    private fun observacaoFooter(contrato: Contrato, numerosBoletos: List<String>): String {
        val boletos = numerosBoletos
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
            .ifBlank { "nao encontrados" }

        return "Faturamento referente a entrega de mercadorias do contrato Nº ${contrato.DocEntry} " +
            "com referencia aos boletos numero $boletos."
    }
}


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class ItemRetirada(
    val itemCode: String,
    val quantidade: Double,
    val LineId : Int){
}
