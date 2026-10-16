package br.andrew.sap.model.calculadora

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class LastPrice(
    val ItemCode : String,
    val LastPurPrc : BigDecimal = BigDecimal.ZERO,
    val LstEvlPric : BigDecimal = BigDecimal.ZERO,
    val AvgPrice : BigDecimal = BigDecimal.ZERO,
    val U_productionCost : BigDecimal = BigDecimal.ZERO,
    //unidade de estoque e grupo de unidades do SAP: todo preco "UnEstoque" abaixo e por essa unidade
    val InvntryUom : String? = null,
    val UgpCode : String? = null,
    val UgpName : String? = null,
    //ultima NF de entrada do item na unidade de estoque (LineTotal / InvQty): a nota vem na unidade
    //de compra (ex.: tonelada) e o custo da calculadora e por unidade de estoque
    var PrecoUltimaNfUnEstoque : BigDecimal = BigDecimal.ZERO,
    //frete bruto dos CTes vinculados a essa NF, ja rateado pro item (total e por unidade de estoque)
    var FreteUltimaNf : BigDecimal = BigDecimal.ZERO,
    var FreteUnEstoque : BigDecimal = BigDecimal.ZERO,
    //(LineTotal + FreteUltimaNf) / InvQty
    var CustoComFreteUnEstoque : BigDecimal = BigDecimal.ZERO,
    var UltimaNf : UltimaNfEntrada? = null,
    var QtdCtesUltimaNf : Int = 0,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class UltimaNfEntrada(
    val DocEntry : Int,
    val ItemCode : String,
    val LineTotal : BigDecimal = BigDecimal.ZERO,
    val InvQty : BigDecimal = BigDecimal.ZERO,
    val DocNum : Int? = null,
    val DocDate : String? = null,
    val UnidadeCompra : String? = null,
    val Quantidade : BigDecimal = BigDecimal.ZERO,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class CteDaNf(
    val DocEntry : Int,
    val FreteBruto : BigDecimal = BigDecimal.ZERO,
)

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class NfDoCte(
    val DocEntry : Int,
    val ValorNf : BigDecimal = BigDecimal.ZERO,
)

class CustoUltimaCompra(
    val precoUnEstoque : BigDecimal,
    val frete : BigDecimal,
    val custoComFreteUnEstoque : BigDecimal,
    val freteUnEstoque : BigDecimal = BigDecimal.ZERO,
) {
    companion object {
        private const val ESCALA = 6

        /**
         * O CTe (tambem uma NF de entrada) e referenciado pela nota de insumo no PCH21 e pode cobrir
         * varias notas. O frete bruto (LineTotal do CTe, sem descontar os impostos do PCH4) e rateado
         * em dois passos, ambos pelo valor da mercadoria: entre as notas que apontam pro CTe e,
         * dentro da nota, entre os itens.
         *
         * @param ctes cada CTe da nota com as notas de insumo que o referenciam (inclusive esta)
         */
        fun calcular(nf : UltimaNfEntrada, ctes : List<Pair<CteDaNf, List<NfDoCte>>>) : CustoUltimaCompra {
            var freteNf = BigDecimal.ZERO
            var totalNf : BigDecimal? = null
            ctes.forEach { (cte, nfs) ->
                val valorNf = nfs.firstOrNull { it.DocEntry == nf.DocEntry }?.ValorNf ?: return@forEach
                val valorTotal = nfs.sumOf { it.ValorNf }
                if (valorTotal.signum() == 0) return@forEach
                totalNf = valorNf
                freteNf += (cte.FreteBruto * valorNf).divide(valorTotal, ESCALA, RoundingMode.HALF_UP)
            }
            val frete = totalNf?.takeIf { it.signum() != 0 }
                ?.let { (freteNf * nf.LineTotal).divide(it, ESCALA, RoundingMode.HALF_UP) }
                ?: BigDecimal.ZERO
            return CustoUltimaCompra(
                porUnidade(nf.LineTotal, nf.InvQty),
                frete,
                porUnidade(nf.LineTotal + frete, nf.InvQty),
                porUnidade(frete, nf.InvQty),
            )
        }

        private fun porUnidade(valor : BigDecimal, quantidade : BigDecimal) =
            if (quantidade.signum() == 0) BigDecimal.ZERO
            else valor.divide(quantidade, ESCALA, RoundingMode.HALF_UP)
    }
}
