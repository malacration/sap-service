package br.andrew.sap.json

import br.andrew.sap.model.sap.tax.SalesTaxCode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

class SalesTaxCodeTest {


    @Test
    fun readJson(){
        val json = "{ \"Code\" : \"windson\"}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val taxCode = mapper.readValue<SalesTaxCode>(json)
    }


    @Test
    fun readJsonNull(){
        val json = "{ \"Code\" : \"windson\",  \"salesTaxCodes_Lines\": null }"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val taxCode = mapper.readValue<SalesTaxCode>(json)
    }

    @Test
    fun writeJson(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        JSONAssert.assertEquals(
            "{ \"Code\" : \"windson\" }",
            mapper.writeValueAsString(SalesTaxCode("windson",null)),
            true);
        JSONAssert.assertEquals(
            "{ \"Code\" : \"code\" }",
            mapper.writeValueAsString(SalesTaxCode("code", listOf())),
            true);
    }

    @Test
    fun readJsonFull(){
        val json = "{\"odata.metadata\":\"https://localhost:50000/b1s/v1/\$metadata#SalesTaxCodes/@Element\",\"ValidForAR\":\"tYES\",\"ValidForAP\":\"tYES\",\"UserSignature\":22,\"Rate\":47.25,\"Name\":\"ICMS DESONERADO 19% - PIS COFINS TRIBUTADO\",\"Freight\":\"tYES\",\"Code\":\"5101-007\",\"IsItemLevel\":\"tNO\",\"Inactive\":\"tNO\",\"FADebit\":\"tNO\",\"SalesTaxCodes_Lines\":[{\"STATaxOnTaxType\":null,\"STATaxonTaxCode\":null,\"STCCode\":\"5101-007\",\"STAType\":10,\"STACode\":\"IC17BI02\",\"RowNumber\":0,\"EffectiveRate\":19.0},{\"STATaxOnTaxType\":null,\"STATaxonTaxCode\":null,\"STCCode\":\"5101-007\",\"STAType\":25,\"STACode\":\"IC17BI02\",\"RowNumber\":1,\"EffectiveRate\":19.0},{\"STATaxOnTaxType\":null,\"STATaxonTaxCode\":null,\"STCCode\":\"5101-007\",\"STAType\":16,\"STACode\":\"IP00BI01\",\"RowNumber\":2,\"EffectiveRate\":0.0},{\"STATaxOnTaxType\":null,\"STATaxonTaxCode\":null,\"STCCode\":\"5101-007\",\"STAType\":19,\"STACode\":\"PI16BT01\",\"RowNumber\":3,\"EffectiveRate\":1.65},{\"STATaxOnTaxType\":null,\"STATaxonTaxCode\":null,\"STCCode\":\"5101-007\",\"STAType\":21,\"STACode\":\"CF76BT01\",\"RowNumber\":4,\"EffectiveRate\":7.6}]}"
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val taxCode = mapper.readValue<SalesTaxCode>(json)
    }

}