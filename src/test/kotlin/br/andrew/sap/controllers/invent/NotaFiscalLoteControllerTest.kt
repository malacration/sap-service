package br.andrew.sap.controllers.invent

import br.andrew.sap.model.fiscal.NotaFiscalLoteArquivo
import br.andrew.sap.model.fiscal.NotaFiscalLoteRequest
import br.andrew.sap.services.fiscal.NotaFiscalLoteService
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.nio.file.Files

/** O zip sai de um temporario: tem de ir inteiro, com tamanho certo, e sumir ao fechar o stream. */
class NotaFiscalLoteControllerTest {

    @Test
    fun `download envia o temporario inteiro e apaga ao fechar`() {
        val temporario = Files.createTempFile("notas-fiscais-", ".zip")
        val bytes = ByteArray(70_000) { (it % 251).toByte() }
        Files.write(temporario, bytes)
        val service = mock<NotaFiscalLoteService>().also {
            whenever(it.zip(any(), any(), any(), any(), any())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 2, 3))
        }

        val resposta = NotaFiscalLoteController(service, mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))

        assertEquals(bytes.size.toLong(), resposta.headers.contentLength)
        assertEquals("2", resposta.headers.getFirst("notas-com-erro"))
        assertEquals("3", resposta.headers.getFirst("notas-sem-boleto"))
        assertTrue(resposta.headers.getFirst("Content-Disposition")!!.contains("notas.zip"))
        val enviado = resposta.body!!.inputStream.use { it.readBytes() }
        assertArrayEquals(bytes, enviado)
        assertFalse(Files.exists(temporario))
    }

    @Test
    fun `falha ao abrir o temporario propaga e nao deixa arquivo`() {
        val temporario = Files.createTempFile("notas-fiscais-", ".zip")
        Files.delete(temporario)
        val service = mock<NotaFiscalLoteService>().also {
            whenever(it.zip(any(), any(), any(), any(), any())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 0, 0))
        }
        assertThrows(java.nio.file.NoSuchFileException::class.java) {
            NotaFiscalLoteController(service, mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))
        }
        assertFalse(Files.exists(temporario))
    }
}
