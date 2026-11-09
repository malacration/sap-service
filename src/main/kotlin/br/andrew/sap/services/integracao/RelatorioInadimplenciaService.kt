package br.andrew.sap.services.integracao

import br.andrew.sap.model.sap.cadastro.SalePerson
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.cadastro.SalesPersonsService
import br.andrew.sap.services.comercial.ParcelasAbertoService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

data class ResultadoEnvio(val enviados: Int, val erros: Int)

/** Monta e envia o e-mail de inadimplencia de cada vendedor marcado com U_envia_relatorio. */
@Service
class RelatorioInadimplenciaService(
    private val mailService: MailService,
    private val templateEngine: TemplateEngine,
    private val salesService: SalesPersonsService,
    private val telegramMsg: TelegramRequestService,
    private val parcelas: ParcelasAbertoService,
) {
    private val logger = LoggerFactory.getLogger(RelatorioInadimplenciaService::class.java)

    // Thread propria: o RestTemplate compartilhado nao tem timeout e o scheduler do app tem uma thread so,
    // entao um SAP travado no meio do lote pararia o pix e o keep-alive da sessao.
    private val executor = Executors.newSingleThreadExecutor { Thread(it, "relatorio-inadimplencia").also { t -> t.isDaemon = true } }
    private val emAndamento = AtomicBoolean(false)

    fun emAndamento() = emAndamento.get()

    /** Roda [tarefa] fora da thread chamadora; false se ja existe um envio em andamento. */
    fun emSegundoPlano(tarefa: () -> Unit): Boolean {
        if (!emAndamento.compareAndSet(false, true)) return false
        executor.execute {
            try {
                tarefa()
            } catch (e: Exception) {
                logger.error("Falha no envio do relatorio de inadimplencia", e)
            } finally {
                emAndamento.set(false)
            }
        }
        return true
    }

    /** @param slpCode so esse vendedor; null = todos os marcados. O vendedor precisa estar marcado para receber. */
    fun enviar(copia: List<String>, slpCode: Int? = null, destino: String? = null): ResultadoEnvio {
        // Teste (destino): qualquer vendedor serve, marcado ou nao; o e-mail vai so para o destino.
        val vendedores = if (destino != null) listOf(salesService.getById(slpCode!!).tryGetValue<SalePerson>())
        else salesService.getEnviaRelatorio().filter { slpCode == null || it.SalesEmployeeCode == slpCode }
        if (slpCode != null && vendedores.isEmpty())
            throw Exception("O vendedor $slpCode nao esta marcado para receber o relatorio de inadimplencia (U_envia_relatorio).")
        val enviados = AtomicInteger()
        val erros = AtomicInteger()
        val emCopia = copia.map { EmailAdrres(it.trim()) }
        vendedores.parallelStream().forEach { vendedor ->
            try {
                val titulos = parcelas.getAllBySql(vendedor.SalesEmployeeCode)
                val body = templateEngine.process("relatorio-inadiplencia", Context().also { it.setVariables(mapOf("titulos" to titulos)) })
                val data = SimpleDateFormat("dd/MM/yyy").format(Date())
                val titulo = "${if (destino != null) "[TESTE] " else ""}Relatório de Inadimplência do vendedor ${vendedor.SalesEmployeeName} - ${data}"
                mailService.sendEmail(MyMailMessage(listOf(EmailAdrres(destino ?: vendedor.getEmailAddress())), titulo, body, From.COMERCIAL, emCopia), true)
                telegramMsg.send("${titulo} enviado com sucesso", TipoMensagem.eventos)
                enviados.incrementAndGet()
            } catch (e: Exception) {
                val erro = "Erro ao enviar relatório de inadimplência para o vendedor ${vendedor.SalesEmployeeCode}"
                erros.incrementAndGet()
                telegramMsg.send(erro, TipoMensagem.eventos)
                logger.error(erro, e)
            }
        }
        return ResultadoEnvio(enviados.get(), erros.get())
    }
}
