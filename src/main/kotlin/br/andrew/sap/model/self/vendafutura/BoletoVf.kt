package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BoletoVf {
    var DocEntry : Int? = null
    var DocNum : String? = ""
    var DocDueDate : String? = ""
    var DocTotal : String? = ""
    var devolucao : String? = ""
    var DocStatus : String? = ""
    var InstallmentId : Int? = null
    var U_QrCodePix : String? = null
    var U_pix_textContent : String? = null
    var U_pix_link : String? = null
    var U_pix_reference : String? = null
    var U_pix_due_date : String? = null
    var U_pix_proxima_consulta_em : String? = null
    var U_pix_consultar_ate : String? = null

    companion object {
        fun from(document: Document, installment: Installment? = null, devolucao: String? = null): BoletoVf {
            return BoletoVf().also {
                it.DocEntry = document.docEntry
                it.DocNum = document.docNum
                it.DocDueDate = installment?.dueDate ?: document.DocDueDate
                it.DocTotal = document.DocTotal
                it.DocStatus = when(document.DocumentStatus) {
                    DocumentStatus.bost_Open -> "O"
                    DocumentStatus.bost_Close -> "C"
                    else -> document.DocumentStatus?.typeName
                }
                it.devolucao = devolucao
                it.InstallmentId = installment?.InstallmentId
                it.U_QrCodePix = installment?.U_QrCodePix
                it.U_pix_textContent = installment?.U_pix_textContent
                it.U_pix_link = installment?.U_pix_link
                it.U_pix_reference = installment?.U_pix_reference
                // envia a validade digital (consultar_ate, timezone-aware) no campo due_date consumido pelo front
                it.U_pix_due_date = installment?.U_pix_consultar_ate
                it.U_pix_proxima_consulta_em = installment?.U_pix_proxima_consulta_em
                it.U_pix_consultar_ate = installment?.U_pix_consultar_ate
            }
        }
    }
}
