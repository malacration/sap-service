package br.andrew.sap.model.documents

import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.documents.futura.ItemRetirada
import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Item
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class PedidoRetiradaFreteTest {

    private fun contrato(precoNegociado : Double, quantidade : Double, valorFrete : Double) : Contrato {
        val item = Item("A", "", precoNegociado, quantidade, precoNegociado, 0.0, 0.0, "KG")
        item.LineId = 0
        return Contrato(1, "C0001", mutableListOf(item), 1, "", 2, valorFrete)
    }

    private fun orderBase() : Document {
        return Document("C0001", null, listOf(Product("A", "1", "1", 0)), "2")
    }

    /** Nota ja faturada do contrato: base de produtos e o frete que ela realmente cobrou. */
    private fun faturado(base : Double, frete : Double, tipo : DocumentTypes = DocumentTypes.oInvoices) : Document {
        return Document("C0001", null, listOf(Product("A", "1", "1", 0)), "2").also {
            it.docObjectCode = tipo
            it.U_entrega_vf = 1
            it.documentAdditionalExpenses = mutableListOf(AdditionalExpenses.frete(frete))
            it.DocTotal = (base + frete).toString()
        }
    }

    private fun freteDa(contrato : Contrato, quantidadeRetirada : Double,
                        jaFaturado : List<Document> = listOf()) : Double? {
        val cotacao = PedidoRetirada(161, listOf(ItemRetirada("A", quantidadeRetirada, 0)))
            .parse(contrato, 0, null, orderBase(), listOf(), jaFaturado)
        return cotacao.documentAdditionalExpenses
            .singleOrNull { it.expenseCode == AdditionalExpenses.CODIGO_FRETE }?.LineTotal
    }

    /**
     * Contrato 161 / pedido 77488: base da retirada 94.454,00 sobre contrato de 128.418,00
     * (proporcao 0,7355199427) com frete de 7.200,00. O divide(divisor, RoundingMode) devolvia
     * a proporcao na escala do dividendo (0,74) e gravava 5.328,00 - 32,26 a mais do que a
     * SBO_SP_VALIDACAO_VENDA_FUTURA sugere no faturamento.
     */
    @Test
    fun freteNaoArredondaProporcaoIntermediaria() {
        Assertions.assertEquals(5295.74, freteDa(contrato(1.0, 128418.0, 7200.0), 94454.0))
    }

    /**
     * Retirada menor que 0,5% do contrato: a proporcao arredondada dava 0,00 e a nota saia sem
     * frete nenhum.
     */
    @Test
    fun retiradaPequenaNaoZeraOFrete() {
        Assertions.assertEquals(21.31, freteDa(contrato(1.0, 1000000.0, 5328.0), 4000.0))
    }

    @Test
    fun contratoSemFreteNaoGeraDespesaAdicional() {
        Assertions.assertNull(freteDa(contrato(1.0, 100.0, 0.0), 10.0))
    }

    /**
     * O caso que motivou o residual: a nota anterior saiu com 5.328,00 quando devia 5.295,74.
     * A retirada seguinte, que fecha o contrato, recebe o residual (7.200,00 - 5.328,00) e nao
     * a proporcao teorica - o contrato fecha exatamente nos 7.200,00 contratados.
     */
    @Test
    fun retiradaSeguinteAbsorveFreteCobradoAMaior() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 5328.0)

        Assertions.assertEquals(1872.00, freteDa(c, 33964.0, listOf(nota)))
        Assertions.assertEquals(7200.00, 5328.0 + freteDa(c, 33964.0, listOf(nota))!!)
    }

    @Test
    fun retiradaSeguinteAbsorveFreteCobradoAMenor() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 5000.0)

        Assertions.assertEquals(2200.00, freteDa(c, 33964.0, listOf(nota)))
    }

    /** Sem erro anterior o residual devolve o mesmo que o rateio simples do contrato. */
    @Test
    fun semDesvioAnteriorResidualEquivaleAoRateioSimples() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 5295.74)

        Assertions.assertEquals(1904.26, freteDa(c, 33964.0, listOf(nota)))
    }

    /** Devolucao volta o saldo para o contrato: base e frete devolvidos voltam a ficar disponiveis. */
    @Test
    fun devolucaoDevolveSaldoParaOContrato() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 5295.74)
        val devolucao = faturado(94454.0, 5295.74, DocumentTypes.oCreditNotes)

        Assertions.assertEquals(5295.74, freteDa(c, 94454.0, listOf(nota, devolucao)))
    }

    /** Contrato que ja cobrou todo o frete: nota sai sem despesa de frete, sem travar. */
    @Test
    fun freteResidualEsgotadoNaoGeraDespesa() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 7200.0)

        Assertions.assertNull(freteDa(c, 33964.0, listOf(nota)))
    }

    /** Frete cobrado a maior que o contrato inteiro: zera, nao lanca despesa negativa. */
    @Test
    fun freteResidualNegativoNaoGeraDespesaNegativa() {
        val c = contrato(1.0, 128418.0, 7200.0)
        val nota = faturado(94454.0, 7500.0)

        Assertions.assertNull(freteDa(c, 33964.0, listOf(nota)))
    }

    /** Somatorio de varias retiradas fecha exatamente no frete contratado. */
    @Test
    fun somatorioDasRetiradasFechaNoFreteDoContrato() {
        val c = contrato(1.0, 100000.0, 3333.33)
        val faturadas = mutableListOf<Document>()
        var soma = 0.0

        listOf(33333.0, 33333.0, 33334.0).forEach { quantidade ->
            val frete = freteDa(c, quantidade, faturadas) ?: 0.0
            soma += frete
            faturadas.add(faturado(quantidade, frete))
        }

        Assertions.assertEquals(3333.33, soma)
    }
}
