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
            whenever(it.zip(any(), any(), any(), any(), any(), any(), anyOrNull(), any(), anyOrNull(), any())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 2, 3))
        }

        val resposta = NotaFiscalLoteController(service, mock(), mock(), mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))

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
            whenever(it.zip(any(), any(), any(), any(), any(), any(), anyOrNull(), any(), anyOrNull(), any())).doReturn(NotaFiscalLoteArquivo("notas.zip", temporario, 0, 0))
        }
        assertThrows(java.nio.file.NoSuchFileException::class.java) {
            NotaFiscalLoteController(service, mock(), mock(), mock(), mock()).download(NotaFiscalLoteRequest(listOf(1)))
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
        val controller = NotaFiscalLoteController(mock(), mock(), mock(), importacao, mock())
        val grande = MockHttpServletRequest().apply { setContent(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
        val msg = "A planilha e grande demais (limite de 1 MB para CSV e 10 MB para XLSX)."
        assertEquals(msg, assertThrows(br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException::class.java) { controller.importarPlanilha(grande) }.message)
        // sem Content-Length, a leitura com teto segura do mesmo jeito
        val semTamanho = object : MockHttpServletRequest() { override fun getContentLengthLong() = -1L }
            .apply { setContent(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
        assertEquals(msg, assertThrows(br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException::class.java) { controller.importarPlanilha(semTamanho) }.message)
        assertTrue(recebidos.isEmpty())

        controller.importarPlanilha(MockHttpServletRequest().apply { setContent("xlsx".toByteArray()) })
        assertArrayEquals("xlsx".toByteArray(), recebidos.single())
    }

    @Test
    fun `corpo ilegivel responde 400 com o corpo padrao de erro`() {
        val r = NotaFiscalLoteController(mock(), mock(), mock(), mock(), mock())
            .onCorpoIlegivel(org.springframework.http.converter.HttpMessageNotReadableException("x", mock<org.springframework.http.HttpInputMessage>()))
        assertEquals(400, r.statusCode.value())
        assertEquals("Corpo da requisicao invalido.", r.body!!.mensagem)
    }

    @Test
    fun `importar-planilha - modo NUMERO_NOTA chama o importador por numero e ignora os filtros`() {
        val importacao = mock<NotaFiscalLoteImportacaoService>()
        val chamadas = mutableListOf<String>()
        whenever(importacao.importar(any())).thenAnswer { chamadas.add("nosso"); ImportacaoLoteResultado(listOf(), listOf()) }
        whenever(importacao.importarPorNumeroNota(any())).thenAnswer { chamadas.add("numero"); ImportacaoLoteResultado(listOf(), listOf()) }
        val controller = NotaFiscalLoteController(mock(), mock(), mock(), importacao, mock())
        fun req() = MockHttpServletRequest().apply { setContent("x".toByteArray()) }
        controller.importarPlanilha(req())
        controller.importarPlanilha(req(), br.andrew.sap.model.fiscal.ModoImportacaoLote.NOSSO_NUMERO, filial = listOf(5))
        controller.importarPlanilha(req(), br.andrew.sap.model.fiscal.ModoImportacaoLote.NUMERO_NOTA, filial = listOf(5), dataInicial = "2026-10-01")
        assertEquals(listOf("nosso", "nosso", "numero"), chamadas)
    }

    @Test
    fun `erros de entrada da importacao respondem 400 e arquivo grande 413, so no controller`() {
        val controller = NotaFiscalLoteController(mock(), mock(), mock(), mock(), mock())
        val invalida = controller.onImportacaoInvalida(br.andrew.sap.model.fiscal.ImportacaoInvalidaException("CSV invalido: aspas sem fechamento na linha 3"))
        assertEquals(400, invalida.statusCode.value()); assertEquals("CSV invalido: aspas sem fechamento na linha 3", invalida.body!!.mensagem)
        val grande = controller.onArquivoGrande(br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException("A planilha e grande demais (limite de 10 MB)."))
        assertEquals(413, grande.statusCode.value())
        // modo desconhecido: erro de binding vira 400
        val modo = org.springframework.web.method.annotation.MethodArgumentTypeMismatchException(
            "XYZ", br.andrew.sap.model.fiscal.ModoImportacaoLote::class.java, "modo", mock<org.springframework.core.MethodParameter>(), IllegalArgumentException("x"))
        val r = controller.onParametroInvalido(modo)
        assertEquals(400, r.statusCode.value()); assertEquals("Parametro 'modo' invalido.", r.body!!.mensagem)
        // o arquivo grande demais lancado pelo controller e a subclasse tratada como 413
        val importacao = mock<NotaFiscalLoteImportacaoService>()
        val c2 = NotaFiscalLoteController(mock(), mock(), mock(), importacao, mock())
        val grandeReq = MockHttpServletRequest().apply { setContent(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
        assertThrows(br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException::class.java) { c2.importarPlanilha(grandeReq) }
        // erro do import (entrada) e uma ImportacaoInvalidaException; falha de outro tipo nao e capturada pelos handlers
        assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) {
            NotaFiscalLoteImportacaoService(mock(), mock()).importar("a,b\n1,2\n".toByteArray())
        }
    }

    @Test
    fun `editar cessionaria delega ao servico e erro de entrada responde com o status e o corpo padrao`() {
        val servico = mock<br.andrew.sap.services.fiscal.NotaFiscalLoteCessionariaService>()
        val pedido = br.andrew.sap.model.fiscal.CessionariaEditRequest("Novo", "Novo Ltda")
        val atualizada = br.andrew.sap.model.fiscal.CessionariaLote("11222333000181", "Novo")
        whenever(servico.editar("11222333000181", pedido)).doReturn(atualizada)
        val c = NotaFiscalLoteController(mock(), mock(), mock(), mock(), servico)
        assertEquals(atualizada, c.editarCessionaria("11222333000181", pedido))
        val r = c.onCessionariaInvalida(br.andrew.sap.model.fiscal.CessionariaInvalidaException("Cessionaria nao encontrada.", org.springframework.http.HttpStatus.NOT_FOUND))
        assertEquals(404, r.statusCode.value())
        assertEquals("Cessionaria nao encontrada.", r.body!!.mensagem)
    }
}
