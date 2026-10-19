package br.andrew.sap.schedules.futura

import br.andrew.sap.controllers.comercial.ContratoVendaFuturaController
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.model.sap.comercial.DocEntry
import br.andrew.sap.model.dto.ContratoParaFinalizar
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.model.self.vendafutura.Status
import br.andrew.sap.services.comercial.ContratoVendaFuturaService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.util.concurrent.TimeUnit


@Component
@Profile("!test")
@ConditionalOnProperty(value = [
    "venda-futura.adiantamento-item",
    "venda-futura.conta-controle"], matchIfMissing = false)
class ConcluiContratosSchedule(
    val sqlQueriesService : SqlQueriesService,
    val contratoService : ContratoVendaFuturaService,
    val controller : ContratoVendaFuturaController,
    val odbc : OdbcClient,
    @Value("\${venda-futura.adiantamento-item}") val itemConciliacaoVendaFutura : String,
    @Value("\${venda-futura.filiais:-2}") val filiais : List<Int>,
    @Value("\${venda-futura.sequencia_adiantamento}") val sequenceCode : Int,
    @Value("\${venda-futura.utilizacao.baixa:79}") val usage : Int,
    @Value("\${venda-futura.conta-controle}") val contaControle : String) {

    val logger: Logger = LoggerFactory.getLogger(ConcluiContratosSchedule::class.java)


    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    fun execute() {
        reabrirContratosComEntregaDesfeita()
        atualizarStatusContratosFinanceiramenteConcluidos()
        atualizarStatusContratosEntregues()
    }

    /**
     * Volta para aberto o contrato entregue/concluido cuja devolucao ou cancelamento de nota
     * desfez parte das entregas (ver Contrato.statusRegredido). Roda antes das outras etapas: o
     * status so avancava, e um contrato concluido com mercadoria devolvida ficava concluido.
     */
    private fun reabrirContratosComEntregaDesfeita() {
        val desde = LocalDate.now().minusDays(JANELA_REABERTURA_DIAS).toString()
        odbc.consultar(SqlResource.carregar("odbc/contratos-para-reabrir.sql"), mapOf("desde" to desde), 1000)
            .rows.map { it["DocEntry"].toString().toBigDecimal().intValueExact() }
            .forEach { docEntry ->
                emCadaContrato(docEntry) {
                    val contrato : Contrato = contratoService.getById(docEntry).tryGetValue<Contrato>()
                    val novoStatus = contrato.statusRegredido(controller.entregas(docEntry)) ?: return@emCadaContrato
                    logger.warn("Contrato $docEntry volta de ${contrato.U_status} para $novoStatus: " +
                        "devolucao ou cancelamento desfez parte das entregas.")
                    atualizarStatus(docEntry, novoStatus)
                }
            }
    }

    private fun atualizarStatusContratosFinanceiramenteConcluidos() {
        var contratosIds : NextLink<ContratoParaFinalizar>? = null
        do {
            contratosIds = if(contratosIds == null)
                sqlQueriesService.execute("para-finalizar.sql")?.tryGetNextValues<ContratoParaFinalizar>()
            else
                sqlQueriesService.nextLink(contratosIds.nextLink)?.tryGetNextValues<ContratoParaFinalizar>()

            contratosIds?.content?.forEach {
                emCadaContrato(it.docEntry) {
                    val contrato : Contrato = contratoService.getById(it.docEntry!!).tryGetValue<Contrato>()
                    val entregas = controller.entregas(contrato.DocEntry!!)
                    val apropriado = BigDecimal.valueOf(it.apropriado ?: 0.0).setScale(2, RoundingMode.HALF_UP)
                    if(contrato.tudoEntregue(entregas) && contrato.total().compareTo(apropriado) == 0){
                        atualizarStatus(contrato.DocEntry!!, Status.concluido)
                    }
                }
            }
        }while (contratosIds?.hasNext() ?: false)

    }

    private fun atualizarStatusContratosEntregues() {
        var contratosIds : NextLink<DocEntry>? = null
        do {
            contratosIds = if(contratosIds == null)
                sqlQueriesService.execute("entrega-finalizada.sql")?.tryGetNextValues<DocEntry>()
            else
                sqlQueriesService.nextLink(contratosIds.nextLink)?.tryGetNextValues<DocEntry>()

            contratosIds?.content?.forEach {
                emCadaContrato(it.DocEntry) {
                    val contrato : Contrato = contratoService.getById(it.DocEntry!!).tryGetValue<Contrato>()
                    val entregas = controller.entregas(contrato.DocEntry!!)
                    if(contrato.tudoEntregue(entregas)){
                        atualizarStatus(contrato.DocEntry!!, Status.entregue)
                    }
                }
            }
        }while (contratosIds?.hasNext() ?: false)
    }

    /**
     * Grava SO o status. Antes ia o contrato inteiro, linhas inclusive: sem o header
     * B1S-ReplaceCollectionsOnPatch o Service Layer faz merge das linhas pelo LineId, e um PATCH
     * de status nao tem motivo para tocar nelas.
     */
    private fun atualizarStatus(docEntry: Int, status: Status) {
        contratoService.update(mapOf("U_status" to status), docEntry.toString())
    }

    /** Um contrato que falha nao impede os demais de serem processados nesta execucao. */
    private fun emCadaContrato(docEntry: Int?, bloco: () -> Unit) {
        try {
            bloco()
        } catch (e: Exception) {
            logger.error("Erro ao atualizar o status do contrato de venda futura [$docEntry]", e)
        }
    }

    companion object {
        /** Devolucao/cancelamento mais antigo que isso ja teve tempo de ser processado. */
        const val JANELA_REABERTURA_DIAS = 30L
    }
}

class Soma(var soma : BigDecimal?){

}
