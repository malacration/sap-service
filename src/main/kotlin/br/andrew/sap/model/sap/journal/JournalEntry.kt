import br.andrew.sap.model.sap.ReconciliationRow
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class JournalEntry(val journalEntryLines : List<JournalEntryLines>, val memo : String) : ReconciliationRow {

    var taxDate : String? = null
    var ReferenceDate : String? = null
    var JdtNum : Int? = null
    var OriginalJournal : String? = null
    var Original : Int? = null
    var U_Atualizar_Observacao : Int? = null
    var U_Atualizar_Centro_de_Custo : Int? = null
    var TransactionCode : String? = null


    var Reference : String? = null
    var Reference2 : String? = null
    var Reference3 : String? = null

    fun costingCodes(costingCode: String, costingCode2: String) {
        journalEntryLines.forEach{
            it.costingCode = costingCode
            it.costingCode2 = costingCode2
        }
    }

    fun costingCodes(document: Document) {
        val line : DocumentLines = document.DocumentLines.filter {
            it.CostingCode != null
            it.CostingCode2 != null
        }.firstOrNull()
            ?: throw Exception("Esse documento nao possuiu nenhuma linha de produto")
        costingCodes(line.CostingCode!!,line.CostingCode2!!)
    }

    //Todo fazer teste de unidade para esse cara
    @JsonIgnore
    fun hasContaResultado(): Boolean {
        return journalEntryLines.any { it.isContaResultado() }
    }

    constructor(filial : Int,
                accDebit : String,
                accCredit : String,
                value : Double,
                memo : String) : this(
        listOf(
            JournalEntryLines(accDebit,value,0.0,filial),
            JournalEntryLines(accCredit,0.0,value,filial)
        ),
        memo
    )

    override fun transNumReconciliation(): Int {
        return this.JdtNum ?:throw Exception("Numero do LC esta null")
    }


}