package br.andrew.sap.model.producao

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * O front (OrdemProducao.from) le os nomes do Service Layer. Sem o @JsonNaming o Jackson
 * escreve absoluteEntry/documentNumber e a listagem aparece com as colunas em branco.
 */
class OrdemProducaoJsonTest {

    private val mapper = jacksonObjectMapper()

    @Test
    fun `listagem sai com os nomes do SAP`() {
        val json: Map<String, Any?> = mapper.readValue(mapper.writeValueAsString(
            OrdemProducao(AbsoluteEntry = 39140, DocumentNumber = 29514, ProductDescription = "PM ENERGIA 20 AGUAS", U_LbrOne_Batelada = 2.0)))

        assertEquals(39140, json["AbsoluteEntry"])
        assertEquals(29514, json["DocumentNumber"])
        assertEquals("PM ENERGIA 20 AGUAS", json["ProductDescription"])
        assertEquals(2.0, json["U_LbrOne_Batelada"])
        assertTrue(json.keys.none { it.first().isLowerCase() }, "chave em camelCase: ${json.keys}")
    }

    @Test
    fun `detalhe sai com os nomes do SAP, inclusive nas linhas`() {
        val json: Map<String, Any?> = mapper.readValue(mapper.writeValueAsString(
            OrdemProducaoDetalhe(AbsoluteEntry = 1, ProductionOrderLines = listOf(OrdemProducaoLinha(ItemNo = "INS*1", ItemName = "CALCARIO")))))

        assertEquals(1, json["AbsoluteEntry"])
        val linha = (json["ProductionOrderLines"] as List<*>).first() as Map<*, *>
        assertEquals("INS*1", linha["ItemNo"])
        assertEquals("CALCARIO", linha["ItemName"])
    }

    @Test
    fun `continua lendo o JSON do Service Layer`() {
        val ordem: OrdemProducao = mapper.readValue("""{"AbsoluteEntry":5,"DocumentNumber":7,"U_LbrOne_Batelada":3,"Outro":1}""")

        assertEquals(5, ordem.AbsoluteEntry)
        assertEquals(3.0, ordem.U_LbrOne_Batelada)
    }
}
