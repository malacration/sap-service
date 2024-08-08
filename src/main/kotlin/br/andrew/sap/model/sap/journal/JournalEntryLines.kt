import br.andrew.sap.services.journal.JournalEntryLines
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class JournalEntry(val journalEntryLines : List<JournalEntryLines>, val memo : String){

    var taxDate : String? = null
    var ReferenceDate : String? = null

    var Reference : String? = null
    var Reference2 : String? = null
    var Reference3 : String? = null

    fun costingCodes(costingCode: String, costingCode2: String) {
        journalEntryLines.forEach{
            it.costingCode = costingCode
            it.costingCode2 = costingCode2
        }
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
}