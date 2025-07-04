package br.andrew.sap.json

import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.model.sap.partner.BusinessPartnerType
import br.andrew.sap.model.sap.partner.CpfCnpj
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AddressJson {

    val json ="{\n" +
            "\t\"telefone\": \"\",\n" +
            "\t\"idCliente\": 1,\n" +
            "\t\"nome\": \"SOVIS SERVICOS EM TECNOLOGIA DA INFORMACAO LTDA\",\n" +
            "\t\"cpfCnpj\": \"09177771000123\",\n" +
            "\t\"email\": \"atendimentosuporte@sovis.com.br\",\n" +
            "\t\"endereco\": {\n" +
            "\t\t\"tipoEndereco\": \"RUA\",\n" +
            "\t\t\"cidade\": \"CASCAVEL\",\n" +
            "\t\t\"estado\": \"PR\",\n" +
            "\t\t\"numero\": 3161,\n" +
            "\t\t\"bairro\": \"CENTRO\",\n" +
            "\t\t\"pais\": \"BR\",\n" +
            "\t\t\"rua\": \"R CARLOS GOMES\",\n" +
            "\t\t\"cep\": 85803000\n" +
            "\t}\n" +
            "}"

    @Test
    fun testJsonCliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, Cliente::class.java)
        val addrs = obj.endereco!!.getAddresse(localidade = 123)
    }

    @Test
    fun getBussinesPartnerAndAddrs(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, Cliente::class.java)
        val bussinesParte = obj.getBusinessPartner()
        val addrs1 = bussinesParte.getAddresses().get(0)
        val addrs2 = bussinesParte.getAddresses().get(1)
        Assertions.assertEquals("bo_ShipTo",addrs1.addressType.toString())
        Assertions.assertEquals("bo_BillTo",addrs2.addressType.toString())
        val saidaJson = mapper.writeValueAsString(addrs1)
        Assertions.assertTrue(saidaJson.contains("bo_ShipTo"))
        Assertions.assertTrue(saidaJson.contains("AddressType"))

        val bussinesParteJson = mapper.writeValueAsString(bussinesParte)
        Assertions.assertTrue(!bussinesParteJson.contains("bpLID"))
        println(bussinesParteJson)
    }


    @Test
    fun test(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val saida = mapper.writeValueAsString(
            BusinessPartner().also { it.attachmentEntry = 666 })
        println(saida)
    }
}