package br.andrew.sap.services.journal.importacao

import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchRecusadoException
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.journal.JournalEntriesService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

/**
 * Importacao de lancamentos contabeis por CSV, com previa. Substitui o curl no /journal/save,
 * que grava linha a linha sem validar: se a linha N falha, as anteriores ja estao no SAP e a
 * resposta nao diz quais entraram.
 *
 * Aqui todos os lancamentos e o log vao num unico changeset do $batch, que o Service Layer
 * aplica como uma transacao: entra o arquivo inteiro ou nada.
 */
@Service
class ImportacaoLancamentoService(
    private val validador: ImportacaoLancamentoValidador,
    private val cadastros: ImportacaoLancamentoCadastros,
    private val logService: ImportacaoLancamentoLogService,
    private val journalEntriesService: JournalEntriesService,
    private val batchService: BatchService,
    @Value("\${journal.importacao.max-linhas:500}") private val maximoLinhas: Int,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val mapper = ObjectMapper()
    //uma importacao por vez na instancia (o sap-prd-v7 roda com uma replica): duplo clique, ou
    //dois usuarios com o mesmo arquivo, nao passam os dois pela checagem de "ja importado"
    private val trava = Any()
    //hash do arquivo -> a importacao dele que ficou sem confirmacao (CONFERIR). O SAP pode ainda estar
    //gravando aquele changeset: reenviar o mesmo arquivo nessa janela duplicaria tudo
    private class Duvida(val code: String, val desde: Instant)
    private val emDuvida = ConcurrentHashMap<String, Duvida>()

    fun validar(conteudo: ByteArray, arquivo: String?): PreviaImportacaoLancamento {
        val leitura = LancamentoCsvParser.ler(conteudo)
        val erros = leitura.erros.toMutableList()
        if (leitura.linhas.size > maximoLinhas)
            erros += "O arquivo tem ${leitura.linhas.size} lançamentos; o limite por importação é $maximoLinhas. " +
                "Divida o arquivo."
        else
            validador.validar(leitura.linhas)

        val hash = sha256(conteudo)
        val anteriores = logService.porHash(hash).map {
            ImportacaoAnterior(it.Code ?: "", it.U_Usuario, it.U_Data?.take(10), it.U_Hora)
        }
        return PreviaImportacaoLancamento(arquivo, hash, leitura.codificacao, leitura.linhas, erros, anteriores)
    }

    fun importar(
        conteudo: ByteArray, arquivo: String?, hashValidado: String, forcarDuplicado: Boolean,
        usuario: String, usuarioId: String,
    ): ResultadoImportacaoLancamento = synchronized(trava) {
        //revalida tudo: o cliente so prova que viu a previa, quem decide e o servidor
        val previa = validar(conteudo, arquivo)
        if (previa.hash != hashValidado)
            throw ImportacaoLancamentoException(HttpStatus.CONFLICT, "O arquivo mudou desde a validação. Valide de novo.")
        barrarSeEmDuvida(previa)
        if (!previa.podeImportar)
            throw ImportacaoLancamentoException(
                HttpStatus.UNPROCESSABLE_ENTITY, "O arquivo tem erros. Corrija e valide de novo.", previa
            )
        if (previa.exigeConfirmacao && !forcarDuplicado) {
            val anterior = previa.importacoesAnteriores.firstOrNull()
            val motivo = if (anterior != null)
                "Este arquivo já foi importado por ${anterior.usuario} em ${anterior.data} ${anterior.hora}."
            else
                "${previa.linhasJaNoSap} lançamento(s) do arquivo já existem no SAP."
            throw ImportacaoLancamentoException(
                HttpStatus.CONFLICT, "$motivo Para importar mesmo assim, confirme a reimportação.", previa
            )
        }

        val log = ImportacaoLancamentoLog.novo(
            usuario, usuarioId, arquivo, previa.hash, previa.totalLinhas, previa.totalValor, forcarDuplicado
        )
        val id = log.Code!!
        val lote = BatchList()
        previa.linhas.forEach { lote.add(BatchMethod.POST, it.paraLancamento(), journalEntriesService) }
        lote.add(BatchMethod.POST, log, logService)

        logger.info("Importacao de lancamentos {}: usuario={}, arquivo={}, linhas={}, total={}, forcado={}",
            id, usuarioId, arquivo, previa.totalLinhas, previa.totalValor, forcarDuplicado)
        val respostas = try {
            batchService.run(lote)
        } catch (e: Exception) {
            return@synchronized falhaNoEnvio(id, previa.hash, e)
        }
        return@synchronized conferir(id, previa.hash, respostas, previa.linhas)
    }

    private fun barrarSeEmDuvida(previa: PreviaImportacaoLancamento) {
        val duvida = emDuvida[previa.hash] ?: return
        val liberaEm = duvida.desde.plus(ESPERA_DUVIDA)
        //so o log DAQUELA tentativa encerra a duvida: num arquivo ja importado antes, o log antigo
        //sempre esta la e liberaria um terceiro envio. Consulta pelo Code porque importacoesAnteriores
        //e so a primeira pagina do hash; se a consulta falhar, a duvida continua
        val entrou = previa.importacoesAnteriores.any { it.id == duvida.code } ||
            runCatching { logService.porCode(duvida.code) != null }.getOrDefault(false)
        if (entrou || Instant.now().isAfter(liberaEm)) {
            emDuvida.remove(previa.hash)
            return
        }
        throw ImportacaoLancamentoException(
            HttpStatus.CONFLICT,
            "Uma importação deste arquivo às ${hora(duvida.desde)} ficou sem confirmação do SAP e ainda pode estar " +
                "entrando. Confira o histórico e, se ela não aparecer, tente de novo a partir das ${hora(liberaEm)}.",
        )
    }

    private fun marcarEmDuvida(id: String, hash: String, mensagem: String): ResultadoImportacaoLancamento {
        emDuvida[hash] = Duvida(id, Instant.now())
        return ResultadoImportacaoLancamento(id, CONFERIR, emptyList(), mensagem)
    }

    private fun hora(instante: Instant) = FORMATO_HORA.format(instante.atZone(ZoneId.systemDefault()))

    fun historico(quantidade: Int = 50): List<HistoricoImportacao> = logService.ultimas(quantidade).map {
        HistoricoImportacao(
            id = it.Code ?: "",
            usuario = it.U_Usuario,
            data = it.U_Data?.take(10),
            hora = it.U_Hora,
            arquivo = it.U_Arquivo,
            linhas = it.U_Linhas,
            total = it.U_Total,
            forcado = it.U_Forcado == "Y",
            lancamentos = ImportacaoLancamentoLogService.lancamentos(it.U_Lancamentos),
        )
    }

    /**
     * Recusa definitiva so quando o SAP respondeu com erro: BatchRecusadoException (o BatchService
     * entendeu o erro do changeset) ou 4xx do $batch inteiro (pedido recusado antes de processar).
     * Timeout, rede, 5xx, corpo vazio ou falha no login nao provam nada: ai a resposta e CONFERIR,
     * mesmo que o log ainda nao exista - o changeset pode estar em andamento.
     */
    private fun falhaNoEnvio(id: String, hash: String, e: Exception): ResultadoImportacaoLancamento {
        val recusaDoSap = e is BatchRecusadoException || e is HttpClientErrorException
        val gravou = try {
            logService.porCode(id) != null
        } catch (releitura: Exception) {
            logger.error("Importacao de lancamentos {}: falha no envio e na releitura do log", id, releitura)
            null
        }
        return when {
            gravou == true -> {
                logger.warn("Importacao de lancamentos {}: resposta do lote se perdeu, mas o log existe", id, e)
                ResultadoImportacaoLancamento(id, IMPORTADO, emptyList(),
                    "Importação concluída, mas o SAP não devolveu os números dos lançamentos. Confira no histórico.")
            }
            gravou == false && recusaDoSap -> {
                logger.info("Importacao de lancamentos {}: recusada pelo SAP: {}", id, e.message)
                throw ImportacaoLancamentoException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O SAP recusou a importação e nenhum lançamento foi gravado: ${e.message}")
            }
            else -> {
                logger.error("Importacao de lancamentos {}: sem confirmacao do SAP", id, e)
                marcarEmDuvida(id, hash, "Não foi possível confirmar se a importação entrou (${e.message}). " +
                    "Confira o histórico em alguns minutos antes de tentar de novo.")
            }
        }
    }

    /**
     * Resposta inteira entendida (uma por lancamento e a do log, todas com sucesso e com JdtNum): o
     * changeset entrou. Senao so confia relendo o log: resposta que o parser do batch nao reconhece
     * vira lista vazia, e "nenhum erro" passaria por sucesso (ver CobrancaService).
     */
    private fun conferir(
        id: String, hash: String, respostas: List<BatchResponse>, linhasDoArquivo: List<LinhaLancamento>,
    ): ResultadoImportacaoLancamento {
        val linhas = linhasDoArquivo.size
        val ordenadas = naOrdemDoLote(respostas, linhas)
        val criados = ordenadas?.take(linhas)?.mapNotNull { criado(it) }.orEmpty()
        val lancamentos = criados.map { it.first }
        if (ordenadas != null && ordenadas.all { it.success } && criados.size == linhas) {
            val numeros = completarNumeros(id, criados)
            val porLinha = linhasDoArquivo.zip(criados) { linha, (transacao, numero) ->
                LancamentoGerado(linha.linha, transacao, numero ?: numeros[transacao])
            }
            try {
                logService.registrarLancamentos(id, porLinha)
            } catch (e: Exception) {
                logger.warn("Importacao de lancamentos {}: falha ao registrar os numeros no log", id, e)
            }
            logger.info("Importacao de lancamentos {}: concluida, lancamentos={}", id, lancamentos)
            return ResultadoImportacaoLancamento(id, IMPORTADO, lancamentos, "$linhas lançamentos importados.", porLinha)
        }

        logger.warn("Importacao de lancamentos {}: resposta do lote nao reconhecida ({} respostas, {} numeros)",
            id, respostas.size, lancamentos.size)
        val gravou = try {
            logService.porCode(id) != null
        } catch (e: Exception) {
            logger.error("Importacao de lancamentos {}: falha ao reler o log", id, e)
            null
        }
        return if (gravou == true)
            ResultadoImportacaoLancamento(id, IMPORTADO, emptyList(),
                "Importação concluída, mas não foi possível ler os números dos lançamentos. Confira no histórico.")
        else
            marcarEmDuvida(id, hash, "O SAP respondeu, mas não foi possível confirmar se a importação entrou. " +
                "Confira o histórico em alguns minutos antes de tentar de novo.")
    }

    /**
     * Respostas na ordem em que as operacoes foram montadas (lancamentos na ordem das linhas, log por
     * ultimo), pelo Content-ID que o BatchService poe em cada uma (1..N+1) e que o Service Layer devolve
     * (ver a resposta real em BatchServiceTest). O changeset pode voltar em outra ordem: casar por
     * posicao trocaria o numero de uma linha pelo de outra. Resposta sem Content-ID, incompleta ou com
     * Content-ID inesperado nao e reconhecida (null): a importacao se confirma pelo log, sem numeros.
     */
    private fun naOrdemDoLote(respostas: List<BatchResponse>, linhas: Int): List<BatchResponse>? {
        if (respostas.size != linhas + 1) return null
        val porId = respostas.mapNotNull { r -> r.contentId?.let { it to r } }.toMap()
        if (porId.size != respostas.size || porId.keys != (1..linhas + 1).toSet()) return null
        return (1..linhas + 1).map { porId.getValue(it) }
    }

    /** Number de quem veio sem ele na resposta, pelo sap-odbc; se falhar, a linha fica so com a transacao. */
    private fun completarNumeros(id: String, criados: List<Pair<Int, Int?>>): Map<Int, Int> {
        val faltando = criados.filter { it.second == null }.map { it.first }.toSet()
        if (faltando.isEmpty()) return emptyMap()
        return try {
            cadastros.numeros(faltando)
        } catch (e: Exception) {
            logger.warn("Importacao de lancamentos {}: sem o Numero de {} lancamentos", id, faltando.size, e)
            emptyMap()
        }
    }

    /**
     * (JdtNum, Number) do lancamento criado. JdtNum e o "No transacao" da tela do SAP, Number e o
     * "Numero". O corpo de cada resposta do batch e o trecho cru do multipart, cabecalhos inclusive.
     */
    private fun criado(resposta: BatchResponse): Pair<Int, Int?>? {
        val corpo = resposta.body ?: return null
        val inicio = corpo.indexOf('{').takeIf { it >= 0 } ?: return null
        return try {
            val json = mapper.readTree(corpo.substring(inicio))
            val transacao = json.get("JdtNum")?.takeIf { it.isNumber }?.asInt() ?: return null
            transacao to json.get("Number")?.takeIf { it.isNumber }?.asInt()
        } catch (e: Exception) {
            null
        }
    }

    private fun sha256(conteudo: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(conteudo).joinToString("") { "%02x".format(it) }

    companion object {
        const val IMPORTADO = "IMPORTADO"
        const val CONFERIR = "CONFERIR"
        //tempo que um changeset grande leva no SAP, com folga
        val ESPERA_DUVIDA: Duration = Duration.ofMinutes(15)
        private val FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm")
    }
}
