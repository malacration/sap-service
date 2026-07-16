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
import org.junit.jupiter.api.Assertions.assertNull
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
        // ERRO nao e sucesso: limpa DtIntegracao/HrIntegracao (null) para nao travar o reprocessamento.
        // Omitir as chaves deixaria o valor antigo no PATCH; por isso enviamos null explicito.
        verify(purchaseInvoiceService).update(
            check<Map<String, Any?>> {
                assertEquals("ERRO", it["U_sysfeed_status"])
                assertTrue(it.containsKey("U_LbrOne_DtIntegracao"))
                assertNull(it["U_LbrOne_DtIntegracao"])
                assertTrue(it.containsKey("U_LbrOne_HrIntegracao"))
                assertNull(it["U_LbrOne_HrIntegracao"])
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
        // PARCIAL nao e sucesso: limpa a trava (DtIntegracao/HrIntegracao = null) para seguir reprocessavel.
        verify(productionOrdersService).update(
            check<Map<String, Any?>> {
                assertEquals("PARCIAL", it["U_sysfeed_status"])
                assertTrue(it.containsKey("U_LbrOne_DtIntegracao"))
                assertNull(it["U_LbrOne_DtIntegracao"])
                assertTrue(it.containsKey("U_LbrOne_HrIntegracao"))
                assertNull(it["U_LbrOne_HrIntegracao"])
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
    fun `deve gravar numero sysfeed apenas na ordem de producao`() {
        service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "ENVIADO", numeroSysfeed = "36001"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_RECEBIMENTO, "144594", "ENVIADO", numeroSysfeed = "1472080"),
                SysfeedStatusUpdate(SysfeedStatusTarget.FORNECEDOR, "FOR0002977", "ENVIADO", numeroSysfeed = "999")
            )
        )

        verify(productionOrdersService).update(
            check<Map<String, Any>> {
                assertEquals("36001", it["U_sysfeed_numero"])
            },
            eq("12345")
        )
        verify(purchaseInvoiceService).update(
            check<Map<String, Any>> {
                assertFalse(it.containsKey("U_sysfeed_numero"))
            },
            eq("144594")
        )
        verify(businessPartnersService).update(
            check<Map<String, Any?>> {
                assertFalse(it.containsKey("U_sysfeed_numero"))
            },
            eq("'FOR0002977'")
        )
    }

    @Test
    fun `nao deve gravar numero sysfeed quando ausente ou em branco`() {
        service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "ENVIADO"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12346", "ENVIADO", numeroSysfeed = "   ")
            )
        )

        verify(productionOrdersService).update(
            check<Map<String, Any>> { assertFalse(it.containsKey("U_sysfeed_numero")) },
            eq("12345")
        )
        verify(productionOrdersService).update(
            check<Map<String, Any>> { assertFalse(it.containsKey("U_sysfeed_numero")) },
            eq("12346")
        )
    }

    @Test
    fun `deve limpar numero sysfeed quando status nao e sucesso`() {
        service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12345", "PENDENTE", numeroSysfeed = "36001"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_PRODUCAO, "12346", "ERRO", numeroSysfeed = "36002"),
            )
        )

        // PENDENTE/ERRO limpam U_sysfeed_numero mesmo que um numero antigo tenha sido enviado no
        // request — nao faz sentido a ordem mostrar "nao enviada" com um numero de uma tentativa anterior.
        verify(productionOrdersService).update(
            check<Map<String, Any?>> {
                assertTrue(it.containsKey("U_sysfeed_numero"))
                assertNull(it["U_sysfeed_numero"])
            },
            eq("12345")
        )
        verify(productionOrdersService).update(
            check<Map<String, Any?>> {
                assertTrue(it.containsKey("U_sysfeed_numero"))
                assertNull(it["U_sysfeed_numero"])
            },
            eq("12346")
        )
    }

    @Test
    fun `deve aceitar nrSysfeed como alias de numeroSysfeed`() {
        val update = jacksonObjectMapper().readValue<SysfeedStatusUpdate>(
            """
            {
              "tipo": "ORDEM_PRODUCAO",
              "identificador": "12345",
              "status": "ENVIADO",
              "nrSysfeed": "36001"
            }
            """.trimIndent()
        )

        assertEquals("36001", update.numeroSysfeed)
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
