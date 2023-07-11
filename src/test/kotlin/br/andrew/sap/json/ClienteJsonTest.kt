package br.andrew.sap.json

import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.forca.PedidoVenda
import br.andrew.sap.model.forca.Produto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.util.Assert

class ClienteJsonTest {

    val json = "{\n" +
            "   \"telefone\":\"5555555\",\n" +
            "   \"idCliente\":6,\n" +
            "   \"endereco\":{\n" +
            "      \"cidade\":3651,\n" +
            "      \"estado\":\"RJ\",\n" +
            "      \"numero\":\"00075\",\n" +
            "      \"bairro\":\"CENTRO\",\n" +
            "      \"tipoEndereco\":\"RUA\",\n" +
            "      \"pais\":\"BR\",\n" +
            "      \"rua\":\"RUA asdas DANTAS\",\n" +
            "      \"cep\": 661\n" +
            "   },\n" +
            "   \"nome\":\"Fake LTDA\",\n" +
            "   \"cpfCnpj\":29601566000100,\n" +
            "   \"email\":\"windson@windson.com\"\n" +
            "}"
    @Test
    fun testeJsonCliente(){
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val obj = mapper.readValue(json, jacksonTypeRef<Cliente>())
        val bp = obj.getBusinessPartner()
        Assertions.assertEquals("5555555",bp.phone1)
        Assertions.assertEquals("6",bp.u_id_forca)
        Assertions.assertEquals("windson@windson.com",bp.emailAddress)
        Assertions.assertEquals("windson@windson.com",bp.emailAddress)
    }



}
