package br.andrew.sap.model.documents

import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.RoundingMode

class FreteNegociadoTest {

    /** Toda entrada de pedido (sovis/forca e angular) passa por AdditionalExpenses.frete. */
    @Test
    fun entradaDePedidoPreencheOFreteNegociado() {
        val frete = AdditionalExpenses.frete(1500.0)

        Assertions.assertEquals(1500.0, frete.LineTotal)
        Assertions.assertEquals(1500.0, frete.U_frete_negociado)
        Assertions.assertTrue(frete.temFreteNegociado())
    }

    /** Documento anterior a funcionalidade: campo vazio, conferencia cai no LineTotal. */
    @Test
    fun despesaSemFreteNegociadoUsaLineTotal() {
        val frete = AdditionalExpenses(AdditionalExpenses.CODIGO_FRETE, 1500.0)

        Assertions.assertFalse(frete.temFreteNegociado())
        Assertions.assertEquals(1500.0, frete.valorConferencia())
    }

    @Test
    fun freteNegociadoZeradoContaComoVazio() {
        val frete = AdditionalExpenses(AdditionalExpenses.CODIGO_FRETE, 1500.0)
            .also { it.U_frete_negociado = 0.0 }

        Assertions.assertFalse(frete.temFreteNegociado())
        Assertions.assertEquals(1500.0, frete.valorConferencia())
    }

    /**
     * Mesma majoracao do preco unitario: com ICMS desonerado o LineTotal sobe para que o liquido
     * volte ao negociado. Aliquota 18% sobre 100% de "outros" -> 1500 / (1 - 0,18) = 1829,27.
     */
    @Test
    fun majoracaoDoFreteUsaAMesmaRotinaDoPrecoUnitario() {
        val tax = SalesTaxAuthorities(1, 18.0, 0.0, 0.0, 100.0)

        val majorado = PrecoUnitarioComDesoneracao()
            .calculaPreco(BigDecimal("1500"), tax)
            .setScale(2, RoundingMode.HALF_UP)

        Assertions.assertEquals(BigDecimal("1829.27"), majorado)
        //liquido volta ao negociado depois que o SAP deduz o desonerado
        Assertions.assertEquals(
            BigDecimal("1500.00"),
            majorado.minus(tax.valorImposto(majorado)).setScale(2, RoundingMode.HALF_UP))
    }

    /** Sem "outros" nao ha desoneracao a compensar - o frete negociado passa direto. */
    @Test
    fun semImpostoDesoneradoOFreteNaoEMajorado() {
        val tax = SalesTaxAuthorities(1, 18.0, 0.0, 0.0, 0.0)

        Assertions.assertEquals(
            BigDecimal("1500.00"),
            PrecoUnitarioComDesoneracao().calculaPreco(BigDecimal("1500"), tax)
                .setScale(2, RoundingMode.HALF_UP))
    }

    /**
     * Conferencia do frete de venda futura enxerga o negociado, nao o LineTotal majorado - senao
     * a nota com ICMS desonerado seria barrada por "frete a maior".
     */
    @Test
    fun conferenciaDoContratoUsaONegociadoENaoOMajorado() {
        val doc = Document("C0001", null, listOf(Product("A", "1", "1", 0)), "2").also {
            it.documentAdditionalExpenses = mutableListOf(
                AdditionalExpenses.frete(1500.0).also { d -> d.LineTotal = 1829.27 })
        }

        Assertions.assertEquals(BigDecimal("1500.00"), doc.freteDespesaAdicional())
    }

    @Test
    fun conferenciaSemNegociadoContinuaNoLineTotal() {
        val doc = Document("C0001", null, listOf(Product("A", "1", "1", 0)), "2").also {
            it.documentAdditionalExpenses = mutableListOf(
                AdditionalExpenses(AdditionalExpenses.CODIGO_FRETE, 1829.27))
        }

        Assertions.assertEquals(BigDecimal("1829.27"), doc.freteDespesaAdicional())
    }
}
