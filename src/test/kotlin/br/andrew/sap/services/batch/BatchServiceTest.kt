package br.andrew.sap.services.batch

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.abstracts.EntitiesService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate

class BatchServiceTest {

    private data class BatchPayload(private val id: String) : BatchId {
        override fun getId(): String = id
    }

    @Test
    fun `should generate close operation without request body`() {
        val restTemplate = mock<RestTemplate>()
        val env = mock<SapEnvrioment>()
        val bpService = mock<BusinessPartnersService>()
        val authService = mock<AuthService>()
        val service = BatchService(restTemplate, env, bpService, authService)
        val orderService = mock<EntitiesService<*>>()
        val payload = BatchPayload("130939")

        whenever(orderService.path()).thenReturn("/b1s/v1/Orders")

        val body = service.body("batch-test", BatchList().add(BatchMethod.CLOSE, payload, orderService))
            .toString(Charsets.UTF_8)

        assertTrue(body.contains("--batch_batch-test"))
        assertTrue(body.contains("changeset_"))
        assertTrue(body.contains("POST /b1s/v1/Orders(130939)/Close"))
        assertFalse(body.contains("{}"))
    }

    @Test
    fun `should build close http verb for entities`() {
        val service = mock<EntitiesService<*>>()
        val payload = BatchPayload("130939")

        whenever(service.path()).thenReturn("/b1s/v1/Orders")

        val http = BatchMethod.CLOSE.getHttp(service, payload)

        assertTrue(http == "POST /b1s/v1/Orders(130939)/Close")
    }
}
