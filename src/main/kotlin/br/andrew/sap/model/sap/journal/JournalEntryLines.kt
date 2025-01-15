import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class JournalEntryLines(
    val AccountCode : String,
    val debit : Double,
    val Credit : Double,
    @JsonProperty("BPLID")
    private val BPLID : Int,
    var costingCode : String? = null,
    var costingCode2 : String? = null
){

    var Line_ID : Int? = null
    var ShortName : String? = null
    @JsonProperty("BPLID")
    fun getBPLID() : Int {
        return BPLID
    }
}