package br.andrew.sap.services.fiscal

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

class ContratoVendaFuturaPdfServiceTest {

    // Como o sap-odbc devolve: uma linha por item, decimal como string.
    private val linhas = listOf(
        mapOf("DocEntry" to 619, "DocNum" to 619, "CardCode" to "CLI1", "CardName" to "Bruno & Filhos", "DataCriacao" to "2026-09-28",
            "ValorFrete" to "557", "LineId" to 0, "Descricao" to "OX ENERGIA 20 AGUAS 30KG", "Quantidade" to "76",
            "PrecoNegociado" to "70.67", "CpfCnpj" to "123.456.789-00"),
        mapOf("DocEntry" to 619, "DocNum" to 619, "CardCode" to "CLI1", "CardName" to "Bruno & Filhos", "DataCriacao" to "2026-09-28",
            "ValorFrete" to "557", "LineId" to 1, "Descricao" to "OX ACELERA", "Quantidade" to "2.5",
            "PrecoNegociado" to "10", "CpfCnpj" to "123.456.789-00"),
    )

    private val consultas = mutableListOf<Map<String, Any?>>()
    private val odbc = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        @Suppress("UNCHECKED_CAST")
        val p = inv.arguments[1] as Map<String, Any?>
        consultas.add(p)
        QueryResponse(rows = linhas.filter { it["DocEntry"] in (p["contratos"] as List<*>) })
    })

    private val servico = ContratoVendaFuturaPdfService(odbc, SpringTemplateEngine().apply {
        setTemplateResolver(ClassLoaderTemplateResolver().apply { prefix = "templates/"; suffix = ".html"; characterEncoding = "UTF-8" })
    })

    @Test
    fun `html traz cliente, data, itens, produtos, frete e total como a tela`() {
        val logo = "data:image/png;base64,iVBORw0KGgo="
        val html = servico.html(linhas, logo)
        assertTrue(html.contains("VF 619"))
        assertTrue(html.contains("Bruno &amp; Filhos"), "nome escapado")
        assertTrue(html.contains("28/09/2026"))
        // 76 x 70,67 = 5.370,92; 2,5 x 10 = 25; produtos 5.395,92; + frete 557 = 5.952,92
        listOf("5.370,92", "5.395,92", "557,00", "5.952,92", "123.456.789-00", "2.5", "CONTROLE DE PEDIDO", logo)
            .forEach { assertTrue(html.contains(it), it) }
    }

    @Test
    fun `logo so entra se for data URI de imagem, nunca URL`() {
        assertTrue(servico.html(linhas).contains("CONTROLE DE PEDIDO"))
        assertFalse(servico.html(linhas).contains("<img"))
        assertFalse(servico.html(linhas, "https://exemplo.com/logo.png").contains("<img"))
        assertFalse(servico.html(linhas, "data:text/html;base64,PHNjcmlwdD4=").contains("<img"))
        assertEquals(null, ContratoVendaFuturaPdfService.logoValido("data:image/png;base64," + "A".repeat(800_000)))
    }

    @Test
    fun `gera PDF de cada contrato numa consulta so, e o que falta vira falha`() {
        val r = servico.gerar(listOf(619, 999, 619))
        assertEquals(setOf(619, 999), r.keys)
        val pdf = r.getValue(619).getOrThrow()
        assertEquals(619, pdf.docNum)
        assertEquals("%PDF", String(pdf.conteudo.copyOfRange(0, 4)))
        assertTrue(r.getValue(999).isFailure)
        assertEquals(listOf(listOf(619, 999)), consultas.map { it["contratos"] })
    }

    @Test
    fun `SQL dos contratos so le e tem so o parametro da lista`() {
        val sql = SqlResource.carregar("odbc/nota-fiscal-lote-contratos.sql")
        assertEquals(setOf("contratos"), Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(sql.replace(Regex("'[^']*'"), "''")).map { it.groupValues[1] }.toSet())
        assertTrue(sql.trimStart().startsWith("SELECT"))
        assertFalse(sql.contains("--"))
        // TaxId4 (CPF) antes de TaxId0, como o CpfCnpj do sap-service
        assertTrue(sql.contains("CASE WHEN IFNULL(T.\"TaxId4\", '') <> '' THEN T.\"TaxId4\" ELSE T.\"TaxId0\" END"))
    }
}
