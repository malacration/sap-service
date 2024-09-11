package br.andrew.sap.json

import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.model.sap.documents.base.adiantamento.DownPaymentsToDraw
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DownPaymentsToDrawJson {

    @Test
    fun teste(){
        val objeto = DownPaymentsToDraw().also {
            it.grossAmountToDraw = BigDecimal(100)
        }
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.writeValueAsString(objeto)
    }
}