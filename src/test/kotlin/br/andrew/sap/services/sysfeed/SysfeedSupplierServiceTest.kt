package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedSupplierPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class SysfeedSupplierServiceTest {
    private val service = SysfeedSupplierService(
        mock<SqlQueriesService>()
    )

    @Test
    fun `deve montar payload de fornecedor`() {
        val payload = service.buildPayload(
            SysfeedSupplierPending(
                CardCode = "FOR0002977",
                CardName = "Fornecedor Teste",
                Cnpj = "00.000.000/0001-00",
                InscricaoEstadual = null,
                Endereco = "Rua Teste",
                Cep = "76.900-000",
                Telefone = "(69) 99999-9999"
            )
        )

        assertEquals("FOR0002977", payload.cardCode)
        assertEquals("Fornecedor Teste", payload.cardName)
        assertEquals("00000000000100", payload.cnpj)
        assertEquals("0", payload.inscricaoEstadual)
        assertEquals("Rua Teste", payload.endereco)
        assertEquals("76900000", payload.cep)
        assertEquals("69999999999", payload.telefone)
        assertEquals("ATIVO", payload.status)
    }

    @Test
    fun `deve extrair codigo do fornecedor`() {
        assertEquals("2977", service.extractCodFornecedor("FOR0002977"))
    }

    @Test
    fun `deve bloquear fornecedor fora do padrao FOR`() {
        assertThrows(SysfeedSupplierException::class.java) {
            service.extractCodFornecedor("ABC0002977")
        }
    }

    @Test
    fun `deve bloquear fornecedor menor que trinta e dois`() {
        assertThrows(SysfeedSupplierException::class.java) {
            service.buildPayload(SysfeedSupplierPending(CardCode = "FOR0000031", CardName = "Fornecedor Teste"))
        }
    }

    @Test
    fun `deve validar campos obrigatorios`() {
        assertThrows(SysfeedSupplierException::class.java) {
            service.buildPayload(SysfeedSupplierPending(CardCode = "FOR0002977", CardName = ""))
        }
    }

}
