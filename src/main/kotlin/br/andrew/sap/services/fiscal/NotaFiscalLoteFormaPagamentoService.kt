package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.CategoriaDocumentoLote
import br.andrew.sap.model.fiscal.FormaPagamentoLote
import br.andrew.sap.model.fiscal.StatusSapNota
import br.andrew.sap.model.fiscal.NotaFiscalResumo
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.model.fiscal.TrocaFormaPagamentoRequest
import br.andrew.sap.model.fiscal.TrocaFormaPagamentoResultado
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.apache.hc.client5.http.ConnectTimeoutException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Troca a forma de pagamento (PaymentMethod) dos documentos do lote: NF (Invoices) e
 * adiantamento de venda futura (DownPayments).
 *
 * A forma tem de ficar tambem no cadastro do parceiro (BPPaymentMethods / CRD2). Dois caminhos:
 *  - o PATCH volta "Linked payment method X is inactive or is no longer linked with business
 *    partner Y": vincula com o BusinessPartnersService.addPaymentMethod - o mesmo que o
 *    SapErrorListener usa quando um POST cai nesse erro - e repete o PATCH uma vez;
 *  - o PATCH passa mesmo sem o vinculo (o SAP aceitou assim no HOMOLOG em 2026-10-06): depois da
 *    troca, o parceiro que nao tem a forma no CRD2 (uma consulta para o lote todo) e vinculado.
 * Cada parceiro e vinculado uma vez so. A mensagem de erro e lida do corpo sem passar pelo
 * SapError.getError(), que construiria a PixPaymentException e dispararia o vinculo outra vez.
 *
 * Documento fechado, cancelado ou devolvido tambem e tentado (decisao do usuario): o que o SAP
 * recusar volta como ERRO com a mensagem dele. Boleto ja emitido nao e mexido: se o documento teve
 * a forma trocada e ainda tem boleto ativo na BankPlus, o resultado so avisa.
 */
