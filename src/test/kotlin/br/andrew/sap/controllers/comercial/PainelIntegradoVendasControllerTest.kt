package br.andrew.sap.controllers.comercial

import br.andrew.sap.model.comercial.PainelTotais
import br.andrew.sap.services.comercial.PainelIntegradoVendasService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class PainelIntegradoVendasControllerTest {

    private val service = mock(PainelIntegradoVendasService::class.java)
    private val mvc = MockMvcBuilders.standaloneSetup(PainelIntegradoVendasController(service)).build()
    private val base = "/painel/pedidos/totais?dataInicial=20260101&dataFinal=20261231"

    private fun chamar(filial: String) = mvc.perform(get("$base$filial"))

    private fun totais(filiais: List<Int>) = verify(service).totais(
        eq("20260101"), eq("20261231"), eq(filiais), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), anyOrNull())

    @Test
    fun `filial unica, repetida e separada por virgula viram a mesma lista`() {
        whenever(service.totais(any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), anyOrNull()))
            .thenReturn(PainelTotais(0, 0, 0))
        chamar("&filial=2").andExpect(status().isOk)
        chamar("&filial=2&filial=5").andExpect(status().isOk)
        chamar("&filial=2,5").andExpect(status().isOk)
        totais(listOf(2))
        org.mockito.Mockito.verify(service, org.mockito.Mockito.times(2)).totais(
            any(), any(), eq(listOf(2, 5)), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), anyOrNull())
    }

    @Test
    fun `filial vazia responde 400 com mensagem e nao consulta`() {
        chamar("&filial=").andExpect(status().isBadRequest).andExpect(jsonPath("$.mensagem").exists())
        verify(service, never()).totais(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
    }
}
