package br.andrew.sap.services.autorizacao

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.ObjectProvider
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SequenciaAutorizacaoTest {

    private fun provider(template: StringRedisTemplate?) = mock<ObjectProvider<StringRedisTemplate>>().also {
        whenever(it.ifAvailable).doReturn(template)
    }

    @Test
    fun `sem Redis usa o contador local a partir do maior codigo do SAP`() {
        val sequencia = SequenciaAutorizacao(provider(null))
        sequencia.semear(41)

        assertEquals(42, sequencia.proximo { error("ja semeada no startup") })
        assertEquals(43, sequencia.proximo { error("ja semeada no startup") })
    }

    @Test
    fun `startup sem SAP semeia na primeira autorizacao`() {
        val sequencia = SequenciaAutorizacao(provider(null))

        assertEquals(8, sequencia.proximo { 7 })
        assertEquals(9, sequencia.proximo { error("so le o SAP uma vez") })
    }

    @Test
    fun `com Redis o codigo sai do INCR, com o contador local como piso`() {
        val redis = mock<StringRedisTemplate>()
        whenever(redis.execute(eq(SequenciaAutorizacao.SCRIPT_PROXIMO), any<List<String>>(), anyVararg<Any>())).doReturn(100L)
        val sequencia = SequenciaAutorizacao(provider(redis))
        sequencia.semear(41)

        assertEquals(100, sequencia.proximo { 0 })
        verify(redis).execute(SequenciaAutorizacao.SCRIPT_PISO, listOf(SequenciaAutorizacao.CHAVE), "41")
        verify(redis).execute(SequenciaAutorizacao.SCRIPT_PROXIMO, listOf(SequenciaAutorizacao.CHAVE), "41")
    }

    @Test
    fun `Redis fora do ar cai no local sem repetir o que o Redis ja entregou, e ao voltar recebe o local como piso`() {
        val redis = mock<StringRedisTemplate>()
        whenever(redis.execute(eq(SequenciaAutorizacao.SCRIPT_PROXIMO), any<List<String>>(), anyVararg<Any>()))
            .doReturn(50L)
            .doThrow(RedisConnectionFailureException("fora"))
            .doReturn(53L)
        val sequencia = SequenciaAutorizacao(provider(redis))
        sequencia.semear(10)

        assertEquals(50, sequencia.proximo { 0 })
        assertEquals(51, sequencia.proximo { 0 })
        assertEquals(53, sequencia.proximo { 0 })
        //na volta, o Redis recebe 51 como piso: o script sobe ate ele antes do INCR
        verify(redis).execute(SequenciaAutorizacao.SCRIPT_PROXIMO, listOf(SequenciaAutorizacao.CHAVE), "51")
    }

    @Test
    fun `contador local nao repete codigo com varias threads ao mesmo tempo, nem na semeadura`() {
        //sem semear antes: as threads disputam tambem a semeadura tardia (SAP lido mais de uma vez,
        //mas o piso so sobe - nenhum codigo volta)
        val sequencia = SequenciaAutorizacao(provider(null))
        val threads = 32
        val porThread = 500
        val codigos = Collections.synchronizedList(mutableListOf<Int>())
        val largada = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)
        repeat(threads) {
            pool.submit {
                largada.await()
                repeat(porThread) { codigos += sequencia.proximo { 1000 } }
            }
        }
        largada.countDown()
        pool.shutdown()
        pool.awaitTermination(30, TimeUnit.SECONDS)

        assertEquals(threads * porThread, codigos.size)
        assertEquals(codigos.size, codigos.toSet().size)
        assertEquals((1001..1000 + threads * porThread).toList(), codigos.sorted())
    }
}
