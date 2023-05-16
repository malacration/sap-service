package br.andrew.sap.json

import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.model.partner.BusinessPartnerType
import br.andrew.sap.model.partner.CpfCnpj
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class BusinessPartnerJsonTest {

    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val saida = mapper.writeValueAsString(BusinessPartner("windson", BusinessPartnerType.C)
                .also { it.setCpfCnpj(CpfCnpj("01847004261")) })
        Assertions.assertTrue(saida.contains("BPFiscalTaxIDCollection"))
        Assertions.assertTrue(!saida.contains("BpfiscalTaxIDCollection"))
        Assertions.assertTrue(saida.contains("BPAddresses"))
        println(saida)
    }
}
