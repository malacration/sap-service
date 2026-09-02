package br.andrew.sap.offline

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.documents.Quotation
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sistema.Autorizacao
import br.andrew.sap.model.sistema.StatusAutorizacao
import br.andrew.sap.model.sistema.TipoDocumentoAutorizacao
import br.andrew.sap.services.autorizacao.AutorizacaoService
import br.andrew.sap.services.autorizacao.RegraAutorizacaoService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.DocumentForAngular
import br.andrew.sap.services.documents.QuotationsService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineQuotationService(
    private val redis: StringRedisTemplate,
    private val mapper: ObjectMapper,
    private val quotationsService: QuotationsService,
    private val itemsService: ItemsService,
    private val businessPartnersService: BusinessPartnersService,
    private val regionService: RegiaoService,
    private val localityService: LocalidadeService,
    private val authorizationRuleService: RegraAutorizacaoService,
    private val authorizationService: AutorizacaoService
) {

    fun sync(request: OfflineQuotationSyncRequest, user: User): OfflineQuotationSyncResponse {
        validate(request)
        cached(request.transmissionId, user.id)?.let { return it }
        val lockValue = UUID.randomUUID().toString()
        val lockKey = lockKey(request.transmissionId, user.id)
        val acquired = redis.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofMinutes(10)) == true
        if (!acquired) {
            return OfflineQuotationSyncResponse(
                request.transmissionId,
                request.localId,
                OfflineQuotationStatus.IN_PROGRESS
            )
        }

        try {
            reconcile(request.transmissionId, user, request.localId)?.let {
                cache(it, user.id)
                return it
            }

            request.quotation.u_offline_id = request.transmissionId
            request.quotation.u_offline_user = user.id
            val document = DocumentForAngular().prepareToSave(
                request.quotation,
                itemsService,
                businessPartnersService,
                regionService,
                localityService,
                user
            )
            document.u_offline_id = request.transmissionId
            document.u_offline_user = user.id
            val reason = authorizationRuleService.avaliar(document)
            val response = if (reason != null) {
                val authorization = authorizationService.criar(
                    TipoDocumentoAutorizacao.COTACAO,
                    reason,
                    document,
                    user.userName,
                    request.transmissionId
                )
                OfflineQuotationSyncResponse(
                    request.transmissionId,
                    request.localId,
                    OfflineQuotationStatus.PENDING_AUTHORIZATION,
                    authorizationId = authorization.Code,
                    reason = reason
                )
            } else {
                val saved = quotationsService.save(document).tryGetValue<Document>()
                OfflineQuotationSyncResponse(
                    request.transmissionId,
                    request.localId,
                    OfflineQuotationStatus.CREATED,
                    docEntry = saved.docEntry,
                    docNum = saved.docNum
                )
            }
            cache(response, user.id)
            return response
        } finally {
            releaseLock(lockKey, lockValue)
        }
    }

    fun reconcile(transmissionId: String, user: User, localId: String? = null): OfflineQuotationSyncResponse? {
        cached(transmissionId, user.id)?.let { cached ->
            if (cached.status != OfflineQuotationStatus.PENDING_AUTHORIZATION) return cached
        }

        val quotation = quotationsService
            .get(Filter(
                Predicate("U_offline_id", transmissionId, Condicao.EQUAL),
                Predicate("U_offline_user", user.id, Condicao.EQUAL)
            ))
            .tryGetValues<Quotation>()
            .firstOrNull()
        if (quotation != null) {
            return OfflineQuotationSyncResponse(
                transmissionId,
                localId,
                OfflineQuotationStatus.CREATED,
                quotation.docEntry,
                quotation.docNum
            ).also { cache(it, user.id) }
        }

        val authorization = authorizationService
            .get(Filter("U_offline_id", transmissionId, Condicao.EQUAL))
            .tryGetValues<Autorizacao>()
            .firstOrNull { it.U_solicitante == user.userName } ?: return null
        val status = when (authorization.U_status) {
            StatusAutorizacao.APROVADO -> OfflineQuotationStatus.APPROVED
            StatusAutorizacao.REJEITADO -> OfflineQuotationStatus.REJECTED
            else -> OfflineQuotationStatus.PENDING_AUTHORIZATION
        }
        val approved = authorization.U_docEntryCriado?.let {
            quotationsService.getById(it).tryGetValue<Quotation>()
        }
        return OfflineQuotationSyncResponse(
            transmissionId,
            localId,
            status,
            docEntry = approved?.docEntry ?: authorization.U_docEntryCriado,
            docNum = approved?.docNum,
            authorizationId = authorization.Code,
            reason = authorization.U_observacao ?: authorization.U_motivo
        ).also { cache(it, user.id) }
    }

    private fun validate(request: OfflineQuotationSyncRequest) {
        require(request.transmissionId.matches(Regex("^[A-Za-z0-9_-]{8,64}$"))) {
            "Identificador de transmissao invalido"
        }
        require(request.localId.isNotBlank()) { "Identificador local obrigatorio" }
    }

    private fun cache(response: OfflineQuotationSyncResponse, userId: String) {
        redis.opsForValue().set(
            resultKey(response.transmissionId, userId),
            mapper.writeValueAsString(response),
            Duration.ofDays(90)
        )
    }

    private fun cached(transmissionId: String, userId: String): OfflineQuotationSyncResponse? {
        val json = redis.opsForValue().get(resultKey(transmissionId, userId)) ?: return null
        return mapper.readValue(json, OfflineQuotationSyncResponse::class.java)
    }

    private fun releaseLock(key: String, value: String) {
        val script = DefaultRedisScript<Long>().also {
            it.setScriptText(
                "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end"
            )
            it.resultType = Long::class.java
        }
        redis.execute(script, listOf(key), value)
    }

    private fun lockKey(id: String, userId: String) = "offline:quotation:lock:$userId:$id"
    private fun resultKey(id: String, userId: String) = "offline:quotation:result:$userId:$id"
}
