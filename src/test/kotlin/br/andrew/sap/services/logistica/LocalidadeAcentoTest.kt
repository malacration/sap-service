package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

/**
 * Acento e preservado no nome da localidade ao cadastrar, e a busca continua achando esse nome.
 *
 * O LIKE do HANA e sensivel a acento do mesmo jeito que a caixa, mas o Service Layer recusa
 * parametro com caractere fora do ASCII (704 "Parameter error.") - a busca nao pode mandar o
 * acento. Cada letra acentuada vai como "_" (curinga de um caractere, Parameter.termoLike):
 * "MANICOR_" casa com o "MANICORÉ" gravado. O Code continua sem acento - ele e a chave do UDO.
 */
class LocalidadeAcentoTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val service = spy(LocalidadeService(sqlQueriesService, mock(), mock(), mock()))

    private fun termoBuscado(texto: String): String {
        whenever(sqlQueriesService.execute(any<String>(), any<List<Parameter>>()))
            .doReturn(OData(linkedMapOf("value" to "[]")))
        service.fullSearchTextFallBack(texto, mock<User>())
        val captor = argumentCaptor<List<Parameter>>()
        org.mockito.Mockito.verify(sqlQueriesService).execute(any<String>(), captor.capture())
        return captor.firstValue.single { it.toString().startsWith("search=") }.toString()
    }

    private fun nomeGravado(nome: String): String {
        doReturn(OData(linkedMapOf("value" to "[]"))).whenever(service).get(any<br.andrew.sap.infrastructure.odata.Filter>())
        val salvo = argumentCaptor<Localidade>()
        doReturn(OData(linkedMapOf("value" to """{"Code":"X","Name":"X"}"""))).whenever(service).save(any())
        service.criar(Localidade("LOC1", nome))
        org.mockito.Mockito.verify(service).save(salvo.capture())
        return salvo.firstValue.Name!!
    }

    @Test
    fun `busca troca o acento digitado pelo curinga de um caractere`() {
        assertEquals("search='%MANICOR_%'", termoBuscado("manicoré"))
    }

    @Test
    fun `busca continua subindo para maiusculo`() {
        assertEquals("search='%PORTO VELHO%'", termoBuscado("porto velho"))
    }

    /** O "*" que o usuario digita vira o curinga "%" do LIKE. */
    @Test
    fun `curinga digitado vira porcento`() {
        assertEquals("search='%PORTO%%'", termoBuscado("porto*"))
    }

    @Test
    fun `cadastro preserva o acento no nome`() {
        assertEquals("MANICORÉ", nomeGravado("Manicoré"))
    }

    @Test
    fun `cadastro continua limpando pontuacao`() {
        assertEquals("SAO JOAO DA BALIZA", nomeGravado("Sao Joao, da Baliza!"))
    }

    /** Nome so de pontuacao vira vazio e a validacao tem que barrar. */
    @Test
    fun `nome sem letra nem numero e recusado`() {
        val erro = assertThrows<Exception> { nomeGravado("!!!") }
        assertEquals("O nome da localidade deve ser informado", erro.message)
    }
}
