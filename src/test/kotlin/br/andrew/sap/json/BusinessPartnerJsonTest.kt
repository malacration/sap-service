package br.andrew.sap.json

import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.CpfCnpj
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class BusinessPartnerJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val saida = mapper.writeValueAsString(
            BusinessPartner("windson", BusinessPartnerType.C)
                .also { it.setCpfCnpj(CpfCnpj("01847004261")) })
        Assertions.assertTrue(saida.contains("BPFiscalTaxIDCollection"))
        Assertions.assertTrue(!saida.contains("BpfiscalTaxIDCollection"))
        println(saida)
    }


    @Test
    fun teste2(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val bp = Cliente("windson","27746773001").getUpdateBusinessPartner("CLI0666")
        val saida = mapper.writeValueAsString(bp)
        println(saida)
    }
}
