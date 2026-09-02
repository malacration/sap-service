package br.andrew.sap.offline

import br.andrew.sap.model.sap.documents.Quotation
import java.time.Instant

data class OfflineQuotationSyncRequest(
    val transmissionId: String,
    val localId: String,
    val catalogId: String?,
    val createdOfflineAt: Instant?,
    val quotation: Quotation
)

enum class OfflineQuotationStatus {
    CREATED,
    PENDING_AUTHORIZATION,
    APPROVED,
    REJECTED,
    IN_PROGRESS,
    NOT_FOUND
}

data class OfflineQuotationSyncResponse(
    val transmissionId: String,
    val localId: String?,
    val status: OfflineQuotationStatus,
    val docEntry: Int? = null,
    val docNum: String? = null,
    val authorizationId: Int? = null,
    val reason: String? = null
)
