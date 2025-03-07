package br.andrew.sap.model.sap

import JournalEntry
import br.andrew.sap.model.enums.YesNo
import br.andrew.sap.model.sap.documents.base.Document
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*
import kotlin.time.times

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class InternalReconciliations {
    var reconNum: Int? = null
    var reconDate: String? = SimpleDateFormat("yyyy-MM-dd").format(Date())
    var cardOrAccount: String? = null
    var reconType: ReconType? = null
    var total: Double? = null
    var cancelAbs: Int? = null //Id da reconciliacao anterior
    var internalReconciliationRows: List<InternalReconciliationOpenTransRow>? = null
    var electronicProtocols: Int? = null
    var internalReconciliationOpenTransRows: List<InternalReconciliationOpenTransRow>? = null
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class InternalReconciliationOpenTransRow(
    var cashDiscount: Double? = null,
    var creditOrDebit: String? = null,
    var reconcileAmount: Double? = null,
    var selected: YesNo? = YesNo.tYES,
    var shortName: String? = null,
    var srcObjAbs: Int? = null,
    var srcObjTyp: String? = null,
    var transId: Int? = null,
    var transRowId: Int? = null
){

}
enum class ReconType{
    rtCancellation,
    rtManual,
    rtPayment,
}

interface ReconciliationRow{
    fun transNumReconciliation() : Int
}
class InternalReconciliationsBuilder(val deb : ReconciliationRow, val cred : ReconciliationRow, val total : Double){

    private var _debitTransRowId = 0
    private var _creditTransRowId = 0

    fun build() : InternalReconciliations{
        return InternalReconciliations().also {
            it.cardOrAccount = "coaCard"
            it.internalReconciliationOpenTransRows = listOf(getDebit(),getCredit())
        }
    }

    fun setDebitTransRowId(transRowId : Int): InternalReconciliationsBuilder {
        this._debitTransRowId = transRowId
        return this
    }

    fun setCreditTransRowId(transRowId : Int): InternalReconciliationsBuilder {
        this._creditTransRowId = transRowId
        return this
    }

    private fun getDebit() : InternalReconciliationOpenTransRow {
        return InternalReconciliationOpenTransRow().also {
            it.creditOrDebit = "codDebit"
            it.transId = deb.transNumReconciliation()
            it.reconcileAmount = total
            it.transRowId = _debitTransRowId
        }
    }

    private fun getCredit() : InternalReconciliationOpenTransRow {
        return InternalReconciliationOpenTransRow().also {
            it.creditOrDebit = "codCredit"
            it.transId = cred.transNumReconciliation()
            it.reconcileAmount = total
            it.transRowId = _creditTransRowId
        }
    }
}