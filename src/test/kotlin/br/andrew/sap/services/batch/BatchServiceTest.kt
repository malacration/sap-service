package br.andrew.sap.services.batch

import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.cadastro.BusinessPartnersService
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
    fun `manda header por requisicao dentro do changeset`() {
        // O PATCH que substitui colecao filha precisa do B1S-ReplaceCollectionsOnPatch: sem ele o
        // Service Layer faz merge e devolve 200 sem apagar a linha omitida. Antes nao havia lugar
        // pra esse header no batch, so o Content-Type fixo.
        val service = batchService()
        val entityService = mock<EntitiesService<*>>()
        whenever(entityService.path()).thenReturn("/b1s/v1/COB_TITULO")

        val body = service.body(
            "batch-test",
            BatchList().add(
                BatchMethod.PATCH, BatchPayload("'NF-500-1'"), entityService,
                mapOf("B1S-ReplaceCollectionsOnPatch" to "true"),
            ),
        ).toString(Charsets.UTF_8)

        assertTrue(body.contains("PATCH /b1s/v1/COB_TITULO('NF-500-1')"), body)
        assertTrue(body.contains("B1S-ReplaceCollectionsOnPatch: true"), body)
        assertTrue(body.contains("Content-Type: application/json"), "PATCH continua com corpo")
    }

    @Test
    fun `sem header informado nada muda pra quem ja usava batch`() {
        val service = batchService()
        val entityService = mock<EntitiesService<*>>()
        whenever(entityService.path()).thenReturn("/b1s/v1/Orders")

        val body = service.body("batch-test", BatchList().add(BatchMethod.PATCH, BatchPayload("130939"), entityService))
            .toString(Charsets.UTF_8)

        assertFalse(body.contains("B1S-"), body)
    }

    @Test
    fun `DELETE vai sem corpo, como as acoes Cancel e Close`() {
        // hasRequestBody era decidido pelo sufixo da URL, e DELETE termina em ")" igual ao PATCH -
        // sem olhar o metodo, ia um corpo JSON num DELETE.
        val service = batchService()
        val entityService = mock<EntitiesService<*>>()
        whenever(entityService.path()).thenReturn("/b1s/v1/COB_TITULO")

        val body = service.body("batch-test", BatchList().add(BatchMethod.DELETE, BatchPayload("'NF-500-1'"), entityService))
            .toString(Charsets.UTF_8)

        assertTrue(body.contains("DELETE /b1s/v1/COB_TITULO('NF-500-1')"), body)
        assertTrue(body.contains("Content-Length: 0"), body)
        assertFalse(body.contains("Content-Type: application/json"), "DELETE nao leva corpo")
    }

    private fun batchService() = BatchService(mock<RestTemplate>(), mock<SapEnvrioment>(), mock(), mock())

    @Test
    fun `should build close http verb for entities`() {
        val service = mock<EntitiesService<*>>()
        val payload = BatchPayload("130939")

        whenever(service.path()).thenReturn("/b1s/v1/Orders")

        val http = BatchMethod.CLOSE.getHttp(service, payload)

        assertTrue(http == "POST /b1s/v1/Orders(130939)/Close")
    }
}
