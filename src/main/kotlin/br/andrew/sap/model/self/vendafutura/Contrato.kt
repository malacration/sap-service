package br.andrew.sap.model.self.vendafutura

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.apache.commons.lang3.mutable.Mutable
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date


@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class Contrato(
    val U_orderDocEntry : Int,
    val U_cardCode : String,
    @JsonProperty("AR_CF_LINHACollection")
    val itens : List<Item>,
    val U_vendedor : Int,
    val U_cardName : String,
    val U_valorFrete  : Double = 0.0,
    dataCriacao : Date = Date()
) {
    init {
        if(total().compareTo(BigDecimal.ZERO) <= 0)
            throw Exception("Nao e permitido contrato sem valor")
    }
    fun total(): BigDecimal {
        return itens.map{ it.total() }
            .sumOf { it }
            .plus(BigDecimal(U_valorFrete.toString()))
            .setScale(2, RoundingMode.HALF_DOWN)
    }

    var DocEntry : Int? = null
    var U_dataCriacao : String = SimpleDateFormat("yyyy-MM-dd").format(dataCriacao)
}