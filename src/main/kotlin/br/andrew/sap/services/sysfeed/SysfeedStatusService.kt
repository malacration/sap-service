package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedStatusTarget
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdate
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdateResult
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.ProductionOrdersService
import br.andrew.sap.services.document.PurchaseInvoiceService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SysfeedStatusService(
    private val businessPartnersService: BusinessPartnersService,
    private val purchaseInvoiceService: PurchaseInvoiceService,
    private val productionOrdersService: ProductionOrdersService
) {
    private val allowedStatuses = setOf("PENDENTE", "ENVIADO", "DUPLICADO", "ERRO", "PARCIAL")
    // Status que representam envio concluido com sucesso ao SYSFEED.
    private val successStatuses = setOf("ENVIADO", "DUPLICADO")
    private val dtIntegracaoFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    fun updateAll(updates: List<SysfeedStatusUpdate>): List<SysfeedStatusUpdateResult> {
        return updates.map { update ->
            try {
                update(update)
                SysfeedStatusUpdateResult(
                    tipo = update.tipo,
                    codigo = update.codigo,
                    status = update.status.trim().uppercase(),
                    success = true
                )
            } catch (e: Exception) {
                SysfeedStatusUpdateResult(
                    tipo = update.tipo,
                    codigo = update.codigo,
                    status = update.status,
                    success = false,
                    error = e.message
                )
            }
        }
    }

    private fun update(update: SysfeedStatusUpdate) {
        val codigo = update.codigo.trim()
        val status = update.status.trim().uppercase()
        if (codigo.isBlank()) {
            throw SysfeedStatusException("codigo obrigatorio")
        }
        if (status !in allowedStatuses) {
            throw SysfeedStatusException("Status Sysfeed invalido: ${update.status}")
        }

        val agora = LocalDateTime.now()
        // DtIntegracao/HrIntegracao marcam envio concluido com sucesso e viram a trava anti-reenvio
        // (a view de pendentes exige DtIntegracao NULL). So gravamos em status de sucesso para nao
        // bloquear o reprocessamento de ERRO/PARCIAL apos o status voltar para PENDENTE.
        val marcarIntegracao = status in successStatuses
        val payload = mutableMapOf<String, Any>("U_sysfeed_status" to status)
        when (update.tipo) {
            SysfeedStatusTarget.FORNECEDOR -> businessPartnersService.update(payload, "'$codigo'")
            SysfeedStatusTarget.ORDEM_RECEBIMENTO -> {
                codigo.toIntOrNull() ?: throw SysfeedStatusException("DocEntry invalido: $codigo")
                if (marcarIntegracao) {
                    payload["U_LbrOne_DtIntegracao"] = agora.format(dtIntegracaoFormatter)
                    payload["U_LbrOne_HrIntegracao"] = agora.hour * 100 + agora.minute
                }
                purchaseInvoiceService.update(payload, codigo)
            }
            SysfeedStatusTarget.ORDEM_PRODUCAO -> {
                codigo.toIntOrNull() ?: throw SysfeedStatusException("DocEntry invalido: $codigo")
                if (marcarIntegracao) {
                    payload["U_LbrOne_DtIntegracao"] = agora.format(dtIntegracaoFormatter)
                    payload["U_LbrOne_HrIntegracao"] = agora.hour * 100 + agora.minute
                }
                update.obs?.trim()?.takeIf { it.isNotBlank() }?.let {
                    payload["U_LbrOne_Obs_Integracao"] = it
                }
                productionOrdersService.update(payload, codigo)
            }
        }
    }
}

class SysfeedStatusException(message: String) : RuntimeException(message)
