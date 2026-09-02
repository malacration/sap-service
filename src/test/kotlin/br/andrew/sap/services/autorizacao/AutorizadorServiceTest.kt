package br.andrew.sap.services.autorizacao

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sistema.Autorizador
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

/**
 * @AUTORIZADOR e bott_MasterData: o service layer recusa POST sem codigo com
 * "Enter valid code [@AUTORIZADOR.Code]", mesmo com ManageSeries ligado no UDO - a numeracao
 * automatica so vale com uma serie configurada no SAP. O Code e gerado no service.
 */
class AutorizadorServiceTest {

    private val regraService = mock<RegraAutorizacaoService>().also {
        whenever(it.motivos()).doReturn(listOf("CLIENTE_EM_ATRASO", "CREDITO", "DESCONTO", "PRAZO"))
    }

    private val service = spy(AutorizadorService(mock(), mock(), mock(), regraService))

    private fun jaCadastrados(vararg existentes: Autorizador) {
        doReturn(existentes.toList()).whenever(service).getTodos()
        //o save devolve o registro criado; o conteudo nao importa aqui, so precisa desserializar
        doReturn(OData(linkedMapOf("value" to """{"U_motivo":"X","U_usuario":"Y"}""")))
            .whenever(service).save(any())
    }

    private fun autorizador(code: String?, motivo: String, usuario: String) =
        Autorizador(motivo, usuario).also { it.Code = code }

    private fun enviado(): Autorizador {
        val captor = argumentCaptor<Autorizador>()
        org.mockito.Mockito.verify(service).save(captor.capture())
        return captor.firstValue
    }

    @Test
    fun `primeiro cadastro recebe o codigo 1`() {
        jaCadastrados()

        service.criar(Autorizador("DESCONTO", "rovema"))

        assertEquals("1", enviado().Code)
    }

    @Test
    fun `codigo segue a sequencia dos existentes`() {
        jaCadastrados(
            autorizador("1", "DESCONTO", "ana"),
            autorizador("7", "PRAZO", "bruno"))

        service.criar(Autorizador("CREDITO", "rovema"))

        assertEquals("8", enviado().Code)
    }

    /** Code alfanumerico e a regra do UDO: codigo nao numerico nao pode quebrar a geracao. */
    @Test
    fun `codigo nao numerico existente nao quebra a sequencia`() {
        jaCadastrados(
            autorizador("ADM", "DESCONTO", "ana"),
            autorizador("2", "PRAZO", "bruno"))

        service.criar(Autorizador("CREDITO", "rovema"))

        assertEquals("3", enviado().Code)
    }

    /** Em UDT o Name costuma ser unico - espelhar o Code evita colisao por truncamento. */
    @Test
    fun `name acompanha o codigo`() {
        jaCadastrados()

        service.criar(Autorizador("DESCONTO", "rovema"))

        assertEquals(enviado().Code, enviado().Name)
    }

    @Test
    fun `recusa autorizador duplicado para o mesmo motivo e usuario`() {
        jaCadastrados(autorizador("1", "DESCONTO", "rovema"))

        val erro = assertThrows<Exception> { service.criar(Autorizador("DESCONTO", "rovema")) }

        assertTrue(erro.message!!.contains("ja e autorizador"), erro.message)
    }

    @Test
    fun `mesmo usuario pode autorizar motivos diferentes`() {
        jaCadastrados(autorizador("1", "DESCONTO", "rovema"))

        service.criar(Autorizador("PRAZO", "rovema"))

        assertEquals("2", enviado().Code)
    }

    @Test
    fun `recusa motivo ou usuario em branco`() {
        jaCadastrados()

        assertThrows<Exception> { service.criar(Autorizador("", "rovema")) }
        assertThrows<Exception> { service.criar(Autorizador("DESCONTO", "  ")) }
    }

    /**
     * A trava tem que estar no backend, nao so no select da tela: motivo que nenhuma regra
     * produz gera autorizador inutil e o documento fica pendente sem ninguem que possa aprovar -
     * falha silenciosa, que so aparece quando alguem trava de verdade.
     */
    @Test
    fun `recusa motivo que nenhuma regra produz`() {
        jaCadastrados()

        val erro = assertThrows<Exception> { service.criar(Autorizador("CLIENTE_ATRASADO", "rovema")) }

        assertTrue(erro.message!!.contains("nao existe no motor de regras"), erro.message)
    }

    /** A mensagem lista o que e valido - erro de digitacao se resolve sem abrir o codigo. */
    @Test
    fun `erro de motivo invalido lista os disponiveis`() {
        jaCadastrados()

        val erro = assertThrows<Exception> { service.criar(Autorizador("XPTO", "rovema")) }

        assertTrue(erro.message!!.contains("CLIENTE_EM_ATRASO"), erro.message)
    }
}
