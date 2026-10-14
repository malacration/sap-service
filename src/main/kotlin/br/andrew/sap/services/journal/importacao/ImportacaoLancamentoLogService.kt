package br.andrew.sap.services.journal.importacao

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ImportacaoLancamentoLogService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<ImportacaoLancamentoLog>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/LC_IMPORTACAO"

    //getAll segue o nextLink: a primeira pagina (20 linhas, sem ordem) podia deixar de fora justamente
    //a importacao que entrou, ou a tentativa ENVIANDO que deveria barrar o reenvio
    fun porHash(hash: String): List<ImportacaoLancamentoLog> =
        getAll(ImportacaoLancamentoLog::class.java, Filter(Predicate("U_Hash", hash, Condicao.EQUAL)))

    /** Tentativas deste arquivo que ficaram ENVIANDO: importacao que pode estar em andamento. */
    fun emEnvio(hash: String): List<ImportacaoLancamentoLog> =
        getAll(ImportacaoLancamentoLog::class.java, Filter(
            Predicate("U_Hash", hash, Condicao.EQUAL),
            Predicate("U_Status", ImportacaoLancamentoLog.ENVIANDO, Condicao.EQUAL),
        ))

    /** Grava a linha ENVIANDO antes do $batch. */
    fun registrarInicio(log: ImportacaoLancamentoLog) {
        save(log)
    }

    fun marcar(code: String, status: String) {
        update(mapOf("U_Status" to status), "'$code'")
    }

    fun porCode(code: String): ImportacaoLancamentoLog? =
        get(Filter(Predicate("Code", code, Condicao.EQUAL))).tryGetValues<ImportacaoLancamentoLog>().firstOrNull()

    /**
     * As ultimas importacoes que entraram, da mais nova para a mais antiga. Pagina ate juntar a
     * quantidade: filtrar so depois de cortar em N deixaria o historico vazio atras de N tentativas
     * recusadas. O filtro e aqui (e nao no $filter) porque linha anterior ao campo Status nao tem
     * status e tambem conta como importada. Para em MAXIMO_PAGINAS para nao varrer a tabela inteira.
     */
    fun ultimasImportadas(quantidade: Int): List<ImportacaoLancamentoLog> {
        val importadas = mutableListOf<ImportacaoLancamentoLog>()
        var pagina = get(Filter(), OrderBy("Code", Order.DESC), Pageable.ofSize(quantidade))
        var lidas = 1
        while (true) {
            importadas += pagina.tryGetValues(ImportacaoLancamentoLog::class.java).filter { it.importado() }
            if (importadas.size >= quantidade || !pagina.hasNext() || lidas >= MAXIMO_PAGINAS) break
            pagina = next(pagina)
            lidas++
        }
        return importadas.take(quantidade)
    }

    /**
     * Guarda em U_Lancamentos, como JSON, qual lancamento cada linha do arquivo gerou: e o que o
     * historico mostra. Formato: [{"linha":2,"numero":793886,"transacao":1499715}, ...].
     */
    fun registrarLancamentos(code: String, gerados: List<LancamentoGerado>) {
        val json = mapper.writeValueAsString(gerados.map {
            mapOf("linha" to it.linha, "numero" to it.numero, "transacao" to it.transacao)
        })
        update(mapOf("U_Lancamentos" to json), "'$code'")
    }

    companion object {
        private val mapper = ObjectMapper()
        private const val MAXIMO_PAGINAS = 20

        /** Le U_Lancamentos; aceita tambem o formato antigo, so as transacoes separadas por virgula. */
        fun lancamentos(texto: String?): List<LancamentoGerado> {
            if (texto.isNullOrBlank()) return emptyList()
            if (!texto.trimStart().startsWith("["))
                return texto.split(",").mapNotNull { it.trim().toIntOrNull() }.map { LancamentoGerado(0, it, null) }
            return try {
                mapper.readTree(texto).mapNotNull { no ->
                    val transacao = no.get("transacao")?.takeIf { it.isNumber }?.asInt() ?: return@mapNotNull null
                    LancamentoGerado(
                        no.get("linha")?.asInt() ?: 0, transacao, no.get("numero")?.takeIf { it.isNumber }?.asInt()
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
