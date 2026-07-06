package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedStatusTarget
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdate
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.ProductionOrdersService
import br.andrew.sap.services.document.PurchaseInvoiceService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SysfeedStatusServiceTest {
    private val businessPartnersService = mock<BusinessPartnersService>()
    private val purchaseInvoiceService = mock<PurchaseInvoiceService>()
    private val productionOrdersService = mock<ProductionOrdersService>()
    private val service = SysfeedStatusService(businessPartnersService, purchaseInvoiceService, productionOrdersService)

    @Test
    fun `deve atualizar status em lote`() {
        val result = service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.FORNECEDOR, "FOR0002977", "ENVIADO"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_RECEBIMENTO, "144594", "ERRO"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "DUPLICADO")
            )
        )

        assertEquals(3, result.size)
        assertTrue(result.all { it.success })
        verify(businessPartnersService).update(eq(mapOf("U_sysfeed_status" to "ENVIADO")), eq("'FOR0002977'"))
        // ERRO nao e sucesso: nao deve marcar DtIntegracao (senao travaria o reprocessamento).
        verify(purchaseInvoiceService).update(
            check<Map<String, Any>> {
                assertEquals("ERRO", it["U_sysfeed_status"])
                assertFalse(it.containsKey("U_LbrOne_DtIntegracao"))
                assertFalse(it.containsKey("U_LbrOne_HrIntegracao"))
                assertFalse(it.containsKey("U_LbrOne_Obs_Integracao"))
            },
            eq("144594")
        )
        // DUPLICADO conta como envio concluido: marca DtIntegracao (trava anti-reenvio).
        verify(productionOrdersService).update(
            check<Map<String, Any>> {
                assertEquals("DUPLICADO", it["U_sysfeed_status"])
                assertTrue(it.containsKey("U_LbrOne_DtIntegracao"))
                assertTrue(it["U_LbrOne_HrIntegracao"] is Int)
                assertFalse(it.containsKey("U_LbrOne_Obs_Integracao"))
            },
            eq("12345")
        )
    }

    @Test
    fun `deve marcar DtIntegracao apenas em status de sucesso`() {
        service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_RECEBIMENTO, "144594", "ENVIADO"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "PARCIAL")
            )
        )

        // ENVIADO -> marca DtIntegracao (vira a trava anti-reenvio na view de pendentes).
        verify(purchaseInvoiceService).update(
            check<Map<String, Any>> {
                assertEquals("ENVIADO", it["U_sysfeed_status"])
                assertTrue(it.containsKey("U_LbrOne_DtIntegracao"))
                assertTrue(it["U_LbrOne_HrIntegracao"] is Int)
            },
            eq("144594")
        )
        // PARCIAL nao trava: sem DtIntegracao, segue reprocessavel.
        verify(productionOrdersService).update(
            check<Map<String, Any>> {
                assertEquals("PARCIAL", it["U_sysfeed_status"])
                assertFalse(it.containsKey("U_LbrOne_DtIntegracao"))
                assertFalse(it.containsKey("U_LbrOne_HrIntegracao"))
            },
            eq("12345")
        )
    }

    @Test
    fun `deve gravar observacao apenas na ordem de producao`() {
        service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "ERRO", "falha ao processar formula"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_RECEBIMENTO, "144594", "ERRO", "obs ignorada no recebimento")
            )
        )

        verify(productionOrdersService).update(
            check<Map<String, Any>> {
                assertEquals("falha ao processar formula", it["U_LbrOne_Obs_Integracao"])
            },
            eq("12345")
        )
        verify(purchaseInvoiceService).update(
            check<Map<String, Any>> {
                assertFalse(it.containsKey("U_LbrOne_Obs_Integracao"))
            },
            eq("144594")
        )
    }

    @Test
    fun `deve retornar erro individual para status invalido`() {
        val result = service.updateAll(
            listOf(SysfeedStatusUpdate(SysfeedStatusTarget.FORNECEDOR, "FOR0002977", "INVALIDO"))
        )

        assertEquals(1, result.size)
        assertFalse(result.first().success)
        assertEquals("INVALIDO", result.first().status)
    }

    @Test
    fun `deve aceitar identificador como alias de codigo`() {
        val update = jacksonObjectMapper().readValue<SysfeedStatusUpdate>(
            """
            {
              "tipo": "ORDEM_PRODUCAO",
              "identificador": "12345",
              "status": "ENVIADO"
            }
            """.trimIndent()
        )

        assertEquals("12345", update.codigo)
    }
}
