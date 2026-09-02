package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.sap.cadastro.Branch
import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerSlin
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.comercial.FormaPagamentoService
import br.andrew.sap.services.comercial.PrazoPagamentoService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.zip.GZIPOutputStream

@Service
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineCatalogGenerator(
    private val store: OfflineCatalogStore,
    private val mapper: ObjectMapper,
    private val businessPlaceService: BussinessPlaceService,
    private val businessPartnersService: BusinessPartnersService,
    private val itemsService: ItemsService,
    private val paymentMethodService: FormaPagamentoService,
    private val paymentTermsService: PrazoPagamentoService,
    private val commissionService: ComissaoService,
    private val regionService: RegiaoService,
    private val localityService: LocalidadeService,
    @Value("\${offline.catalog.ttl-hours:72}") private val ttlHours: Long,
    @Value("\${offline.catalog.part-size-bytes:4194304}") private val partSizeBytes: Int,
    @Value("\${offline.catalog.prewarm-hours:12}") private val prewarmHours: Long
) {

    private class LeaseLostException : RuntimeException()

    @Scheduled(fixedDelayString = "\${offline.catalog.worker-delay-ms:1000}")
    fun consume() {
        store.recoverExpiredJobs()
        val job = store.pollJob() ?: return
        val leaseId = store.acquireSlot(job.id)
        if (leaseId == null) {
            store.requeue(job)
            return
        }

        try {
            logger.info("Starting offline catalog job={} userKey={}", job.id, job.userKey)
            generate(job, leaseId)
        } catch (_: LeaseLostException) {
            logger.warn("Offline catalog lease lost job={}", job.id)
        } catch (error: Exception) {
            store.fail(job, error.message ?: error.javaClass.simpleName)
            logger.error("Offline catalog failed job={}", job.id, error)
        } finally {
            store.releaseSlot(leaseId)
        }
    }

    @Scheduled(fixedDelayString = "\${offline.catalog.recovery-delay-ms:60000}")
    fun recoverAbandonedJobs() {
        store.recoverExpiredJobs()
    }

    @Scheduled(cron = "\${offline.catalog.prewarm-cron:0 0 * * * *}")
    fun prewarmActiveUsers() {
        val threshold = Instant.now().plus(Duration.ofHours(prewarmHours))
        store.activeUserKeys().forEach { userKey ->
            val manifest = store.currentManifest(userKey)
            if (manifest == null || manifest.expiresAt.isBefore(threshold)) {
                store.activeUser(userKey)?.let { context -> store.request(context.toUser(), force = true) }
            }
        }
    }

    private fun generate(original: OfflineCatalogJob, leaseId: String) {
        var job = original.copy(
            state = OfflineCatalogJobState.GENERATING,
            progress = 1,
            updatedAt = Instant.now(),
            error = null
        )
        store.saveJob(job)
        val user = job.user.toUser()
        val snapshotId = UUID.randomUUID().toString()
        val parts = mutableListOf<OfflineCatalogPart>()

        val branches = authorizedBranches(user)
        job = saveDataset(job, leaseId, snapshotId, "branches", 8, branches, parts)

        val customers = authorizedCustomers(user) { current, total ->
            job = progress(job, leaseId, "businessPartners", 8 + ((current.toDouble() / total.coerceAtLeast(1)) * 22).toInt())
        }
        job = saveDataset(job, leaseId, snapshotId, "businessPartners", 30, customers, parts)

        val products = authorizedProducts(user, branches) { current, total ->
            job = progress(job, leaseId, "products", 30 + ((current.toDouble() / total.coerceAtLeast(1)) * 20).toInt())
        }
        job = saveDataset(job, leaseId, snapshotId, "products", 50, products, parts)

        val paymentMethods = paymentMethods(branches, customers) { current, total ->
            job = progress(job, leaseId, "paymentMethods", 50 + ((current.toDouble() / total.coerceAtLeast(1)) * 25).toInt())
        }
        job = saveDataset(job, leaseId, snapshotId, "paymentMethods", 75, paymentMethods, parts)

        val priceLists = products.mapNotNull { (it.product as Product).PriceList }.distinct()
        val paymentTerms = priceLists.map { OfflinePaymentTerms(it, paymentTermsService.getByTabela(it).orEmpty()) }
        job = saveDataset(job, leaseId, snapshotId, "paymentTerms", 82, paymentTerms, parts)
        val commissionsByPriceList = priceLists.map { priceList ->
            mapOf("priceList" to priceList, "commission" to runCatching {
                commissionService.getByIdTabela(priceList)
            }.getOrNull())
        }
        job = saveDataset(job, leaseId, snapshotId, "commissionByPriceList", 85, commissionsByPriceList, parts)
        job = saveDataset(job, leaseId, snapshotId, "commissions", 87, commissionService.getTodas(), parts)
        job = saveDataset(job, leaseId, snapshotId, "freightRegions", 92, regionService.getTodas(null), parts)
        job = saveDataset(job, leaseId, snapshotId, "localities", 96, localityService.getAll(Localidade::class.java), parts)
        job = saveDataset(
            job,
            leaseId,
            snapshotId,
            "profile",
            99,
            mapOf(
                "id" to user.id,
                "name" to user.name,
                "userName" to user.userName,
                "roles" to user.roles,
                "businessPlaces" to branches.map { it.BPLId }
            ),
            parts
        )

        val generatedAt = Instant.now()
        store.complete(
            job,
            OfflineCatalogManifest(
                snapshotId = snapshotId,
                userKey = job.userKey,
                schemaVersion = 1,
                generatedAt = generatedAt,
                expiresAt = generatedAt.plus(Duration.ofHours(ttlHours)),
                userId = user.id,
                userName = user.name ?: user.userName,
                userOrigin = user.origin.name,
                parts = parts
            )
        )
        logger.info("Offline catalog ready job={} snapshot={} parts={}", job.id, snapshotId, parts.size)
    }

    private fun authorizedBranches(user: User): List<Branch> {
        if (user.superVendedor() > -1) return businessPlaceService.getAll(Branch::class.java)
        return when (user.origin) {
            UserOriginEnum.SalePerson -> businessPlaceService.getFilialBySalesPerson(user.getIdInt())
            UserOriginEnum.EmployeesInfo -> businessPlaceService.getFilialByEmployee(user.getIdInt())
            else -> emptyList()
        }
    }

    private fun authorizedCustomers(user: User, onProgress: (Int, Int) -> Unit): List<BusinessPartner> {
        val summaries = mutableListOf<BusinessPartnerSlin>()
        var page = businessPartnersService.fullSearchTextFallBack("*", user)
        summaries.addAll(page.content)
        while (page.hasNext()) {
            page = businessPartnersService.fullSearchTextFallBack(page.nextLink, user)
            summaries.addAll(page.content)
        }

        return summaries.distinctBy { it.CardCode }.mapIndexed { index, summary ->
            if (index % 25 == 0) {
                onProgress(index, summaries.size)
            }
            runCatching {
                businessPartnersService.getById("'${summary.CardCode}'").tryGetValue<BusinessPartner>()
            }.getOrNull()
        }.filterNotNull().also { onProgress(it.size, it.size) }
    }

    private fun authorizedProducts(
        user: User,
        branches: List<Branch>,
        onProgress: (Int, Int) -> Unit
    ): List<OfflineBranchProduct> {
        val result = mutableListOf<OfflineBranchProduct>()
        branches.forEachIndexed { index, branch ->
            var page = itemsService.fullSearchText("*", user.getIdInt(), branch.BPLId, user.superVendedor())
            result.addAll(page.content.map { OfflineBranchProduct(branch.BPLId, it) })
            while (page.hasNext()) {
                page = itemsService.fullSearchText(page.nextLink, user.getIdInt(), branch.BPLId, user.superVendedor())
                result.addAll(page.content.map { OfflineBranchProduct(branch.BPLId, it) })
                onProgress(index, branches.size)
            }
            onProgress(index + 1, branches.size)
        }
        return result
    }

    private fun paymentMethods(
        branches: List<Branch>,
        customers: List<BusinessPartner>,
        onProgress: (Int, Int) -> Unit
    ): List<OfflinePaymentMethods> {
        val total = branches.size * customers.size
        var current = 0
        val result = mutableListOf<OfflinePaymentMethods>()
        branches.forEach { branch ->
            customers.forEach { customer ->
                val cardCode = customer.cardCode ?: return@forEach
                val methods = paymentMethodService.getByFilial(branch.BPLId, cardCode).orEmpty()
                result.add(OfflinePaymentMethods(branch.BPLId, cardCode, methods))
                current++
                if (current % 25 == 0) {
                    onProgress(current, total)
                }
            }
        }
        onProgress(current, total)
        return result
    }

    private fun saveDataset(
        job: OfflineCatalogJob,
        leaseId: String,
        snapshotId: String,
        dataset: String,
        progress: Int,
        content: Any,
        parts: MutableList<OfflineCatalogPart>
    ): OfflineCatalogJob {
        val compressed = gzip(mapper.writeValueAsBytes(content))
        compressed.asList().chunked(partSizeBytes).forEachIndexed { index, chunk ->
            val bytes = chunk.toByteArray()
            val id = "$dataset-$index"
            val part = OfflineCatalogPart(id, dataset, index, bytes.size, sha256(bytes))
            store.savePart(snapshotId, part, bytes)
            parts.add(part)
        }
        return progress(job, leaseId, dataset, progress)
    }

    private fun progress(job: OfflineCatalogJob, leaseId: String, dataset: String, value: Int): OfflineCatalogJob {
        if (!store.renewSlot(leaseId)) throw LeaseLostException()
        return job.copy(
            state = OfflineCatalogJobState.GENERATING,
            currentDataset = dataset,
            progress = value.coerceIn(1, 99),
            updatedAt = Instant.now()
        ).also(store::saveJob)
    }

    private fun gzip(bytes: ByteArray): ByteArray {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { it.write(bytes) }
        return output.toByteArray()
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    companion object {
        private val logger = LoggerFactory.getLogger(OfflineCatalogGenerator::class.java)
    }
}
