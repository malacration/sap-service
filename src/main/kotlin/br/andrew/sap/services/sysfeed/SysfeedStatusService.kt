package br.andrew.sap.services.sysfeed

import br.andrew.sap.model.sysfeed.SysfeedStatusTarget
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdate
import br.andrew.sap.model.sysfeed.SysfeedStatusUpdateResult
import br.andrew.sap.services.BusinessPartnersService
import br.andrew.sap.services.document.PurchaseInvoiceService
import org.springframework.stereotype.Service

@Service
class SysfeedStatusService(
    private val businessPartnersService: BusinessPartnersService,
    private val purchaseInvoiceService: PurchaseInvoiceService
) {
    private val allowedStatuses = setOf("PENDENTE", "ENVIADO", "DUPLICADO", "ERRO", "PARCIAL", "IGNORADO")

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

        val payload = mapOf("U_sysfeed_status" to status)
        when (update.tipo) {
            SysfeedStatusTarget.FORNECEDOR -> businessPartnersService.update(payload, "'$codigo'")
            SysfeedStatusTarget.ORDEM_RECEBIMENTO -> {
                codigo.toIntOrNull() ?: throw SysfeedStatusException("DocEntry invalido: $codigo")
                purchaseInvoiceService.update(payload, codigo)
            }
        }
    }
}

class SysfeedStatusException(message: String) : RuntimeException(message)
