package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import java.time.Instant

enum class OfflineCatalogJobState {
    QUEUED,
    GENERATING,
    READY,
    FAILED
}

data class OfflineUserContext(
    val id: String,
    val name: String,
    val origin: String,
    val userName: String,
    val emailAddress: String?,
    val businessPlaces: List<Int>,
    val roles: List<String>
) {
    fun toUser() = User(
        id = id,
        _name = name,
        origin = UserOriginEnum.valueOf(origin),
        userName = userName,
        emailAddress = emailAddress,
        bussinesPlace = businessPlaces,
        roles = roles
    )

    companion object {
        fun from(user: User) = OfflineUserContext(
            id = user.id,
            name = user.name ?: user.userName,
            origin = user.origin.name,
            userName = user.userName,
            emailAddress = user.emailAddress,
            businessPlaces = user.bussinesPlace,
            roles = user.roles
        )
    }
}

data class OfflineCatalogJob(
    val id: String,
    val userKey: String,
    val user: OfflineUserContext,
    val state: OfflineCatalogJobState = OfflineCatalogJobState.QUEUED,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = createdAt,
    val progress: Int = 0,
    val queuePosition: Int? = null,
    val currentDataset: String? = null,
    val snapshotId: String? = null,
    val error: String? = null
)

data class OfflineCatalogPart(
    val id: String,
    val dataset: String,
    val index: Int,
    val size: Int,
    val sha256: String
)

data class OfflineCatalogManifest(
    val snapshotId: String,
    val userKey: String,
    val schemaVersion: Int,
    val generatedAt: Instant,
    val expiresAt: Instant,
    val userId: String,
    val userName: String,
    val userOrigin: String,
    val parts: List<OfflineCatalogPart>
)

data class OfflineBranchProduct(
    val branchId: Int,
    val product: Any
)

data class OfflinePaymentMethods(
    val branchId: Int,
    val cardCode: String,
    val methods: Any
)

data class OfflinePaymentTerms(
    val priceList: Int,
    val terms: Any
)
