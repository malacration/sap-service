package br.andrew.sap.services.tax

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

/**
 * A `LineNum` da QUT13/RDR13/INV13/RIN13 e a linha de PRODUTO que recebeu o rateio da despesa,
 * nao a linha da despesa. Conferido em homolog:
 *
 *     cotacao 169655 (2 produtos, 1 frete) -> view devolve LineNum 0 e 1
 *     cotacao 169620 (5 produtos, 1 frete) -> view devolve LineNum 0,1,2,3,4
 *
 * Todas com o mesmo TaxCode `5101-009` ("ICMS OUTROS FRETE"), que e o codigo do frete rateado.
 * Por isso a leitura agrega por ExpnsCode em vez de indexar por LineNum.
 */
class TaxCodeDespesaServiceTest {

    private fun linha(lineNum: Int, taxCode: String?) = mapOf(
        "DocEntry" to 169655, "LineNum" to lineNum, "ExpnsCode" to 1, "TaxCode" to taxCode)

    private fun servico(vararg linhas: Map<String, Any?>): TaxCodeDespesaService {
        val sql = mock(SqlQueriesService::class.java, Answer {
            OData().also { it.putAll(mapOf("value" to linhas.toList())) }
        })
        return TaxCodeDespesaService(sql)
    }

    private fun busca(s: TaxCodeDespesaService) =
        s.taxCodeDoFrete(DocumentTypes.oQuotations, 169655, 1)

    @Test
    fun `frete rateado em varios produtos devolve um unico codigo`() {
        val s = servico(linha(0, "5101-009"), linha(1, "5101-009"),
                        linha(2, "5101-009"), linha(3, "5101-009"), linha(4, "5101-009"))

        assertEquals("5101-009", busca(s))
    }

    @Test
    fun `documento de uma linha so`() {
        assertEquals("5109-004", busca(servico(linha(0, "5109-004"))))
    }

    /** Sem alíquota unica nao da para majorar - melhor deixar o frete intacto. */
    @Test
    fun `rateios com codigos diferentes devolvem nulo`() {
        assertNull(busca(servico(linha(0, "5101-009"), linha(1, "5109-003"))))
    }

    @Test
    fun `sem linha nenhuma devolve nulo`() {
        assertNull(busca(servico()))
    }

    @Test
    fun `codigo vazio ou nulo e descartado`() {
        assertNull(busca(servico(linha(0, null), linha(1, "  "))))
        assertEquals("5101-009", busca(servico(linha(0, null), linha(1, "5101-009"))))
    }

    @Test
    fun `sem tipo ou docEntry nao consulta`() {
        val s = servico(linha(0, "5101-009"))
        assertNull(s.taxCodeDoFrete(null, 169655, 1))
        assertNull(s.taxCodeDoFrete(DocumentTypes.oQuotations, null, 1))
    }

    @Test
    fun `falha na leitura nao propaga excecao`() {
        val sql = mock(SqlQueriesService::class.java, Answer<Any> { throw RuntimeException("view fora") })
        assertNull(TaxCodeDespesaService(sql).taxCodeDoFrete(DocumentTypes.oQuotations, 169655, 1))
    }
}
