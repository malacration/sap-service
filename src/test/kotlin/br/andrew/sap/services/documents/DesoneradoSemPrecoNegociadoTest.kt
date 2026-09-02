package br.andrew.sap.services.documents

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

/**
 * Pedido sem preço negociado (DocNum 65506 / DocEntry 118771: "U_preco_negociado" = 0).
 *
 * O desonerado calculava desconto de 100% e o SAP recusava com "(7) Desconto não permitido"
 * (SBO_SP_TransactionNotification_Rovema, bloco ORDR). A causa é o "totalEsperado" nascer 0
 * quando não há preço negociado, fazendo o "resto" absorver a linha inteira.
 */
class DesoneradoSemPrecoNegociadoTest {

    private fun odata(vararg pares: Pair<String, Any?>) = OData().also { it.putAll(pares.toMap()) }

    private fun servico(rate: Double, outros: Double): DesoneradoService {
        //Answer fixo no lugar de matcher: os dois servicos so respondem OData nesse fluxo,
        //e ArgumentMatchers.any() devolve null, que o Kotlin recusa em parametro nao-nulo.
        val taxCode = mock(SalesTaxCodeService::class.java, Answer {
            odata("Code" to "5101-002",
                  "SalesTaxCodes_Lines" to listOf(
                      mapOf("STCCode" to "5101-002", "STACode" to "IC17BT14", "STAType" to 25)))
        })
        val authorities = mock(SalesTaxAuthoritiesService::class.java, Answer {
            odata("Type" to 25, "Rate" to rate,
                  "U_Base" to 0.0, "U_Isento" to 0.0, "U_Outros" to outros)
        })

        return DesoneradoService(taxCode, ImpostosDesonerados(listOf(25), listOf(10, 28), listOf(6)), authorities)
    }

    private fun pedido(precoNegociado: Double) = Document(
        "CLI0006841", "2025-11-25T00:00:00Z",
        listOf(Product("BOV0000009", "82.0", "3829.5968", 66).also {
            it.LineNum = 0
            it.TaxCode = "5101-002"
            it.U_preco_negociado = precoNegociado
            it.DiscountPercent = 0.0
        }),
        "6")

    /** Sem imposto desonerado a compensar, não há desconto nenhum a aplicar. */
    @Test
    fun semPrecoNegociadoNaoGeraDesconto() {
        val pedido = servico(rate = 19.5, outros = 0.0).aplicaDesonerado(pedido(0.0))

        assertEquals(0.0, pedido.discountPercent,
            "desconto sem preço negociado é recusado pelo SAP com '(7) Desconto não permitido'")
    }

    /** O preço da linha vira o alvo: o líquido depois do desonerado tem que voltar a ele. */
    @Test
    fun semPrecoNegociadoUsaOPrecoDaLinhaComoAlvo() {
        val pedido = servico(rate = 19.5, outros = 100.0).aplicaDesonerado(pedido(0.0))

        assertEquals("4757.2631", pedido.DocumentLines[0].UnitPrice)
        assertEquals(0.0, pedido.discountPercent)
    }

    /** Com preço negociado o comportamento de hoje não muda. */
    @Test
    fun comPrecoNegociadoContinuaMajorandoAteONegociado() {
        val pedido = servico(rate = 19.5, outros = 100.0).aplicaDesonerado(pedido(4000.0))

        assertEquals("4968.9441", pedido.DocumentLines[0].UnitPrice)
    }
}
