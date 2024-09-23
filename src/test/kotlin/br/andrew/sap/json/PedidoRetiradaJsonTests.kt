package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.futura.PedidoRetirada
import br.andrew.sap.services.EmailAdrres
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test

class PedidoRetiradaJsonTests {

    val json = "{\"dataEntrega\":\"2024-08-26\",\"docEntryVendaFutura\":42,\"itensRetirada\":[{\"itemCode\":\"PAC0000054\",\"quantity\":50,\"descricao\":\"OX CONFINAMENTO FINAL + GRANEL\"}]}"

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<PedidoRetirada>())
    }


}