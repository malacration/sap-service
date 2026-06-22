package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedProductionOrderPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SysfeedProductionOrderServiceTest {
    private val service = SysfeedProductionOrderService(
        mock<SqlQueriesService>(),
        "2026-04-01"
    )

    @Test
    fun `deve montar payload de ordem de producao`() {
        val payload = service.buildPayload(pending())

        assertEquals("12345", payload.codOrdemProducao)
        assertEquals("12345", payload.codIntOrdemProducao)
        assertEquals("100", payload.codFormula)
        assertEquals("100", payload.codIntFormula)
        assertEquals("250", payload.quantidade)
        assertEquals("1", payload.prioridade)
        assertEquals("4", payload.totalQuantidade)
        assertEquals("4", payload.quantBat)
        assertEquals("A", payload.tipoOrdemProducao)
        assertEquals("ORDEM DE PRODUCAO 12345", payload.descricaoOrdemProducao)
        assertEquals("18/06/2026", payload.dataEntradaOP)
        assertEquals("19/06/2026", payload.dataEntregaProducao)
    }

    @Test
    fun `nao deve serializar regLido`() {
        val json = ObjectMapper().writeValueAsString(service.buildPayload(pending()))

        assertFalse(json.contains("regLido"))
    }

    @Test
    fun `deve formatar data sap sem hifens`() {
        val payload = service.buildPayload(
            pending(
                dataEntrada = "20260518",
                dataEntrega = "20260519"
            )
        )

        assertEquals("18/05/2026", payload.dataEntradaOP)
        assertEquals("19/05/2026", payload.dataEntregaProducao)
    }

    @Test
    fun `deve bloquear codigo de ordem com mais de dez digitos`() {
        assertThrows(SysfeedProductionOrderValidationException::class.java) {
            service.buildPayload(pending(docEntry = 12345678901))
        }
    }

    @Test
    fun `deve bloquear descricao com mais de duzentos caracteres`() {
        assertThrows(SysfeedProductionOrderValidationException::class.java) {
            service.buildPayload(pending(descricao = "A".repeat(201)))
        }
    }

    @Test
    fun `deve bloquear batelada zero`() {
        assertThrows(SysfeedProductionOrderValidationException::class.java) {
            service.buildPayload(pending(quantBat = "0"))
        }
    }

    private fun pending(
        docEntry: Long = 12345,
        descricao: String? = null,
        dataEntrada: String? = "2026-06-18",
        dataEntrega: String? = "2026-06-19",
        quantBat: String? = "250"
    ): SysfeedProductionOrderPending {
        return SysfeedProductionOrderPending(
            DocEntry = docEntry,
            DocNum = "12345",
            ItemCode = "PA0001",
            Quantidade = "1000.000000",
            DataEntradaOP = dataEntrada,
            DataEntregaProducao = dataEntrega,
            DescricaoOrdemProducao = descricao,
            CodFormula = "100",
            QuantBat = quantBat,
            SysfeedStatus = null
        )
    }
}
