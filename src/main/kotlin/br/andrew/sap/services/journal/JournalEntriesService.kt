package br.andrew.sap.services.journal

import JournalEntry
import JournalEntryLines
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.InputStream
import java.text.SimpleDateFormat

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
                        it.costingCodes(list.getOrNull(7)?:"501",list[8].trim().replace(";",""))
                        it.Reference = list.getOrNull(9)
                        it.Reference2 = list.getOrNull(10)
                        it.Reference3 = list.getOrNull(11)
                }
            }.toList()
    }

    fun search(docEntry: Int): List<JournalEntry> {
        val filter = Filter(
            Predicate("JdtNum", docEntry,Condicao.EQUAL )
        )
        val result = get(filter)
        return result.tryGetValues<JournalEntry>()
    }

    //TODO Modificar isso
    fun getByDocEntry(docEntry: Int): JournalEntry?{
        val filter = Filter((mutableListOf(Predicate("JdtNum", docEntry, Condicao.EQUAL))))
        return get(filter).tryGetValues<JournalEntry>().firstOrNull()
    }

    fun updateMemoJournalEntry(JdtNum: Int, memo: String): OData?{
        val json = "{" +
                "\"Memo\": \"$memo\"," +
                "\"U_Atualizar_Observacao\": \"1\"" +
                "}"
        return update(json, JdtNum.toString() )
    }

    fun markMemoChecked(idJournal: Int): OData? {
        val json = "{" +
                "\"U_Atualizar_Observacao\": \"1\"" +
                "}"
        return update(json, idJournal.toString() )

    }

    fun saveOrRecouverReference(entry: JournalEntry): JournalEntry {
        //TODO fazer a query para evitar registro cancelados
        //Nao encontrei uma forma de identificar se o registro esta cancelado
        return if(entry.Reference != null){
            val filter = Filter(
                Predicate("Reference", entry.Reference!!, Condicao.EQUAL),
                Predicate("TransactionCode", entry.TransactionCode!!, Condicao.EQUAL),
            )
            get(filter).tryGetValues<JournalEntry>().firstOrNull() ?: save(entry).tryGetValue()
        }
        else
            save(entry).tryGetValue()

    }
}
