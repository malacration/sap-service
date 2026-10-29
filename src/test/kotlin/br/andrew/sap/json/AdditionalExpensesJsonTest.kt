package br.andrew.sap.json

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * O PATCH de despesa adicional e sensivel ao que volta no corpo: devolver o "TaxCode" que o SAP
 * determinou faz o Service Layer responder "Internal error (-5002)" (visto no
 * SalesOrderCalculaDesoneradoSchedule, DocNum 65581). O campo existe so para o DesoneradoService
 * descobrir se a despesa tem ICMS desonerado, entao entra na leitura e nunca sai na escrita.
 */
class AdditionalExpensesJsonTest {

    private val mapper = ObjectMapper().registerModule(KotlinModule())

    private val doSap = """
        {"ExpenseCode":1,"LineTotal":1500.0,"TaxCode":"ICM00","LineNum":0,"DistributionMethod":"aedm_RowTotal"}
    """.trimIndent()

    @Test
    fun `TaxCode e lido do SAP`() {
        val despesa = mapper.readValue(doSap, jacksonTypeRef<AdditionalExpenses>())

        Assertions.assertEquals("ICM00", despesa.TaxCode)
        Assertions.assertEquals(1500.0, despesa.LineTotal)
    }

    @Test
    fun `vinculo com documento base so sai, nunca e lido do SAP`() {
        //Despesa de pedido lida do SAP: vinculada a cotacao. Copiar isso para a nota quebra o batch.
        val doPedido = mapper.readValue(
            """{"ExpenseCode":1,"LineTotal":940.0,"BaseDocEntry":177801,"BaseDocLine":0,"BaseDocType":23}""",
            jacksonTypeRef<AdditionalExpenses>())
        Assertions.assertFalse(mapper.writeValueAsString(doPedido).contains("BaseDoc"))

        val daEntrega = AdditionalExpenses(5, 300.0).also { it.BaseDocEntry = 153135; it.BaseDocLine = 0; it.BaseDocType = 13 }
        val json = mapper.readTree(mapper.writeValueAsString(daEntrega))
        Assertions.assertEquals(13, json["BaseDocType"].asInt())
        Assertions.assertEquals(153135, json["BaseDocEntry"].asInt())
        Assertions.assertEquals(0, json["BaseDocLine"].asInt())
    }

    @Test
    fun `TaxCode nunca volta no JSON enviado ao SAP`() {
        val despesa = mapper.readValue(doSap, jacksonTypeRef<AdditionalExpenses>())

        Assertions.assertFalse(mapper.writeValueAsString(despesa).contains("TaxCode"),
            "devolver o TaxCode no PATCH quebra o Service Layer com -5002")
    }

    @Test
    fun `frete negociado vai para o SAP`() {
        val json = mapper.writeValueAsString(AdditionalExpenses.frete(1500.0))

        Assertions.assertTrue(json.contains("U_frete_negociado"))
        Assertions.assertTrue(json.contains("1500"))
    }

    /** Despesa anterior a funcionalidade: campo nulo, NON_EMPTY omite, payload nao muda. */
    @Test
    fun `frete negociado nulo nao vai para o SAP`() {
        val despesa = mapper.readValue(doSap, jacksonTypeRef<AdditionalExpenses>())

        Assertions.assertFalse(mapper.writeValueAsString(despesa).contains("U_frete_negociado"))
    }
}
