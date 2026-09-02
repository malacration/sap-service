package br.andrew.sap.infrastructure.configurations

import org.slf4j.LoggerFactory
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.HttpRequest

/**
 * O Service Layer responde varios erros de PATCH/POST so com "Internal error (-5002) occurred",
 * sem dizer qual campo ou qual regra derrubou. Sem ver o corpo que saiu daqui nao da pra separar
 * "propriedade que o SAP nao conhece" de "trava do SBO_SP_TransactionNotification" - as duas
 * chegam como 400 generico (ver SalesOrderCalculaDesoneradoSchedule, DocNum 65581).
 *
 * So loga quando a resposta nao e 2xx, entao o caminho feliz continua silencioso. A resposta nao
 * e lida aqui de proposito: consumir o stream quebraria o RestTemplate, que ainda precisa dele.
 *
 * # Por que o corpo nao e logado sempre
 *
 * Este interceptor vive no RestTemplate COMPARTILHADO da aplicacao, o mesmo usado pelo
 * AuthService.login (usuario e senha do SAP) e pelas integracoes de RD Station, UzziPay, Serasa e
 * BankPlus (client secret, refresh token, senha). Logar todo corpo que falha transformava uma
 * falha de autenticacao rotineira - senha errada, conta expirada, provedor fora do ar - em
 * credencial reutilizavel gravada no log da aplicacao.
 *
 * Por isso a regra e permitir por excecao, nao proibir por excecao:
 *  - so corpo de requisicao para o Service Layer do SAP (/b1s/v1/...), que e onde o -5002
 *    acontece e onde esta o valor de diagnostico;
 *  - nunca o /b1s/v1/Login, que e exatamente o corpo com a senha do SAP;
 *  - e, mesmo no que passa, campo sensivel e mascarado - o proprio Service Layer tem entidade
 *    com senha (Users), e endpoint novo nao pode vazar so porque ninguem lembrou de excluir.
 */
class LogRequisicaoComFalhaInterceptor : ClientHttpRequestInterceptor {

    private val logger = LoggerFactory.getLogger(LogRequisicaoComFalhaInterceptor::class.java)

    companion object {
        private const val SERVICE_LAYER = "/b1s/v1/"
        private const val LOGIN = "/b1s/v1/Login"
        private const val LIMITE_CORPO = 20_000

        //"password":"x" / "Senha" : "x" / "client_secret":"x" -> valor trocado por ***
        private val CAMPO_SENSIVEL = Regex(
            """("(?:[^"]*(?:password|senha|secret|token|apikey|authorization|credential)[^"]*)"\s*:\s*)"[^"]*"""",
            RegexOption.IGNORE_CASE)
    }

    override fun intercept(request: HttpRequest, body: ByteArray,
                           execution: ClientHttpRequestExecution): ClientHttpResponse {
        val response = execution.execute(request, body)
        if (response.statusCode.isError && body.isNotEmpty() && podeLogarCorpo(request))
            logger.error("${request.method} ${request.uri} respondeu ${response.statusCode}. " +
                "Corpo enviado: ${corpoSeguro(body)}")
        else if (response.statusCode.isError)
            logger.error("${request.method} ${request.uri} respondeu ${response.statusCode}")
        return response
    }

    private fun podeLogarCorpo(request: HttpRequest): Boolean {
        val path = request.uri.path ?: return false
        return path.contains(SERVICE_LAYER) && !path.endsWith(LOGIN)
    }

    private fun corpoSeguro(body: ByteArray): String {
        val texto = String(body).let {
            if (it.length > LIMITE_CORPO) it.take(LIMITE_CORPO) + "...(truncado)" else it
        }
        return CAMPO_SENSIVEL.replace(texto) { "${it.groupValues[1]}\"***\"" }
    }
}
