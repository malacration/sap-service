package br.andrew.sap.controllers.invent

import br.andrew.sap.model.fiscal.NotaFiscalLoteArquivo
import br.andrew.sap.model.fiscal.NotaFiscalLoteRequest
import br.andrew.sap.model.fiscal.ImportacaoLoteResultado
import br.andrew.sap.services.fiscal.NotaFiscalLoteImportacaoService
import br.andrew.sap.services.fiscal.NotaFiscalLoteService
import br.andrew.sap.services.fiscal.PlanilhaXlsx
import org.springframework.mock.web.MockHttpServletRequest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
            whenever(it.zip(any(), any(), any(), any(), any(), any(), anyOrNull())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 2, 3))
        }

        val resposta = NotaFiscalLoteController(service, mock(), mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))

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
            whenever(it.zip(any(), any(), any(), any(), any(), any(), anyOrNull())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 0, 0))
        }
        assertThrows(java.nio.file.NoSuchFileException::class.java) {
            NotaFiscalLoteController(service, mock(), mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))
        }
        assertFalse(Files.exists(temporario))
    }

    @Test
    fun `planilha acima do teto e recusada sem chegar ao servico, abaixo passa inteira`() {
        val recebidos = mutableListOf<ByteArray>()
        val importacao = mock<NotaFiscalLoteImportacaoService>().also {
            whenever(it.importar(any())).thenAnswer { inv ->
                recebidos.add(inv.arguments[0] as ByteArray); ImportacaoLoteResultado(listOf(), listOf())
            }
        }
        val controller = NotaFiscalLoteController(mock(), mock(), mock(), importacao)
        val grande = MockHttpServletRequest().apply { setContent(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
        assertThrows(IllegalArgumentException::class.java) { controller.importarPlanilha(grande) }
        // sem Content-Length, a leitura com teto segura do mesmo jeito
        val semTamanho = object : MockHttpServletRequest() { override fun getContentLengthLong() = -1L }
            .apply { setContent(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
        assertThrows(IllegalArgumentException::class.java) { controller.importarPlanilha(semTamanho) }
        assertTrue(recebidos.isEmpty())

        controller.importarPlanilha(MockHttpServletRequest().apply { setContent("xlsx".toByteArray()) })
        assertArrayEquals("xlsx".toByteArray(), recebidos.single())
    }
}
