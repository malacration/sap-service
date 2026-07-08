package br.andrew.sap.services.sysfeed

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SysfeedReceivingOrderServiceTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val supplierService = mock<SysfeedSupplierService>()
    private val service = SysfeedReceivingOrderService(
        sqlQueriesService,
        supplierService,
        "1"
    )

    init {
        whenever(supplierService.extractCodFornecedor("FOR0002977")).thenReturn("2977")
    }

    @Test
    fun `deve usar dataCorte e usage informados`() {
        service.getPendingPayloads("2026-05-10", listOf(20))

        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService).execute(eq("sysfeed-ordens-recebimento-pendentes.sql"), captor.capture())
        assertEquals("startDate='2026-05-10'", captor.firstValue[0].toString())
        assertEquals("usage=20", captor.firstValue[1].toString())
    }

    @Test
    fun `deve consultar uma vez por usage informado`() {
        service.getPendingPayloads("2026-01-01", listOf(15, 20))

        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService, times(2)).execute(eq("sysfeed-ordens-recebimento-pendentes.sql"), captor.capture())
        assertEquals(listOf("usage=15", "usage=20"), captor.allValues.map { it[1].toString() })
        assertEquals("startDate='2026-01-01'", captor.firstValue[0].toString())
    }

    @Test
    fun `deve bloquear dataCorte ausente no recebimento`() {
        assertThrows(SysfeedReceivingValidationException::class.java) {
            service.getPendingPayloads("", listOf(15))
        }
    }

    @Test
    fun `deve bloquear usage ausente no recebimento`() {
        assertThrows(SysfeedReceivingValidationException::class.java) {
            service.getPendingPayloads("2026-01-01", emptyList())
        }
    }

    @Test
    fun `deve bloquear dataCorte invalida no recebimento`() {
        assertThrows(SysfeedReceivingValidationException::class.java) {
            service.getPendingPayloads("data-invalida", listOf(15))
        }
    }

    @Test
    fun `deve montar recebimento por linha`() {
        val pending = pending()

        val payload = service.buildPayload(pending)

        assertEquals("1475400", payload.NrProducao)
        assertEquals("1", payload.CodProd)
        assertEquals("2977", payload.CodFornecedor)
        assertEquals("10000", payload.PesoNominalNF)
        assertEquals("GRANEL", payload.TipoProdutoRecebimento)
        assertEquals("0", payload.NrBag)
        assertEquals("147540", payload.NrNotaFiscal)
        assertEquals("LOTE-001", payload.NrLoteCodigoRecebimento)
        assertEquals("ABC1D23", payload.Placa)
        assertEquals("NAO", payload.RegLido)
    }

    @Test
    fun `deve bloquear nr producao com mais de oito digitos`() {
        val pending = pending(docEntry = 10000000, lineNum = 0)

        assertThrows(SysfeedReceivingValidationException::class.java) {
            service.buildPayload(pending)
        }
    }

    @Test
    fun `deve montar recebimento de nota de entrada com docentry e linha`() {
        val pending = SysfeedReceivingPending(
            DocEntry = 147540,
            LineNum = 0,
            DocNum = "147540",
            CardCode = "FOR0002977",
            Serial = null,
            ItemCode = "INS000001",
            CodProd = "1",
            Quantity = "10000",
            NrLoteCodigoRecebimento = "LOTE-001",
            Placa = "ABC1D23",
            SysfeedStatus = null
        )

        val payload = service.buildPayload(pending)

        assertEquals("1475400", payload.NrProducao)
        assertEquals("1", payload.CodProd)
        assertEquals("10000", payload.PesoNominalNF)
        assertEquals("147540", payload.NrNotaFiscal)
    }

    private fun pending(docEntry: Int = 147540, lineNum: Int = 0): SysfeedReceivingPending {
        return SysfeedReceivingPending(
            DocEntry = docEntry,
            LineNum = lineNum,
            DocNum = "147540",
            CardCode = "FOR0002977",
            Serial = null,
            ItemCode = "INS000001",
            Quantity = "10000",
            NrLoteCodigoRecebimento = "LOTE-001",
            Placa = "ABC1D23",
            CodProd = "1",
            SysfeedStatus = null
        )
    }
}
