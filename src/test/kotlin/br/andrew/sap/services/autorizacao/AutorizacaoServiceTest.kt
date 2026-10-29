package br.andrew.sap.services.autorizacao

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sistema.Autorizacao
import br.andrew.sap.model.sistema.Autorizador
import br.andrew.sap.model.sistema.TipoDocumentoAutorizacao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * @AUTORIZACAO e bott_MasterData: sem Code o service layer recusa o POST com
 * "Enter valid code [@AUTORIZACAO.Code]" (erro visto em producao). E motivo sem autorizador
 * nao pode reter documento: ficaria pendente sem ninguem para decidir.
 */
class AutorizacaoServiceTest {

    private val autorizadorService = mock<AutorizadorService>()
    private val service = spy(AutorizacaoService(mock(), mock(), mock(), mock(), mock(), autorizadorService, mock())).also {
        doReturn(OData(linkedMapOf("value" to """{"U_tipoDocumento":"COTACAO","U_motivo":"X","U_cardCode":"C","U_payload":"{}"}""")))
            .whenever(it).save(any())
    }

    private val documento = mock<Document>().also {
        whenever(it.CardCode).doReturn("C001")
    }

    private fun autorizadores(vararg motivos: String) {
        whenever(autorizadorService.getTodos()).doReturn(motivos.map { Autorizador(it, "fulano") })
    }

    private fun enviado(): Autorizacao {
        val captor = argumentCaptor<Autorizacao>()
        verify(service).save(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `motivo sem autorizador nao retem e explica o motivo`() {
        autorizadores("OUTRO_MOTIVO")

        val erro = assertThrows<Exception> {
            service.criar(TipoDocumentoAutorizacao.PEDIDO_VENDA, "CLIENTE_EM_ATRASO", documento, "vendedor")
        }

        assertTrue(erro.message!!.contains("CLIENTE_EM_ATRASO"), erro.message)
        assertTrue(erro.message!!.contains("autorizador"), erro.message)
        verify(service, never()).save(any())
    }

    @Test
    fun `Code vem da sequencia e vai como texto`() {
        autorizadores("CLIENTE_EM_ATRASO")
        doReturn(12).whenever(service).proximoCodigo()

        service.criar(TipoDocumentoAutorizacao.PEDIDO_VENDA, "CLIENTE_EM_ATRASO", documento, "vendedor")

        val autorizacao = enviado()
        assertEquals(12, autorizacao.Code)
        assertEquals("12", autorizacao.Name)
        val json = OData().mapper.writeValueAsString(autorizacao)
        assertTrue(json.contains("\"Code\":\"12\""), json)
    }

    @Test
    fun `maior codigo do SAP ignora ordem de texto e codigo nao numerico`() {
        val pagina = OData(linkedMapOf("value" to listOf(mapOf("Code" to "9"), mapOf("Code" to "10"), mapOf("Code" to "abc"))))
        doReturn(pagina).whenever(service).codigos()

        assertEquals(10, service.maiorCodigoNoSap())
    }
}
