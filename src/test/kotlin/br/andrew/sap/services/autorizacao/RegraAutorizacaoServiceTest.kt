package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sistema.RegraFilial
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RegraAutorizacaoServiceTest {

    private val regraFilialService = spy(RegraFilialService(mock(), mock(), mock()))

    private fun regra(motivo: String, bate: Boolean) = mock<RegraAutorizacao>().also {
        whenever(it.motivo).doReturn(motivo)
        whenever(it.avalia(org.mockito.kotlin.any())).doReturn(bate)
    }

    private fun documento(filial: String) = mock<Document>().also {
        whenever(it.getBPL_IDAssignedToInvoice()).doReturn(filial)
    }

    private fun cadastrado(vararg linhas: RegraFilial) {
        doReturn(linhas.toList()).whenever(regraFilialService).getTodos()
    }

    @Test
    fun `cadastro de filial e lido uma vez por avaliacao, qualquer que seja o numero de regras`() {
        cadastrado(RegraFilial("B", "2"))
        val service = RegraAutorizacaoService(
            listOf(regra("A", false), regra("B", false), regra("C", true)), regraFilialService)

        assertEquals("C", service.avaliar(documento("1")))

        verify(regraFilialService, times(1)).getTodos()
    }

    @Test
    fun `regra restrita a outra filial nao e avaliada`() {
        cadastrado(RegraFilial("A", "2"))
        val restrita = regra("A", true)
        val service = RegraAutorizacaoService(listOf(restrita), regraFilialService)

        assertNull(service.avaliar(documento("1")))
        assertEquals("A", service.avaliar(documento("2")))

        //so a avaliacao da filial 2 chegou a consultar a regra
        verify(restrita, times(1)).avalia(org.mockito.kotlin.any())
    }

    @Test
    fun `motivo sem linha cadastrada vale em toda filial`() {
        cadastrado(RegraFilial("OUTRO", "9"))
        val livre = regra("A", true)
        val service = RegraAutorizacaoService(listOf(livre), regraFilialService)

        assertEquals("A", service.avaliar(documento("1")))
    }

    @Test
    fun `sem cadastro de filial, regras seguem ativas`() {
        cadastrado()
        val naoBate = regra("A", false)
        val service = RegraAutorizacaoService(listOf(naoBate), regraFilialService)

        assertNull(service.avaliar(documento("1")))
        verify(naoBate, times(1)).avalia(org.mockito.kotlin.any())
    }
}
