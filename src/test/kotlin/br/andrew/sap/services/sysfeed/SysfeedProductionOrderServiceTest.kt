package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedProductionOrderPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SysfeedProductionOrderServiceTest {
    private val service = SysfeedProductionOrderService(
        mock<SqlQueriesService>()
    )

    @Test
    fun `deve usar dataCorte informado na consulta`() {
        val sqlQueriesService = mock<SqlQueriesService>()
        val service = SysfeedProductionOrderService(sqlQueriesService)

        service.getPendingPayloads("2026-05-10")

        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService).execute(eq("sysfeed-ordens-producao-pendentes.sql"), captor.capture())
        assertEquals("startDate='2026-05-10'", captor.firstValue.first().toString())
    }

    @Test
    fun `deve bloquear dataCorte ausente`() {
        assertThrows(SysfeedProductionOrderValidationException::class.java) {
            service.getPendingPayloads("")
        }
    }

    @Test
    fun `deve bloquear dataCorte invalida`() {
        assertThrows(SysfeedProductionOrderValidationException::class.java) {
            service.getPendingPayloads("data-invalida")
        }
    }

    @Test
    fun `deve montar payload de ordem de producao`() {
        val payload = service.buildPayload(pending())

        assertEquals("12345", payload.codOrdemProducao)
        assertEquals("12345", payload.codIntOrdemProducao)
        assertEquals("100", payload.codFormula)
        assertEquals("100", payload.codIntFormula)
        assertEquals("2000", payload.quantidade)
        assertEquals("1", payload.prioridade)
        assertEquals("10", payload.totalQuantidade)
        assertEquals("10", payload.quantBat)
        assertEquals("A", payload.tipoOrdemProducao)
        assertNull(payload.descricaoOrdemProducao)
        assertEquals("18/06/2026", payload.dataEntradaOP)
        assertEquals("19/06/2026", payload.dataEntregaProducao)
    }

    @Test
    fun `deve enviar apenas o comentario da ordem quando presente`() {
        val payload = service.buildPayload(pending(descricao = "Observacao do operador"))

        assertEquals("Observacao do operador", payload.descricaoOrdemProducao)
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
        quantBat: String? = "10"
    ): SysfeedProductionOrderPending {
        return SysfeedProductionOrderPending(
            DocEntry = docEntry,
            DocNum = "12345",
            ItemCode = "PA0001",
            Quantidade = "20000.000000",
            DataEntradaOP = dataEntrada,
            DataEntregaProducao = dataEntrega,
            DescricaoOrdemProducao = descricao,
            CodFormula = "100",
            QuantBat = quantBat,
            SysfeedStatus = null
        )
    }
}
