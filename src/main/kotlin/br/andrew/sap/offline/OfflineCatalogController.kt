package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("offline/catalog")
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineCatalogController(private val store: OfflineCatalogStore) {

    @PostMapping("jobs")
    fun requestJob(
        authentication: Authentication,
        @RequestParam(defaultValue = "false") force: Boolean
    ): ResponseEntity<OfflineCatalogJob> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val job = store.request(user, force)
        val status = if (job.state == OfflineCatalogJobState.READY) HttpStatus.OK else HttpStatus.ACCEPTED
        return ResponseEntity.status(status).body(job)
    }

    @GetMapping("jobs/{jobId}")
    fun job(@PathVariable jobId: String, authentication: Authentication): ResponseEntity<OfflineCatalogJob> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val job = store.getJob(jobId) ?: return ResponseEntity.notFound().build()
        if (job.user.id != user.id || job.user.origin != user.origin.name) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        return ResponseEntity.ok(job)
    }

    @GetMapping("snapshots/{snapshotId}/manifest")
    fun manifest(
        @PathVariable snapshotId: String,
        authentication: Authentication
    ): ResponseEntity<OfflineCatalogManifest> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val manifest = authorizedManifest(snapshotId, user) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(manifest)
    }

    @GetMapping("snapshots/{snapshotId}/parts/{partId}")
    fun part(
        @PathVariable snapshotId: String,
        @PathVariable partId: String,
        authentication: Authentication
    ): ResponseEntity<ByteArray> {
        val user = authentication as? User ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        val manifest = authorizedManifest(snapshotId, user) ?: return ResponseEntity.notFound().build()
        if (manifest.parts.none { it.id == partId }) return ResponseEntity.notFound().build()
        val bytes = store.getPart(snapshotId, partId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$partId.bin\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(bytes)
    }

    private fun authorizedManifest(snapshotId: String, user: User): OfflineCatalogManifest? {
        val manifest = store.getManifest(snapshotId) ?: return null
        return manifest.takeIf {
            it.userId == user.id
                && it.userOrigin == user.origin.name
                && it.userKey == store.userKey(user)
        }
    }
}
