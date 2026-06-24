package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.dto.InstallmentPixPersistido
import br.andrew.sap.model.dto.InvoicePixUpdatePayload
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService

/**
 * Atualiza as parcelas com os dados de PIX no SAP e confirma a persistencia.
 *
 * Compartilhado por faturas (InvoiceService) e adiantamentos (DownPaymentService).
 *
 * Confirmacao em camadas, para contornar o cache/replica do Service Layer (ler logo
 * apos escrever na mesma sessao retornava o registro antigo):
 *  1. tenta confirmar pela API nativa (GET);
 *  2. se o GET nao confirmar, consulta direto no banco via SQLQueries;
 *  3. so entao espera (crescente) e tenta de novo.
 */
object PixInstallmentUpdater {

    private const val ESPERA_BASE_MS = 500L
    private const val TENTATIVAS = 4

    fun atualizar(
        service: EntitiesService<*>,
        sqlQueriesService: SqlQueriesService,
        viewPersistencia: String,
        docEntry: Int,
        installments: List<Installment>,
        tipoDocumento: String
    ) {
        if (installments.isEmpty()) {
            return
        }
        val payload = InvoicePixUpdatePayload.from(installments)
        repeat(TENTATIVAS) { tentativa ->
            service.update(payload, docEntry.toString())
            Thread.sleep(ESPERA_BASE_MS * (tentativa + 1))
            if (persistidoViaGet(service, docEntry, installments)) {
                return
            }
            if (persistidoViaSql(sqlQueriesService, viewPersistencia, docEntry, installments)) {
                return
            }
        }
        throw Exception("SAP retornou sucesso, mas não persistiu os dados PIX $tipoDocumento $docEntry apos $TENTATIVAS tentativas")
    }

    private fun persistidoViaGet(
        service: EntitiesService<*>,
        docEntry: Int,
        installments: List<Installment>
    ): Boolean {
        val persistidas = service.getById(docEntry).tryGetValue<Document>().documentInstallments.orEmpty()
            .associateBy { it.InstallmentId }
        return todasPersistidas(installments) { id ->
            persistidas[id]?.let {
                CamposPix(it.U_QrCodePix, it.U_pix_reference, it.U_pix_textContent, it.U_pix_link, it.U_pix_due_date)
            }
        }
    }

    private fun persistidoViaSql(
        sqlQueriesService: SqlQueriesService,
        viewPersistencia: String,
        docEntry: Int,
        installments: List<Installment>
    ): Boolean {
        val persistidas = sqlQueriesService
            .getAll<InstallmentPixPersistido>(viewPersistencia, listOf(Parameter("docEntry", docEntry)))
            .associateBy { it.installmentId }
        return todasPersistidas(installments) { id ->
            persistidas[id]?.let {
                CamposPix(it.qrCodePix, it.pixReference, it.pixTextContent, it.pixLink, it.pixDueDate)
            }
        }
    }

    private fun todasPersistidas(
        installments: List<Installment>,
        buscaPersistida: (Int?) -> CamposPix?
    ): Boolean {
        return installments.all { atualizada ->
            val p = buscaPersistida(atualizada.InstallmentId) ?: return@all false
                p.qrCodePix == atualizada.U_QrCodePix &&
                p.pixReference == atualizada.U_pix_reference &&
                p.pixTextContent == atualizada.U_pix_textContent &&
                p.pixLink == atualizada.U_pix_link
        }
    }

    private data class CamposPix(
        val qrCodePix: String?,
        val pixReference: String?,
        val pixTextContent: String?,
        val pixLink: String?,
        val pixDueDate: String?,
    )
}
