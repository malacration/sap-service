package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date

@Service
class JournalEntriesService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
        EntitiesService<JournalEntry>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/JournalEntries"
    }

    fun readCsv(inputStream: InputStream): List<JournalEntry> {
        val reader = inputStream.bufferedReader()
        val header = reader.readLine()
        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val list = it.split(';', ignoreCase = false)
                JournalEntry(
                    list[0].trim().toInt(),
                    list[3].trim(),
                    list[4].trim(),
                    list[5].replace(",",".").toDouble(),
                    list[6]).also {
                        it.taxDate = SimpleDateFormat("yyy-MM-dd").format(SimpleDateFormat("dd/MM/yyy").parse(list[2]))
                        it.ReferenceDate = it.taxDate
                        it.costingCodes("501",list[8].trim().replace(";",""))
                        it.Reference = list.getOrNull(9)
                        it.Reference2 = list.getOrNull(10)
                        it.Reference3 = list.getOrNull(11)
                }
            }.toList()
    }

}


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
    @JsonProperty("BPLID")
    fun getBPLID() : Int {
        return BPLID
    }
}