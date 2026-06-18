package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedStatusTarget
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdate
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.document.PurchaseInvoiceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SysfeedStatusServiceTest {
    private val businessPartnersService = mock<BusinessPartnersService>()
    private val purchaseInvoiceService = mock<PurchaseInvoiceService>()
    private val service = SysfeedStatusService(businessPartnersService, purchaseInvoiceService)

    @Test
    fun `deve atualizar status em lote`() {
        val result = service.updateAll(
            listOf(
                SysfeedStatusUpdate(SysfeedStatusTarget.FORNECEDOR, "FOR0002977", "ENVIADO"),
                SysfeedStatusUpdate(SysfeedStatusTarget.ORDEM_RECEBIMENTO, "144594", "ERRO")
            )
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.success })
        verify(businessPartnersService).update(eq(mapOf("U_sysfeed_status" to "ENVIADO")), eq("'FOR0002977'"))
        verify(purchaseInvoiceService).update(eq(mapOf("U_sysfeed_status" to "ERRO")), eq("144594"))
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
}
