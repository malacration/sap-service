package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.services.autorizacao.AutorizacaoService
import br.andrew.sap.services.autorizacao.RegraAutorizacaoService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.QuotationsService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations

class OfflineQuotationServiceTest {

    private val redis = mock<StringRedisTemplate>()
    private val values = mock<ValueOperations<String, String>>()
    private val quotations = mock<QuotationsService>()
    private val items = mock<ItemsService>()
    private val partners = mock<BusinessPartnersService>()
    private val regions = mock<RegiaoService>()
    private val localities = mock<LocalidadeService>()
    private val rules = mock<RegraAutorizacaoService>()
    private val authorizations = mock<AutorizacaoService>()
    private val mapper = jacksonObjectMapper().findAndRegisterModules()
    private val service = OfflineQuotationService(
        redis,
        mapper,
        quotations,
        items,
        partners,
        regions,
        localities,
        rules,
        authorizations
    )

    @Test
    fun `repetir transmissionId devolve recibo salvo sem acessar o SAP`() {
        val cached = OfflineQuotationSyncResponse(
            transmissionId = TRANSMISSION_ID,
            localId = "local-1",
            status = OfflineQuotationStatus.CREATED,
            docEntry = 321,
            docNum = "9001"
        )
        whenever(redis.opsForValue()).thenReturn(values)
        whenever(values.get("offline:quotation:result:55:$TRANSMISSION_ID"))
            .thenReturn(mapper.writeValueAsString(cached))

        val response = service.sync(request(), user("55"))

        assertEquals(cached, response)
        verifyNoInteractions(quotations, items, partners, regions, localities, rules, authorizations)
    }

    @Test
    fun `identificador invalido e rejeitado antes de acessar Redis ou SAP`() {
        val invalid = request().copy(transmissionId = "curto")

        assertThrows<IllegalArgumentException> { service.sync(invalid, user("55")) }

        verifyNoInteractions(redis, quotations)
    }

    private fun request() = OfflineQuotationSyncRequest(
        transmissionId = TRANSMISSION_ID,
        localId = "local-1",
        catalogId = "catalog-1",
        createdOfflineAt = null,
        quotation = Quotation("CLI001", "2026-08-28", emptyList(), "2")
    )

    private fun user(id: String) = User(
        id = id,
        _name = "Vendedor $id",
        origin = UserOriginEnum.SalePerson,
        userName = "vendedor$id",
        bussinesPlace = listOf(2)
    )

    companion object {
        private const val TRANSMISSION_ID = "offline-transmission-0001"
    }
}
