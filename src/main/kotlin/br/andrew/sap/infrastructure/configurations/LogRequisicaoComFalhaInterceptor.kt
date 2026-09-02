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
 */
class LogRequisicaoComFalhaInterceptor : ClientHttpRequestInterceptor {

    private val logger = LoggerFactory.getLogger(LogRequisicaoComFalhaInterceptor::class.java)

    override fun intercept(request: HttpRequest, body: ByteArray,
                           execution: ClientHttpRequestExecution): ClientHttpResponse {
        val response = execution.execute(request, body)
        if (response.statusCode.isError && body.isNotEmpty())
            logger.error("${request.method} ${request.uri} respondeu ${response.statusCode}. Corpo enviado: ${String(body)}")
        return response
    }
}
