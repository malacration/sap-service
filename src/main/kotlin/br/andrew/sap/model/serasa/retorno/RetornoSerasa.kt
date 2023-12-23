package br.andrew.sap.model.serasa.retorno

import java.text.SimpleDateFormat
import java.util.*


open class RetornoSerasa(val dataFormat : String = "yyyyMMdd") {

    fun getDate(date : String) : Date {
        return SimpleDateFormat(dataFormat).parse(date)
    }

    fun getDouble(value : String) : Double {
        return value.toDouble().div(100)
    }

}