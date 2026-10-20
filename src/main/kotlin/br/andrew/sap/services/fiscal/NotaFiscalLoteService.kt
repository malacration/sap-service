package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.ChaveDocumentoLote
import br.andrew.sap.model.fiscal.NotaFiscalLoteArquivo
import br.andrew.sap.model.fiscal.NotaFiscalLoteFiltro
import br.andrew.sap.model.fiscal.NotaFiscalResumo
import br.andrew.sap.model.fiscal.StatusSefaz
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import com.fasterxml.jackson.core.JsonProcessingException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Download em lote do DANFE (pdf), do XML e dos boletos das NF-e de saida emitidas para a SEFAZ,
 * e dos boletos dos adiantamentos de venda futura (que nao emitem NF-e).
 *
 * A listagem vem do sap-odbc porque precisa da "Process" da TaxPlus (chave de acesso e status
 * na SEFAZ), que o Service Layer nao expoe. DANFE e XML vem da TaxPlus (uma consulta por nota
 * mais uma chamada por arquivo) e o boleto da BankPlus (uma lista por nota mais um pdf por
 * parcela); por isso o lote tem teto e roda em paralelo.
 */
@Service
class NotaFiscalLoteService(
    val odbc: OdbcClient,
    val taxNfeService: TaxNfeService,
    val bankPlusService: BankPlusService,
    @Value("\${nota-fiscal.lote.maximo:200}") val maximo: Int,
    @Value("\${nota-fiscal.lote.paralelismo:4}") val paralelismo: Int,
    // Teto do lote inteiro. O RestTemplate da BankPlus nao tem timeout de leitura: sem isso uma
    // resposta pendurada prenderia a requisicao para sempre.
    @Value("\${nota-fiscal.lote.prazo-segundos:300}") val prazoSegundos: Long = 300,
    val contratoPdf: ContratoVendaFuturaPdfService? = null,
) {

    private val logger = LoggerFactory.getLogger(NotaFiscalLoteService::class.java)

    private val sqlNotas by lazy { SqlResource.carregar("odbc/nota-fiscal-lote.sql") }
    private val sqlStatusSefaz by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-status-sefaz.sql") }

    fun listar(filtro: NotaFiscalLoteFiltro, page: Pageable): Page<NotaFiscalResumo> {
        // "Selecionar todas do filtro" pede uma pagina do tamanho do teto; acima disso nao baixaria.
        val pagina = if (page.pageSize > maximo) PageRequest.of(page.pageNumber, maximo) else page
        val inicio = pagina.pageNumber * pagina.pageSize
        val linhas = odbc.consultar(sqlNotas, parametros(filtro, listOf(), listOf(), inicio, inicio + pagina.pageSize),
            pagina.pageSize).rows
        // O total vem em toda linha (COUNT(*) OVER ()). Pagina alem da ultima volta vazia e sem
        // total: conta de novo pela primeira linha em vez de inventar a partir do deslocamento.
        val total = linhas.firstOrNull()?.let(::total)
            ?: if (inicio == 0) 0L else odbc.consultar(sqlNotas, parametros(filtro, listOf(), listOf(), 0, 1), 1).rows.firstOrNull()?.let(::total) ?: 0L
        return PageImpl(linhas.map { NotaFiscalResumo.from(it) }, pagina, total)
    }

    private fun total(linha: Map<String, Any?>): Long = linha["Total"].toString().toBigDecimal().toLong()

    fun statusSefaz(): List<StatusSefaz> {
        return odbc.consultar(sqlStatusSefaz, mapOf(), 1000).rows.map {
            StatusSefaz(it["Id"].toString().toBigDecimal().toInt(), it["Descricao"]?.toString())
        }
    }

    /**
     * [docEntries] sao NF (OINV) e [adiantamentos] sao ODPI. Com as duas vazias e listagem (tudo o
     * que o filtro pega); com alguma preenchida, cada perna do SQL so traz o que esta na sua lista.
     */
    fun parametros(filtro: NotaFiscalLoteFiltro, docEntries: List<Int>, adiantamentos: List<Int>,
                   inicio: Int, fim: Int): Map<String, Any?> {
        val filiais = filtro.filial.orEmpty()
        // Sem data na tela, faixa larga em vez de filtro opcional: comparar DATE com '' no
        // HANA e erro de conversao, mesmo do lado do OR que nao seria avaliado.
        val dataInicial = data(filtro.dataInicial, "1900-01-01")
        val dataFinal = data(filtro.dataFinal, "2999-12-31")
        val vencimentoDe = data(filtro.vencimentoDe, "1900-01-01")
        val vencimentoAte = data(filtro.vencimentoAte, "2999-12-31")
        if (dataInicial > dataFinal)
            throw Exception("A data de emissao inicial e depois da final.")
        if (vencimentoDe > vencimentoAte)
            throw Exception("O vencimento inicial e depois do final.")
        if (filtro.numeroDe != null && filtro.numeroAte != null && filtro.numeroDe > filtro.numeroAte)
            throw Exception("A NF inicial e maior que a final.")
        val semVencimento = filtro.vencimentoDe.isNullOrBlank() && filtro.vencimentoAte.isNullOrBlank()
        val listagem = docEntries.isEmpty() && adiantamentos.isEmpty()
        return mapOf(
            "dataInicial" to dataInicial,
            "dataFinal" to dataFinal,
            "todasFiliais" to if (filiais.isEmpty()) 1 else 0,
            "filiais" to filiais.ifEmpty { listOf(-1) },
            "todosDocs" to if (listagem) 1 else 0,
            // Listagem: uma linha por parcela, salvo pedido por documento. Download e troca: uma por documento.
            "porParcela" to if (listagem && !filtro.porDocumento) 1 else 0,
            "docEntries" to docEntries.ifEmpty { listOf(-1) },
            "adiantamentos" to adiantamentos.ifEmpty { listOf(-1) },
            // :tipo desliga a perna inteira do SQL; :categoria separa NF de saida da NF futura antiga.
            "tipo" to (filtro.categoria?.tipo?.name ?: ""),
            "categoria" to (filtro.categoria?.name ?: ""),
            "cardCode" to (filtro.cardCode?.trim() ?: ""),
            "vendedor" to (filtro.salesPersonCode ?: -1),
            "numeroDe" to (filtro.numeroDe ?: -1),
            "numeroAte" to (filtro.numeroAte ?: -1),
            "todosVencimentos" to if (semVencimento) 1 else 0,
            "vencimentoDe" to vencimentoDe,
            "vencimentoAte" to vencimentoAte,
            "statusSap" to (filtro.statusSap?.name ?: ""),
            "statusSefaz" to (filtro.statusSefaz ?: -1),
            "inicio" to inicio,
            "fim" to fim,
        )
    }

    // Volta a data validada em AAAA-MM-DD; vazia vira o limite da faixa larga.
    private fun data(valor: String?, padrao: String): String {
        val data = valor?.takeIf { it.isNotBlank() }?.trim() ?: return padrao
        try {
            return LocalDate.parse(data).toString()
        } catch (t: Throwable) {
            throw Exception("Data invalida: '$data'. Use o formato AAAA-MM-DD.")
        }
    }

    /**
     * Monta o zip num arquivo temporario, escrevendo cada nota assim que ela termina e soltando
     * os bytes: a memoria fica proporcional as notas em andamento, nao ao lote inteiro (200 notas
     * com DANFE, XML e boletos passam de 40 MB). Quem chama apaga o arquivo depois de enviar.
     */
    fun zip(docEntries: List<Int>, adiantamentos: List<Int>, pdf: Boolean, xml: Boolean,
            boleto: Boolean = false, contrato: Boolean = false, logo: String? = null): NotaFiscalLoteArquivo {
        val pedidas = chaves(docEntries, adiantamentos)
        if (pedidas.isEmpty())
            throw Exception("Selecione ao menos um documento.")
        if (!pdf && !xml && !boleto && !contrato)
            throw Exception("Escolha baixar o DANFE, o XML, o boleto, o contrato ou mais de um.")
        if (pedidas.size > maximo)
            throw Exception("O lote aceita no maximo $maximo documentos por vez; foram selecionados ${pedidas.size}.")

        // O prazo limita os downloads e conta desde ja. A busca das notas e o carregamento da
        // BankPlus sao sincronos e nao sao interrompidos por ele, mas tem limite proprio (timeout
        // do sap-odbc e da leitura da BankPlus); se passarem do prazo, as notas saem todas como
        // "tempo esgotado" sem esperar mais nada.
        val prazo = System.nanoTime() + TimeUnit.SECONDS.toNanos(prazoSegundos)
        // Busca de novo em vez de confiar no que veio da tela: so entra NF-e emitida para a
        // SEFAZ, e o nome do arquivo sai do numero da nota.
        val porChave = buscaDocumentos(docEntries, adiantamentos).associateBy { it.chave }
        val notas = pedidas.mapNotNull { porChave[it] }
        // So contrato marcado e nenhum adiantamento: o zip sairia vazio, como se tivesse dado certo.
        if (contrato && !pdf && !xml && !boleto && notas.none { it.tipo == TipoDocumentoLote.AD && it.contratoDocEntry != null })
            throw Exception("O contrato so vem de adiantamento de venda futura, e o lote nao tem nenhum.")
        // Nome decidido antes do paralelo e na ordem pedida: a nota que termina primeiro nao
        // pega o nome sem sufixo de outra com o mesmo numero.
        val bases = mutableSetOf<String>()
        val basePorDoc = notas.associate { it.chave to nomeUnico(nomeBase(it), bases) }
        val bankPlusDisponivel = !boleto || carregaEmpresasBankPlus()

        val emPastas = listOf(pdf, xml, boleto, contrato).count { it } > 1
        val caminhos = mutableSetOf<String>()
        val errosPorDoc = mutableMapOf<ChaveDocumentoLote, List<String>>()
        val avisosPorDoc = mutableMapOf<ChaveDocumentoLote, List<String>>()
        var notasSemBoleto = 0
        val arquivo = Files.createTempFile("notas-fiscais-", ".zip")
        try {
            ZipOutputStream(BufferedOutputStream(Files.newOutputStream(arquivo))).use { zip ->
                baixaEmParalelo(notas, prazo, { nota -> baixa(nota, pdf, xml, boleto, bankPlusDisponivel) }) { resultado ->
                    val base = basePorDoc.getValue(resultado.nota.chave)
                    resultado.arquivos.forEach { item ->
                        val pasta = if (emPastas) "${item.tipo.pasta}/" else ""
                        val caminho = nomeUnico("$pasta$base${item.sufixo}", caminhos)
                        zip.putNextEntry(ZipEntry("$caminho.${item.tipo.extensao}"))
                        zip.write(item.conteudo)
                        zip.closeEntry()
                    }
                    errosPorDoc[resultado.nota.chave] = resultado.erros.map { "$base: $it" }
                    avisosPorDoc[resultado.nota.chave] = resultado.avisos.map { "$base: $it" }
                    if (resultado.semBoleto) notasSemBoleto++
                }
                if (contrato)
                    contratos(notas, logo, emPastas, caminhos, zip, errosPorDoc)
                // Na ordem pedida, nao na de conclusao, para o arquivo ser legivel.
                val erros = pedidas.flatMap { doc ->
                    if (doc !in porChave) listOf(naoEncontrado(doc))
                    else errosPorDoc[doc].orEmpty()
                }
                val avisos = pedidas.flatMap { avisosPorDoc[it].orEmpty() }
                if (erros.isNotEmpty())
                    zip.texto("erros.txt", erros)
                if (avisos.isNotEmpty())
                    zip.texto("avisos.txt", avisos)
            }
        } catch (t: Throwable) {
            Files.deleteIfExists(arquivo)
            throw t
        }
        val notasComErro = (pedidas.size - notas.size) + errosPorDoc.values.count { it.isNotEmpty() }
        val nome = "notas-fiscais-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))}.zip"
        return NotaFiscalLoteArquivo(nome, arquivo, notasComErro, notasSemBoleto)
    }

    /**
     * Um PDF por contrato de venda futura dos adiantamentos do lote ("VF<n>.pdf"; varios
     * adiantamentos do mesmo contrato geram um so). NF fica de fora em silencio: o contrato so
     * vem de adiantamento, e a tela ja diz isso. Falha num contrato vira erro dos adiantamentos
     * dele, sem derrubar o zip.
     */
    private fun contratos(notas: List<NotaFiscalResumo>, logo: String?, emPastas: Boolean, caminhos: MutableSet<String>, zip: ZipOutputStream,
                          errosPorDoc: MutableMap<ChaveDocumentoLote, List<String>>) {
        val comContrato = notas.filter { it.tipo == TipoDocumentoLote.AD && it.contratoDocEntry != null }
        if (comContrato.isEmpty())
            return
        val gerador = contratoPdf ?: throw IllegalStateException("Gerador do PDF do contrato nao configurado")
        val resultados = try {
            gerador.gerar(comContrato.mapNotNull { it.contratoDocEntry }, logo)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao buscar os contratos de venda futura", t)
            comContrato.mapNotNull { it.contratoDocEntry }.associateWith { Result.failure(t) }
        }
        val pasta = if (emPastas) "contrato/" else ""
        resultados.forEach { (docEntry, resultado) ->
            resultado.onSuccess { pdf ->
                val caminho = nomeUnico("${pasta}VF${pdf.docNum ?: docEntry}" + (nomeCliente(pdf.cliente)?.let { "-$it" } ?: ""), caminhos)
                zip.putNextEntry(ZipEntry("$caminho.pdf"))
                zip.write(pdf.conteudo)
                zip.closeEntry()
            }.onFailure { t ->
                logger.warn("Lote NF: falha no PDF do contrato $docEntry", t)
                comContrato.filter { it.contratoDocEntry == docEntry }.forEach {
                    errosPorDoc.merge(it.chave, listOf("${nomeBase(it)}: contrato nao gerado (${if (t.javaClass == Exception::class.java) t.message else motivo(t)})"), List<String>::plus)
                }
            }
        }
    }

    private fun ZipOutputStream.texto(nome: String, linhas: List<String>) {
        putNextEntry(ZipEntry(nome))
        write(linhas.joinToString("\r\n", postfix = "\r\n").toByteArray())
        closeEntry()
    }

    // Ordem pedida, sem repetir: NF primeiro, depois adiantamentos.
    fun chaves(docEntries: List<Int>, adiantamentos: List<Int>): List<ChaveDocumentoLote> =
        docEntries.distinct().map { ChaveDocumentoLote(TipoDocumentoLote.NF, it) } +
            adiantamentos.distinct().map { ChaveDocumentoLote(TipoDocumentoLote.AD, it) }

    fun naoEncontrado(chave: ChaveDocumentoLote): String = when (chave.tipo) {
        TipoDocumentoLote.NF -> "NF DocEntry ${chave.docEntry}: nao encontrada ou nao foi emitida para a SEFAZ"
        TipoDocumentoLote.AD -> "Adiantamento DocEntry ${chave.docEntry}: nao encontrado ou nao e de venda futura"
    }

    /**
     * Busca de novo no banco em vez de confiar no que veio da tela: so volta NF-e emitida para a
     * SEFAZ e adiantamento de venda futura, sem os filtros da tela.
     */
    fun buscaDocumentos(docEntries: List<Int>, adiantamentos: List<Int>): List<NotaFiscalResumo> {
        val nf = docEntries.distinct()
        val ad = adiantamentos.distinct()
        if (nf.isEmpty() && ad.isEmpty())
            return listOf()
        val quantidade = nf.size + ad.size
        return odbc.consultar(sqlNotas, parametros(NotaFiscalLoteFiltro(), nf, ad, 0, quantidade),
            quantidade).rows.map { NotaFiscalResumo.from(it) }
    }

    // O cache de empresas da BankPlus e estatico e sem trava: com ele vazio, as tarefas
    // paralelas buscariam /empresas cada uma. Carrega uma vez antes; se falhar, nenhuma nota
    // tenta de novo (seriam ate 200 chamadas a um servico que acabou de falhar) - o boleto de
    // cada uma vira erro. Os GETs da BankPlus tem timeout de leitura (BankPlusService).
    private fun carregaEmpresasBankPlus(): Boolean {
        return try {
            bankPlusService.getEmpresas(comPrazo = true)
            true
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao carregar empresas da BankPlus", t)
            false
        }
    }

    /**
     * Entrega cada nota a [escreve] na ordem em que termina; quem escreve solta os bytes antes da
     * proxima. Estourado o prazo, o que nao terminou e cancelado
     * e vira erro "tempo esgotado" - o resto do zip sai normalmente. A interrupcao nao derruba
     * uma leitura de socket em andamento; ela termina pelo timeout de leitura de cada cliente HTTP
     * (OkHttp da TaxPlus, GETs da BankPlus). Threads daemon para isso nao segurar a JVM.
     */
    private fun baixaEmParalelo(notas: List<NotaFiscalResumo>, prazo: Long,
                                tarefa: (NotaFiscalResumo) -> ResultadoNota, escreve: (ResultadoNota) -> Unit) {
        if (notas.isEmpty())
            return
        val pool = Executors.newFixedThreadPool(paralelismo.coerceIn(1, notas.size)) { trabalho ->
            Thread(trabalho, "nota-fiscal-lote").also { it.isDaemon = true }
        }
        val conclusao = ExecutorCompletionService<ResultadoNota>(pool)
        val futuros = notas.associateWith { nota -> conclusao.submit { tarefa(nota) } }
        val escritas = mutableSetOf<ChaveDocumentoLote>()
        try {
            while (escritas.size < notas.size) {
                val restante = prazo - System.nanoTime()
                val pronto = (if (restante > 0) conclusao.poll(restante, TimeUnit.NANOSECONDS) else null) ?: break
                val resultado = pronto.get()
                escritas.add(resultado.nota.chave)
                escreve(resultado)
            }
            for ((nota, futuro) in futuros.filterKeys { it.chave !in escritas }) {
                if (futuro.isDone && !futuro.isCancelled) {
                    escreve(futuro.get())
                } else {
                    futuro.cancel(true)
                    logger.warn("Lote NF: DocEntry ${nota.docEntry} passou do prazo de ${prazoSegundos}s")
                    escreve(ResultadoNota(nota).also { it.erros.add("tempo esgotado (prazo do lote: ${prazoSegundos}s)") })
                }
            }
        } finally {
            pool.shutdownNow()
        }
    }

    // Nao lanca: qualquer falha vira erro da nota, para uma nota quebrada nao derrubar o zip.
    private fun baixa(nota: NotaFiscalResumo, pdf: Boolean, xml: Boolean, boleto: Boolean,
                      bankPlusDisponivel: Boolean): ResultadoNota {
        val resultado = ResultadoNota(nota)
        try {
            if ((pdf || xml) && nota.tipo == TipoDocumentoLote.AD)
                resultado.avisos.add("adiantamento nao emite NF-e: sem DANFE nem XML")
            else if (pdf || xml)
                baixaFiscal(nota, pdf, xml, resultado)
            if (boleto && !bankPlusDisponivel)
                resultado.erros.add("boleto nao buscado: BankPlus indisponivel")
            else if (boleto)
                baixaBoletos(nota, resultado)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha inesperada no DocEntry ${nota.docEntry}", t)
            resultado.erros.add("falha inesperada (${motivo(t)})")
        }
        return resultado
    }

    /**
     * O que vai para o erros.txt. Excecao lancada por este codigo (Exception pura) tem texto
     * controlado; as de HTTP/IO/JSON carregam corpo de resposta e detalhe interno das APIs,
     * que ficam so no log (cada chamada ja loga a excecao inteira).
     */
    fun motivo(t: Throwable): String = when {
        t.javaClass == Exception::class.java -> t.message ?: "erro sem mensagem"
        t is HttpStatusCodeException -> "HTTP ${t.statusCode.value()}"
        t is JsonProcessingException -> "resposta em formato inesperado"
        t is ResourceAccessException || t is IOException -> "sem resposta do servico"
        else -> "erro interno (${t.javaClass.simpleName})"
    }

    private fun baixaFiscal(nota: NotaFiscalResumo, pdf: Boolean, xml: Boolean, resultado: ResultadoNota) {
        val documento = try {
            taxNfeService.consultar(nota.docEntry, 13)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao consultar DocEntry ${nota.docEntry} na TaxPlus", t)
            resultado.erros.add("consulta na TaxPlus falhou (${motivo(t)})")
            return
        }
        listOfNotNull(
            if (pdf) TipoArquivo.DANFE to taxNfeService::pdf else null,
            if (xml) TipoArquivo.XML to taxNfeService::xml else null,
        ).forEach { (tipo, busca) ->
            try {
                resultado.arquivos.add(Arquivo(tipo, "", busca(documento)))
            } catch (t: Throwable) {
                logger.warn("Lote NF: falha ao baixar ${tipo.pasta} do DocEntry ${nota.docEntry}", t)
                resultado.erros.add("${tipo.pasta} nao baixado (${motivo(t)})")
            }
        }
    }

    /**
     * Um pdf por boleto da nota na BankPlus (um por parcela). Nota sem boleto e o caso comum - a
     * forma de pagamento nao e boleto - e vai para avisos.txt, nao para erros. Boleto cancelado
     * ou "Aguardando Cancelamento" fica de fora: o lote costuma ir para o cliente.
     */
    private fun baixaBoletos(nota: NotaFiscalResumo, resultado: ResultadoNota) {
        val filial = nota.filialId ?: run {
            resultado.erros.add("boleto nao buscado: nota sem filial")
            return
        }
        val boletos = try {
            bankPlusService.getBoletosBy(filial.toString(), nota.docEntry.toString(), origemBoleto(nota), comPrazo = true)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao listar boletos do DocEntry ${nota.docEntry} na BankPlus", t)
            resultado.erros.add("consulta de boleto na BankPlus falhou (${motivo(t)})")
            return
        }
        val validos = boletos.filter { !(it.statusDescricao ?: "").contains("cancel", ignoreCase = true) && it.id != null }
        if (boletos.isEmpty())
            resultado.avisos.add("sem boleto na BankPlus")
        else if (validos.isEmpty())
            resultado.avisos.add("boleto cancelado, nao incluido (${boletos.mapNotNull { it.statusDescricao }.distinct().joinToString()})")
        if (validos.isEmpty()) {
            resultado.semBoleto = true
            return
        }
        validos.sortedBy { it.numeroDaParcela ?: 0 }.forEach { b ->
            val parcela = b.numeroDaParcela?.let { "-parcela$it" } ?: "-boleto${b.id}"
            try {
                val conteudo = bankPlusService.getPdf(b.id.toString(), comPrazo = true) ?: ByteArray(0)
                if (!String(conteudo.copyOfRange(0, minOf(conteudo.size, 8))).startsWith("%PDF"))
                    throw Exception(if (conteudo.isEmpty()) "pdf vazio" else "BankPlus nao devolveu um pdf")
                resultado.arquivos.add(Arquivo(TipoArquivo.BOLETO, parcela, conteudo))
            } catch (t: Throwable) {
                logger.warn("Lote NF: falha ao baixar boleto ${b.id} do DocEntry ${nota.docEntry}", t)
                resultado.erros.add("boleto$parcela nao baixado (${motivo(t)})")
            }
        }
    }

    // Na BankPlus o boleto do adiantamento fica numa origem propria, nao na da nota fiscal.
    fun origemBoleto(nota: NotaFiscalResumo): OrigemBoletoEnum = when (nota.tipo) {
        TipoDocumentoLote.NF -> OrigemBoletoEnum.notafiscal
        TipoDocumentoLote.AD -> OrigemBoletoEnum.adiantamento
    }

    // "NF501-S2-C0001-JOAO_DA_SILVA": numero, serie, codigo e nome do cliente (quem abre o zip
    // procura pelo nome).
    private fun nomeBase(nota: NotaFiscalResumo): String {
        val numero = nota.numero?.toString() ?: "doc${nota.docEntry}"
        val cliente = nota.cardCode?.let { "-$it" } ?: ""
        val base = when (nota.tipo) {
            TipoDocumentoLote.NF -> "NF$numero" + (nota.serie?.takeIf { it.isNotBlank() }?.let { "-S$it" } ?: "")
            TipoDocumentoLote.AD -> "AD$numero" + (nota.contrato?.let { "-VF$it" } ?: "")
        }
        return "$base$cliente".replace(Regex("[^A-Za-z0-9._-]"), "_") + (nomeCliente(nota.cardName)?.let { "-$it" } ?: "")
    }

    private fun nomeUnico(base: String, usados: MutableSet<String>): String {
        var nome = base
        var i = 2
        while (!usados.add(nome))
            nome = "$base-${i++}"
        return nome
    }

    companion object {
        // Nome do cliente para nome de arquivo: sem acento, so letra/numero/_ e ate 40 caracteres.
        fun nomeCliente(nome: String?): String? = nome
            ?.let { java.text.Normalizer.normalize(it, java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}"), "") }
            ?.replace(Regex("[^A-Za-z0-9]+"), "_")?.trim('_')?.take(40)?.trimEnd('_')?.ifEmpty { null }
    }

    private enum class TipoArquivo(val pasta: String, val extensao: String) {
        DANFE("danfe", "pdf"), XML("xml", "xml"), BOLETO("boleto", "pdf")
    }

    private class Arquivo(val tipo: TipoArquivo, val sufixo: String, val conteudo: ByteArray)

    private class ResultadoNota(val nota: NotaFiscalResumo) {
        val arquivos = mutableListOf<Arquivo>()
        val erros = mutableListOf<String>()
        val avisos = mutableListOf<String>()
        var semBoleto = false
    }
}