@Service
class NotaFiscalLoteFormaPagamentoService(
    val odbc: OdbcClient,
    val lote: NotaFiscalLoteService,
    val invoiceService: InvoiceService,
    val downPaymentService: DownPaymentService,
    val businessPartnersService: BusinessPartnersService,
    val bankPlusService: BankPlusService,
    @Value("\${nota-fiscal.lote.maximo:200}") val maximo: Int = 200,
    @Value("\${nota-fiscal.lote.paralelismo:4}") val paralelismo: Int = 4,
    // Teto da troca inteira. O RestTemplate do Service Layer nao tem timeout de leitura: sem isso
    // um PATCH pendurado prenderia a requisicao para sempre.
    @Value("\${nota-fiscal.lote.forma-prazo-segundos:300}") val prazoSegundos: Long = 300,
) {

    private val logger = LoggerFactory.getLogger(NotaFiscalLoteFormaPagamentoService::class.java)
    private val json = ObjectMapper()

    // Um pool so para todas as trocas, nao um por requisicao: o prazo devolve a resposta mas nao
    // corta um PATCH pendurado (sem timeout de leitura no Service Layer), e com pool por requisicao
    // cada nova tentativa somaria threads presas. Assim ficam no maximo [paralelismo]; documento de
    // outra troca que nao consegue thread dentro do prazo volta "nada foi gravado".
    private val pool = ThreadPoolExecutor(paralelismo.coerceAtLeast(1), paralelismo.coerceAtLeast(1), 0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue()) { trabalho ->
        Thread(trabalho, "nota-fiscal-lote-forma").also { it.isDaemon = true }
    }

    @PreDestroy
    fun encerrar() {
        pool.shutdownNow()
    }

    // Inicio de documento e fim do prazo sob a mesma trava: sem isso uma thread podia conferir o
    // parar, ser suspensa, e gravar depois de a resposta dizer "nada foi gravado".
    private class Controle {
        var parar = false
        val iniciados = mutableSetOf<ChaveResultado>()
    }

    private val sqlFormas by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-formas-pagamento.sql") }
    private val sqlParceirosComForma by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-parceiros-com-forma.sql") }

    // Formas de recebimento ativas da OPYM. A tela oferece todas, nao so as do parceiro: a que
    // faltar no cadastro e vinculada na troca.
    fun formas(): List<FormaPagamentoLote> {
        return odbc.consultar(sqlFormas, mapOf(), 1000).rows.map {
            FormaPagamentoLote(it["Codigo"].toString(), it["Descricao"]?.toString())
        }
    }

    fun trocar(request: TrocaFormaPagamentoRequest, usuario: String): List<TrocaFormaPagamentoResultado> {
        // O prazo conta desde ja: as consultas de preparo (formas, documentos, CRD2) entram nele. Elas
        // nao sao cortadas no prazo - ja tem teto proprio no timeout de leitura do OdbcClient.
        val prazo = System.nanoTime() + TimeUnit.SECONDS.toNanos(prazoSegundos)
        val forma = request.formaPagamento.trim()
        if (forma.isEmpty())
            throw Exception("Escolha a forma de pagamento.")
        if (formas().none { it.codigo == forma })
            throw Exception("Forma de pagamento '$forma' nao existe ou nao esta ativa para recebimento.")
        val pedidas = lote.chaves(request.docEntries, request.adiantamentos)
        if (pedidas.isEmpty())
            throw Exception("Selecione ao menos um documento.")
        if (pedidas.size > maximo)
            throw Exception("O lote aceita no maximo $maximo documentos por vez; foram selecionados ${pedidas.size}.")

        val porChave = lote.buscaDocumentos(request.docEntries, request.adiantamentos).associateBy { it.chave }
        // NF de venda futura do padrao antigo o SAP sempre recusa ("Field cannot be updated"): nem tenta.
        val (naoPermitidos, aTrocar) = pedidas.mapNotNull { porChave[it] }
            .partition { it.categoria == CategoriaDocumentoLote.NF_FUTURA && it.formaPagamento != forma }
        val jaTemForma = parceirosComForma(forma, aTrocar.mapNotNull { it.cardCode }.distinct())
        val resultados = (trocaPorParceiro(aTrocar, forma, usuario, jaTemForma, prazo) + naoPermitidos.map { naoPermitido(it) })
            .associateBy { ChaveResultado(it.tipo, it.docEntry) }
        return pedidas.map { chave ->
            resultados[ChaveResultado(chave.tipo, chave.docEntry)] ?: TrocaFormaPagamentoResultado(
                chave.tipo, chave.docEntry, null, null, null, null, null, SituacaoTrocaFormaPagamento.NAO_ENCONTRADO,
                lote.naoEncontrado(chave))
        }
    }

    private fun naoPermitido(doc: NotaFiscalResumo) =
        TrocaFormaPagamentoResultado(doc.tipo, doc.docEntry, doc.numero, doc.cardCode, doc.cardName, doc.categoria,
            doc.formaPagamento, SituacaoTrocaFormaPagamento.NAO_PERMITIDO, NF_FUTURA_SEM_TROCA)

    private fun parceirosComForma(forma: String, parceiros: List<String>): Set<String> {
        if (parceiros.isEmpty())
            return setOf()
        return odbc.consultar(sqlParceirosComForma, mapOf("forma" to forma, "parceiros" to parceiros), parceiros.size)
            .rows.mapNotNull { it["CardCode"]?.toString() }.toSet()
    }

    /**
     * Um parceiro por vez em cada thread: dois documentos do mesmo parceiro em paralelo cairiam os
     * dois no erro de vinculo e vinculariam a forma duas vezes. Com eles em sequencia, o primeiro
     * vincula e o segundo ja passa direto.
     *
     * Prazo: estourado, nenhum PATCH novo comeca (o [parar] e conferido antes de cada documento) e a
     * resposta sai. Nada e interrompido no meio: o documento cujo PATCH estava em andamento volta
     * SEM_CONFIRMACAO (pode ter gravado), e os que nem comecaram voltam ERRO dizendo que nada foi
     * gravado - dizer "tempo esgotado" de um PATCH que ainda pode gravar mentiria sobre o resultado.
     * O PATCH confirmado entra em [feitos] na hora, antes do vinculo e da BankPlus: se o prazo acabar
     * numa dessas conferencias, o documento volta com a situacao gravada e o aviso do que faltou.
     */
    private fun trocaPorParceiro(documentos: List<NotaFiscalResumo>, forma: String, usuario: String,
                                 jaTemForma: Set<String>, prazo: Long): List<TrocaFormaPagamentoResultado> {
        if (documentos.isEmpty())
            return listOf()
        val grupos = documentos.groupBy { it.cardCode ?: "" }.values.toList()
        val feitos = ConcurrentHashMap<ChaveResultado, TrocaFormaPagamentoResultado>()
        val controle = Controle()
        val futuros = mutableListOf<Future<*>>()
        try {
            // Cada grupo e um parceiro so: o "ja vinculado" do grupo nao e disputado entre threads.
            grupos.mapTo(futuros) { grupo ->
                val vinculado = booleanArrayOf(grupo.first().cardCode in jaTemForma)
                pool.submit {
                    for (doc in grupo) {
                        val chave = ChaveResultado(doc.tipo, doc.docEntry)
                        // O prazo tambem e conferido aqui: vencido no preparo, a thread pode pegar o
                        // grupo antes de o parar ser ligado.
                        val comecou = synchronized(controle) {
                            !controle.parar && System.nanoTime() < prazo && controle.iniciados.add(chave)
                        }
                        if (!comecou)
                            break
                        feitos[chave] = troca(doc, forma, usuario, vinculado) { feitos[chave] = it }
                    }
                }
            }
            for (futuro in futuros) {
                val restante = prazo - System.nanoTime()
                try {
                    if (restante <= 0) break
                    futuro.get(restante, TimeUnit.NANOSECONDS)
                } catch (e: TimeoutException) {
                    break
                } catch (e: ExecutionException) {
                    logger.warn("Lote NF: falha inesperada num grupo da troca de forma", e.cause)
                }
            }
        } finally {
            synchronized(controle) { controle.parar = true }
            // Grupo que nem comecou sai da fila: com as threads presas num PATCH, a fila (sem limite)
            // cresceria a cada troca. O que esta rodando nao e interrompido - so para no proximo documento.
            futuros.forEach { it.cancel(false) }
            pool.purge()
        }
        // Com o parar ligado nenhum documento comeca mais: o que nao esta aqui nao foi gravado.
        val iniciados = synchronized(controle) { controle.iniciados.toSet() }
        return documentos.map { doc ->
            val chave = ChaveResultado(doc.tipo, doc.docEntry)
            feitos[chave] ?: if (chave in iniciados) {
                logger.warn("Lote NF: troca de forma de {} {} sem confirmacao no prazo de {}s", doc.tipo, doc.docEntry, prazoSegundos)
                TrocaFormaPagamentoResultado(doc.tipo, doc.docEntry, doc.numero, doc.cardCode, doc.cardName, doc.categoria,
                    doc.formaPagamento, SituacaoTrocaFormaPagamento.SEM_CONFIRMACAO,
                    "o prazo da troca (${prazoSegundos}s) acabou com a gravacao em andamento: confira no SAP")
            } else {
                TrocaFormaPagamentoResultado(doc.tipo, doc.docEntry, doc.numero, doc.cardCode, doc.cardName, doc.categoria,
                    doc.formaPagamento, SituacaoTrocaFormaPagamento.ERRO,
                    "nao processado: o prazo da troca (${prazoSegundos}s) acabou antes; nada foi gravado")
            }
        }
    }

    /**
     * [vinculado] e o estado do parceiro do grupo (um elemento, compartilhado pelos documentos dele):
     * true quando a forma ja esta no cadastro ou acabou de ser vinculada nesta troca.
     * [confirmado] recebe o resultado assim que o documento fica decidido, antes das conferencias
     * de vinculo e boleto (ver [trocaPorParceiro]).
     */
    private fun troca(doc: NotaFiscalResumo, forma: String, usuario: String, vinculado: BooleanArray,
                      confirmado: (TrocaFormaPagamentoResultado) -> Unit): TrocaFormaPagamentoResultado {
        fun resultado(situacao: SituacaoTrocaFormaPagamento, mensagens: List<String> = listOf(), boletoAtivo: Boolean = false,
                      pendencia: Boolean = false) =
            TrocaFormaPagamentoResultado(doc.tipo, doc.docEntry, doc.numero, doc.cardCode, doc.cardName, doc.categoria,
                doc.formaPagamento, situacao, mensagens.joinToString("; ").ifEmpty { null }, boletoAtivo, pendencia)
        fun semResposta(t: Throwable): TrocaFormaPagamentoResultado {
            logger.warn("Lote NF: troca de forma sem resposta do SAP em ${doc.tipo} ${doc.docEntry}", t)
            return resultado(SituacaoTrocaFormaPagamento.SEM_CONFIRMACAO,
                listOf("a conexao com o SAP caiu durante a gravacao (${lote.motivo(t)}): confira no SAP"))
        }
        val conferenciaPendente = "o prazo da troca (${prazoSegundos}s) acabou antes de conferir o vinculo ao parceiro e o boleto"

        if (doc.formaPagamento == forma) {
            // Ja estava com a forma; o parceiro ainda pode nao ter (o SAP deixou gravar sem vinculo).
            confirmado(resultado(SituacaoTrocaFormaPagamento.SEM_MUDANCA, listOf(conferenciaPendente), pendencia = true))
            val vinculo = garanteVinculo(doc, forma, usuario, vinculado)
            return resultado(SituacaoTrocaFormaPagamento.SEM_MUDANCA, listOfNotNull(vinculo.mensagem), pendencia = vinculo.falhou)
        }

        var situacao = try {
            grava(doc, forma)
            SituacaoTrocaFormaPagamento.ATUALIZADO
        } catch (t: Throwable) {
            if (gravacaoIncerta(t))
                return semResposta(t)
            val mensagem = mensagemSap(t)
            if (mensagem == null || !mensagem.contains("Linked payment method", ignoreCase = true)) {
                logger.warn("Lote NF: troca de forma recusada em ${doc.tipo} ${doc.docEntry}", t)
                return resultado(SituacaoTrocaFormaPagamento.ERRO, listOfNotNull(explicaRecusa(doc, mensagem), mensagem ?: lote.motivo(t)))
            }
            // false = outra troca vinculou no meio do caminho: esta so gravou.
            val vinculou = try {
                vincula(doc, forma, usuario).also { vinculado[0] = true }
            } catch (t2: Throwable) {
                logger.warn("Lote NF: falha ao vincular a forma $forma ao parceiro ${doc.cardCode} antes de repetir a troca", t2)
                return resultado(SituacaoTrocaFormaPagamento.ERRO, listOf(mensagemSap(t2) ?: lote.motivo(t2)))
            }
            try {
                grava(doc, forma)
                if (vinculou) SituacaoTrocaFormaPagamento.VINCULADO_E_ATUALIZADO else SituacaoTrocaFormaPagamento.ATUALIZADO
            } catch (t2: Throwable) {
                if (gravacaoIncerta(t2))
                    return semResposta(t2)
                logger.warn("Lote NF: troca de forma falhou depois de vincular em ${doc.tipo} ${doc.docEntry}", t2)
                return resultado(SituacaoTrocaFormaPagamento.ERRO, listOf(mensagemSap(t2) ?: lote.motivo(t2)))
            }
        }
        confirmado(resultado(situacao, listOf(conferenciaPendente), pendencia = true))
        logger.info("Lote NF: forma de pagamento de {} {} trocada de {} para {} por {}",
            doc.tipo, doc.docEntry, doc.formaPagamento, forma, usuario)
        val vinculo = garanteVinculo(doc, forma, usuario, vinculado)
        if (vinculo.vinculou)
            situacao = SituacaoTrocaFormaPagamento.VINCULADO_E_ATUALIZADO
        val boleto = avisoBoleto(doc)
        return resultado(situacao, listOfNotNull(vinculo.mensagem, boleto?.mensagem), boleto?.ativo ?: false,
            vinculo.falhou || boleto?.naoConferido == true)
    }

    /**
     * "Field cannot be updated" no PeyMethod nao diz o porque. Os dois casos vistos no HOMOLOG2
     * (2026-10-06): NF futura do padrao antigo (fatura de reserva com entrega), que nem chega aqui
     * (NAO_PERMITIDO), e documento fechado.
     */
    private fun explicaRecusa(doc: NotaFiscalResumo, mensagem: String?): String? {
        if (mensagem == null || !mensagem.contains("cannot be updated", ignoreCase = true) || doc.statusSap == StatusSapNota.ABERTO)
            return null
        return "documento ${doc.statusSap?.name?.lowercase() ?: "fechado"}: o SAP nao deixa trocar a forma"
    }

    private class Vinculo(val vinculou: Boolean, val mensagem: String? = null, val falhou: Boolean = false)

    // Vincula a forma ao parceiro se ele ainda nao tem. Falha aqui nao desfaz a troca do documento:
    // so vira aviso no resultado.
    private fun garanteVinculo(doc: NotaFiscalResumo, forma: String, usuario: String, vinculado: BooleanArray): Vinculo {
        if (vinculado[0])
            return Vinculo(false)
        return try {
            val vinculou = vincula(doc, forma, usuario)
            vinculado[0] = true
            if (vinculou) Vinculo(true, "forma vinculada ao parceiro") else Vinculo(false)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao vincular a forma $forma ao parceiro ${doc.cardCode}", t)
            Vinculo(false, "forma NAO vinculada ao parceiro (${mensagemSap(t) ?: lote.motivo(t)})", falhou = true)
        }
    }

    /**
     * true = esta chamada vinculou; false = a forma ja estava no cadastro. Se o vinculo falhar,
     * confere o cadastro de novo: outra troca ao mesmo tempo pode ter vinculado primeiro (o "ja tem"
     * desta troca so vale para ela), e ai a falha nao e problema.
     */
    private fun vincula(doc: NotaFiscalResumo, forma: String, usuario: String): Boolean {
        val cardCode = doc.cardCode ?: throw Exception("documento sem parceiro")
        try {
            businessPartnersService.addPaymentMethod(cardCode, forma)
        } catch (t: Throwable) {
            if (cardCode in parceirosComForma(forma, listOf(cardCode))) {
                logger.info("Lote NF: forma {} ja estava vinculada ao parceiro {} (vinculo concorrente)", forma, cardCode)
                return false
            }
            throw t
        }
        logger.info("Lote NF: forma {} vinculada ao parceiro {} por {}", forma, cardCode, usuario)
        return true
    }

    private fun grava(doc: NotaFiscalResumo, forma: String) {
        val corpo = mapOf("PaymentMethod" to forma)
        when (doc.tipo) {
            TipoDocumentoLote.NF -> invoiceService.update(corpo, doc.docEntry.toString())
            TipoDocumentoLote.AD -> downPaymentService.update(corpo, doc.docEntry.toString())
        }
    }

    /**
     * Toda troca que mudou a forma confere a BankPlus: o boleto emitido continua valendo e ninguem
     * no sap-service o cancela. Nao depende da forma anterior ser de boleto - depois de um boleto ->
     * Pix, uma troca Pix -> outra ainda tem o boleto antigo ativo - nem da lista boleto.ids, que nao
     * tem todas as formas de boleto (a do Sicoob, por exemplo). Nulo = sem boleto ativo.
     */
    private fun avisoBoleto(doc: NotaFiscalResumo): AvisoBoleto? {
        val filial = doc.filialId ?: return AvisoBoleto("nao deu para conferir boleto: documento sem filial", naoConferido = true)
        return try {
            val ativos = bankPlusService.getBoletosBy(filial.toString(), doc.docEntry.toString(), lote.origemBoleto(doc), comPrazo = true)
                .filter { !(it.statusDescricao ?: "").contains("cancel", ignoreCase = true) }
            if (ativos.isEmpty()) null
            else AvisoBoleto("ainda tem ${ativos.size} boleto(s) ativo(s) na BankPlus", ativo = true)
        } catch (t: Throwable) {
            logger.warn("Lote NF: falha ao conferir boleto de ${doc.tipo} ${doc.docEntry}", t)
            AvisoBoleto("nao deu para conferir boleto na BankPlus (${lote.motivo(t)})", naoConferido = true)
        }
    }

    private class AvisoBoleto(val mensagem: String, val ativo: Boolean = false, val naoConferido: Boolean = false)

    /**
     * O PATCH saiu e a conexao caiu sem resposta (reset, resposta truncada): o SAP pode ter gravado.
     * Recusa do SAP (HttpStatusCodeException), falha ao conectar e falha no login da sessao (feito
     * antes do PATCH, pelo AuthService) sao certas: nada foi gravado. Na duvida, incerta.
     */
    private fun gravacaoIncerta(t: Throwable): Boolean {
        if (t !is ResourceAccessException)
            return false
        // O RestTemplate poe a URL na mensagem: "I/O error on POST request for ".../b1s/v1/Login": ..."
        if (t.message?.contains("/b1s/v1/Login") == true)
            return false
        return generateSequence<Throwable>(t) { it.cause }.none {
            it is ConnectException || it is UnknownHostException || it is ConnectTimeoutException
        }
    }

    // Texto da mensagem de erro do Service Layer: {"error":{"code":..,"message":{"value":".."}}}.
    // Lida do corpo direto, sem o SapError.getError() (ver doc da classe).
    private fun mensagemSap(t: Throwable): String? {
        val http = t as? HttpStatusCodeException ?: return null
        return runCatching {
            json.readTree(http.responseBodyAsString).path("error").path("message").path("value").asText()
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private data class ChaveResultado(val tipo: TipoDocumentoLote, val docEntry: Int)

    private companion object {
        const val NF_FUTURA_SEM_TROCA =
            "NF de venda futura do padrao antigo (fatura de reserva com entrega): o SAP nao deixa trocar a forma"
    }
}
