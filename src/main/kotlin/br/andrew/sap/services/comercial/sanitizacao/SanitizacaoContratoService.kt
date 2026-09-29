package br.andrew.sap.services.comercial.sanitizacao

import com.github.benmanes.caffeine.cache.Caffeine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
class SanitizacaoContratoService(
    private val consulta: SanitizacaoContratoConsulta,
    private val lancamentos: SanitizacaoContratoLancamentos,
) {
    private class Sessao(val usuario: String, val previa: PreviaSanitizacao) {
        val resultados = mutableMapOf<Int, ResultadoSanitizacao>()
    }
    private val previas = Caffeine.newBuilder().maximumSize(50).expireAfterWrite(Duration.ofMinutes(30)).build<String, Sessao>()
    private val logger = LoggerFactory.getLogger(javaClass)

    fun previa(usuario: String): PreviaSanitizacao {
        val previa = PreviaSanitizacao(UUID.randomUUID().toString(), consulta.dataCorrente(), consulta.buscar())
        previas.put(previa.id, Sessao(usuario, previa))
        return previa
    }

    fun aplicar(usuario: String, pedido: AplicarSanitizacao): ResultadoSanitizacao {
        val sessao = previas.getIfPresent(pedido.previaId)
            ?: throw IllegalArgumentException("Prévia expirada ou indisponível. Gere uma nova prévia.")
        require(sessao.usuario == usuario) { "A prévia pertence a outro usuário." }
        // Um clique duplo ou repetição após timeout devolve o mesmo resultado.
        // O cliente envia somente o token e o TransId: vínculos e valores vêm do servidor.
        synchronized(sessao) {
            sessao.resultados[pedido.transId]?.let { return it }
            val original = sessao.previa.itens.singleOrNull { it.transId == pedido.transId }
                ?: throw IllegalArgumentException("Reclassificação não consta da prévia confirmada.")
            require(original.podeAplicar) { "A reclassificação está bloqueada para correção automática." }
            var enviou = false
            val resultado = try {
                val atual = consulta.buscar(original.transId).singleOrNull()
                check(atual == original) { "Dados alterados desde a prévia, ou lançamento já estornado. Gere uma nova prévia." }
                logger.info("Sanitização contrato: usuario={}, transId={}, reconciliacoes={}, apropriacoes={}",
                    usuario, original.transId, original.reconciliacoes.map { it.numero },
                    original.apropriacoes.filter { !it.cancelada }.map { it.docEntry })
                enviou = true
                val dataSap = consulta.dataCorrente()
                lancamentos.aplicar(original)
                consulta.verificar(original, dataSap)
            } catch (e: Exception) {
                logger.error("Falha sanitização contrato: usuario={}, transId={}, enviado={}", usuario, original.transId, enviou, e)
                ResultadoSanitizacao(original.transId, if (enviou) "CONFERIR" else "REJEITADO",
                    if (enviou) "Não foi possível confirmar a correção. Confira no SAP e gere nova prévia antes de repetir. ${e.message.orEmpty()}"
                    else e.message ?: "Não foi possível revalidar a reclassificação.")
            }
            sessao.resultados[original.transId] = resultado
            logger.info("Resultado sanitização: usuario={}, transId={}, status={}, estorno={}", usuario, original.transId, resultado.status, resultado.estorno)
            return resultado
        }
    }
}
