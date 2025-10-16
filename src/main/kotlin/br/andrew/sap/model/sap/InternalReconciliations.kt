package br.andrew.sap.model.sap

import br.andrew.sap.model.enums.YesNo
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*

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
    fun transRowId() : Int
    fun reconcileAmount() : Double
}

enum class DebOrCredt{
    Debit,
    Credt,
}

interface ReconciliationListRows{
    fun getReconciliationRows(debOrCredit : DebOrCredt) : List<ReconciliationRow>
}

class InternalReconciliationsBuilder(val debs : List<ReconciliationRow>, val creds: List<ReconciliationRow>){

    constructor(deb : ReconciliationListRows, cred : ReconciliationListRows) : this(
        deb.getReconciliationRows(DebOrCredt.Debit),
        cred.getReconciliationRows(DebOrCredt.Credt)
    )

    fun build() : InternalReconciliations{
        return InternalReconciliations().also {
            it.cardOrAccount = "coaCard"
            it.internalReconciliationOpenTransRows = mutableListOf<InternalReconciliationOpenTransRow>().apply {
                addAll(getCredit())
                addAll(getDebit())
            }

        }
    }

    private fun getDebit() : List<InternalReconciliationOpenTransRow> {
        return debs.map { deb ->
            InternalReconciliationOpenTransRow().also {
                it.creditOrDebit = "codDebit"
                it.transId = deb.transNumReconciliation()
                it.reconcileAmount = deb.reconcileAmount()
                it.transRowId = deb.transRowId()
            }
        }
    }

    private fun getCredit() : List<InternalReconciliationOpenTransRow> {
        return creds.map { cred -> InternalReconciliationOpenTransRow().also {
            it.creditOrDebit = "codCredit"
            it.transId = cred.transNumReconciliation()
            it.reconcileAmount = cred.reconcileAmount()
            it.transRowId = cred.transRowId()
        } }
    }
}