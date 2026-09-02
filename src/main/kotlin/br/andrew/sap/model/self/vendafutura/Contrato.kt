package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.stock.ItemsService
import br.andrew.sap.services.batch.BatchId
import br.andrew.sap.services.comercial.FreteContratoService
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
    //var, nao val: a troca recalcula o frete pela regiao vigente (ver troca())
    var U_valorFrete  : Double = 0.0,
    dataCriacao : Date = Date()
): BatchId {

    /**
     * Localidade de entrega negociada. Unica entrada de calculo de frete guardada no contrato -
     * a regiao e as faixas sao sempre relidas do cadastro vigente da filial, entao editar a
     * tabela de frete nao muda contrato nenhum sozinho.
     *
     * Nulo em contrato criado antes desta funcionalidade: a retirada segue normal (ela confia no
     * U_valorFrete ja atribuido), mas a troca exige atribuir antes de recalcular.
     */
    @JsonProperty("U_Localidade")
    var U_Localidade : Int? = null

    /** Regiao que valia na assinatura. Historico - nunca entra em calculo. */
    @JsonProperty("U_RegiaoCode")
    var U_RegiaoCode : String? = null

    @JsonProperty("U_status")
    var U_status : Status = Status.aberto

    @JsonProperty("U_valorProdutos")
    var U_valorProdutos : Double = 0.0
        get() {
            return if(itens.size == 0)
                field
            else
                totalProdutos().toDouble()
        }
        set(value) {
            field = value
        }

    fun total(): BigDecimal {
        return totalProdutos()
            .plus(BigDecimal(U_valorFrete.toString()))
            .setScale(2, RoundingMode.HALF_DOWN)
    }

    /**
     * Troca produtos ainda nao retirados por outros, devolvendo a diferenca de valor que o
     * chamador usa para ajustar os adiantamentos.
     *
     * O frete E recalculado aqui. Antes nao era: total() soma U_valorFrete, entao ele aparecia
     * nos dois lados da subtracao e se cancelava - trocar 20 itens de A por 5 de B mudava a
     * quantidade (e portanto o frete devido) sem que o contrato registrasse nada.
     *
     * O recalculo usa a regiao VIGENTE da filial e a tabela de preco de agora, nao a da
     * assinatura: o preco do frete pode ter mudado entre as duas datas, e a troca e o momento
     * certo de o contrato absorver isso. Fora da troca nada revaloriza sozinho.
     *
     * @param freteContratoService nulo so em teste que nao exercita frete; em producao sempre vem.
     */
    fun troca(pedidoTroca: PedidoTroca, itemService: ItemsService, comissaoService: ComissaoService,
              freteContratoService: FreteContratoService? = null): BigDecimal {
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
        recalculaFrete(freteContratoService)

        val valorFinal = total()
        return valorFinal.minus(valorOriginal)
    }

    /**
     * Refaz o U_valorFrete pela regiao vigente da filial, para a nova quantidade de itens.
     *
     * Contrato legado (sem U_Localidade) nao tem destino registrado, entao nao ha o que
     * calcular - o chamador ja exige a atribuicao antes de chegar aqui. Deixar passar mantendo o
     * frete antigo seria pior: a troca gravaria um contrato com frete que nao corresponde mais
     * aos itens dele.
     *
     * Falha em qualquer etapa aborta a troca inteira, de proposito: o /troca roda tudo num
     * BatchList, entao nada e gravado pela metade, e frete que nao pode ser calculado e cadastro
     * faltando, nao caso a ignorar.
     */
    private fun recalculaFrete(freteContratoService: FreteContratoService?) {
        val localidade = U_Localidade ?: return
        val servico = freteContratoService ?: return
        U_valorFrete = servico.calcula(U_filial, localidade, quantidadeTotal())
    }

    /** Base do frete: a formula da regiao multiplica pela quantidade de itens, nao pelo valor. */
    fun quantidadeTotal(): Double {
        return itens.sumOf { it.U_quantity }
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
        val entregue: Map<String, Double> = entregas.asSequence()
            .filter { it.Cancelled == Cancelled.tNO }
            .flatMap { e ->
                val sign = if (e.docObjectCode == DocumentTypes.oCreditNotes) -1.0 else 1.0
                e.DocumentLines.asSequence().map { it.ItemCode to ((it.Quantity ?: "0").replace(',', '.').toDoubleOrNull() ?: 0.0) * sign }
            }
            .groupBy { it.first ?: throw Exception("Nao e permitido item sem ItemCode") }
            .mapValues { (_, xs) -> xs.sumOf { it.second } }

        val contratado: Map<String, Double> = this.itens
            .groupBy { it.U_itemCode }
            .mapValues { (_, xs) -> xs.sumOf { it.U_quantity } }

        // Conclui somente quando cada item foi entregue exatamente na quantidade contratada.
        // Entrega a mais que o contrato (ou item fora do contrato) reprova a conclusao.
        return (contratado.keys + entregue.keys).all { itemCode ->
            Math.abs((entregue[itemCode] ?: 0.0) - (contratado[itemCode] ?: 0.0)) < 0.0001
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
                "U_Localidade",
                "U_RegiaoCode",
                "U_status",
                "DocNum",
                "DocEntry",
                "Series",
                "U_dataCriacao",
            )
        }
    }
}
