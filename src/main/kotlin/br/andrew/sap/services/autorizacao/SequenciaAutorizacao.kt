package br.andrew.sap.services.autorizacao

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

/**
 * Sequencia do Code da @AUTORIZACAO. O UDO e bott_MasterData: o service layer recusa POST sem codigo
 * ("Enter valid code [@AUTORIZACAO.Code]") mesmo com ManageSeries ligado.
 *
 * Com Redis, o codigo sai de um INCR atomico - dois documentos retidos no mesmo instante (dois
 * vendedores, ou o envio offline) recebem codigos diferentes, inclusive com mais de uma instancia.
 * Sem Redis, de um contador em memoria: vale para uma instancia so.
 *
 * O piso e o maior Code que ja esta no SAP, enviado no startup (semear). O contador local acompanha o
 * Redis, e cada INCR sobe o Redis ate o contador local antes de incrementar - assim, se o Redis caiu e
 * o fallback local gerou codigos, ele volta sem repetir nenhum.
 */
@Component
class SequenciaAutorizacao(private val redis: ObjectProvider<StringRedisTemplate>) {

    private val log = LoggerFactory.getLogger(SequenciaAutorizacao::class.java)
    private val local = AtomicInteger(0)

    @Volatile
    private var semeada = false

    /** Sobe a sequencia (Redis e local) ate o maior Code que ja esta no SAP. */
    fun semear(maiorNoSap: Int) {
        local.accumulateAndGet(maiorNoSap, ::maxOf)
        redis.ifAvailable?.let { template ->
            try {
                template.execute(SCRIPT_PISO, listOf(CHAVE), maiorNoSap.toString())
            } catch (e: Exception) {
                log.warn("Redis indisponivel ao semear a sequencia de autorizacao; segue com o contador local", e)
            }
        }
        semeada = true
    }

    /** @param maiorNoSap so e chamado se o startup nao conseguiu semear (SAP fora do ar no boot). */
    fun proximo(maiorNoSap: () -> Int): Int {
        if (!semeada)
            semear(maiorNoSap())
        redis.ifAvailable?.let { template ->
            try {
                val codigo = template.execute(SCRIPT_PROXIMO, listOf(CHAVE), local.get().toString())!!.toInt()
                local.accumulateAndGet(codigo, ::maxOf)
                return codigo
            } catch (e: Exception) {
                log.warn("Redis indisponivel para a sequencia de autorizacao; usando o contador local", e)
            }
        }
        return local.incrementAndGet()
    }

    companion object {
        const val CHAVE = "sap-rovema:autorizacao:code"

        //ARGV[1] = piso. Nenhum outro comando do Redis entra no meio de um script
        val SCRIPT_PISO = DefaultRedisScript(
            """
            if tonumber(redis.call('GET', KEYS[1]) or '0') < tonumber(ARGV[1]) then
                redis.call('SET', KEYS[1], ARGV[1])
            end
            return tonumber(redis.call('GET', KEYS[1]))
            """.trimIndent(),
            Long::class.java
        )

        val SCRIPT_PROXIMO = DefaultRedisScript(
            """
            if tonumber(redis.call('GET', KEYS[1]) or '0') < tonumber(ARGV[1]) then
                redis.call('SET', KEYS[1], ARGV[1])
            end
            return redis.call('INCR', KEYS[1])
            """.trimIndent(),
            Long::class.java
        )
    }
}

/**
 * Envia ao Redis, no startup, o maior Code de autorizacao que ja esta no SAP. Fora do profile de teste
 * para o contexto de teste nao tentar falar com o SAP. Se o SAP nao responder aqui, a primeira
 * autorizacao semeia (SequenciaAutorizacao.proximo).
 */
@Component
@Profile("!test")
class SemeiaSequenciaAutorizacao(
    private val sequencia: SequenciaAutorizacao,
    private val autorizacaoService: AutorizacaoService,
) {
    private val log = LoggerFactory.getLogger(SemeiaSequenciaAutorizacao::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun semear() {
        try {
            sequencia.semear(autorizacaoService.maiorCodigoNoSap())
        } catch (e: Exception) {
            log.warn("Nao foi possivel ler o maior codigo de autorizacao no startup; a primeira autorizacao semeia", e)
        }
    }
}
