package br.andrew.sap.services.odbc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import java.time.Duration

/**
 * Cliente do sap-odbc, o gateway SQL somente-leitura do HANA.
 *
 * Existe porque o SQLQueries do Service Layer nao aceita agregacao: o validador
 * da SAP recusa YEAR()/MONTH() em GROUP BY (ver comentario em PainelVendasService).
 * Um painel analitico precisa somar no banco, entao as consultas agregadas passam
 * por aqui.
 *
 * Todo valor de filtro viaja como bind parameter. Nada e concatenado no SQL.
 */
@Service
class OdbcClient(
    builder: RestTemplateBuilder,
    @Value("\${odbc.base-url:http://localhost:8080}") private val baseUrl: String,
    @Value("\${odbc.api-key:}") private val apiKey: String,
    @Value("\${odbc.query-timeout-seconds:30}") private val queryTimeout: Int,
) {
    private val log = LoggerFactory.getLogger(OdbcClient::class.java)

    // Leitura maior que o timeout da consulta: se fosse menor, o cliente desistiria
    // antes de o sap-odbc responder o proprio 504, e um timeout legitimo chegaria
    // aqui como falha de rede, sem mensagem util.
    private val rest: RestTemplate = builder
        .setConnectTimeout(Duration.ofSeconds(10))
        .setReadTimeout(Duration.ofSeconds((queryTimeout + 20).toLong()))
        .build()

    fun consultar(sql: String, params: Map<String, Any?>, maxRows: Int): QueryResponse {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            if (apiKey.isNotBlank()) set("X-API-Key", apiKey)
        }
        val corpo = mapOf(
            "sql" to sql,
            "params" to params,
            // maxRows SEMPRE explicito: o default do sap-odbc e 1.000, nao o teto.
            "maxRows" to maxRows,
            "timeoutSeconds" to queryTimeout,
        )

        val resposta = try {
            rest.postForObject("$baseUrl/api/v1/query", HttpEntity(corpo, headers), QueryResponse::class.java)
                ?: throw OdbcException("erro_odbc", "Resposta vazia do sap-odbc.")
        } catch (ex: HttpStatusCodeException) {
            throw traduzir(ex)
        } catch (ex: Exception) {
            log.error("Falha de comunicacao com o sap-odbc", ex)
            throw OdbcException("erro_odbc", "Nao foi possivel consultar os dados.")
        }

        // truncated = dado faltando. Num painel isso e pior que erro: o numero
        // aparece plausivel e errado. Recusar em vez de exibir recorte silencioso.
        if (resposta.truncated) {
            throw OdbcException(
                "resultado_truncado",
                "O periodo selecionado gera mais dados do que o painel consegue processar. " +
                    "Restrinja o periodo ou os filtros.",
            )
        }
        return resposta
    }

    private fun traduzir(ex: HttpStatusCodeException): OdbcException {
        // O SQL nunca vai na mensagem ao usuario final; fica so no log.
        log.warn("sap-odbc respondeu {}: {}", ex.statusCode, ex.responseBodyAsString.take(500))

        // Envelope padrao das APIs do workspace: {erro, mensagem}. Ja foi
        // {error, message} no sap-odbc, o que fazia todo erro virar generico aqui.
        val codigo = runCatching {
            ObjectMapper().readTree(ex.responseBodyAsString).get("erro")?.asText()
        }.getOrNull() ?: "erro_odbc"

        val upstream = runCatching {
            ObjectMapper().readTree(ex.responseBodyAsString).get("mensagem")?.asText()
        }.getOrNull()

        val mensagem = when {
            ex.statusCode.value() == 401 -> "O painel nao esta autorizado no servico de consultas."
            ex.statusCode.value() == 504 || codigo == "timeout" -> "A consulta excedeu o tempo limite. Restrinja o periodo."
            codigo == "rota_nao_encontrada" -> "Servico de consultas indisponivel (rota nao encontrada). Verifique odbc.base-url."
            codigo == "sql_invalido" -> "A consulta do painel foi recusada pelo servico de consultas."
            else -> "Falha ao consultar os dados."
        }

        // `detalhe` carrega a mensagem de origem apenas para erros de VALIDACAO,
        // que indicam bug na montagem da nossa consulta - e cujo texto nao contem
        // SQL nem dado de cliente. Para erro vindo do banco continua suprimido:
        // ali a mensagem pode ecoar a instrucao e nomes de tabela.
        val detalhe = if (codigo == "sql_invalido" || codigo == "rota_nao_encontrada") upstream else null

        return OdbcException(codigo, mensagem, detalhe)
    }
}

/**
 * @param detalhe causa tecnica, preenchida so quando nao ha risco de vazar SQL
 *   ou dado de cliente. Existe porque suprimir tudo deixava o usuario (e o
 *   suporte) sem nenhuma pista: o erro real ficava apenas no log do servidor.
 */
class OdbcException(
    val codigo: String,
    override val message: String,
    val detalhe: String? = null,
) : RuntimeException(message)

@JsonIgnoreProperties(ignoreUnknown = true)
data class QueryResponse(
    val columns: List<ColumnMeta> = listOf(),
    val rows: List<Map<String, Any?>> = listOf(),
    val rowCount: Int = 0,
    val truncated: Boolean = false,
    val elapsedMs: Long = 0,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ColumnMeta(val name: String = "", val type: String = "", val nullable: Boolean = true)
