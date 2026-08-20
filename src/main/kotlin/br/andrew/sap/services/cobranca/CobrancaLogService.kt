package br.andrew.sap.services.cobranca

import br.andrew.sap.model.cobranca.CobrancaRemocaoLog
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Auditoria das remocoes do historico de cobranca, em @COB_TITULO_LOG.
 *
 * Quem grava e o changeset montado em CobrancaService.removerHistorico - a auditoria vai junto da
 * remocao na mesma transacao, entao nao existe janela em que uma acontece sem a outra. Este
 * servico existe pelo path(), que o batch usa pra montar a URL, e pelo delete de compensacao do
 * caso em que o SAP responde 200 e ignora a remocao.
 */
@Service
class CobrancaLogService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<CobrancaRemocaoLog>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/COB_TITULO_LOG"
}
