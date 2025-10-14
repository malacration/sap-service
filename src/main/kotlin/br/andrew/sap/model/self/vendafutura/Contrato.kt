package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.batch.BatchId
import br.andrew.sap.services.pricing.ComissaoService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Contrato(
    val U_orderDocEntry : Int,
    val U_cardCode : String,
    @JsonProperty("AR_CF_LINHACollection")
    val itens : MutableList<Item>,
    val U_vendedor : Int,
    val U_cardName : String,
    val U_filial  : Int,
    val U_valorFrete  : Double = 0.0,
    dataCriacao : Date = Date()
): BatchId {

    @JsonProperty("U_status")
    var U_status : Status = Status.aberto

    init {
//        if(total().compareTo(BigDecimal.ZERO) <= 0)
//            throw Exception("Nao e permitido contrato sem valor")
    }

    fun total(): BigDecimal {
        return totalProdutos()
            .plus(BigDecimal(U_valorFrete.toString()))
            .setScale(2, RoundingMode.HALF_DOWN)
    }

    fun troca(pedidoTroca: PedidoTroca, itemService: ItemsService, comissaoService: ComissaoService): BigDecimal {
        val valorOriginal = total()
        pedidoTroca.itemSaida.forEach { saida ->
            itens.firstOrNull { it.U_itemCode == saida.itemCode && it.U_quantity < saida.quantidade }?.also{
                throw Exception("Nao e possivel trocar uma quantidade superior ao contrato")
            }

            itens.removeIf { it.U_itemCode == saida.itemCode && it.U_quantity == saida.quantidade }

            itens.firstOrNull { it.U_itemCode == saida.itemCode && it.U_quantity > saida.quantidade }?.also {
                it.U_quantity -= saida.quantidade
            }
        }

        val tableaEx = Exception("IdTabela nao pode ser nulo")
        pedidoTroca.itemRecebido.forEach { item ->
            //TODO pegar o desconto que o vendedor aplicou e aplicar aqui!
            itens.add(ContratoParse.parse(
                item.aplicaBase(0.0,item.PriceList ?: throw tableaEx,comissaoService.getByIdTabela(item.PriceList!!))
                    .atualizaPrecoBase(itemService).also {
                        it.DiscountPercent = item.DiscountPercent ?: 0.0
                    }
            ))
        }
        val valorFinal = total()
        return valorFinal.minus(valorOriginal)
    }

    var DocNum : Int? = null
    var DocEntry : Int? = null
    var Series : String? = null
    var U_dataCriacao : String = SimpleDateFormat("yyyy-MM-dd").format(dataCriacao)

    var SalesEmployeeName: String? = null
    var OrderDocNum: String? = null
    var Bplname: String? = null
    var TotalProdutosCalculado : Double? = null

    override fun getId(): String {
        return this.DocEntry.toString()
    }

    fun totalProdutos(): BigDecimal {
        return itens.map{ it.total() }.sumOf { it }
    }

    fun tudoEntregue(entregas: List<Document>): Boolean {
        val totais: Map<String, Double> = entregas.asSequence()
            .filter { it.Cancelled == Cancelled.tNO }
            .flatMap { e ->
                val sign = if (e.docObjectCode == DocumentTypes.oCreditNotes) -1.0 else 1.0
                e.DocumentLines.asSequence().map { it.ItemCode to ((it.Quantity ?: "0").replace(',', '.').toDoubleOrNull() ?: 0.0) * sign }
            }
            .groupBy { it.first ?: throw Exception("Nao e permitido item sem ItemCode") }
            .mapValues { (_, xs) -> xs.sumOf { it.second } }

        return !this.itens.any {
            totais[it.U_itemCode] != it.U_quantity
        }
    }

    companion object{
        @JsonIgnoreProperties
        fun getAllProperties(): List<String> {
            return listOf(
                "U_orderDocEntry",
                "U_cardCode",
                "U_vendedor",
                "U_cardName",
                "U_filial",
                "U_valorFrete",
                "U_status",
                "DocNum",
                "DocEntry",
                "Series",
                "U_dataCriacao",
            )
        }
    }
}