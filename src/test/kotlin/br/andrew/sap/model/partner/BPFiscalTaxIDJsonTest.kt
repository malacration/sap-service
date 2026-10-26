package br.andrew.sap.model.partner

import br.andrew.sap.model.sap.partner.BPFiscalTaxID
import br.andrew.sap.model.sap.partner.CpfCnpj
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * O Service Layer so aceita os nomes exatos do BPFiscalTaxID. Com o JsonNaming UpperCamelCase o
 * BPCode saia como "Bpcode": o POST de cliente novo era recusado e a leitura nunca preenchia o campo.
 */
class BPFiscalTaxIDJsonTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `cliente novo manda so o documento, sem BPCode nulo`() {
        val json = mapper.readTree(mapper.writeValueAsString(BPFiscalTaxID(CpfCnpj("03998928260"))))

        assertEquals(setOf("TaxId4"), json.fieldNames().asSequence().toSet())
        assertEquals("039.989.282-60", json.get("TaxId4").asText())
    }

    @Test
    fun `CNPJ vai em TaxId0 com a inscricao estadual em TaxId1`() {
        val json = mapper.readTree(mapper.writeValueAsString(BPFiscalTaxID(CpfCnpj("12345678000199"), "123456")))

        assertEquals(setOf("TaxId0", "TaxId1"), json.fieldNames().asSequence().toSet())
    }

    @Test
    fun `BPCode sai e entra com o nome do Service Layer`() {
        val fiscal = BPFiscalTaxID().also { it.BPCode = "CLI0001" }
        assertEquals(setOf("BPCode"), mapper.readTree(mapper.writeValueAsString(fiscal)).fieldNames().asSequence().toSet())

        val lido = mapper.readValue("""{"BPCode":"CLI0002","TaxId4":"039.989.282-60"}""", BPFiscalTaxID::class.java)
        assertEquals("CLI0002", lido.BPCode)
        assertEquals("039.989.282-60", lido.TaxId4)
    }
}
