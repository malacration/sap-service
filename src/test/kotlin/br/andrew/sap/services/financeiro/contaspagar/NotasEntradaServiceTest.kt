package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.integracao.AttachmentService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

/** Dados ficticios: CNPJs, CardCodes, chaves e valores nao correspondem a registros reais. */
class NotasEntradaServiceTest {

    private val gateway = mock<ContasPagarSapGateway>()
    private val odbc = mock<OdbcClient>()
    private val attachments = mock<AttachmentService>()
    private val service = NotasEntradaService(gateway, odbc, attachments)

    private val filial = "12345678000190"

    private fun linha(docEntry: Int, bpl: Int = 7, modelo: String = "39", serial: Int = 100,
                      cnpj: String? = "98.765.432/0001-00", cpf: String? = "", anexos: Int = 1,
                      moeda: String? = "R$", moedaLocal: String? = "R$") = mapOf(
        "DocEntry" to docEntry, "DocNum" to docEntry + 1000, "Serial" to serial, "SeriesStr" to "1", "Model" to modelo,
        "U_ChaveAcesso" to " 11261198765432000100550010000001001000000001 ", "TaxDate" to "2026-11-10",
        "DocDate" to "2026-11-11", "BPLId" to bpl, "BPLName" to "Filial X", "CardCode" to "FOR1",
        "CardName" to "Fornecedor X", "CnpjFornecedor" to cnpj, "CpfFornecedor" to cpf, "DocTotal" to "1000.005",
        "DocCur" to moeda, "MainCurncy" to moedaLocal, "AtcEntry" to 55, "RetencaoPagamento" to "15", "RetencaoFatura" to "50.5", "AnexosPdf" to anexos,
    )

