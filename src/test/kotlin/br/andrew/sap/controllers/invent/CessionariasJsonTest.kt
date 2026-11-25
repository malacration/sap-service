package br.andrew.sap.controllers.invent

import br.andrew.sap.model.fiscal.CessionariaEditRequest
import br.andrew.sap.model.fiscal.CessionariaLote
import br.andrew.sap.services.fiscal.NotaFiscalLoteCessionariaService
import br.andrew.sap.services.fiscal.NotaFiscalLoteService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.MockMvcBuilders

// Passa pelo MockMvc real (conversores JSON do Spring): contrato JSON da tela de cessionarias.
class CessionariasJsonTest {
    private val lote = mock<NotaFiscalLoteService>()
    private val cessionarias = mock<NotaFiscalLoteCessionariaService>()
    private val mvc = MockMvcBuilders.standaloneSetup(NotaFiscalLoteController(lote, mock(), mock(), mock(), cessionarias)).build()

    @Test
    fun `GET e PATCH devolvem nomePdf e nunca o telefone`() {
        whenever(lote.cessionarias()).doReturn(listOf(CessionariaLote("00000000000191", "X", "(65) 3000-0000", "X Ltda")))
        mvc.perform(get("/nota-fiscal-lote/cessionarias"))
            .andExpect(jsonPath("$[0].nomePdf").value("X Ltda"))
            .andExpect(jsonPath("$[0].telefone").doesNotExist())
        whenever(cessionarias.editar(any(), any())).doReturn(CessionariaLote("00000000000191", "X", "(65) 3000-0000", "X Ltda"))
        mvc.perform(patch("/nota-fiscal-lote/cessionarias/00000000000191").contentType(MediaType.APPLICATION_JSON).content("""{"nome":"X","nomePdf":"X Ltda"}"""))
            .andExpect(jsonPath("$.nomePdf").value("X Ltda"))
            .andExpect(jsonPath("$.telefone").doesNotExist())
    }

    @Test
    fun `PATCH sem nomePdf, com null e com texto chega ao servico com a semantica certa`() {
        whenever(cessionarias.editar(any(), any())).doReturn(CessionariaLote("1", "X"))
        listOf("""{"nome":"N"}""", """{"nome":"N","nomePdf":null}""", """{"nome":"N","nomePdf":"T"}""").forEach {
            mvc.perform(patch("/nota-fiscal-lote/cessionarias/1").contentType(MediaType.APPLICATION_JSON).content(it))
        }
        val captor = argumentCaptor<CessionariaEditRequest>()
        verify(cessionarias, times(3)).editar(any(), captor.capture())
        val (ausente, nulo, texto) = captor.allValues
        assertFalse(ausente.nomePdfInformado)
        assertTrue(nulo.nomePdfInformado)
        assertEquals(null, nulo.nomePdf)
        assertTrue(texto.nomePdfInformado)
        assertEquals("T", texto.nomePdf)
    }
}
