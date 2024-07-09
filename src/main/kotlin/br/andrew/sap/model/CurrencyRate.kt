package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.text.SimpleDateFormat
import java.util.*

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class CurrencyRate(
    var Currency : String? = null,
    var RateDate : String? = null,
    var Rate : String? = null
){
    var Date : String? = null
    constructor(date : Date, rate: String) : this(null, SimpleDateFormat("yyyy-MM-dd").format(date),rate)
}