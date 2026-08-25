package br.andrew.sap.model.sistema
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Query(val sqlCode: String, val sqlName: String, sqlText: String){

    //ATENCAO: isto achata o SQL inteiro em UMA linha antes de subir pro Service Layer.
    //Por isso os .sql de src/main/resources/views NAO podem ter comentarios: um "--"
    //deixa de comentar so a linha dele e comenta todo o resto da query, e a view sobe
    //truncada pro SAP. Ver views/README.md.
    val sqlText = sqlText.replace("\t"," ").replace("\n", " ").replace("\r", " ")

    init{
        val pattern = ":(\\w+)".toRegex()
        pattern.findAll(sqlText).forEach { matchResult ->
            val value = matchResult.groupValues[1]
        }
    }

}