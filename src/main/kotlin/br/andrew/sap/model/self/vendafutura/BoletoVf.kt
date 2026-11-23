package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Installment
import java.math.BigDecimal
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

enum class StatusPagamentoBoleto { DEVOLVIDO, PAGO, PARCIAL, ABERTO }

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class BoletoVf {
    var DocEntry : Int? = null
    var DocNum : String? = ""
    var DocDueDate : String? = ""
    var DocTotal : String? = ""
    var devolucao : String? = ""
    var recebimento : String? = ""
    var DocStatus : String? = ""
    // valor ja recebido do adiantamento (ODPI.PaidToDate, ja desconta recebimentos cancelados)
    var PaidToDate : Double? = null
    var StatusPagamento : StatusPagamentoBoleto? = null
    var InstallmentId : Int? = null
    var U_QrCodePix : String? = null
    var U_pix_textContent : String? = null
    var U_pix_link : String? = null
    var U_pix_reference : String? = null
    var U_pix_due_date : String? = null
    var U_pix_proxima_consulta_em : String? = null
    var U_pix_consultar_ate : String? = null
    var TaxaJurosMoraPercent : Double = 0.0
    var JurosValor : Double = 0.0
    var ValorTitulo : Double = 0.0
    var ValorTotal : Double = 0.0

    /** [linha] e a linha do SQL boletos-status do mesmo ODPI; sem ela (ou sem valores) o status fica null. */
    fun aplicaPagamento(linha: BoletoVf?) {
        PaidToDate = linha?.PaidToDate
        StatusPagamento = statusPagamento(linha?.devolucao, linha?.PaidToDate, linha?.DocTotal?.toDoubleOrNull(), linha != null)
    }

    companion object {
        private val LIMITE_PAGO = BigDecimal("0.01")
        // DEVOLVIDO > PAGO (falta de ate R$0,01 conta como pago) > PARCIAL > ABERTO; dado ausente => null
        fun statusPagamento(devolucao: String?, pago: Double?, total: Double?, temLinha: Boolean = true): StatusPagamentoBoleto? {
            if (!temLinha) return null
            if (!devolucao.isNullOrBlank()) return StatusPagamentoBoleto.DEVOLVIDO
            if (pago == null || total == null) return null
            // BigDecimal.valueOf usa a forma decimal do double (46800.0 - 46799.99 = 0.01 exato, sem erro binario)
            val falta = BigDecimal.valueOf(total).subtract(BigDecimal.valueOf(pago))
            return when {
                falta <= LIMITE_PAGO -> StatusPagamentoBoleto.PAGO
                pago > 0.0 -> StatusPagamentoBoleto.PARCIAL
                else -> StatusPagamentoBoleto.ABERTO
            }
        }

        fun from(
            document: Document,
            installment: Installment? = null,
            devolucao: String? = null,
            jurosMoraPercent: Double = 0.0
        ): BoletoVf {
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
                // O juros nao e gravado no SAP: so existe no valor do QR Code gerado na UzziPay.
                // Por isso o detalhamento so vem preenchido na resposta da geracao, quando a taxa
                // aplicada e conhecida. Num GET posterior a taxa e 0.0 e o breakdown fica zerado.
                val juros = installment?.calcularJurosPix(jurosMoraPercent) ?: 0.0
                val valorTitulo = installment?.total
                    ?: document.DocTotal?.toDoubleOrNull()
                    ?: 0.0
                it.TaxaJurosMoraPercent = jurosMoraPercent
                it.JurosValor = juros
                it.ValorTitulo = valorTitulo
                it.ValorTotal = valorTitulo + juros
            }
        }
    }
}
