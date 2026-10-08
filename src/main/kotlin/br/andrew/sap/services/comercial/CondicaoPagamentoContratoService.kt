package br.andrew.sap.services.comercial

import br.andrew.sap.model.comercial.PrazoPagamentoDto
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.PedidoTroca
import br.andrew.sap.services.documents.OrdersService
import br.andrew.sap.services.stock.ItemsService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Condicao de pagamento (OCTG.GroupNum) do contrato de venda futura.
 *
 * O contrato guarda a condicao do pedido que o originou (U_condicaoPagamento) porque o
 * desconto/juros financeiro de cada produto depende dela cruzada com a tabela de preco
 * (@CONDICOESFV, ver prazo-por-tabela-preco.sql). Sem isso a troca precificava os produtos
 * novos sem desconto nenhum.
 */
@Service
class CondicaoPagamentoContratoService(
    private val contratoService: ContratoVendaFuturaService,
    private val orderService: OrdersService,
    private val prazoPagamentoService: PrazoPagamentoService,
    private val itemService: ItemsService,
) {

    //mesma folga de centavo do arredondamento do front (Item.formula, toFixed(2))
    private val TOLERANCIA_PRECO = BigDecimal("0.01")

    /**
     * Preenche a condicao de pagamento de um contrato criado antes deste campo, lendo-a do
     * pedido original. Sob demanda: o front chama ao abrir a troca de um contrato legado.
     *
     * O valor vem SEMPRE do pedido no SAP, nunca do cliente da API - o front so pede a
     * sanitizacao. Contrato que ja tem a condicao volta como esta (idempotente: clique duplo ou
     * duas abas nao sobrescrevem nada).
     */
    fun sanitiza(docEntry: Int): Contrato {
        val contrato = contratoService.getById(docEntry).tryGetValue<Contrato>()
        if(contrato.U_condicaoPagamento != null)
            return contrato

        val pedido = orderService.getById(contrato.U_orderDocEntry).tryGetValue<OrderSales>()
        val condicao = pedido.paymentGroupCode?.takeIf { it >= 0 }
            ?: throw Exception("O pedido original ${pedido.docNum ?: contrato.U_orderDocEntry} do contrato $docEntry " +
                "nao possui condicao de pagamento - nao ha como definir o desconto da troca")

        contratoService.update(mapOf("U_condicaoPagamento" to condicao), docEntry.toString())
        contrato.U_condicaoPagamento = condicao
        return contrato
    }

    /**
     * Confere que cada produto novo da troca foi precificado com a condicao do contrato:
     * preco da tabela x (1 - desconto) x (1 + juros) da condicao, x (1 - desconto do vendedor).
     * Mesma formula do front (Item.formula).
     *
     * Sem isso o back gravaria qualquer U_preco_negociado que chegasse - o desconto da condicao
     * so existiria se a tela lembrasse de aplicar.
     */
    fun validaPrecosDaTroca(contrato: Contrato, pedidoTroca: PedidoTroca) {
        val condicao = contrato.U_condicaoPagamento
            ?: throw Exception("O contrato ${contrato.DocEntry} nao possui condicao de pagamento. " +
                "Reabra a troca para atualizar o contrato com a condicao do pedido original.")

        val prazosPorTabela = mutableMapOf<Int, List<PrazoPagamentoDto>>()
        pedidoTroca.itemRecebido.forEach { item ->
            val tabela = item.PriceList ?: throw Exception("IdTabela nao pode ser nulo")
            val itemCode = item.ItemCode ?: throw Exception("A propriedade ItemCode nao pode ser null")
            val prazo = prazosPorTabela
                .getOrPut(tabela) { prazoPagamentoService.getByTabela(tabela) ?: listOf() }
                .firstOrNull { it.GroupNum == condicao.toString() }
                ?: throw Exception("A condicao de pagamento $condicao do contrato nao esta disponivel para a " +
                    "tabela de preco $tabela do produto $itemCode - escolha o produto em outra tabela")

            val esperado = precoComCondicao(itemService.getPriceBase(itemCode, tabela), prazo, item.DiscountPercent ?: 0.0)
            val enviado = BigDecimal((item.U_preco_negociado ?: 0.0).toString())
            if(esperado.subtract(enviado).abs() > TOLERANCIA_PRECO)
                throw Exception("Preco do produto $itemCode divergente da condicao de pagamento do contrato " +
                    "(enviado: R$ ${enviado.setScale(2, RoundingMode.HALF_UP)}, esperado: R$ $esperado)")
        }
    }

    companion object {
        fun precoComCondicao(precoTabela: Double, prazo: PrazoPagamentoDto, descontoVendedor: Double): BigDecimal {
            val cem = BigDecimal(100)
            return BigDecimal(precoTabela.toString())
                .multiply(BigDecimal.ONE.subtract(BigDecimal(prazo.U_desconto.toString()).divide(cem)))
                .multiply(BigDecimal.ONE.add(BigDecimal(prazo.U_juros.toString()).divide(cem)))
                .multiply(cem.subtract(BigDecimal(descontoVendedor.toString())).divide(cem))
                .setScale(2, RoundingMode.HALF_UP)
        }
    }
}
