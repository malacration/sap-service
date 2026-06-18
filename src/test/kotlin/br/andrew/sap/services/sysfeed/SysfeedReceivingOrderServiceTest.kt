package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedReceivingPending
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SysfeedReceivingOrderServiceTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val configService = mock<SysfeedConfigService>()
    private val supplierService = mock<SysfeedSupplierService>()
    private val service = SysfeedReceivingOrderService(
        sqlQueriesService,
        configService,
        supplierService,
        "1"
    )

    init {
        whenever(supplierService.extractCodFornecedor("FOR0002977")).thenReturn("2977")
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
