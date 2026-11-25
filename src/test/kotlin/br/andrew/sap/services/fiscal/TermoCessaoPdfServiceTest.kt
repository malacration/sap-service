package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.CessionariaLote
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.thymeleaf.spring6.SpringTemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver

class TermoCessaoPdfServiceTest {

    private val cessionarias = listOf(
        mapOf("Codigo" to "12345678000199", "Nome" to "Securitizadora & Cia", "Cnpj" to "12.345.678/0001-99", "Telefone" to "(65) 3000-0000", "NomePdf" to "Securitizadora & Cia Ltda"))

    private fun parcela(docEntry: Int, n: Int) = mapOf(
        "DocEntry" to docEntry, "Serial" to 501, "Parcela" to n, "Saldo" to "1234.5", "DueDate" to "2026-12-05",
        "CardCode" to "C0001", "CardName" to "Bruno & Filhos", "BPLId" to 2, "BPLName" to "Sustennutri Filial",
        "TaxIdNum" to "98.765.432/0001-98", "Street" to "Rua A", "StreetNo" to "100", "Block" to "Centro",
        "City" to "Cuiaba", "State" to "MT", "ZipCode" to "78000-000")

    private val consultas = mutableListOf<Map<String, Any?>>()
    private val taxIdInvalido = mutableSetOf<Int>()
    private val odbc = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        @Suppress("UNCHECKED_CAST")
        val p = inv.arguments[1] as Map<String, Any?>
        consultas.add(p)
        if ((inv.arguments[0] as String).contains("NFL_CESSIONARIA")) QueryResponse(rows = cessionarias)
        else QueryResponse(rows = listOf(parcela(10, 1), parcela(10, 2), parcela(11, 1)).filter { it["DocEntry"] in (p["docEntries"] as List<*>) }
            .map { if (it["DocEntry"] in taxIdInvalido) it + ("TaxIdNum" to "123") else it })
    })

    private val servico = TermoCessaoPdfService(odbc, SpringTemplateEngine().apply {
        setTemplateResolver(ClassLoaderTemplateResolver().apply { prefix = "templates/"; suffix = ".html"; characterEncoding = "UTF-8" })
    })

    private val cessionaria = CessionariaLote("12345678000199", "Securitizadora & Cia", "(65) 3000-0000")

    @Test
    fun `html traz o texto do comunicado com os dados da parcela, da filial e da cessionaria`() {
        val logo = "data:image/png;base64,iVBORw0KGgo="
        val html = servico.html(listOf(parcela(10, 1), parcela(10, 2)), cessionaria, logo)
        listOf(
            "COMUNICADO – CESSÃO DE TÍTULO", "Prezado(a) Cliente, ", "Bruno &amp; Filhos", "Sustennutri Filial",
            "98.765.432/0001-98", "Nota Fiscal nº <span>501</span>", "501-1", "501-2", "R$ <span>1.234,50</span>", "05/12/2026", "Securitizadora &amp; Cia",
            "(69) 99956-8185", "Rua A, 100 - Centro - Cuiaba/MT - CEP 76801-970", "Atenciosamente,",
        ).forEach { assertTrue(html.contains(it), it) }
        // papel timbrado fixo: 3 logos do cabecalho + a marca do rodape, como data: URI; o logo da tela nao entra
        assertEquals(4, Regex("<img [^>]*src=\"data:image/png;base64,").findAll(html).count())
        assertFalse(html.contains(logo))
        assertFalse(servico.html(listOf(parcela(10, 2)), cessionaria, "https://x/logo.png").contains("https://x/logo.png"))
    }

    @Test
    fun `NF com varias parcelas tem uma linha por parcela, cada uma com saldo e vencimento`() {
        val html = servico.html(listOf(parcela(10, 1) + ("Saldo" to "100"), parcela(10, 2) + ("Saldo" to "200.5") + ("DueDate" to "2027-01-10"),
            parcela(10, 3) + ("Saldo" to "300") + ("DueDate" to "2027-02-10")), cessionaria)
        assertEquals(3, Regex("<td>501-\\d</td>").findAll(html).count())
        listOf("100,00", "200,50", "300,00", "05/12/2026", "10/01/2027", "10/02/2027").forEach { assertTrue(html.contains(it), it) }
        assertEquals(1, Regex("Prezado\\(a\\) Cliente").findAll(html).count())
    }

    @Test
    fun `rodape com o endereco da filial aparece em todas as paginas de uma NF com muitas parcelas`() {
        val bytes = br.andrew.sap.services.integracao.PdfService().htmlToPdf(servico.html((1..50).map { parcela(10, it) }, cessionaria))
        com.itextpdf.kernel.pdf.PdfDocument(com.itextpdf.kernel.pdf.PdfReader(java.io.ByteArrayInputStream(bytes))).use { doc ->
            assertTrue(doc.numberOfPages > 1, "esperava mais de uma pagina")
            (1..doc.numberOfPages).forEach { n ->
                val texto = com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(doc.getPage(n))
                assertTrue(texto.contains("Rua A, 100"), "pagina $n sem o rodape: $texto")
            }
        }
    }

    @Test
    fun `valor do comunicado e o saldo da parcela, nao o total`() {
        val html = servico.html(listOf(parcela(10, 1) + ("Saldo" to "400.25") + ("InsTotal" to "1234.5")), cessionaria)
        assertTrue(html.contains("400,25"))
        assertFalse(html.contains("1.234,50"))
    }

    @Test
    fun `nome que ja termina em ponto nao ganha ponto duplicado`() {
        val html = servico.html(listOf(parcela(10, 1) + ("CardName" to "Fazenda Boa S.A.")), CessionariaLote("1", "Securitizadora S.A.", "1"))
        assertFalse(html.contains(".."), html)
        assertTrue(html.contains("Cliente, <span>Fazenda Boa S.A.</span>"))
        assertEquals(2, Regex("Securitizadora S\\.A\\.</span>").findAll(html).count())
        // nome sem ponto final continua com o ponto da frase
        assertTrue(servico.html(listOf(parcela(10, 1)), cessionaria).contains("Securitizadora &amp; Cia.</span>"))
    }

    @Test
    fun `gera um PDF por NF, com todas as parcelas em aberto, numa consulta so`() {
        val r = mutableListOf<TermoCessaoPdfService.TermoPdf>()
        val comParcela = servico.gerar(listOf(10, 11, 10, 99), cessionaria) { r.add(it) }
        assertEquals(listOf(10, 11), r.map { it.docEntry })
        r.forEach { assertEquals("%PDF", String(it.pdf.getOrThrow().copyOfRange(0, 4))) }
        assertEquals(setOf(10, 11), comParcela)
        assertEquals(listOf(listOf(10, 11, 99)), consultas.map { it["docEntries"] })
        servico.gerar((1..250).toList(), cessionaria) { }
        assertEquals(listOf(100, 100, 50), consultas.drop(1).map { (it["docEntries"] as List<*>).size })
    }

    @Test
    fun `selecao restringe as parcelas da NF que esta nela, as outras vao com todas as em aberto`() {
        val r = mutableListOf<TermoCessaoPdfService.TermoPdf>()
        servico.gerar(listOf(10, 11), cessionaria, null, mapOf(10 to setOf(2))) { r.add(it) }
        assertEquals(mapOf(10 to listOf(2), 11 to listOf(1)), r.associate { it.docEntry to it.parcelas })
        r.clear()
        servico.gerar(listOf(10, 11), cessionaria) { r.add(it) }
        assertEquals(mapOf(10 to listOf(1, 2), 11 to listOf(1)), r.associate { it.docEntry to it.parcelas })
    }

    @Test
    fun `NF com so parcelas escolhidas sem saldo nao gera PDF`() {
        val r = mutableListOf<TermoCessaoPdfService.TermoPdf>()
        val comParcela = servico.gerar(listOf(10, 11), cessionaria, null, mapOf(10 to setOf(7))) { r.add(it) }
        assertEquals(listOf(11), r.map { it.docEntry })
        assertEquals(setOf(11), comParcela)
    }

    @Test
    fun `grupo que falha na consulta nao derruba os outros e o PDF sai antes da consulta do proximo grupo`() {
        var chamadas = 0
        val odbcFalha = mock(OdbcClient::class.java, Answer { inv ->
            if (inv.method.name != "consultar") return@Answer null
            if (++chamadas == 2) throw java.io.IOException("timeout")
            @Suppress("UNCHECKED_CAST")
            val p = inv.arguments[1] as Map<String, Any?>
            QueryResponse(rows = (p["docEntries"] as List<*>).take(1).map { parcela(it as Int, 1) })
        })
        val s = TermoCessaoPdfService(odbcFalha, servico.templateEngine)
        val geradas = mutableListOf<Int>()
        val falhas = mutableListOf<List<Int>>()
        val consultasAteOPrimeiroPdf = mutableListOf<Int>()
        s.gerar((1..150).toList(), cessionaria, null, mapOf(), { null }, { ids, _ -> falhas.add(ids) }) {
            if (geradas.isEmpty()) consultasAteOPrimeiroPdf.add(chamadas)
            geradas.add(it.docEntry)
        }
        assertEquals(listOf(1), geradas)
        assertEquals(listOf((101..150).toList()), falhas)
        assertEquals(listOf(1), consultasAteOPrimeiroPdf)
    }

    @Test
    fun `linha com DocEntry ou parcela invalidos falha so o grupo dela, e o grupo bom sai`() {
        var chamadas = 0
        val odbcRuim = mock(OdbcClient::class.java, Answer { inv ->
            if (inv.method.name != "consultar") return@Answer null
            @Suppress("UNCHECKED_CAST")
            val ids = (inv.arguments[1] as Map<String, Any?>)["docEntries"] as List<*>
            QueryResponse(rows = if (++chamadas == 2) listOf(parcela(101, 1), parcela(102, 1) + ("Parcela" to "abc"))
                else listOf(parcela(ids.first() as Int, 1)))
        })
        val geradas = mutableListOf<Int>()
        val falhas = mutableListOf<List<Int>>()
        TermoCessaoPdfService(odbcRuim, servico.templateEngine).gerar((1..150).toList(), cessionaria, null, mapOf(), { null },
            { ids, _ -> falhas.add(ids) }) { geradas.add(it.docEntry) }
        assertEquals(listOf(1), geradas)
        assertEquals(listOf((101..150).toList()), falhas)
    }

    @Test
    fun `prazo vencido antes da consulta - nenhuma consulta e todas as NF falham`() {
        val falhas = mutableListOf<List<Int>>()
        servico.gerar((1..150).toList(), cessionaria, null, mapOf(), { Exception("tempo esgotado") }, { ids, _ -> falhas.add(ids) }) { }
        assertTrue(consultas.isEmpty())
        assertEquals(listOf((1..150).toList()), falhas)
    }

    @Test
    fun `prazo que vence no meio do grupo - o que falta e as NF dos grupos seguintes falham`() {
        var checagens = 0
        val geradas = mutableListOf<Int>()
        val falhas = mutableListOf<List<Int>>()
        // passam a checagem do grupo 1 e a do 1o PDF; a do 2o PDF vence (sobram a NF 11 e o grupo 2)
        servico.gerar((10..11).toList() + (200..299).toList(), cessionaria, null, mapOf(), { if (++checagens > 2) Exception("tempo esgotado") else null },
            { ids, _ -> falhas.add(ids) }) { geradas.add(it.docEntry) }
        assertEquals(listOf(10), geradas)
        assertEquals(listOf(listOf(11, 298, 299)), falhas)
    }

    @Test
    fun `NF cuja filial tem CNPJ invalido no SAP falha so ela, com diagnostico controlado, e as outras saem`() {
        taxIdInvalido.add(10)
        val r = mutableListOf<TermoCessaoPdfService.TermoPdf>()
        servico.gerar(listOf(10, 11), cessionaria) { r.add(it) }
        val por = r.associateBy { it.docEntry }
        assertTrue(por.getValue(10).pdf.isFailure)
        assertEquals("CNPJ da filial invalido no SAP: nao deu para montar o CNPJ da matriz", por.getValue(10).pdf.exceptionOrNull()!!.message)
        assertTrue(por.getValue(11).pdf.isSuccess)
    }

    @Test
    fun `CNPJ impresso e sempre o da matriz (mesma raiz, 0001, digitos recalculados)`() {
        // filial /0005 -> matriz /0001; matriz inalterada; entrada invalida passa como veio
        assertEquals("12.345.678/0001-95", TermoCessaoPdfService.cnpjMatriz("12.345.678/0005-19"))
        assertEquals("12.345.678/0001-95", TermoCessaoPdfService.cnpjMatriz("12345678000519"))
        assertEquals("98.765.432/0001-98", TermoCessaoPdfService.cnpjMatriz("98.765.432/0001-98"))
        // CNPJ invalido na filial: nao imprime nada da filial, falha com diagnostico controlado
        listOf("12.345.678/0005-18", "123", "", "X12.345.678/0005-19", "12.345.678/0005-19x", "12.345.678/0005-19 (matriz)").forEach {
            val e = assertThrows(Exception::class.java) { TermoCessaoPdfService.cnpjMatriz(it) }
            assertEquals("CNPJ da filial invalido no SAP: nao deu para montar o CNPJ da matriz", e.message, it)
        }
        // no PDF: corpo e rodape trazem o da matriz, o nome da filial continua o da filial
        val filialDeFora = parcela(10, 1) + ("TaxIdNum" to "12.345.678/0005-19")
        val html = servico.html(listOf(filialDeFora), cessionaria)
        assertEquals(3, Regex("12\\.345\\.678/0001-95").findAll(html).count())  // rodape, corpo e assinatura
        assertFalse(html.contains("0005-19"))
        assertTrue(html.contains("Sustennutri Filial"))
    }

    @Test
    fun `telefone de contato e sempre o fixo, o da cessionaria e ignorado`() {
        listOf("(65) 3000-0000", null, "", "  ").forEach {
            val html = servico.html(listOf(parcela(10, 1)), CessionariaLote("1", "C", it))
            assertTrue(Regex("entre em contato pelo telefone\\s*<span>\\(69\\) 99956-8185</span>\\.").containsMatchIn(html), it)
            assertFalse(html.contains("3000-0000"), it)
            // CEP do rodape tambem e fixo: o ZipCode da filial (78000-000) nao aparece
            assertTrue(html.contains("CEP 76801-970"), it)
            assertFalse(html.contains("78000-000"), it)
        }
    }

    @Test
    fun `termo imprime o nome para o PDF e cai no nome quando a linha nao tem`() {
        val cheio = servico.html(listOf(parcela(10, 1)), CessionariaLote("1", "Curto", "1", "Razao Social Completa Ltda"))
        assertTrue(cheio.contains("Razao Social Completa Ltda"))
        assertFalse(cheio.contains("Curto"))
        listOf(null, "  ").forEach {
            assertTrue(servico.html(listOf(parcela(10, 1)), CessionariaLote("1", "Curto", "1", it)).contains("Curto"), it)
        }
        assertEquals("Securitizadora & Cia Ltda", servico.cessionaria("12345678000199").nomePdf)
    }

    @Test
    fun `cessionaria so vale se estiver na lista de ativas`() {
        assertEquals("Securitizadora & Cia", servico.cessionaria(" 12345678000199 ").nome)
        listOf(null, "", "  ", "Securitizadora & Cia", "999").forEach { informada ->
            val erro = assertThrows(Exception::class.java) { servico.cessionaria(informada) }
            assertEquals(Exception::class.java, erro.javaClass)
            assertTrue(erro.message!!.contains("essionaria"), erro.message)
        }
    }

    // Mesma regra do validador do sap-odbc: todo parametro do SQL no mapa e vice-versa.
    private fun parametros(sql: String) = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)")
        .findAll(sql.replace(Regex("'[^']*'"), "''").replace(Regex("\"[^\"]*\""), "\"\"")).map { it.groupValues[1] }.toSet()

    @Test
    fun `SQL das parcelas so le, so tem o parametro da lista e filtra parcela em aberto de NF nao cancelada`() {
        val sql = SqlResource.carregar("odbc/nota-fiscal-lote-termo-cessao.sql")
        assertEquals(setOf("docEntries"), parametros(sql))
        assertTrue(sql.trimStart().startsWith("SELECT"))
        assertFalse(sql.contains("--"))
        assertFalse(Regex("\\b(INSERT|UPDATE|DELETE|MERGE|DROP|ALTER|CREATE)\\b", RegexOption.IGNORE_CASE).containsMatchIn(sql))
        assertTrue(sql.contains("P.\"InsTotal\" - P.\"PaidToDate\" AS \"Saldo\""))
        listOf("FROM OINV N", "INNER JOIN INV6 P", "LEFT JOIN OBPL B", "N.\"CANCELED\" = 'N'", "N.\"DocStatus\" IN ('O','D')",
            "P.\"InsTotal\" - P.\"PaidToDate\" > ${NotaFiscalLoteImportacaoService.LIMIAR_EM_ABERTO.toPlainString()}")
            .forEach { assertTrue(sql.contains(it), it) }
    }

    @Test
    fun `SQL das cessionarias so le a tabela, so as ativas, sem parametros`() {
        val sql = SqlResource.carregar("odbc/nota-fiscal-lote-cessionarias.sql")
        assertEquals(setOf<String>(), parametros(sql))
        assertTrue(sql.trimStart().startsWith("SELECT"))
        assertFalse(sql.contains("--"))
        assertTrue(sql.contains("FROM \"@NFL_CESSIONARIA\""))
        assertTrue(sql.contains("WHERE \"U_Ativo\" = 'Y'"))
        listOf("\"Code\" AS \"Codigo\"", "\"Name\" AS \"Nome\"", "\"U_Cnpj\"", "\"U_Telefone\"", "\"U_NomePdf\" AS \"NomePdf\"").forEach { assertTrue(sql.contains(it), it) }
    }
}
