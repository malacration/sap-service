package br.andrew.sap.model.logistica

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * A colecao do UDO tem nome todo maiusculo: sem o @JsonProperty nos dois acessores o Jackson
 * gera uma segunda propriedade e o Service Layer recebe nome errado (mesmo caso do CFOPCode).
 */
class CarregamentoJsonTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val doSap = """
        {"DocEntry":9,"U_nameOrdem":"x","U_Status":"Aberto","U_filial":2,"U_placa":null,"U_motorista":null,
         "U_capacidadeCaminhao":null,"U_transportadora":null,
         "ORD_CRG_LINHACollection":[
           {"DocEntry":9,"LineId":1,"U_baseType":13,"U_docEntryFatura":262554,"U_baseLine":0,"U_qtdRetirada":2.5},
           {"DocEntry":9,"LineId":2,"U_baseType":null,"U_orderDocEntry":10}]}
    """.trimIndent()

    @Test
    fun `le a colecao e separa as retiradas de fatura`() {
        val ordem = mapper.readValue(doSap, Carregamento::class.java)

        Assertions.assertEquals(2, ordem.ORD_CRG_LINHACollection!!.size)
        Assertions.assertEquals(1, ordem.retiradasDeFatura().size)
        Assertions.assertEquals(262554, ordem.retiradasDeFatura()[0].U_docEntryFatura)
        Assertions.assertEquals(2.5, ordem.retiradasDeFatura()[0].U_qtdRetirada)
    }

    @Test
    fun `colecao sai com o nome do UDO uma unica vez, e nula nao sai`() {
        val ordem = mapper.readValue(doSap, Carregamento::class.java)
        val json = mapper.writeValueAsString(ordem)
        Assertions.assertEquals(1, Regex("ORD_CRG_LINHACollection", RegexOption.IGNORE_CASE).findAll(json).count(), json)
        Assertions.assertTrue(json.contains("\"ORD_CRG_LINHACollection\""), json)

        ordem.ORD_CRG_LINHACollection = null
        Assertions.assertFalse(mapper.writeValueAsString(ordem).contains("LINHACollection", ignoreCase = true))
    }
}
