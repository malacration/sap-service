package br.andrew.sap.model.sap.documents.base

import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.sap.ReconciliationRow
import br.andrew.sap.model.sap.documents.DocumentStatus
import br.andrew.sap.model.uzzipay.DataRetonroPixQrCode
import br.andrew.sap.model.uzzipay.RequestPixDueDate
import br.andrew.sap.model.uzzipay.RequestPixImmediate
import br.andrew.sap.model.uzzipay.Transaction
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.MINUTES
import kotlin.math.abs

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Installment(
    @JsonProperty("DueDate") private val _dueDate : LocalDate?, val total : Double) {

    var Status : DocumentStatus? = null
    var InstallmentId : Int? = null
    var PaymentOrdered : String? = null
    var Percentage : String? = null
    val TotalFC : Double? = null
    var U_QrCodePix : String? = null
    var U_pix_textContent : String? = null
    var U_pix_link : String? = null
    var U_pix_reference : String? = null
    var U_pix_due_date : String? = null
    var U_pix_proxima_consulta_em : String? = null
    var U_pix_consultar_ate : String? = null

    var DocEntry : Int? = null

    fun getReconciliationRow(transId : Int, transRowId : Int): InstallmentRow {
        return InstallmentRow(transId, transRowId, total)

    }

    val dueDate : String?
        get() = if(_dueDate == null) null else _dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    fun createExternalIdentifier(document : Document): String {
        return  "Num${document.docNum}" +
                "-Entry${document.docEntry}" +
                "-ins:${this.InstallmentId}" +
                "-${document.docObjectCode}" +
                "-"+System.currentTimeMillis()
    }

    fun getBy(transaction: Transaction): Boolean {
        if(this.U_pix_reference == null)
            return false
        return (this.U_pix_reference == transaction.txId)
    }

    fun setPix(
        request: RequestPixDueDate,
        chave: DataRetonroPixQrCode,
        dataReferencia: LocalDateTime = LocalDateTime.now()
    ): Installment {
        applyPixData(chave, request.getDueDate(), dataReferencia)
        return this
    }

    fun setPix(
        request: RequestPixImmediate,
        chave: DataRetonroPixQrCode,
        dataReferencia: LocalDateTime = LocalDateTime.now()
    ): Installment {
        val expiracao = dataReferencia.plusSeconds(request.expiration.toLong()).withNano(0)
        applyPixData(chave, expiracao.toString(), dataReferencia)
        return this
    }

    private fun applyPixData(
        chave: DataRetonroPixQrCode,
        dueDate: String,
        dataReferencia: LocalDateTime
    ) {
        U_QrCodePix = chave.data.textContent
        U_pix_textContent = chave.data.textContent
        U_pix_link = chave.data.link
        U_pix_reference = chave.data.reference
        U_pix_due_date = dueDate
        sanitizarControleConsultaPix(dataReferencia)
    }

    @JsonIgnore
    fun isPixValido(): Boolean {
        if(U_pix_reference.isNullOrEmpty() || U_pix_due_date.isNullOrEmpty()) {
            return false
        }
        return try {
            val data = parsePixDueDate(U_pix_due_date!!)
            !data.isBefore(LocalDate.now())
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun sanitizarControleConsultaPix(dataReferencia: LocalDateTime = LocalDateTime.now()) {
        U_pix_proxima_consulta_em = dataReferencia.truncatedTo(MINUTES).toString()
        U_pix_consultar_ate = getPixDueDateTime()
            ?.toString()
            ?: dataReferencia.plusDays(1).toString()
    }

    fun podeConsultarPixPagamento(dataReferencia: LocalDateTime = LocalDateTime.now()): Boolean {
        if(U_pix_reference.isNullOrBlank()) {
            return false
        }
        val limiteConsulta = getPixConsultarAte()
        if(limiteConsulta != null && dataReferencia.isAfter(limiteConsulta)) {
            return false
        }
        val proximaConsulta = getPixProximaConsultaEm() ?: return true
        return proximaConsulta.truncatedTo(MINUTES) <= dataReferencia.truncatedTo(MINUTES)
    }

    fun registrarConsultaPix(intervaloMinutos: Long, dataReferencia: LocalDateTime = LocalDateTime.now()) {
        U_pix_proxima_consulta_em = dataReferencia.plusMinutes(intervaloMinutos).truncatedTo(MINUTES).toString()
    }

    fun getPixProximaConsultaEm(): LocalDateTime? {
        return parseIsoDateTime(U_pix_proxima_consulta_em)
    }

    fun getPixConsultarAte(): LocalDateTime? {
        return parseIsoDateTime(U_pix_consultar_ate)
    }

    fun calcularJurosSimplesPorDia(taxaDiariaPercent: Double, dataReferencia: LocalDate = LocalDate.now(),
                                   valorBase: Double = total): Double {
        if(_dueDate == null) {
            return 0.0
        }
        val diasAtraso = diasAtraso(dataReferencia)
        if(diasAtraso <= 0) {
            return 0.0
        }
        val taxaDiaria = BigDecimal(taxaDiariaPercent)
            .divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        val juros = BigDecimal(valorBase)
            .multiply(taxaDiaria)
            .multiply(BigDecimal(diasAtraso))
        return juros.setScale(2, RoundingMode.DOWN).toDouble()
    }

    private fun parsePixDueDate(value: String): LocalDate {
        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (ex: Exception) {
            getPixDueDateTime(value)?.toLocalDate() ?: throw ex
        }
    }

    private fun getPixDueDateTime(value: String? = U_pix_due_date): LocalDateTime? {
        if(value.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atTime(LocalTime.MAX)
        } catch (_: Exception) {
            try {
                OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime()
            } catch (_: Exception) {
                LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }
        }
    }

    private fun parseIsoDateTime(value: String?): LocalDateTime? {
        if(value.isNullOrBlank()) {
            return null
        }
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (_: Exception) {
            OffsetDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME)
                .withOffsetSameInstant(ZoneOffset.UTC)
                .toLocalDateTime()
        }
    }

    fun diasAtraso(dataReferencia: LocalDate = LocalDate.now()): Long {
        if(_dueDate == null) {
            return 0
        }
        return ChronoUnit.DAYS.between(_dueDate, dataReferencia)
    }

    fun getBy(boleto: Boleto): Boolean {
        if(this.InstallmentId == null)
            return false
        return this.InstallmentId == boleto.numeroDaParcela
    }

    companion object{
        fun reverseExternalIdentifier(id : String) : String{
            return ""
        }
    }

}

class InstallmentRow(val _transId : Int, val _transRowId : Int, val ammount : Double) : ReconciliationRow{
    @JsonIgnoreProperties
    override fun transNumReconciliation(): Int {
        return _transId
    }

    @JsonIgnoreProperties
    override fun transRowId(): Int {
        return _transRowId
    }

    @JsonIgnoreProperties
    override fun reconcileAmount(): Double {
        return abs(ammount)
    }
}
