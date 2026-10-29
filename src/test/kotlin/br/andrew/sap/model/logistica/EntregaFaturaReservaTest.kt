package br.andrew.sap.model.logistica

import br.andrew.sap.model.producao.BatchStock
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

/**
 * Valores da OINV 262554 do HOMOLOG2 (1000 sacos, LineTotal 55625,86, desonerado 4005,06). A
 * entrega de 2 sacos montada com essas regras passou na TransactionNotification em 2026-10-27
 * (ODLN 60567, desconto 8,01 = desonerado 8,01).
 */
class EntregaFaturaReservaTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private fun linha(lineNum: Int = 0, cfop: String = "6922", openQty: String = "550") = LinhaFaturaReserva(
        "CLI0006212", 2, 7, "11261009054087000154550020000060221653602385", lineNum, "PAC0000242",
        BigDecimal("1000"), BigDecimal(openQty), BigDecimal("55625.86"), cfop, "500.01", BigDecimal("4005.06"))

    private fun retirada(qtd: Double, lineNum: Int = 0) =
        CarregamentoLinha(U_docEntryFatura = 262554, U_baseLine = lineNum, U_qtdRetirada = qtd)

    private fun json(fatura: List<LinhaFaturaReserva>, retiradas: List<CarregamentoLinha>): JsonNode =
        mapper.valueToTree(EntregaFaturaReserva.montar(262554, fatura, retiradas, 88) {
            listOf(BatchStock("S18007454", it.Quantity, it.WarehouseCode))
        })

    @Test
    fun `entrega referencia a fatura com CFOP de remessa e desconto igual ao desonerado`() {
        val entrega = json(listOf(linha()), listOf(retirada(2.0)))
        val l = entrega["DocumentLines"][0]

        Assertions.assertEquals(7.2, entrega["DiscountPercent"].asDouble(), 0.000001)
        Assertions.assertEquals("11261009054087000154550020000060221653602385", entrega["U_ChaveAcesso"].asText())
        Assertions.assertEquals(88, entrega["SequenceCode"].asInt())
        Assertions.assertEquals(7, entrega["SalesPersonCode"].asInt())
        Assertions.assertEquals(13, l["BaseType"].asInt())
        Assertions.assertEquals(262554, l["BaseEntry"].asInt())
        Assertions.assertEquals(0, l["BaseLine"].asInt())
        Assertions.assertEquals(17, l["Usage"].asInt())
        Assertions.assertEquals("6116", l["CFOPCode"].asText())
        Assertions.assertEquals("2", l["Quantity"].asText())
        Assertions.assertEquals("S18007454", l["BatchNumbers"][0]["BatchNumber"].asText())
        Assertions.assertFalse(l.has("UnitPrice"), "preco vem da fatura; mandar sobrescreve")
        Assertions.assertFalse(l.has("LineTotal"))
        Assertions.assertFalse(entrega.has("TotalDiscount"), "o Service Layer ignora e usa o percentual")
    }

    /** Entrega 28624 feita no SAP: 596 de 4000 da NF 73975 (frete 32000, despesa 5) = 4768. */
    @Test
    fun `frete da NF vai proporcional a quantidade entregue sobre o total da NF`() {
        fun comFrete(lineNum: Int, quantidade: String) = LinhaFaturaReserva("C", 2, null, "CHAVE", lineNum, "I$lineNum",
            BigDecimal(quantidade), BigDecimal(quantidade), BigDecimal("1000"), "5922", "500.01", BigDecimal.ZERO,
            FreteLinha = 3, FreteCodigo = 5, Frete = BigDecimal("32000"))
        val despesa = json(listOf(comFrete(0, "1000"), comFrete(1, "3000")), listOf(retirada(596.0)))["DocumentAdditionalExpenses"][0]

        Assertions.assertEquals(5, despesa["ExpenseCode"].asInt(), "venda futura usa a despesa da NF (5), nao o frete 1 do pedido")
        Assertions.assertEquals(4768.0, despesa["LineTotal"].asDouble(), 0.001)
        Assertions.assertEquals(262554, despesa["BaseDocEntry"].asInt(), "vinculado a despesa da NF, como no SAP")
        Assertions.assertEquals(3, despesa["BaseDocLine"].asInt())
        Assertions.assertEquals(13, despesa["BaseDocType"].asInt())
    }

    /**
     * Rascunhos no HOMOLOG2 (NF 69805, DiscSum 14388,85): com o frete vinculado o SL grava desconto =
     * DiscSum da NF / (1 - percentual/100). Para 1 saco (desonerado 35,97) o percentual -39900 deu
     * desconto 35,97 e a entrega 28644 passou na TN.
     */
    @Test
    fun `frete vinculado envia o percentual que leva o desconto copiado da NF ao desonerado`() {
        val percentual = EntregaFaturaReserva.percentualComFreteVinculado(BigDecimal("14388.85"), BigDecimal("35.97"))
        Assertions.assertEquals(35.97, 14388.85 / (1 - percentual / 100), 0.005, "desconto que o SL grava")
        Assertions.assertEquals(0.0, EntregaFaturaReserva.percentualComFreteVinculado(BigDecimal("15.85"), BigDecimal("15.85")), 0.0001, "NF inteira")
        Assertions.assertEquals(0.0, EntregaFaturaReserva.percentualComFreteVinculado(BigDecimal.ZERO, BigDecimal("10")), 0.0001, "NF sem desconto")
    }

    @Test
    fun `sem desonerado na entrega e com desconto na NF o frete vai sem vinculo`() {
        //Linha sem desonerado: a formula do vinculo nao chega a desconto zero.
        val nf = LinhaFaturaReserva("C", 2, null, "CHAVE", 0, "I", BigDecimal("200"), BigDecimal("200"),
            BigDecimal("2000"), "5922", "500.01", BigDecimal.ZERO, FreteLinha = 0, FreteCodigo = 5,
            Frete = BigDecimal("1000"), DescontoNF = BigDecimal("50"))
        val entrega = json(listOf(nf), listOf(retirada(100.0)))

        Assertions.assertEquals(500.0, entrega["DocumentAdditionalExpenses"][0]["LineTotal"].asDouble(), 0.001)
        Assertions.assertFalse(entrega["DocumentAdditionalExpenses"][0].has("BaseDocEntry"))
        Assertions.assertEquals(0.0, entrega["DiscountPercent"]?.asDouble() ?: 0.0, 0.00001)
    }

    /** NF 39012: frete 1020 ja todo baixado (entrega 19017 + devolucao 1785); a entrega 28651 cobrou 51 a mais. */
    @Test
    fun `frete limitado ao que resta na NF`() {
        fun nf(baixado: String) = LinhaFaturaReserva("C", 2, null, "CHAVE", 0, "I", BigDecimal("200"), BigDecimal("200"),
            BigDecimal("2000"), "5922", "500.01", BigDecimal("400"), FreteLinha = 0, FreteCodigo = 5,
            Frete = BigDecimal("1020"), FreteBaixado = BigDecimal(baixado))

        Assertions.assertEquals(30.0, json(listOf(nf("990")), listOf(retirada(10.0)))["DocumentAdditionalExpenses"][0]["LineTotal"].asDouble(), 0.001)
        Assertions.assertTrue(json(listOf(nf("1020")), listOf(retirada(10.0)))["DocumentAdditionalExpenses"]?.isEmpty ?: true)
    }

    @Test
    fun `NF com mais de uma despesa recusa a entrega com frete em vez de copiar a despesa errada`() {
        val nf = LinhaFaturaReserva("C", 2, null, "CHAVE", 0, "I", BigDecimal("200"), BigDecimal("200"),
            BigDecimal("2000"), "5922", "500.01", BigDecimal.ZERO, FreteLinha = 0, FreteCodigo = 5,
            Frete = BigDecimal("1000"), QtdDespesas = 2)

        assertThrows<Exception> { json(listOf(nf), listOf(retirada(100.0))) }
    }

    @Test
    fun `desconto zero na NF com desonerado positivo vai sem vinculo e com o percentual do desonerado`() {
        //DiscSum 0 e desonerado 400: a formula do vinculo daria desconto 0 e a TN recusaria; sem vinculo o
        //percentual normal (desonerado / (itens + frete)) fecha.
        val nf = LinhaFaturaReserva("C", 2, null, "CHAVE", 0, "I", BigDecimal("200"), BigDecimal("200"),
            BigDecimal("2000"), "5922", "500.01", BigDecimal("400"), FreteLinha = 0, FreteCodigo = 5,
            Frete = BigDecimal("1000"), DescontoNF = BigDecimal.ZERO)
        val entrega = json(listOf(nf), listOf(retirada(100.0)))

        Assertions.assertFalse(entrega["DocumentAdditionalExpenses"][0].has("BaseDocEntry"))
        Assertions.assertEquals(13.333333, entrega["DiscountPercent"].asDouble(), 0.00001)
    }

    @Test
    fun `NF sem desconto e sem desonerado vincula o frete com percentual zero`() {
        val nf = LinhaFaturaReserva("C", 2, null, "CHAVE", 0, "I", BigDecimal("200"), BigDecimal("200"),
            BigDecimal("2000"), "5922", "500.01", BigDecimal.ZERO, FreteLinha = 0, FreteCodigo = 5,
            Frete = BigDecimal("1000"), DescontoNF = BigDecimal.ZERO)
        val entrega = json(listOf(nf), listOf(retirada(100.0)))

        Assertions.assertEquals(13, entrega["DocumentAdditionalExpenses"][0]["BaseDocType"].asInt())
        Assertions.assertEquals(0.0, entrega["DiscountPercent"]?.asDouble() ?: 0.0, 0.00001)
    }

    @Test
    fun `NF sem frete gera entrega sem despesa`() {
        Assertions.assertTrue(json(listOf(linha()), listOf(retirada(2.0)))["DocumentAdditionalExpenses"]?.isEmpty ?: true)
    }

    @Test
    fun `CFOP interno vira 5116`() {
        val entrega = json(listOf(linha(cfop = "5922")), listOf(retirada(2.0)))
        Assertions.assertEquals("5116", entrega["DocumentLines"][0]["CFOPCode"].asText())
    }

    @Test
    fun `recusa retirada acima do saldo a entregar`() {
        val erro = assertThrows<Exception> { json(listOf(linha(openQty = "1")), listOf(retirada(2.0))) }
        Assertions.assertTrue(erro.message!!.contains("maior que o saldo"))
    }

    @Test
    fun `recusa fatura sem chave de acesso`() {
        val semChave = LinhaFaturaReserva("C", 2, null, null, 0, "I", BigDecimal.ONE, BigDecimal.ONE,
            BigDecimal.ONE, "5922", "500.01", BigDecimal.ZERO)
        assertThrows<Exception> { json(listOf(semChave), listOf(retirada(1.0))) }
    }
}
