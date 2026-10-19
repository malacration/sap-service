package br.andrew.sap.services.autorizacao

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.cadastro.BussinessPlace
import br.andrew.sap.model.sistema.RegraFilial
import br.andrew.sap.services.cadastro.BussinessPlaceService
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

class RegraFilialServiceTest {

    private val filiais = mock<BussinessPlaceService>().also {
        whenever(it.getAllBusinessPlaces()).doReturn(listOf(
            BussinessPlace().also { f -> f.BPLID = 1 }, BussinessPlace().also { f -> f.BPLID = 2 }))
    }
    private val service = spy(RegraFilialService(mock(), mock(), mock(), filiais)).also {
        doReturn(emptyList<RegraFilial>()).whenever(it).getTodos()
        doReturn(OData(linkedMapOf("value" to """{"U_motivo":"X","U_filial":"1"}"""))).whenever(it).save(any())
    }

    @Test
    fun `filial inexistente e recusada sem gravar - desligaria a regra em toda filial`() {
        listOf("99", "invalid", "0").forEach { filial ->
            val erro = assertThrows<Exception> { service.criar(RegraFilial("CREDITO", filial), listOf("CREDITO")) }
            assertTrue(erro.message!!.contains("nao existe no SAP"), erro.message)
        }
        verify(service, never()).save(any())
    }

    @Test
    fun `filial e gravada no formato canonico do BPLID`() {
        service.criar(RegraFilial("CREDITO", " 01 "), listOf("CREDITO"))

        val captor = argumentCaptor<RegraFilial>()
        verify(service).save(captor.capture())
        assertEquals("1", captor.firstValue.U_filial)
    }
}
