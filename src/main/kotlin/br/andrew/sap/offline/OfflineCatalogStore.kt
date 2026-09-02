package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineCatalogStore(
    private val redis: StringRedisTemplate,
    private val binaryRedis: RedisTemplate<String, ByteArray>,
    private val mapper: ObjectMapper,
    @Value("\${offline.catalog.ttl-hours:72}") private val ttlHours: Long,
    @Value("\${offline.catalog.max-concurrency:1}") private val maxConcurrency: Long,
    @Value("\${offline.catalog.lease-minutes:60}") private val leaseMinutes: Long
) {

    private val ttl: Duration get() = Duration.ofHours(ttlHours)

    fun request(user: User, force: Boolean = false): OfflineCatalogJob {
        val context = OfflineUserContext.from(user)
        val userKey = userKey(user)
        rememberActiveUser(userKey, context)

        if (!force) {
            readyJob(userKey)?.let { return it }
            val pendingId = redis.opsForValue().get(dedupeKey(userKey))
            if (pendingId != null) getJob(pendingId)?.let { return it }
        }

        val job = OfflineCatalogJob(UUID.randomUUID().toString(), userKey, context)
        val script = DefaultRedisScript<String>().also {
            it.setScriptText(
                "local existing = redis.call('GET', KEYS[1]); " +
                    "if existing then return existing end; " +
                    "redis.call('SET', KEYS[1], ARGV[1], 'PX', ARGV[3]); " +
                    "redis.call('SET', KEYS[2], ARGV[2], 'PX', ARGV[4]); " +
                    "redis.call('LPUSH', KEYS[3], ARGV[1]); return ARGV[1]"
            )
            it.resultType = String::class.java
        }
        val selectedId = redis.execute(
            script,
            listOf(dedupeKey(userKey), jobKey(job.id), QUEUE_KEY),
            job.id,
            mapper.writeValueAsString(job),
            ttl.toMillis().toString(),
            ttl.toMillis().toString()
        ) ?: error("Redis nao retornou o identificador do job offline")
        return getJob(selectedId) ?: error("Job offline deduplicado nao encontrado")
    }

    fun getJob(jobId: String): OfflineCatalogJob? {
        val json = redis.opsForValue().get(jobKey(jobId)) ?: return null
        val job = mapper.readValue(json, OfflineCatalogJob::class.java)
        if (job.state != OfflineCatalogJobState.QUEUED) return job.copy(queuePosition = null)
        val queued = redis.opsForList().range(QUEUE_KEY, 0, -1).orEmpty()
        val index = queued.indexOf(job.id)
        return job.copy(queuePosition = if (index < 0) null else queued.size - index)
    }

    fun saveJob(job: OfflineCatalogJob) {
        redis.opsForValue().set(jobKey(job.id), mapper.writeValueAsString(job.copy(queuePosition = null)), ttl)
    }

    fun pollJob(): OfflineCatalogJob? {
        val id = redis.opsForList().rightPop(QUEUE_KEY) ?: return null
        return getJob(id)
    }

    fun requeue(job: OfflineCatalogJob) {
        saveJob(job.copy(state = OfflineCatalogJobState.QUEUED, updatedAt = Instant.now()))
        redis.opsForList().leftPush(QUEUE_KEY, job.id)
    }

    fun acquireSlot(jobId: String): String? {
        val now = Instant.now().toEpochMilli()
        val expiresAt = now + Duration.ofMinutes(leaseMinutes).toMillis()
        val leaseId = "$jobId::${UUID.randomUUID()}"
        val script = DefaultRedisScript<Long>().also {
            it.setScriptText(
                "if redis.call('ZCARD', KEYS[1]) < tonumber(ARGV[2]) then " +
                    "redis.call('ZADD', KEYS[1], ARGV[3], ARGV[4]); return 1 else return 0 end"
            )
            it.resultType = Long::class.java
        }
        val acquired = redis.execute(
            script,
            listOf(SLOTS_KEY),
            now.toString(),
            maxConcurrency.toString(),
            expiresAt.toString(),
            leaseId
        ) == 1L
        return leaseId.takeIf { acquired }
    }

    fun renewSlot(leaseId: String): Boolean {
        val script = DefaultRedisScript<Long>().also {
            it.setScriptText(
                "if redis.call('ZSCORE', KEYS[1], ARGV[1]) then " +
                    "redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1]); return 1 else return 0 end"
            )
            it.resultType = Long::class.java
        }
        return redis.execute(
            script,
            listOf(SLOTS_KEY),
            leaseId,
            Instant.now().plus(Duration.ofMinutes(leaseMinutes)).toEpochMilli().toString()
        ) == 1L
    }

    fun releaseSlot(leaseId: String) {
        redis.opsForZSet().remove(SLOTS_KEY, leaseId)
    }

    fun recoverExpiredJobs() {
        val expired = redis.opsForZSet().rangeByScore(SLOTS_KEY, 0.0, Instant.now().toEpochMilli().toDouble()).orEmpty()
        expired.forEach { leaseId ->
            val removed = redis.opsForZSet().remove(SLOTS_KEY, leaseId) ?: 0
            if (removed == 0L) return@forEach
            val job = getJob(leaseId.substringBefore("::"))
            if (job?.state == OfflineCatalogJobState.GENERATING || job?.state == OfflineCatalogJobState.QUEUED)
                requeue(job)
        }
    }

    fun savePart(snapshotId: String, part: OfflineCatalogPart, bytes: ByteArray) {
        binaryRedis.opsForValue().set(partKey(snapshotId, part.id), bytes, ttl)
    }

    fun getPart(snapshotId: String, partId: String): ByteArray? {
        return binaryRedis.opsForValue().get(partKey(snapshotId, partId))
    }

    fun complete(job: OfflineCatalogJob, manifest: OfflineCatalogManifest): OfflineCatalogJob {
        manifest.parts.forEach { part -> binaryRedis.expire(partKey(manifest.snapshotId, part.id), ttl) }
        redis.opsForValue().set(manifestKey(manifest.snapshotId), mapper.writeValueAsString(manifest), ttl)
        redis.opsForValue().set(readyKey(job.userKey), manifest.snapshotId, ttl)
        redis.delete(dedupeKey(job.userKey))
        val completed = job.copy(
            state = OfflineCatalogJobState.READY,
            progress = 100,
            currentDataset = null,
            snapshotId = manifest.snapshotId,
            updatedAt = Instant.now(),
            error = null
        )
        saveJob(completed)
        return completed
    }

    fun fail(job: OfflineCatalogJob, error: String): OfflineCatalogJob {
        redis.delete(dedupeKey(job.userKey))
        return job.copy(
            state = OfflineCatalogJobState.FAILED,
            updatedAt = Instant.now(),
            error = error.take(1000)
        ).also(::saveJob)
    }

    fun getManifest(snapshotId: String): OfflineCatalogManifest? {
        val json = redis.opsForValue().get(manifestKey(snapshotId)) ?: return null
        return mapper.readValue(json, OfflineCatalogManifest::class.java)
    }

    fun activeUserKeys(): Set<String> = redis.opsForSet().members(ACTIVE_USERS_KEY).orEmpty()

    fun activeUser(userKey: String): OfflineUserContext? {
        val json = redis.opsForValue().get(activeUserKey(userKey)) ?: return null
        return mapper.readValue(json, OfflineUserContext::class.java)
    }

    fun currentManifest(userKey: String): OfflineCatalogManifest? {
        val snapshotId = redis.opsForValue().get(readyKey(userKey)) ?: return null
        return getManifest(snapshotId)
    }

    fun userKey(user: User): String = sha256(
        listOf(
            user.origin.name,
            user.id,
            user.userName,
            user.roles.sorted().joinToString(","),
            user.bussinesPlace.sorted().joinToString(",")
        ).joinToString(":")
    )

    private fun readyJob(userKey: String): OfflineCatalogJob? {
        val manifest = currentManifest(userKey) ?: return null
        if (!manifest.expiresAt.isAfter(Instant.now())) return null
        return OfflineCatalogJob(
            id = "ready-${manifest.snapshotId}",
            userKey = userKey,
            user = activeUser(userKey) ?: return null,
            state = OfflineCatalogJobState.READY,
            progress = 100,
            snapshotId = manifest.snapshotId,
            createdAt = manifest.generatedAt,
            updatedAt = manifest.generatedAt
        )
    }

    private fun rememberActiveUser(userKey: String, user: OfflineUserContext) {
        redis.opsForSet().add(ACTIVE_USERS_KEY, userKey)
        redis.opsForValue().set(activeUserKey(userKey), mapper.writeValueAsString(user), Duration.ofDays(30))
    }

    private fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(StandardCharsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun jobKey(id: String) = "offline:catalog:job:$id"
    private fun dedupeKey(userKey: String) = "offline:catalog:dedupe:$userKey"
    private fun readyKey(userKey: String) = "offline:catalog:ready:$userKey"
    private fun activeUserKey(userKey: String) = "offline:catalog:user:$userKey"
    private fun manifestKey(id: String) = "offline:catalog:manifest:$id"
    private fun partKey(snapshotId: String, partId: String) = "offline:catalog:part:$snapshotId:$partId"

    companion object {
        private const val QUEUE_KEY = "offline:catalog:queue"
        private const val SLOTS_KEY = "offline:catalog:slots"
        private const val ACTIVE_USERS_KEY = "offline:catalog:active-users"
    }
}
