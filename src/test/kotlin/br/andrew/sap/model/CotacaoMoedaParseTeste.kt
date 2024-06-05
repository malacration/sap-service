package br.andrew.sap.model

import br.andrew.sap.model.bacen.CotacaoMoeda
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CotacaoMoedaParseTeste {

    val json = "{\"paridadeCompra\":1.00000,\"paridadeVenda\":1.00000,\"cotacaoCompra\":5.16980,\"cotacaoVenda\":5.17040,\"dataHoraCotacao\":\"2024-05-27 13:15:28.728\",\"tipoBoletim\":\"Fechamento\"}"

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<CotacaoMoeda>())
        Assertions.assertEquals(5.1704,obj.cotacaoVenda)
        Assertions.assertEquals(5.1698,obj.cotacaoCompra)
        Assertions.assertEquals("Fechamento",obj.tipoBoletim)
    }
}