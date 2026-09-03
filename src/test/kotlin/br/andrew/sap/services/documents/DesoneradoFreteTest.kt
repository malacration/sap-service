package br.andrew.sap.services.documents

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.tax.SalesTaxCodeLine
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import br.andrew.sap.services.tax.TaxCodeDespesaService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

/**
 * O caso real: cotacao 176199 (DocNum 18555), frete negociado de R$ 450 com ICMS desonerado de
 * 19% - tax code `5109-003`, autoridade `IC17BI02`/25 com Rate 19 e U_Outros 100.
 *
 * Sem majorar, o SAP abate 85,50 e o cliente paga 364,50 de frete, enquanto o
 * `valorConferencia()` e a view `frete-faturado-contrato.sql` contabilizam os 450 negociados.
 * A diferenca so aparece la na frente, quando o residuo de frete do contrato nao fecha.
 * Majorado para 555,56, o abatimento vira 105,56 e o liquido volta a ser 450.
 *
 * O `TaxCode` da despesa e SEMPRE nulo no Service Layer (ver [TaxCodeDespesaService]), entao o
 * codigo chega pela view - que e como o fluxo real funciona.
 */
class DesoneradoFreteTest {

    private fun odata(vararg pares: Pair<String, Any?>) = OData().also { it.putAll(pares.toMap()) }

    /**
     * `5109-003` tem duas linhas que passam pelo filtro de desonerado: a 10 (`U_Outros` 0, ICMS
     * comum) e a 25 (`U_Outros` 100, o desonerado de verdade). A autoridade responde conforme o
     * STAType pedido, senao o teste nao distingue as duas.
     */
    private fun servico(taxCodePorLinha: Map<Int, String>,
                        linhasDoTaxCode: List<Map<String, Any>> = listOf(
                            mapOf("STCCode" to "5109-003", "STACode" to "IC17BI02", "STAType" to 10),
                            mapOf("STCCode" to "5109-003", "STACode" to "IC17BI02", "STAType" to 25))
    ): DesoneradoService {
        val taxCode = mock(SalesTaxCodeService::class.java, Answer {
            odata("Code" to "5109-003", "SalesTaxCodes_Lines" to linhasDoTaxCode)
        })
        val authorities = mock(SalesTaxAuthoritiesService::class.java, Answer { invocacao ->
            val tipo = (invocacao.arguments[0] as SalesTaxCodeLine).STAType
            odata("Type" to tipo, "Rate" to 19.0, "U_Base" to 0.0,
                  "U_Isento" to if (tipo == 10) 100.0 else 0.0,
                  "U_Outros" to if (tipo == 25) 100.0 else 0.0)
        })
        val despesas = mock(TaxCodeDespesaService::class.java, Answer { taxCodePorLinha })

        return DesoneradoService(
            taxCode,
            ImpostosDesonerados(listOf(25), listOf(10, 28), listOf(2, 4, 11, 17, 18)),
            authorities,
            despesas)
    }

    private fun cotacao(freteNegociado: Double?, lineTotal: Double = freteNegociado ?: 0.0): Document {
        val despesa = AdditionalExpenses(AdditionalExpenses.CODIGO_FRETE, lineTotal).also {
            it.U_frete_negociado = freteNegociado
            it.LineNum = 0
        }
        return Document("CLI0006186", null, listOf(), "4").also {
            it.docEntry = 176199
            it.docNum = "18555"
            it.docObjectCode = DocumentTypes.oQuotations
            it.documentAdditionalExpenses = mutableListOf(despesa)
        }
    }

    private fun frete(doc: Document) = doc.documentAdditionalExpenses.first()

    @Test
    fun `frete com ICMS desonerado e majorado ate o liquido voltar ao negociado`() {
        val doc = cotacao(450.0)
        servico(mapOf(0 to "5109-003")).aplicaDesoneradoFrete(doc)

        val majorado = frete(doc).LineTotal
        assertEquals(555.56, majorado, 0.001, "450 / (1 - 0,19)")
        assertEquals(450.0, majorado - majorado * 0.19, 0.01,
            "depois do abatimento do SAP o liquido tem que ser o negociado")
    }

    /**
     * A linha STAType 10 tem `U_Outros` 0 e devolveria o proprio valor alvo. Antes ela
     * sobrescrevia o resultado da 25 quando vinha por ultimo; hoje e pulada.
     */
    @Test
    fun `linha de imposto sem U_Outros nao desfaz a majoracao`() {
        val doc = cotacao(450.0)
        servico(mapOf(0 to "5109-003"), listOf(
            mapOf("STCCode" to "5109-003", "STACode" to "IC17BI02", "STAType" to 25),
            mapOf("STCCode" to "5109-003", "STACode" to "IC17BI02", "STAType" to 10))
        ).aplicaDesoneradoFrete(doc)

        assertEquals(555.56, frete(doc).LineTotal, 0.001)
    }

    @Test
    fun `o negociado nao muda - e ele que a conferencia do contrato usa`() {
        val doc = cotacao(450.0)
        servico(mapOf(0 to "5109-003")).aplicaDesoneradoFrete(doc)

        assertEquals(450.0, frete(doc).U_frete_negociado)
        assertEquals(450.0, doc.freteDespesaAdicional().toDouble())
    }

    @Test
    fun `TaxCode do Service Layer tem precedencia sobre a view`() {
        val doc = cotacao(450.0)
        frete(doc).TaxCode = "5109-003"
        servico(mapOf()).aplicaDesoneradoFrete(doc)

        assertEquals(555.56, frete(doc).LineTotal, 0.001)
    }

    @Test
    fun `sem codigo de imposto em lugar nenhum o LineTotal fica intacto`() {
        val doc = cotacao(450.0)
        servico(mapOf()).aplicaDesoneradoFrete(doc)

        assertEquals(450.0, frete(doc).LineTotal)
    }

    /** Cotacao 169667: o frete tem codigo, mas o codigo nao carrega imposto desonerado. */
    @Test
    fun `codigo sem imposto desonerado nao majora`() {
        val doc = cotacao(233.58)
        servico(mapOf(0 to "5102-001"), listOf(
            mapOf("STCCode" to "5102-001", "STACode" to "PI00BO02", "STAType" to 19))
        ).aplicaDesoneradoFrete(doc)

        assertEquals(233.58, frete(doc).LineTotal)
    }

    /** Cotacao 156559: documento anterior a funcionalidade, sem U_frete_negociado. */
    @Test
    fun `despesa sem frete negociado nao e tocada`() {
        val doc = cotacao(null, lineTotal = 270.0)
        servico(mapOf(0 to "5109-003")).aplicaDesoneradoFrete(doc)

        assertEquals(270.0, frete(doc).LineTotal)
    }
}
