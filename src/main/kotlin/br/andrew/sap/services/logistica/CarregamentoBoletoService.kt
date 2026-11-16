package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.BoletoIdsConfig
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.services.fiscal.OrigemBoletoEnum
import br.andrew.sap.services.fiscal.motivoFalha
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.client.HttpStatusCodeException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Boletos BankPlus das NFs geradas pela ordem de carregamento, para o usuario nao precisar entrar
 * no SAP. Gera por parcela so o que nunca teve boleto: clicar de novo nao duplica. Parcela com boleto
 * cancelado NAO e reemitida: o boleto e cancelado quando a parcela e paga por PIX
 * (AccountsReceivableService.cancelBoletos), e reemitir cobraria de novo.
 * "E boleto" = forma de pagamento da nota em boleto.ids (a mesma lista da tela de faturas).
 */
@Service
class CarregamentoBoletoService(val bankPlus: BankPlusService, val boletoIds: BoletoIdsConfig, val odbc: OdbcClient) {

    private val logger = LoggerFactory.getLogger(CarregamentoBoletoService::class.java)

    // Duplo clique ou duas abas: as duas leriam "sem boleto" e gerariam em dobro.
    // shortcut: trava so nesta instancia, como a geracao de notas do CarregamentoController.
    private val gerando = ConcurrentHashMap.newKeySet<Int>()

    fun gerar(idOrdem: Int, notas: List<Document>): List<ResultadoBoleto> {
        if (!gerando.add(idOrdem))
            throw Exception("Os boletos desta ordem já estão sendo gerados; aguarde terminar.")
        try {
            val abertas = parcelasEmAberto(notas)
            return notas.map { gerar(it, abertas) }
        } finally {
            gerando.remove(idOrdem)
        }
    }

    // Uma consulta para a ordem toda. Sem ela nao da para saber o que ja foi pago: nao gera nada.
    private fun parcelasEmAberto(notas: List<Document>): Set<Pair<Int, Int>> {
        val docEntries = notas.mapNotNull { it.docEntry }.distinct()
        if (docEntries.isEmpty()) return emptySet()
        return try {
            odbc.consultar(SqlResource.carregar("odbc/carregamento-parcelas-abertas.sql"), mapOf("docEntries" to docEntries), 5000).rows
                .map { it["DocEntry"].toString().toInt() to it["InstlmntID"].toString().toInt() }.toSet()
        } catch (t: Throwable) {
            logger.warn("Carregamento: falha ao consultar as parcelas em aberto (INV6)", t)
            throw Exception("Não foi possível conferir as parcelas pagas no SAP; nenhum boleto foi gerado. Tente de novo.")
        }
    }

