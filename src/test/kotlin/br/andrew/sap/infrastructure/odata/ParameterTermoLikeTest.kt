package br.andrew.sap.infrastructure.odata

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * O Service Layer recusa parametro de SQLQueries com caractere fora do ASCII (704 "Parameter error.").
 * O termo de busca tem que sair daqui so com ASCII, e cada letra acentuada tem que continuar ocupando
 * exatamente uma posicao, senao o LIKE deixa de achar o cadastro gravado com acento.
 */
class ParameterTermoLikeTest {

    @Test
    fun `acento e cedilha viram curinga de um caractere`() {
        assertEquals("RA__O", Parameter.termoLike("RAÇÃO"))
        assertEquals("CONCEI__O", Parameter.termoLike("CONCEIÇÃO"))
        assertEquals("JOS_", Parameter.termoLike("JOSÉ"))
    }

    @Test
    fun `termo so com ASCII nao muda`() {
        assertEquals("MILHO EM GRAO 50KG", Parameter.termoLike("MILHO EM GRAO 50KG"))
        assertEquals("%MILHO%", Parameter.termoLike("%MILHO%"))
    }

    @Test
    fun `saida nunca tem caractere fora do ASCII`() {
        val entrada = "ÁÀÂÃÄÉÊÍÓÔÕÚÜÇáàâãéêíóôõúüçñº"
        val termo = Parameter.termoLike(entrada)
        assertTrue(termo.all { it.code <= 127 })
        assertEquals(entrada.length, termo.length)
    }
}
