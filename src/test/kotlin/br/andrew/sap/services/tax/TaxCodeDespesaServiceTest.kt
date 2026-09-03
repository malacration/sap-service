package br.andrew.sap.services.tax

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

/**
 * A `LineNum` da QUT13/RDR13/INV13/RIN13 e a linha de PRODUTO que recebeu o rateio da despesa,
 * nao a linha da despesa. Conferido em homolog:
 *
 *     cotacao 169655 (2 produtos, 1 frete) -> LineNum 0 e 1
 *     cotacao 169620 (5 produtos, 1 frete) -> LineNum 0,1,2,3,4
 *
 * Todas com o mesmo TaxCode `5101-009` ("ICMS OUTROS FRETE"), que e o codigo do frete rateado.
 * Por isso a busca e por ExpnsCode e o servico devolve os rateios inteiros - quem chama
 * pondera pelo LineTotal, ja que os codigos podem divergir entre eles.
 */
class TaxCodeDespesaServiceTest {

    private fun linha(lineNum: Int, taxCode: String?, lineTotal: Double = 100.0) = mapOf(
        "DocEntry" to 169655, "LineNum" to lineNum, "ExpnsCode" to 1,
        "LineTotal" to lineTotal, "TaxCode" to taxCode)

    private fun servico(vararg linhas: Map<String, Any?>): TaxCodeDespesaService {
        val sql = mock(SqlQueriesService::class.java, Answer {
            OData().also { it.putAll(mapOf("value" to linhas.toList())) }
        })
        return TaxCodeDespesaService(sql)
    }

    private fun busca(s: TaxCodeDespesaService) =
        s.rateiosDoFrete(DocumentTypes.oQuotations, 169655, 1)

    @Test
    fun `frete rateado em varios produtos devolve um rateio por produto`() {
        val rateios = busca(servico(
            linha(0, "5101-009", 99.0), linha(1, "5101-009", 99.0),
            linha(2, "5101-009", 99.0), linha(3, "5101-009", 99.0), linha(4, "5101-009", 99.0)))

        assertEquals(5, rateios.size)
        assertEquals(listOf(0, 1, 2, 3, 4), rateios.map { it.LineNum })
        assertEquals(495.0, rateios.sumOf { it.LineTotal ?: 0.0 }, 0.001)
    }

    @Test
    fun `documento de uma linha so`() {
        val rateios = busca(servico(linha(0, "5109-004", 270.0)))

        assertEquals(1, rateios.size)
        assertEquals("5109-004", rateios.first().TaxCode)
        assertEquals(270.0, rateios.first().LineTotal)
    }

    /** Os 9 documentos que a varredura em producao achou. */
    @Test
    fun `rateios com codigos diferentes vem todos, para quem chama ponderar`() {
        val rateios = busca(servico(linha(0, "5101-009", 225.0), linha(1, "5109-003", 225.0)))

        assertEquals(2, rateios.size)
        assertEquals(setOf("5101-009", "5109-003"), rateios.mapNotNull { it.TaxCode }.toSet())
    }

    @Test
    fun `rateio sem codigo e descartado`() {
        val rateios = busca(servico(linha(0, null), linha(1, "  "), linha(2, "5101-009")))

        assertEquals(1, rateios.size)
        assertEquals("5101-009", rateios.first().TaxCode)
    }

    @Test
    fun `sem linha nenhuma devolve vazio`() {
        assertTrue(busca(servico()).isEmpty())
    }

    @Test
    fun `sem tipo ou docEntry nao consulta`() {
        val s = servico(linha(0, "5101-009"))

        assertTrue(s.rateiosDoFrete(null, 169655, 1).isEmpty())
        assertTrue(s.rateiosDoFrete(DocumentTypes.oQuotations, null, 1).isEmpty())
    }

    @Test
    fun `falha na leitura nao propaga excecao`() {
        val sql = mock(SqlQueriesService::class.java, Answer<Any> { throw RuntimeException("view fora") })

        assertTrue(TaxCodeDespesaService(sql)
            .rateiosDoFrete(DocumentTypes.oQuotations, 169655, 1).isEmpty())
    }
}
