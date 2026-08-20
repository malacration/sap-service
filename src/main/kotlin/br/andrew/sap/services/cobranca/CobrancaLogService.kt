package br.andrew.sap.services.cobranca

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaRemocaoLog
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Auditoria das remocoes do historico de cobranca, em @COB_TITULO_LOG.
 *
 * Gravar ANTES de apagar e proposital: a ordem contraria abriria a janela em que a linha sai da
 * UDT e o registro do que ela dizia nao chega a existir. Se a auditoria falhar, a remocao para -
 * e melhor o cobrador tentar de novo do que apagar sem rastro.
 */
@Service
class CobrancaLogService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<CobrancaRemocaoLog>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/COB_TITULO_LOG"

    fun registrarRemocao(registro: String, linha: CobrancaHistorico, auth: User) {
        try {
            save(CobrancaRemocaoLog.de(registro, linha, auth._name, auth.id))
        } catch (e: Exception) {
            throw CobrancaException(
                "Não foi possível registrar a remoção na auditoria de cobrança ($registro linha ${linha.LineId}): " +
                    "${e.message}. Nada foi apagado.",
            )
        }
    }
}
