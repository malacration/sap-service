package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("offline/quotations")
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineQuotationController(private val service: OfflineQuotationService) {

    @PostMapping("sync")
    fun sync(
        @RequestBody request: OfflineQuotationSyncRequest,
        authentication: Authentication
    ): ResponseEntity<OfflineQuotationSyncResponse> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val response = service.sync(request, user)
        val status = if (response.status == OfflineQuotationStatus.IN_PROGRESS) HttpStatus.ACCEPTED else HttpStatus.OK
        return ResponseEntity.status(status).body(response)
    }

    @GetMapping("{transmissionId}")
    fun status(
        @PathVariable transmissionId: String,
        authentication: Authentication
    ): ResponseEntity<OfflineQuotationSyncResponse> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val response = service.reconcile(transmissionId, user)
            ?: OfflineQuotationSyncResponse(transmissionId, null, OfflineQuotationStatus.NOT_FOUND)
        return ResponseEntity.ok(response)
    }
}