    private fun gerar(nota: Document, abertas: Set<Pair<Int, Int>>): ResultadoBoleto {
        val nome = "NF ${nota.SequenceSerial ?: nota.docNum}"
        if (nota.paymentMethod !in boletoIds.ids)
            return ResultadoBoleto(nome, SituacaoBoleto.NAO_E_BOLETO,
                "forma de pagamento ${nota.paymentMethod ?: "vazia"} não é boleto")
        val docEntry = nota.docEntry ?: return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "nota sem DocEntry")
        val todas = nota.documentInstallments?.mapNotNull { it.InstallmentId }.orEmpty()
        if (todas.isEmpty())
            return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "nota sem parcela no SAP")
        val pagas = todas.filter { (docEntry to it) !in abertas }
        val parcelas = todas - pagas.toSet()
        val avisoPagas = pagas.takeIf { it.isNotEmpty() }?.let { "; parcela(s) ${it.joinToString()} já paga(s)" } ?: ""
        if (parcelas.isEmpty())
            return ResultadoBoleto(nome, SituacaoBoleto.JA_PAGA, "parcela(s) ${pagas.joinToString()} já paga(s), sem boleto a gerar")
        val filial = nota.getBPL_IDAssignedToInvoice()

        val boletos = try {
            bankPlus.getBoletosBy(filial, docEntry.toString(), OrigemBoletoEnum.notafiscal, comPrazo = true)
        } catch (t: Throwable) {
            logger.warn("Carregamento: falha ao consultar boletos da NF $docEntry na BankPlus", t)
            return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "consulta na BankPlus falhou (${motivoFalha(t)})")
        }
        val ativas = ativos(boletos).mapNotNull { it.numeroDaParcela }.toSet()
        val canceladas = parcelas.filter { p -> p !in ativas && boletos.any { it.numeroDaParcela == p } }
        val aviso = avisoPagas + (canceladas.takeIf { it.isNotEmpty() }
            ?.let { "; parcela(s) ${it.joinToString()} com boleto cancelado, não reemitida(s) (pode ter sido paga por PIX)" } ?: "")
        val faltam = parcelas.filter { it !in ativas && it !in canceladas }
        if (faltam.isEmpty())
            return ResultadoBoleto(nome, SituacaoBoleto.JA_TINHA, "já tinha boleto nas parcelas em aberto$aviso")

        val erros = faltam.mapNotNull { parcela ->
            try {
                bankPlus.geraBoletos(filial.toInt(), docEntry, parcela, OrigemBoletoEnum.notafiscal, comPrazo = true)
                null
            } catch (t: Throwable) {
                logger.warn("Carregamento: falha ao gerar boleto da NF $docEntry parcela $parcela", t)
                "parcela $parcela: ${motivoFalha(t)}${recusaBankPlus(t)?.let { " - $it" } ?: ""}"
            }
        }
        return if (erros.isEmpty())
            ResultadoBoleto(nome, SituacaoBoleto.GERADO, "parcela(s) ${faltam.joinToString()} gerada(s)$aviso")
        else
            ResultadoBoleto(nome, SituacaoBoleto.ERRO, erros.joinToString("; ") + aviso)
    }

    /**
     * Zip com o pdf de cada boleto ativo das notas, no molde do CarregamentoService.danfes: o que
     * falhar vai para erros.txt sem derrubar o resto.
     * shortcut: monta em memoria; uma ordem tem poucas notas.
     */
    fun zip(notas: List<Document>): ByteArray {
        val saida = ByteArrayOutputStream()
        val erros = mutableListOf<String>()
        val usados = mutableSetOf<String>()
        ZipOutputStream(saida).use { zip ->
            notas.forEach { nota ->
                val nome = "NF ${nota.SequenceSerial ?: nota.docNum}"
                val boletos = try {
                    ativos(bankPlus.getBoletosBy(nota.getBPL_IDAssignedToInvoice(), nota.docEntry.toString(),
                        OrigemBoletoEnum.notafiscal, comPrazo = true))
                } catch (t: Throwable) {
                    logger.warn("Carregamento: falha ao consultar boletos da NF ${nota.docEntry} na BankPlus", t)
                    erros += "$nome: consulta na BankPlus falhou (${motivoFalha(t)})"
                    return@forEach
                }
                if (boletos.isEmpty()) erros += "$nome: sem boleto na BankPlus"
                boletos.sortedBy { it.numeroDaParcela ?: 0 }.forEach { b ->
                    try {
                        val pdf = bankPlus.getPdf(b.id.toString(), comPrazo = true) ?: ByteArray(0)
                        if (!String(pdf.copyOfRange(0, minOf(pdf.size, 4))).startsWith("%PDF"))
                            throw Exception("BankPlus não devolveu um pdf")
                        // Dois boletos ativos da mesma parcela (ou NF de mesmo numero em outra filial): o id desempata.
                        val base = "boleto-nota-${nota.SequenceSerial ?: nota.docNum}-parcela${b.numeroDaParcela}"
                        val nomeArquivo = if (usados.add("$base.pdf")) "$base.pdf" else "$base-${b.id}.pdf".also { usados.add(it) }
                        zip.putNextEntry(ZipEntry(nomeArquivo))
                        zip.write(pdf)
                        zip.closeEntry()
                    } catch (t: Throwable) {
                        erros += "$nome parcela ${b.numeroDaParcela}: boleto não baixado (${motivoFalha(t)})"
                    }
                }
            }
            if (erros.isNotEmpty()) {
                zip.putNextEntry(ZipEntry("erros.txt"))
                zip.write(erros.joinToString("\n").toByteArray())
                zip.closeEntry()
            }
        }
        return saida.toByteArray()
    }

    companion object {
        private val json = ObjectMapper()

        /**
         * Motivo que a BankPlus mandou, para a tela (interna; o zip nao usa). Ela recusa regra de
         * negocio ate com 500 - ex.: NF-e sem chave de acesso: {"error": "Não foi possível identificar
         * a chave de acesso..."}. Corpo que nao e esse JSON so aparece em 4xx (5xx pode ser stack).
         */
        fun recusaBankPlus(t: Throwable): String? {
            val http = t as? HttpStatusCodeException ?: return null
            val corpo = http.responseBodyAsString
            val texto = runCatching { json.readTree(corpo) }.getOrNull()
                ?.let { no -> listOf("error", "mensagem", "message").firstNotNullOfOrNull { no.path(it).textValue() } }
            return (texto ?: corpo.takeIf { http.statusCode.is4xxClientError })?.trim()?.take(200)?.takeIf { it.isNotEmpty() }
        }

        // So NF valendo: entrega de venda futura nao tem boleto e o documento de cancelamento
        // (CancelStatus csCancellationDocument) pode herdar a UDF da ordem - ver CancelStatus.
        fun notasComBoleto(docs: List<Document>) =
            docs.filter { it.docObjectCode == DocumentTypes.oInvoices && it.CancelStatus?.cancelado() != true }

        // "Cancelado" e "Aguardando Cancelamento" nao contam: a parcela fica sem boleto valido.
        fun ativos(boletos: List<Boleto>) = boletos.filter { !(it.statusDescricao ?: "").contains("cancel", ignoreCase = true) }
    }
}

data class ResultadoBoleto(val nota: String, val situacao: SituacaoBoleto, val mensagem: String)

enum class SituacaoBoleto { GERADO, JA_TINHA, JA_PAGA, NAO_E_BOLETO, ERRO }
