import br.andrew.sap.model.sap.ReconciliationRow
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import kotlin.jvm.Throws

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
) : ReconciliationRow{

    var Line_ID : Int? = null
    var ShortName : String? = null
    @JsonProperty("BPLID")
    fun getBPLID() : Int {
        return BPLID
    }

    @JsonIgnore
    var JdtNum : Int? = null

    @JsonIgnore
    fun isContaResultado(): Boolean {
        val number = AccountCode.trim().split("").getOrNull(1)?.toIntOrNull() ?: throw Exception("A string deve comecar com um numero")
        return number >= 3 && number  <= 5
    }

    @JsonIgnore
    override fun transNumReconciliation(): Int {
        return JdtNum ?: throw Exception("JdtNum nao existe")
    }

    @JsonIgnore
    override fun transRowId(): Int {
        return Line_ID ?: throw Exception("Line Id nao existe")
    }

    @JsonIgnore
    override fun reconcileAmount(): Double {
        return debit+Credit
    }
}

