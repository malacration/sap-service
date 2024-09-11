package br.andrew.sap.model.sap

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
    var reconType: String? = null
    var total: Double? = null
    var cancelAbs: Int? = null
    var internalReconciliationRows: Int? = null
    var electronicProtocols: Int? = null
    var internalReconciliationOpenTransRows: List<InternalReconciliationOpenTransRow>? = null
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class InternalReconciliationOpenTransRow(
    var cashDiscount: Double? = null,
    var creditOrDebit: String? = null,
    @JsonProperty("ReconcileAmount")
    private var _reconcileAmount: Double? = null,
    var selected: YesNo? = YesNo.tYES,
    var shortName: String? = null,
    var srcObjAbs: Int? = null,
    var srcObjTyp: String? = null,
    var transId: Int? = null,
    var transRowId: Int? = null
){

    var reconcileAmount : Double?
        get() {
            return if((_reconcileAmount ?: 0.0) < 0)
                -1*(_reconcileAmount ?: 0.0)
            else
                _reconcileAmount
        }
        set(value) {
            _reconcileAmount = value
        }

}


class InternalReconciliationsBuilder(val deb : Document, val cred : Document){

    fun build() : InternalReconciliations{
        return InternalReconciliations().also {
            it.cardOrAccount = "coaCard"
            it.internalReconciliationOpenTransRows = listOf(getDebit(),getCredit())
        }
    }

    private fun getDebit() : InternalReconciliationOpenTransRow {
        return InternalReconciliationOpenTransRow().also {
            it.creditOrDebit = "codDebit"
            it.transId = deb.TransNum
            it.reconcileAmount = deb.DocTotal?.toDoubleOrNull()
        }
    }

    private fun getCredit() : InternalReconciliationOpenTransRow {
        return InternalReconciliationOpenTransRow().also {
            it.creditOrDebit = "codCredit"
            it.transId = cred.TransNum
            it.reconcileAmount = cred.DocTotal?.toDoubleOrNull()
        }
    }
}