    private fun responde(vararg linhas: Map<String, Any?>) {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = linhas.toList(), rowCount = linhas.size))
    }

    @Test
    fun `lista por periodo com filiais do CNPJ e calcula bruto e liquido pelas retencoes`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7, 8))
        responde(linha(1), linha(2, modelo = "46"), linha(3, modelo = "37"))

        val notas = service.listar(ListagemNotasEntradaRequest("12.345.678/0001-90", "2026-11-01", "2026-11-30")).notas

        assertEquals(listOf(1, 2, 3), notas.map { it.docEntry })
        val n = notas.first()
        assertEquals(TipoNotaEntrada.NFE, n.tipo)
        assertEquals(TipoNotaEntrada.NFSE, notas[1].tipo)
        assertNull(notas[2].tipo)
        assertEquals("98765432000100", n.cpfCnpjFornecedor)
        assertEquals("11261198765432000100550010000001001000000001", n.chaveAcesso)
        assertEquals(BigDecimal("1000.01"), n.valorTotal)
        assertEquals(BigDecimal("1050.51"), n.valorBruto)
        assertEquals(BigDecimal("985.01"), n.valorLiquido)
        assertEquals(filial, n.cnpjFilial)
        assertEquals(100L, n.numero)

        val params = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(eq(SqlResource.carregar(NotasEntradaService.SQL_PERIODO)), params.capture(), eq(2000))
        assertEquals(mapOf("filiais" to listOf(7, 8), "dataInicial" to "2026-11-01", "dataFinal" to "2026-11-30",
            "fornecedor" to "", "fornecedorPadrao" to "", "numero" to -1L), params.firstValue)
    }

    @Test
    fun `filtra fornecedor por CNPJ ou CPF e numero sem zeros a esquerda`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde(linha(1, serial = 100), linha(2, serial = 200), linha(3, cnpj = "", cpf = "123.456.789-01"))

        assertEquals(listOf(2), service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02",
            cpfCnpjFornecedor = "98765432000100", numeroNota = "000200")).notas.map { it.docEntry })
        assertEquals(listOf(3), service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02",
            cpfCnpjFornecedor = "123.456.789-01")).notas.map { it.docEntry })
    }

    @Test
    fun `fornecedor e numero vao no SQL antes do limite de linhas`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        // Periodo com mais de MAX_NOTAS notas: sem filtro o sap-odbc trunca; com filtro devolve so a nota.
        whenever(odbc.consultar(any(), any(), any())).thenAnswer { inv ->
            val p = inv.getArgument<Map<String, Any?>>(1)
            if (p["fornecedor"] == "" && p["numero"] == -1L) throw OdbcException("resultado_truncado", "truncado")
            QueryResponse(rows = listOf(linha(9, serial = 4321)), rowCount = 1)
        }

        assertThrows<OdbcException> { service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-30")) }
        assertEquals(listOf(9), service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-30",
            numeroNota = "0004321")).notas.map { it.docEntry })
        assertEquals(listOf(9), service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-30",
            cpfCnpjFornecedor = "98.765.432/0001-00")).notas.map { it.docEntry })

        val params = argumentCaptor<Map<String, Any?>>()
        verify(odbc, org.mockito.kotlin.times(3)).consultar(any(), params.capture(), eq(2000))
        assertEquals(4321L, params.allValues[1]["numero"])
        assertEquals("", params.allValues[1]["fornecedor"])
        assertEquals("98765432000100", params.allValues[2]["fornecedor"])
        assertEquals(-1L, params.allValues[2]["numero"])

        assertEquals("%9%8%7%6%5%4%3%2%0%0%0%1%0%0%", params.allValues[2]["fornecedorPadrao"])

        val sql = SqlResource.carregar(NotasEntradaService.SQL_PERIODO)
        assertTrue(sql.contains("AND (:numero = -1 OR N.\"Serial\" = :numero)"))
        assertFalse(sql.contains("REPLACE_REGEXPR"), "o parser do sap-odbc nao aceita")
    }

    /**
     * O filtro de fornecedor do SQL reproduz a regra do modelo (CNPJ de TaxId0 com 14 digitos; senao CPF de
     * TaxId4 com 11), comparando so digitos: contagem de digitos + LIKE com os digitos em ordem.
     * Conferido contra o HANA (sap-odbc local) em 2026-11-27: mesmas notas que o filtro do modelo.
     */
    @Test
    fun `filtro de fornecedor no SQL compara so digitos com prioridade do CNPJ`() {
        val sql = SqlResource.carregar(NotasEntradaService.SQL_PERIODO)
        fun digitos(coluna: String) = "(10 * LENGTH(IFNULL(T.\"$coluna\", ''))\n" +
            (0..9).joinToString("\n") { "            - LENGTH(REPLACE(IFNULL(T.\"$coluna\", ''), '$it', ''))" } + ")"
        assertTrue(sql.contains("""
            |       OR (LENGTH(:fornecedor) = 14
            |           AND ${digitos("TaxId0")} = 14
            |           AND IFNULL(T."TaxId0", '') LIKE :fornecedorPadrao)
            |       OR (LENGTH(:fornecedor) = 11
            |           AND ${digitos("TaxId0")} <> 14
            |           AND ${digitos("TaxId4")} = 11
            |           AND IFNULL(T."TaxId4", '') LIKE :fornecedorPadrao))""".trimMargin()), sql)

        // Mascara qualquer (ex.: parenteses) e irrelevante: o padrao e so dos digitos em ordem.
        val p = NotasEntradaService.parametrosPeriodo(listOf(1), LocalDate.now(), LocalDate.now(), "12345678901", null)
        assertEquals("12345678901", p["fornecedor"])
        assertEquals("%1%2%3%4%5%6%7%8%9%0%1%", p["fornecedorPadrao"])
        assertTrue(Regex((p["fornecedorPadrao"] as String).replace("%", ".*")).matches("(123) 456.789-01"))
        assertEquals("", NotasEntradaService.parametrosPeriodo(listOf(1), LocalDate.now(), LocalDate.now())["fornecedorPadrao"])
    }

    @Test
    fun `digito nao ASCII nao conta no documento, como no SQL`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        // '\u0661' (1 arabe-indico): Char.isDigit aceitaria e o CNPJ teria 14 digitos; o SQL conta so 0-9.
        responde(linha(1, cnpj = "98.765.432/0001-0\u0661", cpf = "123.456.789-01"), linha(2))

        val notas = service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02")).notas
        assertEquals("12345678901", notas.first { it.docEntry == 1 }.cpfCnpjFornecedor, "CNPJ invalido cai no CPF")
        assertEquals("98765432000100", notas.first { it.docEntry == 2 }.cpfCnpjFornecedor)

        // Filtro com digito nao ASCII: so os ASCII valem (13 digitos -> invalido).
        assertThrows<ConsultaInvalidaException> {
            service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02", cpfCnpjFornecedor = "9876543200010\u0661"))
        }
        assertEquals("%1%2%", NotasEntradaService.parametrosPeriodo(listOf(1), LocalDate.now(), LocalDate.now(), "1\u06612", null)["fornecedorPadrao"])
    }

    @Test
    fun `filtro informado sem digito ASCII valido e recusado, nao vira todos`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde(linha(1))
        for (fornecedor in listOf("\u0661\u0662\u0663\u0664\u0665\u0666\u0667\u0668\u0669\u0660\u0661", "abc", "./-"))
            assertThrows<ConsultaInvalidaException>(fornecedor) {
                service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02", cpfCnpjFornecedor = fornecedor))
            }
        for (numero in listOf("\u0661\u0662", "-1", "+5", "1 2", "1".repeat(20)))
            assertThrows<ConsultaInvalidaException>(numero) {
                service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02", numeroNota = numero))
            }
        verify(odbc, never()).consultar(any(), any(), any())

        // Ausente ou em branco continua sem filtro.
        assertEquals(listOf(1), service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02",
            cpfCnpjFornecedor = "  ", numeroNota = " ")).notas.map { it.docEntry })
    }

    @Test
    fun `nota fora da moeda local vem com problema e sem valores`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde(linha(1), linha(2, moeda = "USD"), linha(3, moeda = null), linha(4, moedaLocal = null))

        val notas = service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02")).notas.associateBy { it.docEntry }

        assertNull(notas.getValue(1).problema)
        assertEquals(BigDecimal("1000.01"), notas.getValue(1).valorTotal)
        assertEquals("R$", notas.getValue(1).moedaLocal)
        assertEquals(BigDecimal("50.50"), notas.getValue(1).retencaoFatura)
        assertEquals(BigDecimal("15.00"), notas.getValue(1).retencaoPagamento)
        assertEquals("Nota de entrada em moeda estrangeira (USD); so a moeda local e suportada", notas.getValue(2).problema)
        for (n in listOf(2, 3, 4).map(notas::getValue)) {
            assertTrue(n.problema != null)
            assertNull(n.valorTotal); assertNull(n.valorBruto); assertNull(n.valorLiquido)
            assertNull(n.retencaoFatura); assertNull(n.retencaoPagamento)
        }
        assertEquals("Moeda da nota de entrada desconhecida no SAP", notas.getValue(3).problema)

        for (arquivo in listOf(NotasEntradaService.SQL_PERIODO, NotasEntradaService.SQL_DOCENTRIES)) {
            val sql = SqlResource.carregar(arquivo)
            assertTrue(sql.contains("E.\"MainCurncy\" AS \"MainCurncy\""), arquivo)
            assertTrue(sql.contains("CROSS JOIN OADM E"), arquivo)
        }
    }

    @Test
    fun `por DocEntry ignora periodo e descarta nota de outra filial`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde(linha(1, bpl = 7), linha(2, bpl = 9))

        val notas = service.listar(ListagemNotasEntradaRequest(filial, docEntries = listOf(1, 2, 2))).notas

        assertEquals(listOf(1), notas.map { it.docEntry })
        verify(odbc).consultar(eq(SqlResource.carregar(NotasEntradaService.SQL_DOCENTRIES)),
            eq(mapOf("docEntries" to listOf(1, 2))), eq(2000))
    }

    @Test
    fun `validacoes da requisicao`() {
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest("123", "2026-11-01", "2026-11-02")) }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", null)) }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, "01/11/2026", "2026-11-02")) }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, "2026-11-10", "2026-11-01")) }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, "2026-10-01", "2026-11-01")) }
        assertThrows<ConsultaInvalidaException> {
            service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02", cpfCnpjFornecedor = "123"))
        }
        assertThrows<ConsultaInvalidaException> {
            service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02", numeroNota = "12A"))
        }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, docEntries = emptyList())) }
        assertThrows<ConsultaInvalidaException> { service.listar(ListagemNotasEntradaRequest(filial, docEntries = listOf(0))) }
        verify(odbc, never()).consultar(any(), any(), any())
    }

    @Test
    fun `periodo de 31 dias e o maximo aceito`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde()
        val inicio = LocalDate.parse("2026-11-01")
        assertTrue(service.listar(ListagemNotasEntradaRequest(filial, "$inicio", "${inicio.plusDays(30)}")).notas.isEmpty())
    }

    @Test
    fun `filial inexistente e nao encontrada`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(emptyList())
        assertThrows<NotaEntradaNaoEncontradaException> {
            service.listar(ListagemNotasEntradaRequest(filial, "2026-11-01", "2026-11-02"))
        }
    }

    @Test
    fun `anexo PDF baixa o arquivo pelo nome do ATC1 e exige conteudo PDF`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        whenever(odbc.consultar(eq(SqlResource.carregar(NotasEntradaService.SQL_DOCENTRIES)), any(), any()))
            .thenReturn(QueryResponse(rows = listOf(linha(1)), rowCount = 1))
        whenever(odbc.consultar(eq(SqlResource.carregar(NotasEntradaService.SQL_ANEXOS)), any(), any()))
            .thenReturn(QueryResponse(rows = listOf(mapOf("AbsEntry" to 55, "Line" to 2, "FileName" to "NFe123", "FileExt" to "pdf")), rowCount = 1))
        val pdf = "%PDF-1.4 conteudo".toByteArray()
        whenever(attachments.downloadArquivo(55, "NFe123.pdf")).thenReturn("application/octet-stream" to pdf)

        val (nome, bytes) = service.anexoPdf(1, filial)

        assertEquals("NFe123.pdf", nome)
        assertArrayEquals(pdf, bytes)

        whenever(attachments.downloadArquivo(55, "NFe123.pdf")).thenReturn("application/json" to "{\"error\":1}".toByteArray())
        assertThrows<NotaEntradaNaoEncontradaException> { service.anexoPdf(1, filial) }

        whenever(attachments.downloadArquivo(55, "NFe123.pdf")).thenThrow(
            org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.NOT_FOUND))
        assertThrows<AnexoIndisponivelException> { service.anexoPdf(1, filial) }
    }

    @Test
    fun `anexo PDF de nota sem PDF ou de outra filial e nao encontrado sem chamar o Service Layer`() {
        whenever(gateway.filiaisPorCnpj(filial)).thenReturn(listOf(7))
        responde(linha(1, anexos = 0))
        assertThrows<NotaEntradaNaoEncontradaException> { service.anexoPdf(1, filial) }
        responde(linha(1, bpl = 9))
        assertThrows<NotaEntradaNaoEncontradaException> { service.anexoPdf(1, filial) }
        verify(attachments, never()).downloadArquivo(any(), any())
    }

    @Test
    fun `SQL somente SELECT sem comentario e com exatamente os parametros enviados`() {
        fun semAspas(t: String) = t.replace(Regex("'[^']*'"), "''").replace(Regex("\"[^\"]*\""), "\"\"")
        fun parametros(t: String) = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(semAspas(t)).map { it.groupValues[1] }.toSet()
        val periodo = SqlResource.carregar(NotasEntradaService.SQL_PERIODO)
        val docEntries = SqlResource.carregar(NotasEntradaService.SQL_DOCENTRIES)
        val anexos = SqlResource.carregar(NotasEntradaService.SQL_ANEXOS)
        for (sql in listOf(periodo, docEntries, anexos)) {
            assertTrue(sql.startsWith("SELECT"))
            assertFalse(sql.contains("--") || sql.contains("/*") || sql.contains(";"))
            assertTrue(sql.contains("\"CANCELED\" = 'N'"))
        }
        assertEquals(parametros(periodo),
            NotasEntradaService.parametrosPeriodo(listOf(1), LocalDate.now(), LocalDate.now()).keys)
        assertEquals(parametros(docEntries), NotasEntradaService.parametrosDocEntries(listOf(1)).keys)
        assertEquals(parametros(anexos), NotasEntradaService.parametrosAnexos(1).keys)
    }
}
