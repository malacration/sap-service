package br.andrew.sap.services.logistica

import br.andrew.sap.infrastructure.BoletoIdsConfig
import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.fiscal.BankPlusService
import br.andrew.sap.services.fiscal.OrigemBoletoEnum
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Boletos BankPlus das NFs geradas pela ordem de carregamento, para o usuario nao precisar entrar
 * no SAP. Gera por parcela so o que ainda nao tem boleto ativo: clicar de novo nao duplica.
 * "E boleto" = forma de pagamento da nota em boleto.ids (a mesma lista da tela de faturas).
 */
@Service
class CarregamentoBoletoService(val bankPlus: BankPlusService, val boletoIds: BoletoIdsConfig) {

    private val logger = LoggerFactory.getLogger(CarregamentoBoletoService::class.java)

    // Duplo clique ou duas abas: as duas leriam "sem boleto" e gerariam em dobro.
    // shortcut: trava so nesta instancia, como a geracao de notas do CarregamentoController.
    private val gerando = ConcurrentHashMap.newKeySet<Int>()

    fun gerar(idOrdem: Int, notas: List<Document>): List<ResultadoBoleto> {
        if (!gerando.add(idOrdem))
            throw Exception("Os boletos desta ordem já estão sendo gerados; aguarde terminar.")
        try {
            return notas.map { gerar(it) }
        } finally {
            gerando.remove(idOrdem)
        }
    }

    private fun gerar(nota: Document): ResultadoBoleto {
        val nome = "NF ${nota.SequenceSerial ?: nota.docNum}"
        if (nota.paymentMethod !in boletoIds.ids)
            return ResultadoBoleto(nome, SituacaoBoleto.NAO_E_BOLETO,
                "forma de pagamento ${nota.paymentMethod ?: "vazia"} não é boleto")
        val parcelas = nota.documentInstallments?.mapNotNull { it.InstallmentId }.orEmpty()
        if (parcelas.isEmpty())
            return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "nota sem parcela no SAP")
        val docEntry = nota.docEntry ?: return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "nota sem DocEntry")
        val filial = nota.getBPL_IDAssignedToInvoice()

        val comBoleto = try {
            ativos(bankPlus.getBoletosBy(filial, docEntry.toString(), OrigemBoletoEnum.notafiscal, comPrazo = true))
                .mapNotNull { it.numeroDaParcela }.toSet()
        } catch (t: Throwable) {
            logger.warn("Carregamento: falha ao consultar boletos da NF $docEntry na BankPlus", t)
            return ResultadoBoleto(nome, SituacaoBoleto.ERRO, "consulta na BankPlus falhou: ${t.message}")
        }
        val faltam = parcelas.filter { it !in comBoleto }
        if (faltam.isEmpty())
            return ResultadoBoleto(nome, SituacaoBoleto.JA_TINHA, "já tinha boleto em todas as parcelas")

        val erros = faltam.mapNotNull { parcela ->
            try {
                bankPlus.geraBoletos(filial.toInt(), docEntry, parcela, OrigemBoletoEnum.notafiscal)
                null
            } catch (t: Throwable) {
                logger.warn("Carregamento: falha ao gerar boleto da NF $docEntry parcela $parcela", t)
                "parcela $parcela: ${t.message}"
            }
        }
        return if (erros.isEmpty())
            ResultadoBoleto(nome, SituacaoBoleto.GERADO, "parcela(s) ${faltam.joinToString()} gerada(s)")
        else
            ResultadoBoleto(nome, SituacaoBoleto.ERRO, erros.joinToString("; "))
    }

    /**
     * Zip com o pdf de cada boleto ativo das notas, no molde do CarregamentoService.danfes: o que
     * falhar vai para erros.txt sem derrubar o resto.
     * shortcut: monta em memoria; uma ordem tem poucas notas.
     */
    fun zip(notas: List<Document>): ByteArray {
        val saida = ByteArrayOutputStream()
        val erros = mutableListOf<String>()
        ZipOutputStream(saida).use { zip ->
            notas.forEach { nota ->
                val nome = "NF ${nota.SequenceSerial ?: nota.docNum}"
                val boletos = try {
                    ativos(bankPlus.getBoletosBy(nota.getBPL_IDAssignedToInvoice(), nota.docEntry.toString(),
                        OrigemBoletoEnum.notafiscal, comPrazo = true))
                } catch (t: Throwable) {
                    logger.warn("Carregamento: falha ao consultar boletos da NF ${nota.docEntry} na BankPlus", t)
                    erros += "$nome: consulta na BankPlus falhou (${t.message})"
                    return@forEach
                }
                if (boletos.isEmpty()) erros += "$nome: sem boleto na BankPlus"
                boletos.sortedBy { it.numeroDaParcela ?: 0 }.forEach { b ->
                    try {
                        val pdf = bankPlus.getPdf(b.id.toString(), comPrazo = true) ?: ByteArray(0)
                        if (!String(pdf.copyOfRange(0, minOf(pdf.size, 4))).startsWith("%PDF"))
                            throw Exception("BankPlus não devolveu um pdf")
                        zip.putNextEntry(ZipEntry("boleto-nota-${nota.SequenceSerial ?: nota.docNum}-parcela${b.numeroDaParcela}.pdf"))
                        zip.write(pdf)
                        zip.closeEntry()
                    } catch (t: Throwable) {
                        erros += "$nome parcela ${b.numeroDaParcela}: boleto não baixado (${t.message})"
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
        // "Cancelado" e "Aguardando Cancelamento" nao contam: a parcela fica sem boleto valido.
        fun ativos(boletos: List<Boleto>) = boletos.filter { !(it.statusDescricao ?: "").contains("cancel", ignoreCase = true) }
    }
}

data class ResultadoBoleto(val nota: String, val situacao: SituacaoBoleto, val mensagem: String)

enum class SituacaoBoleto { GERADO, JA_TINHA, NAO_E_BOLETO, ERRO }
