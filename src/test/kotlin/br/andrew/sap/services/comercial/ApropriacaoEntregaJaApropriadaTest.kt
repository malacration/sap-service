package br.andrew.sap.services.comercial

import JournalEntry
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.financeiro.InternalReconciliationsService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.nio.file.Files
import java.nio.file.Path

/**
 * Cada VFET da mesma entrega gerava a sua apropriacao. Caso real: contrato 342, NF 72733 apropriada
 * duas vezes (72736 e 72737), R$ 19.714,37 do adiantamento do cliente consumidos em dobro.
 */
class ApropriacaoEntregaJaApropriadaTest {

    private val invoiceService = mock<InvoiceService>()

    private fun service(apropriacoes: List<Map<String, Any?>>): ApropriacaoVendaFuturaService {
        val odbc = mock<OdbcClient> {
            on { consultar(any(), eq(mapOf("nota" to 249752)), any()) } doReturn QueryResponse(rows = apropriacoes)
        }
        return ApropriacaoVendaFuturaService(
            invoiceService, mock<JournalEntriesService>(), mock<InternalReconciliationsService>(), mock<BatchService>(),
            odbc, "FIN0000002", 10, 9, "1.1.2.01",
        )
    }

    private val entrega = Invoice("CLI0005057", null, listOf(Product("PAC0000100", "1", "19714.37")), "4").also {
        it.docEntry = 249752
        it.docNum = "72733"
        it.U_venda_futura = 342
    }

    private val segundoVfet = JournalEntry(listOf(), "Reclassificação entrega. Mercadoria venda futura [342]. NF Num 72733")
        .also { it.JdtNum = 1491668 }

    @Test
    fun `entrega ja apropriada nao ganha segunda apropriacao`() {
        val erro = assertThrows<EntregaJaApropriadaException> {
            service(listOf(mapOf("DocEntry" to 249757, "DocNum" to 72736, "Apropriado" to 19714.37)))
                .conciliar(entrega, segundoVfet, listOf())
        }

        assertTrue(erro.message!!.contains("72736"))
        verify(invoiceService, never()).save(anyOrNull())
    }

    @Test
    fun `consulta conta so apropriacoes nao canceladas da nota`() {
        val sql = Files.readString(Path.of("src/main/resources/odbc/apropriacao-entrega-ativa.sql"))

        assertTrue(sql.contains("apropriacao.\"U_TX_DocEntryRef\" = :nota"))
        assertTrue(sql.contains("apropriacao.\"CANCELED\" = 'N'"))
        assertTrue(sql.contains("INNER JOIN INV9 adiantamento"))
    }
}
