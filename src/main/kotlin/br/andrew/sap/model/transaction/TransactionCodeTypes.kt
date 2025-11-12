package br.andrew.sap.model.transaction

import br.andrew.sap.services.batch.BatchId
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

enum class TransactionCodeTypes(val description : String) {
    VFET("Entrega Venda futura"),
    VFDV("Dev. Venda futura"),
    AROU("Outros"),
    CanC("Cancelled"),
    VFEC("Ent. VF Conciliado");

    fun get() : TransactionCode{
        return TransactionCode(this.toString(),this.description)
    }
}

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class UpdateTransactionCode(
    private val id : String,
    val TransactionCode : TransactionCodeTypes
) : BatchId{
    override fun getId(): String {
        return id
    }
}