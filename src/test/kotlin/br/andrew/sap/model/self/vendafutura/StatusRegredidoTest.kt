package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.enums.Cancelled
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * O status do contrato so avancava. Caso real: contrato 585 concluido com 12 sacas entregues; a
 * devolucao de 2 sacas deixou 10 entregues e ele continuou concluido.
 */
class StatusRegredidoTest {

    private fun contrato(status: Status, quantidade: Double = 12.0) = Contrato(
        1, "CLI0007239",
        mutableListOf(Item("PAC0000112", "OX PROTEINA", 80.0, quantidade, 83.87, 9.09, 0.0, "SACA 30KG")),
        79, "REGINALDO", 11,
    ).also { it.U_status = status }

    private fun documento(tipo: DocumentTypes, quantidade: String, cancelado: Cancelled = Cancelled.tNO): Document =
        Invoice("CLI0007239", null, listOf(Product("PAC0000112", quantidade, "80")), "11").also {
            it.docObjectCode = tipo
            it.Cancelled = cancelado
        }

    private val entregas585 = listOf(
        documento(DocumentTypes.oInvoices, "4"),
        documento(DocumentTypes.oInvoices, "2"),
        documento(DocumentTypes.oInvoices, "6"),
    )

    @Test
    fun `concluido com devolucao volta para aberto`() {
        val entregas = entregas585 + documento(DocumentTypes.oCreditNotes, "2")
        assertEquals(Status.aberto, contrato(Status.concluido).statusRegredido(entregas))
    }

    @Test
    fun `entregue com nota cancelada volta para aberto`() {
        val entregas = listOf(
            documento(DocumentTypes.oInvoices, "4"),
            documento(DocumentTypes.oInvoices, "8", Cancelled.tYES),
        )
        assertEquals(Status.aberto, contrato(Status.entregue).statusRegredido(entregas))
    }

    @Test
    fun `tudo entregue nao regride`() {
        assertNull(contrato(Status.concluido).statusRegredido(entregas585))
    }

    @Test
    fun `aberto e cancelado nunca sao alterados`() {
        val entregas = entregas585 + documento(DocumentTypes.oCreditNotes, "2")
        assertNull(contrato(Status.aberto).statusRegredido(entregas))
        assertNull(contrato(Status.cancelado).statusRegredido(entregas))
    }

    @Test
    fun `candidatos sao so entregue ou concluido com devolucao ou cancelamento posterior`() {
        val sql = Files.readString(Path.of("src/main/resources/odbc/contratos-para-reabrir.sql"))

        assertTrue(sql.contains("vf.\"U_status\" IN ('entregue', 'concluido')"))
        assertTrue(sql.contains("devolucao.\"CreateDate\" >= vf.\"UpdateDate\""))
        assertTrue(sql.contains("nota.\"CANCELED\" = 'Y'"))
        assertTrue(sql.contains("nota.\"UpdateDate\" >= vf.\"UpdateDate\""))
    }
}
