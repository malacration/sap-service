package br.andrew.sap.services.fiscal

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Base64

class TaxNfeServiceConteudoTest {

    private val nfeProc = "<nfeProc versao=\"4.00\" xmlns=\"http://www.portalfiscal.inf.br/nfe\"><NFe/></nfeProc>"

    // Formato real do consultar/xml (DocEntry 251441, 2026-10-01): JSON com o XML em base64.
    private fun envelope(xml: String?, sucesso: Boolean = true, menssagem: String = "Sucesso!", pdf: String? = null) =
        """{"sucesso":$sucesso,"menssagem":"$menssagem","batchId":"813251441","statusId":"4","docType":"13",
           "docEntry":"251441","docNum":null,"pdf":${pdf?.let { "\"$it\"" } ?: "null"},"imprimirLink":false,
           "linkDeImpressao":null,"xml":${xml?.let { "\"$it\"" } ?: "null"}}""".toByteArray()

    private fun b64(texto: String) = Base64.getEncoder().encodeToString(texto.toByteArray())

    @Test
    fun `xml em base64 dentro do JSON da TaxPlus vira o nfeProc`() {
        val xml = TaxNfeService.extraiArquivo("xml", envelope(b64(nfeProc)))
        assertArrayEquals(nfeProc.toByteArray(), xml)
    }

    @Test
    fun `base64 com quebra de linha tambem decodifica`() {
        val quebrado = Base64.getMimeEncoder(16, "\r\n".toByteArray()).encodeToString(nfeProc.toByteArray())
        // no JSON a quebra vem escapada (\r\n); o Jackson devolve CR/LF de verdade para o decoder
        val emJson = quebrado.replace("\r\n", "\\r\\n")
        assertArrayEquals(nfeProc.toByteArray(), TaxNfeService.extraiArquivo("xml", envelope(emJson)))
    }

    @Test
    fun `xml cru continua aceito`() {
        assertArrayEquals(nfeProc.toByteArray(), TaxNfeService.extraiArquivo("xml", nfeProc.toByteArray()))
        val comBom = "﻿<?xml version=\"1.0\"?>$nfeProc".toByteArray()
        assertArrayEquals(comBom, TaxNfeService.extraiArquivo("xml", comBom))
    }

    @Test
    fun `pdf cru passa direto e pdf em base64 no envelope tambem`() {
        val pdf = "%PDF-1.4\n...".toByteArray()
        assertArrayEquals(pdf, TaxNfeService.extraiArquivo("pdf", pdf))
        assertArrayEquals(pdf, TaxNfeService.extraiArquivo("pdf", envelope(null, pdf = Base64.getEncoder().encodeToString(pdf))))
    }

    @Test
    fun `sucesso false traz a mensagem da TaxPlus`() {
        val erro = assertThrows(Exception::class.java) {
            TaxNfeService.extraiArquivo("xml", envelope(null, sucesso = false, menssagem = "Documento nao autorizado"))
        }
        assertTrue(erro.message!!.contains("Documento nao autorizado"), erro.message)
    }

    @Test
    fun `envelope sem o arquivo pedido e erro, nao arquivo vazio`() {
        val erro = assertThrows(Exception::class.java) { TaxNfeService.extraiArquivo("xml", envelope(null)) }
        assertTrue(erro.message!!.contains("sem o xml"), erro.message)
        assertThrows(Exception::class.java) { TaxNfeService.extraiArquivo("pdf", envelope(b64(nfeProc))) }
    }

    @Test
    fun `base64 que nao e xml nao entra no zip`() {
        assertThrows(Exception::class.java) { TaxNfeService.extraiArquivo("xml", envelope(b64("{\"nao\":\"xml\"}"))) }
    }

    @Test
    fun `resposta que nao e arquivo nem JSON e erro, sem o corpo na mensagem`() {
        val erro = assertThrows(Exception::class.java) {
            TaxNfeService.extraiArquivo("xml", "Internal error at 10.117.163.209".toByteArray())
        }
        assertTrue(erro.message == "TaxPlus nao devolveu um xml valido", erro.message)
        assertThrows(Exception::class.java) { TaxNfeService.extraiArquivo("pdf", ByteArray(0)) }
        assertDoesNotThrow { TaxNfeService.validaConteudo("pdf", "%PDF-1.7".toByteArray()) }
    }
}
