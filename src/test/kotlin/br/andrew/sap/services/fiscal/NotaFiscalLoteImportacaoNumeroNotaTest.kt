package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.CriterioImportacaoNumeroNota
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.AMBIGUO
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.DIVERGENTE
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.ENCONTRADO
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.NAO_ENCONTRADO
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.PAGA
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import java.nio.charset.Charset

// CSV sintetico no layout do Sicoob (nomes e numeros ficticios): linhas de metadados antes do
// cabecalho, cabecalho repetido a cada pagina, ultima pagina com uma coluna a mais, rodape.
class NotaFiscalLoteImportacaoNumeroNotaTest {

    private val candidatas = mutableListOf<Map<String, Any?>>()
    private val sqls = mutableListOf<String>()
    private var paramsListagem: Map<String, Any?>? = null
    private var paramsNumeros = mutableListOf<List<*>>()
    private val maxRowsNumeros = mutableListOf<Int>()

    private fun nota(docEntry: Int, numero: Int, filial: Int, serie: String, vararg parcelas: Pair<Int, String>, nome: String = "CLIENTE GENERICO") {
        // parcela to "valor|vencimento"
        parcelas.forEach { (p, vv) ->
            candidatas.add(mapOf("DocEntry" to docEntry, "Numero" to numero, "Serie" to serie, "BPLId" to filial,
                "BPLName" to "Filial $filial", "CardName" to nome, "Parcela" to p, "ValorParcela" to vv.split('|')[0],
                "VencimentoParcela" to vv.split('|').getOrElse(1) { "2099-01-01" }, "PagoParcela" to vv.split('|').getOrElse(2) { "0" }))
        }
    }

    private val odbc = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        val sql = inv.arguments[0] as String
        @Suppress("UNCHECKED_CAST")
        val p = inv.arguments[1] as Map<String, Any?>
        when {
            sql.contains(":nossosNumeros") -> { sqls.add("boletos"); QueryResponse(rows = listOf()) }
            sql.contains(":numeros") -> {
                sqls.add("numeros"); paramsNumeros.add(p["numeros"] as List<*>)
                val maxRows = inv.arguments[2] as Int
                maxRowsNumeros.add(maxRows)
                check(maxRows <= 10_000) { "maxRows $maxRows acima do teto do sap-odbc" }
                val rows = candidatas.filter { it["Numero"] in (p["numeros"] as List<*>) }
                // como o OdbcClient real: resultado maior que maxRows = erro "resultado_truncado"
                if (rows.size > maxRows) throw OdbcException("resultado_truncado", "truncado")
                QueryResponse(rows = rows)
            }
            else -> {
                sqls.add("listagem"); if (paramsListagem == null) paramsListagem = p
                QueryResponse(rows = candidatas.map { it["DocEntry"] as Int }.distinct()
                    .filter { it in (p["docEntries"] as List<*>) }
                    .map { mapOf("Tipo" to "NF", "DocEntry" to it, "Numero" to 1, "Parcela" to 1, "Linha" to 1, "Total" to 1) })
            }
        }
    })

    private val lote = NotaFiscalLoteService(odbc, mock(TaxNfeService::class.java), mock(BankPlusService::class.java), 200, 2)

    // COMPLETO explicito: o padrao agora e NUMERO_NOME (testado a parte).
    private val servico = NotaFiscalLoteImportacaoService(odbc, lote, criterio = CriterioImportacaoNumeroNota.COMPLETO)

    private val cabecalho = "Sacado,,,,,Nosso Número,,,Seu Número,,,Entrada,Vencimento,,Dt. Limite Pgto,,Valor (R$),"
    private val cabecalhoUltima = "Sacado,,,,,Nosso Número,,,Seu Número,,,Entrada,,Vencimento,,Dt. Limite Pgto,,Valor (R$),"

    private fun csv(): String = listOf(
        ",,,,Banco X - Títulos descontados,,,,,,,,,,,,,",
        "Cedente:,,1000 - EMPRESA FICTICIA LTDA,,,,,,,,,,,,,,,",
        "Tipo Operação:,,Entrada,Data Inicial:,,,,01/10/2026,,,Data Final:,,,02/03/2027,,,,",
        cabecalho,
        "CLIENTE UM DE TESTE    ,,,,,3-8,,,3956 1         ,,,07/10/2026,16/11/2026,,,,\"27.750,00\",",
        "CLIENTE DOIS,,,,,3-9,,,0005647-2,,,08/10/2026,01/01/2027,,15/01/2027,,\"51.836,09\",",
        "CLIENTE TRES,,,,,4-0,,,0000830-02,,,08/10/2026,21/11/2026,,30/11/2026,,\"6.500,01\",",
        "CLIENTE QUATRO,,,,,4-1,,,0005521,,,08/10/2026,15/12/2026,,,,\"1.000,00\",",
        "CLIENTE CINCO,,,,,4-2,,,9999 1,,,08/10/2026,30/11/2026,,,,\"10,00\",",
        ",,,,,,,,,,,,Total de Registros:,,,,,5,",
        "Gerado em:,24/11/2026 09:39:57,,,,,Usuário:,,,fulano,,,,,,,Página 1,, de 2",
        "Cedente:,,1000 - EMPRESA FICTICIA LTDA,,,,,,,,,,,,,,,",
        cabecalhoUltima,
        "CLIENTE SEIS,,,,,4-3,,,4879 3         ,,,20/10/2026,,07/12/2026,,,,\"23.639,97\",",
        "CLIENTE SETE,,,,,4-4,,,7777 1,,,20/10/2026,,07/12/2026,,,,\"5,00\",",
        "CLIENTE OITO,,,,,4-5,,,abc,,,20/10/2026,,07/12/2026,,,,\"5,00\",",
        ",,,,,,,,,,,,Total de Registros:,,,,,,3",
        "Gerado em:,24/11/2026 09:39:57,,,,,,,,,,,,,,,Página 2,, de 2",
    ).joinToString("\r\n")

    private fun cenario() {
        nota(1001, 3956, 5, "1", 1 to "27750.00|2026-11-16", 2 to "27750.00|2026-12-16")
        nota(1002, 5647, 5, "1", 1 to "51836.09|2026-12-01", 2 to "51000.00|2027-01-01")
        nota(1003, 830, 5, "1", 2 to "6500.01|2026-11-21")
        nota(1004, 830, 11, "2", 2 to "6500.01|2026-11-21")
        nota(1005, 5521, 5, "1", 1 to "1000.00|2026-12-15")
        nota(1006, 4879, 5, "1", 1 to "23639.97", 2 to "23639.97")
        nota(1007, 7777, 5, "1", 1 to "5.00")
    }

    // Sem criterio: usa o padrao da propriedade (NUMERO_NOME).
    private val servicoNome = NotaFiscalLoteImportacaoService(odbc, lote)

    private fun importaNome(bytes: ByteArray) = servicoNome.importarPorNumeroNota(bytes)

    private fun importa(bytes: ByteArray = csv().toByteArray()) = servico.importarPorNumeroNota(bytes)

    @Test
    fun `csv com cabecalho repetido, ultima pagina deslocada e rodape vira uma linha por titulo`() {
        cenario()
        val r = importa()
        assertEquals(listOf(5, 6, 7, 8, 9, 14, 15, 16), r.linhas.map { it.linha })
        assertEquals(listOf(ENCONTRADO, DIVERGENTE, AMBIGUO, ENCONTRADO, NAO_ENCONTRADO, NAO_ENCONTRADO, DIVERGENTE, NAO_ENCONTRADO),
            r.linhas.map { it.situacao })
    }

    @Test
    fun `resultado por situacao, so entra no lote o que casou com uma nota`() {
        cenario()
        val r = importa()
        val l = r.linhas
        assertEquals(1001, l[0].docEntry); assertEquals(1, l[0].parcela)
        assertEquals("2026-11-16", l[0].vencimento)
        // valor diferente da parcela 2 da NF 5647
        assertEquals(1002, l[1].docEntry); assertTrue(l[1].mensagem!!.contains("valor na planilha 51836.09, na parcela 51000.00"))
        // mesmo numero e parcela em duas filiais dentro do filtro: nao entra e nao expoe cliente
        assertNull(l[2].docEntry)
        assertTrue(l[2].mensagem!!.contains("filial 5 Filial 5 serie 1") && l[2].mensagem!!.contains("filial 11 Filial 11 serie 2"))
        assertFalse(l[2].mensagem!!.contains("CLIENTE"))
        // sem parcela no "Seu Numero": parcela 1
        assertEquals(1005, l[3].docEntry); assertEquals(1, l[3].parcela)
        assertNull(l[4].docEntry)
        // parcela 3 que a NF nao tem
        assertNull(l[5].docEntry); assertEquals("NF 4879 nao tem a parcela 3", l[5].mensagem)
        assertEquals(1007, l[6].docEntry); assertTrue(l[6].mensagem!!.contains("vencimento na planilha 07/12/2026, na parcela 01/01/2099"), l[6].mensagem)
        assertEquals("linha 16: numero da nota ilegivel", l[7].mensagem)
        assertEquals(setOf(1001, 1002, 1005, 1007), r.documentos.map { it.docEntry }.toSet())
    }

    private fun csvSimples(vararg titulos: String) = (listOf("Sacado,,Nosso Número,Seu Número,Vencimento,Valor (R\$)") +
        titulos.map { t -> t.split('|').let { "FULANO,,3-8,${it[0]},${it[1]},\"${it[2]}\"" } }).joinToString("\n").toByteArray()

    // titulo = "pagador|seu numero|vencimento|valor"
    private fun csvNome(vararg titulos: String) = (listOf("Sacado,,Nosso Número,Seu Número,Vencimento,Valor (R\$)") +
        titulos.map { t -> t.split('|').let { "${it[0]},,3-8,${it[1]},${it[2]},\"${it[3]}\"" } }).joinToString("\n").toByteArray()

    private fun duasFiliais(valorB: String = "500.00", vencB: String = "2026-12-10") {
        nota(2001, 100, 6, "1", 1 to "500.00|2026-11-10")
        nota(2002, 100, 18, "1", 1 to "$valorB|$vencB")
    }

    @Test
    fun `desempata por valor e vencimento quando so uma nota bate`() {
        duasFiliais()
        val l = importa(csvSimples("100 1|10/12/2026|500,00")).linhas.single()
        assertEquals(ENCONTRADO, l.situacao); assertEquals(2002, l.docEntry); assertEquals(1, l.parcela)
        val r = importa(csvSimples("100 1|10/11/2026|500,00"))
        assertEquals(2001, r.linhas.single().docEntry)
        assertEquals(listOf(2001), r.documentos.map { it.docEntry })
    }

    @Test
    fun `nenhuma nota bate com valor e vencimento continua ambiguo e diz o que tentou`() {
        duasFiliais()
        val l = importa(csvSimples("100 1|15/12/2026|500,00")).linhas.single()
        assertEquals(AMBIGUO, l.situacao); assertNull(l.docEntry)
        assertTrue(l.mensagem!!.contains("2 notas com a NF 100 parcela 1") && l.mensagem!!.contains("nenhuma bate com valor e vencimento"))
        assertTrue(l.mensagem!!.contains("filial 6 Filial 6 serie 1") && l.mensagem!!.contains("filial 18 Filial 18 serie 1"))
        assertFalse(l.mensagem!!.contains("FULANO"))
        // vencimento igual escolhe a nota (so uma tem 10/11) mesmo sem bater no valor
        val porVenc = importa(csvSimples("100 1|10/11/2026|501,00")).linhas.single()
        assertEquals(DIVERGENTE, porVenc.situacao); assertEquals(2001, porVenc.docEntry)
        assertTrue(porVenc.mensagem!!.endsWith("escolhida pelo vencimento"), porVenc.mensagem)
    }

    @Test
    fun `mais de uma nota bate com valor e vencimento continua ambiguo`() {
        duasFiliais(vencB = "2026-11-10")
        val l = importa(csvSimples("100 1|10/11/2026|500,00")).linhas.single()
        assertEquals(AMBIGUO, l.situacao); assertNull(l.docEntry)
        assertTrue(l.mensagem!!.contains("mais de uma bate com valor e vencimento"))
    }

    @Test
    fun `valor ou vencimento ilegivel nao desempata`() {
        duasFiliais()
        val semValor = importa(csvSimples("100 1|10/12/2026|abc")).linhas.single()
        // valor ilegivel mas vencimento legivel: so uma nota tem esse vencimento, escolhe por ele
        assertEquals(DIVERGENTE, semValor.situacao); assertEquals(2002, semValor.docEntry)
        assertEquals("valor ilegivel: nao conferido; escolhida pelo vencimento", semValor.mensagem)
        val semData = importa(csvSimples("100 1|sem data|500,00")).linhas.single()
        assertEquals(AMBIGUO, semData.situacao)
        assertTrue(semData.mensagem!!.contains("vencimento da planilha ilegivel: nao deu para desempatar"))
        assertTrue(importa(csvSimples("100 1|x|y")).linhas.single().mensagem!!.contains("valor e vencimento da planilha ilegivel"))
    }

    @Test
    fun `nota unica compara tambem o vencimento e diz qual campo diverge`() {
        nota(3001, 200, 6, "1", 1 to "500.00|2026-11-10")
        assertEquals(ENCONTRADO, importa(csvSimples("200 1|10/11/2026|500,00")).linhas.single().situacao)
        val v = importa(csvSimples("200 1|10/12/2026|500,00")).linhas.single()
        assertEquals(DIVERGENTE, v.situacao); assertEquals(3001, v.docEntry)
        assertEquals("vencimento na planilha 10/12/2026, na parcela 10/11/2026", v.mensagem)
        val dois = importa(csvSimples("200 1|10/12/2026|499,90")).linhas.single()
        assertEquals("valor na planilha 499.90, na parcela 500.00; vencimento na planilha 10/12/2026, na parcela 10/11/2026", dois.mensagem)
    }

    @Test
    fun `tolerancia de 5 centavos no valor - 0,05 casa, 0,06 diverge, nos dois sentidos`() {
        nota(3001, 200, 6, "1", 1 to "500.00|2026-11-10")
        listOf("500,05", "499,95", "500,00").forEach {
            assertEquals(ENCONTRADO, importa(csvSimples("200 1|10/11/2026|$it")).linhas.single().situacao, it)
        }
        listOf("500,06", "499,94").forEach {
            val l = importa(csvSimples("200 1|10/11/2026|$it")).linhas.single()
            assertEquals(DIVERGENTE, l.situacao, it); assertTrue(l.mensagem!!.startsWith("valor na planilha"), it)
        }
    }

    @Test
    fun `desempate aceita candidata dentro de 5 centavos e recusa fora`() {
        nota(2001, 100, 6, "1", 1 to "500.00|2026-11-10")
        nota(2002, 100, 18, "1", 1 to "520.00|2026-11-10")
        val dentro = importa(csvSimples("100 1|10/11/2026|500,05")).linhas.single()
        assertEquals(ENCONTRADO, dentro.situacao); assertEquals(2001, dentro.docEntry)
        assertEquals(AMBIGUO, importa(csvSimples("100 1|10/11/2026|500,06")).linhas.single().situacao)
    }

    @Test
    fun `sem parcela e duas NF de mesmo numero em filiais diferentes - resolve pela que bate com valor e vencimento`() {
        nota(4001, 870, 6, "1", 1 to "2667.96|2026-12-06")
        nota(4002, 870, 18, "2", 1 to "4597.97|2026-11-07")
        val r = importa(csvSimples("0000870|07/11/2026|4.597,97"))
        val l = r.linhas.single()
        assertEquals(ENCONTRADO, l.situacao); assertEquals(4002, l.docEntry); assertEquals(1, l.parcela)
        assertEquals(listOf(4002), r.documentos.map { it.docEntry })
        assertEquals(4002, importa(csvSimples("0000870-1|07/11/2026|4.597,97")).linhas.single().docEntry)
        assertEquals(4001, importa(csvSimples("0000870|06/12/2026|2.667,96")).linhas.single().docEntry)
    }

    @Test
    fun `sem parcela escolhe a parcela por valor e vencimento`() {
        nota(4101, 5343, 6, "1", 1 to "3289.67|2026-09-27", 2 to "3290.66|2026-11-26")
        val l = importa(csvSimples("0005343|26/11/2026|3.290,66")).linhas.single()
        assertEquals(ENCONTRADO, l.situacao); assertEquals(4101, l.docEntry); assertEquals(2, l.parcela)
        assertEquals(2, importa(csvSimples("0005343|26/11/2026|3.290,70")).linhas.single().parcela)
        assertEquals(1, importa(csvSimples("0005343|27/09/2026|3.289,67")).linhas.single().parcela)
    }

    @Test
    fun `sem parcela e nenhuma bate - AMBIGUO listando as parcelas, nunca DIVERGENTE com a NF inteira`() {
        nota(4101, 5343, 6, "1", 1 to "3289.67|2026-09-27", 2 to "3290.66|2026-11-26")
        val l = importa(csvSimples("0005343|15/01/2027|100,00")).linhas.single()
        assertEquals(AMBIGUO, l.situacao); assertNull(l.docEntry); assertNull(l.parcela)
        assertTrue(l.mensagem!!.contains("nenhuma parcela bate com valor e vencimento"))
        assertTrue(l.mensagem!!.contains("parcela 1 (3289.67 em 27/09/2026), parcela 2 (3290.66 em 26/11/2026)"), l.mensagem)
        assertTrue(importa(csvSimples("0005343|x|y")).linhas.single().mensagem!!.contains("ilegivel: nao deu para escolher a parcela"))
    }

    @Test
    fun `sem parcela, nota com uma parcela so e comparada e diz o que difere`() {
        nota(4301, 900, 6, "1", 1 to "500.00|2026-11-10")
        val l = importa(csvSimples("0000900|10/11/2026|600,00")).linhas.single()
        assertEquals(DIVERGENTE, l.situacao); assertEquals(1, l.parcela)
        assertEquals("valor na planilha 600.00, na parcela 500.00", l.mensagem)
    }

    @Test
    fun `sem parcela e duas notas sem parcela que bata - AMBIGUO listando as parcelas`() {
        nota(4401, 910, 6, "1", 1 to "500.00|2026-11-10")
        nota(4402, 910, 18, "1", 1 to "700.00|2026-11-12", 2 to "700.00|2026-12-12")
        val l = importa(csvSimples("0000910|01/01/2027|1,00")).linhas.single()
        assertEquals(AMBIGUO, l.situacao); assertNull(l.docEntry)
        assertTrue(l.mensagem!!.contains("filial 6 Filial 6 serie 1: parcela 1") && l.mensagem!!.contains("filial 18 Filial 18 serie 1: parcela 1"), l.mensagem)
    }

    @Test
    fun `parcela informada com valor diferente continua DIVERGENTE, e a nota que bate vence a que so tem o numero`() {
        nota(4201, 818, 6, "1", 3 to "3506.94|2026-12-10")
        val l = importa(csvSimples("0000818-003|10/12/2026|3.505,90")).linhas.single()
        assertEquals(DIVERGENTE, l.situacao); assertEquals(4201, l.docEntry); assertEquals(3, l.parcela)
        assertEquals("valor na planilha 3505.90, na parcela 3506.94", l.mensagem)
        nota(4202, 818, 18, "2", 3 to "3505.90|2026-12-10")
        assertEquals(4202, importa(csvSimples("0000818-003|10/12/2026|3.505,90")).linhas.single().docEntry)
    }

    @Test
    fun `varias notas e parcelas que batem com valor e vencimento - AMBIGUO`() {
        nota(4203, 819, 6, "1", 1 to "10.00|2026-12-10")
        nota(4204, 819, 18, "2", 1 to "10.00|2026-12-10")
        val l = importa(csvSimples("0000819|10/12/2026|10,00")).linhas.single()
        assertEquals(AMBIGUO, l.situacao); assertNull(l.docEntry)
        assertTrue(l.mensagem!!.contains("filial 6 Filial 6 serie 1") && l.mensagem!!.contains("filial 18 Filial 18 serie 2"), l.mensagem)
    }

    @Test
    fun `112 linhas = 2 consultas de numeros (100 + 12) e uma de documentos, sem N+1, maxRows dentro do teto`() {
        val linhas = (1..112).map { n -> nota(5000 + n, n, 6, "1", 1 to "10.00|2026-12-10"); "$n 1|10/12/2026|10,00" }
        val r = importa(csvSimples(*linhas.toTypedArray()))
        assertEquals(112, r.linhas.count { it.situacao == ENCONTRADO })
        assertEquals(listOf("numeros", "numeros", "listagem"), sqls)
        assertEquals(listOf(100, 12), paramsNumeros.map { it.size })
        assertTrue(maxRowsNumeros.all { it <= 10_000 })
        assertEquals(112, r.documentos.size)
    }

    @Test
    fun `101 e 200 numeros distintos (teto da importacao) - lotes de 100, maxRows sempre no teto`() {
        (1..200).forEach { n -> nota(9000 + n, n, 6, "1", 1 to "10.00|2026-12-10") }
        importa(csvSimples(*(1..101).map { "$it 1|10/12/2026|10,00" }.toTypedArray()))
        assertEquals(listOf(100, 1), paramsNumeros.map { it.size })
        assertTrue(maxRowsNumeros.all { it <= 10_000 }, maxRowsNumeros.toString())
        sqls.clear(); paramsNumeros.clear(); maxRowsNumeros.clear()
        importa(csvSimples(*(1..200).map { "$it 1|10/12/2026|10,00" }.toTypedArray()))
        assertEquals(2, sqls.count { it == "numeros" })
        assertTrue(maxRowsNumeros.all { it <= 10_000 })
    }

    @Test
    fun `uma NF com 120 parcelas funciona, e resultado truncado divide o grupo ate caber`() {
        // um serial com 120 linhas junto de outros 99 numeros: cabe em uma ida
        nota(9901, 50, 6, "1", *(1..120).map { it to "1.00|2026-12-10" }.toTypedArray())
        (1..100).filter { it != 50 }.forEach { nota(9000 + it, it, 6, "1", 1 to "10.00|2026-12-10") }
        val r = importa(csvSimples(*(1..100).map { if (it == 50) "50 120|10/12/2026|1,00" else "$it 1|10/12/2026|10,00" }.toTypedArray()))
        assertEquals(100, r.linhas.count { it.situacao == ENCONTRADO })
        assertEquals(1, sqls.count { it == "numeros" })
        // 100 numeros x 120 parcelas = 12.000 linhas > 10.000: o grupo trunca, divide em 50+50 (6.000 cada) e reconsulta
        sqls.clear(); paramsNumeros.clear(); maxRowsNumeros.clear(); candidatas.clear()
        (1..100).forEach { nota(7000 + it, it, 6, "1", *(1..120).map { p -> p to "1.00|2026-12-10" }.toTypedArray()) }
        val grande = importa(csvSimples(*(1..100).map { "$it 7|10/12/2026|1,00" }.toTypedArray()))
        assertEquals(100, grande.linhas.count { it.situacao == ENCONTRADO })
        assertEquals(listOf(100, 50, 50), paramsNumeros.map { it.size })
        assertTrue(maxRowsNumeros.all { it <= 10_000 })
    }

    @Test
    fun `parcela ilegivel ou inexistente nunca vira a NF inteira - NAO_ENCONTRADO fora do lote, nos dois criterios`() {
        nota(8101, 818, 6, "1", 1 to "10.00|2026-12-10", 2 to "10.00|2027-01-10", nome = "ALFA COMERCIO LTDA")
        // coluna Parcela com texto/simbolo, e sufixo de Seu Numero com letra
        val modelo = "Numero da nota,Parcela,Nome do parceiro\n818,abc,ALFA COMERCIO\n818,-,ALFA COMERCIO\n818,9,ALFA COMERCIO\n818,1,ALFA COMERCIO\n"
        val nome = importaNome(modelo.toByteArray())
        assertEquals(listOf(NAO_ENCONTRADO, NAO_ENCONTRADO, NAO_ENCONTRADO, ENCONTRADO), nome.linhas.map { it.situacao })
        assertEquals("linha 2: parcela ilegivel", nome.linhas[0].mensagem)
        assertEquals("linha 3: parcela ilegivel", nome.linhas[1].mensagem)
        assertEquals("NF 818 nao tem a parcela 9", nome.linhas[2].mensagem.orEmpty().substringBefore(" ("))
        assertEquals(listOf(null, null, null, 1), nome.linhas.map { it.parcela })
        assertEquals(listOf(8101), nome.documentos.map { it.docEntry })
        // COMPLETO: sufixo ilegivel, parcela inexistente
        val c = importa(csvSimples("0000818-A|10/12/2026|10,00", "0000818-9|10/12/2026|10,00", "0000818-1|10/12/2026|10,00")).linhas
        assertEquals(listOf(NAO_ENCONTRADO, NAO_ENCONTRADO, ENCONTRADO), c.map { it.situacao })
        assertEquals("linha 2: parcela ilegivel", c[0].mensagem)
        assertEquals("NF 818 nao tem a parcela 9", c[1].mensagem)
        assertTrue(c.filter { it.situacao != ENCONTRADO }.all { it.docEntry == null })
    }

    @Test
    fun `contrato - ENCONTRADO e DIVERGENTE so tem parcela nula quando a planilha nao trouxe parcela`() {
        nota(8201, 820, 6, "1", 1 to "10.00|2026-12-10", 2 to "10.00|2027-01-10", nome = "ALFA COMERCIO LTDA")
        nota(8202, 821, 6, "1", 1 to "10.00|2026-12-10", nome = "ALFA COMERCIO LTDA")
        val completo = importa(csvSimples("0000820|10/12/2026|10,00", "0000820|10/01/2027|99,00", "0000820|01/01/2030|1,00", "0000821|10/12/2026|11,00")).linhas
        assertTrue(completo.filter { it.situacao == ENCONTRADO || it.situacao == DIVERGENTE }.all { it.parcela != null && it.docEntry != null }, completo.map { it.situacao }.toString())
        val nome = importaNome(csvNome("ALFA COMERCIO|0000820-2|10/01/2027|10,00", "ALFA COMERCIO|0000820|10/01/2027|10,00")).linhas
        assertEquals(listOf(2, null), nome.map { it.parcela })
    }

    @Test
    fun `caso 818 - sem bater no valor, o vencimento escolhe a parcela (DIVERGENTE) e o nome desempata se o vencimento for igual`() {
        nota(6001, 818, 6, "1", 1 to "3505.90|2026-11-06", 2 to "3505.90|2026-12-06", 3 to "3506.94|2027-01-06", nome = "ALFA COMERCIO LTDA")
        nota(6002, 818, 18, "2", 3 to "3506.94|2027-02-06", nome = "BETA AGRO S.A.")
        val l = importa(csvNome("ALFA COMERCIO|0000818-003|06/01/2027|3.505,90")).linhas.single()
        assertEquals(DIVERGENTE, l.situacao); assertEquals(6001, l.docEntry); assertEquals(3, l.parcela)
        assertEquals("valor na planilha 3505.90, na parcela 3506.94; escolhida pelo vencimento", l.mensagem)
        nota(6003, 818, 11, "1", 3 to "3506.94|2027-01-06", nome = "GAMA TRANSPORTES LTDA")
        val dois = importa(csvNome("ALFA COMERCIO|0000818-003|06/01/2027|3.505,90")).linhas.single()
        assertEquals(DIVERGENTE, dois.situacao); assertEquals(6001, dois.docEntry)
        assertEquals("valor na planilha 3505.90, na parcela 3506.94; escolhida pelo vencimento; desempatada pelo nome do pagador", dois.mensagem)
        assertFalse(dois.mensagem!!.contains("ALFA"))
        // nome que nao parece com nenhuma: continua ambiguo
        assertEquals(AMBIGUO, importa(csvNome("OUTRO NOME QUALQUER|0000818-003|06/01/2027|3.505,90")).linhas.single().situacao)
    }

    @Test
    fun `nome desempata quando valor e vencimento batem em mais de uma nota - prefixo truncado`() {
        nota(6101, 700, 6, "1", 1 to "10.00|2026-12-10", nome = "FAZENDA SANTA MARIA DO NORTE LTDA")
        nota(6102, 700, 18, "1", 1 to "10.00|2026-12-10", nome = "COMERCIAL XYZ ME")
        val l = importa(csvNome("FAZENDA SANTA MARIA DO|0000700|10/12/2026|10,00")).linhas.single()
        assertEquals(ENCONTRADO, l.situacao); assertEquals(6101, l.docEntry); assertEquals("desempatada pelo nome do pagador", l.mensagem)
    }

    @Test
    fun `nome desempata com acento, sufixo e termos em subconjunto`() {
        nota(6201, 701, 6, "1", 1 to "10.00|2026-12-10", nome = "JOSE DA SILVA E FILHOS ME")
        nota(6202, 701, 18, "1", 1 to "10.00|2026-12-10", nome = "MARIA APARECIDA DOS SANTOS SOUZA")
        assertEquals(6201, importa(csvNome("JOSÉ DA SILVA & FILHOS LTDA.|0000701|10/12/2026|10,00")).linhas.single().docEntry)
        assertEquals(6202, importa(csvNome("MARIA APARECIDA SOUZA|0000701|10/12/2026|10,00")).linhas.single().docEntry)
    }

    @Test
    fun `dois nomes parecidos continuam ambiguos, e nota unica nao depende do nome`() {
        nota(6301, 702, 6, "1", 1 to "10.00|2026-12-10", nome = "AGRO FORTE LTDA")
        nota(6302, 702, 18, "1", 1 to "10.00|2026-12-10", nome = "AGRO FORTE COMERCIO DE GRAOS")
        assertEquals(AMBIGUO, importa(csvNome("AGRO FORTE|0000702|10/12/2026|10,00")).linhas.single().situacao)
        nota(6401, 703, 6, "1", 1 to "10.00|2026-12-10", nome = "NOME TOTALMENTE DIFERENTE")
        val u = importa(csvNome("PAGADOR NAO RELACIONADO|0000703|10/12/2026|10,00")).linhas.single()
        assertEquals(ENCONTRADO, u.situacao); assertNull(u.mensagem)
        val d = importa(csvNome("PAGADOR NAO RELACIONADO|0000703|10/12/2026|99,00")).linhas.single()
        assertEquals(DIVERGENTE, d.situacao); assertEquals("valor na planilha 99.00, na parcela 10.00", d.mensagem)
    }

    @Test
    fun `normalizacao e similaridade de nomes`() {
        val n = NotaFiscalLoteImportacaoService.Companion::normalizaNome
        assertEquals("JOSE DA SILVA FILHOS", n(" José da  Silva & Filhos Ltda. "))
        assertEquals("ACME", n("Acme S/A")); assertEquals("ACME", n("ACME S.A. EPP"))
        val p = NotaFiscalLoteImportacaoService.Companion::nomesParecidos
        assertTrue(p("FAZENDA BOA VISTA", "FAZENDA BOA VISTA DO SUL LTDA"))
        assertTrue(p("FAZENDA BOA VI", "FAZENDA BOA VISTA DO SUL"))
        assertTrue(p("MARIA SOUZA", "MARIA APARECIDA DE SOUZA"))
        assertFalse(p("SOUZA MARIA", "MARIA APARECIDA DE SOUZA"))
        assertFalse(p("AGRO SUL", "AGRO NORTE")); assertFalse(p(null, "X")); assertFalse(p("LTDA", "ME"))
        // um termo so ("AGRO") e fraco demais; igualdade exata e prefixo de 10+ continuam valendo
        // o minimo de 2 termos vale tambem para o prefixo; prefixo de dois termos truncado continua valendo
        assertFalse(p("COOPERATIVA", "COOPERATIVA BETA")); assertFalse(p("COOPERATIVA AGRO", "COOPERATIVA AGRO")  .not())
        assertTrue(p("FAZENDA SANTA MARIA DO", "FAZENDA SANTA MARIA DO NORTE LTDA"))
        assertFalse(p("AGRO", "BETA AGRO LTDA")); assertFalse(p("BETA", "BETA AGRO LTDA")); assertFalse(p("AGRO LTDA", "BETA AGRO"))
        assertTrue(p("MARIA", "MARIA LTDA")); assertTrue(p("AGRO FORTE", "AGRO FORTE COMERCIO"))
        assertTrue(p("JOSE LIMA", "JOSE DA SILVA LIMA")); assertTrue(p("SOUZA LIMA", "JOSE SOUZA DA LIMA FILHO"))
        // termos com menos de 3 letras nao contam: sobra um so
        assertFalse(p("JO SILVA", "JO SILVA FILHOS COMERCIO")); assertFalse(p("AB CD EF", "AB CD EF GH IJ"))
    }

    @Test
    fun `NUMERO_NOME - uma nota com nome parecido entra, parcela nula sem parcela informada, valor e vencimento so informacao`() {
        nota(7001, 800, 6, "1", 1 to "500.00|2026-11-10|500.00", 2 to "500.00|2026-12-10", nome = "ALFA COMERCIO DE GRAOS LTDA")
        nota(7002, 800, 18, "1", 1 to "500.00|2026-11-10", nome = "BETA AGRO S.A.")
        val r = importaNome(csvNome("ALFA COMERCIO DE GRAOS|0000800|01/01/2030|999,00"))
        val l = r.linhas.single()
        assertEquals(ENCONTRADO, l.situacao); assertEquals(7001, l.docEntry)
        // sem parcela no "Seu Numero": nao escolhe sozinho (nula = todas as em aberto), mesmo com uma so em aberto
        assertNull(l.parcela)
        assertEquals("conferido por numero e nome; parcela nao definida: valem todas as parcelas em aberto", l.mensagem)
        assertFalse(l.mensagem!!.contains("ALFA"))
        assertEquals(listOf(7001), r.documentos.map { it.docEntry })
        // parcela informada em aberto: valor e vencimento so informam a diferenca
        val dada = importaNome(csvNome("ALFA COMERCIO DE GRAOS|0000800-2|01/01/2030|999,00")).linhas.single()
        assertEquals(2, dada.parcela)
        assertEquals("conferido por numero e nome; valor na planilha 999.00, na parcela 500.00; vencimento na planilha 01/01/2030, na parcela 10/12/2026", dada.mensagem)
        // casamento limpo (parcela definida, sem diferenca): sem mensagem de rotina
        assertNull(importaNome(csvNome("ALFA COMERCIO DE GRAOS|0000800-2|10/12/2026|500,00")).linhas.single().mensagem)
    }

    @Test
    fun `NUMERO_NOME e o padrao da propriedade, COMPLETO continua disponivel`() {
        assertEquals(CriterioImportacaoNumeroNota.NUMERO_NOME, servicoNome.criterio)
        assertEquals(CriterioImportacaoNumeroNota.COMPLETO, servico.criterio)
    }

    @Test
    fun `NUMERO_NOME - PAGA quando a parcela informada esta paga ou a NF nao tem parcela em aberto, parcial conta como aberta`() {
        nota(7401, 810, 6, "1", 1 to "100.00|2026-11-10|100.00", 2 to "100.00|2026-12-10|40.00", nome = "DELTA COMERCIO LTDA")
        nota(7402, 811, 6, "1", 1 to "50.00|2026-11-10|50.00", 2 to "50.00|2026-12-10|50.00", nome = "DELTA COMERCIO LTDA")
        val r = importaNome(csvNome(
            "DELTA COMERCIO|0000810-1|10/11/2026|100,00", "DELTA COMERCIO|0000810-2|10/12/2026|100,00",
            "DELTA COMERCIO|0000811|10/11/2026|50,00", "DELTA COMERCIO|0000810|10/11/2026|100,00"))
        assertEquals(listOf(PAGA, ENCONTRADO, PAGA, ENCONTRADO), r.linhas.map { it.situacao })
        assertEquals("NF 810 parcela 1 ja paga", r.linhas[0].mensagem)
        assertEquals(7401, r.linhas[0].docEntry); assertEquals(1, r.linhas[0].parcela)
        assertEquals("NF 811 sem parcela em aberto", r.linhas[2].mensagem)
        assertEquals(7402, r.linhas[2].docEntry); assertNull(r.linhas[2].parcela)
        // parcela parcialmente paga e aberta; so as linhas ENCONTRADAS levam a NF ao lote (a 811 so tinha PAGA)
        assertEquals(2, r.linhas[1].parcela)
        assertEquals(listOf(7401), r.documentos.map { it.docEntry })
        // NF so com linha PAGA nao entra
        assertTrue(importaNome(csvNome("DELTA COMERCIO|0000810-1|10/11/2026|100,00")).documentos.isEmpty())
    }

    @Test
    fun `modelo numero e nome - coluna Parcela separada, com e sem parcela, numero com zeros, xlsx e csv`() {
        nota(8001, 818, 6, "1", 3 to "3506.94|2027-01-06", 4 to "3506.94|2027-02-06", nome = "ALFA COMERCIO LTDA")
        val csv = "Numero da nota,Parcela,Nome do parceiro,Valor,Vencimento\n" +
            "0000818,3,ALFA COMERCIO,\"3.505,90\",06/01/2027\n" +
            "0000818,,ALFA COMERCIO,,\n" +
            "818-4,,ALFA COMERCIO,,\n" +
            "818.0,4.0,ALFA COMERCIO,,\n"
        val l = importaNome(csv.toByteArray()).linhas
        assertEquals(listOf(ENCONTRADO, ENCONTRADO, ENCONTRADO, ENCONTRADO), l.map { it.situacao })
        assertEquals(listOf(3, null, 4, 4), l.map { it.parcela })
        assertEquals(listOf(2, 3, 4, 5), l.map { it.linha })
        assertTrue(l[0].mensagem!!.contains("valor na planilha 3505.90, na parcela 3506.94"))
        val xlsx = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Número da nota", "B1" to "Parcela", "C1" to "Nome do parceiro", "D1" to "Valor", "E1" to "Vencimento"),
            2 to mapOf("A2" to 818, "B2" to 3, "C2" to "ALFA COMERCIO", "D2" to 3506.94, "E2" to 46393),
            3 to mapOf("A3" to "0000818", "C3" to "Alfa Comércio")))
        val x = importaNome(xlsx).linhas
        assertEquals(listOf(ENCONTRADO, ENCONTRADO), x.map { it.situacao })
        assertEquals(listOf(3, null), x.map { it.parcela })
        // sinonimos de cabecalho
        val sin = importaNome("NF,Cliente\n818,ALFA COMERCIO\n".toByteArray()).linhas.single()
        assertEquals(ENCONTRADO, sin.situacao); assertNull(sin.parcela)
    }

    @Test
    fun `layout Sicoob continua sendo lido igual (Seu Numero e Sacado) nos dois criterios`() {
        cenario()
        val nome = importaNome(csv().toByteArray())
        assertEquals(listOf(5, 6, 7, 8, 9, 14, 15, 16), nome.linhas.map { it.linha })
        assertEquals(listOf("3956 1", "0005647-2", "0000830-02", "0005521", "9999 1", "4879 3", "7777 1", "abc"), nome.linhas.map { it.beneficiario })
        assertEquals("CLIENTE UM DE TESTE", nome.linhas[0].pagador)
    }

    @Test
    fun `NUMERO_NOME - varias parcelas em aberto sem parcela informada ficam nulas (todas as em aberto), com parcela vale a dela`() {
        nota(7101, 801, 6, "1", 1 to "100.00|2026-11-10", 2 to "100.00|2026-12-10", nome = "GAMA TRANSPORTES LTDA")
        val r = importaNome(csvNome("GAMA TRANSPORTES|0000801|10/11/2026|100,00", "GAMA TRANSPORTES|0000801-2|10/12/2026|100,00"))
        assertEquals(listOf(null, 2), r.linhas.map { it.parcela })
        assertEquals(listOf(ENCONTRADO, ENCONTRADO), r.linhas.map { it.situacao })
        assertTrue(r.linhas[0].mensagem!!.contains("parcela nao definida: valem todas as parcelas em aberto"))
        assertEquals(listOf(7101), r.documentos.map { it.docEntry })
    }

    @Test
    fun `NUMERO_NOME - nomes parecidos em varias notas e AMBIGUO, nome diferente, vazio ou nota inexistente e NAO_ENCONTRADO`() {
        nota(7201, 802, 6, "1", 1 to "10.00|2026-11-10", nome = "AGRO FORTE LTDA")
        nota(7202, 802, 18, "2", 1 to "10.00|2026-11-10", nome = "AGRO FORTE COMERCIO DE GRAOS")
        val amb = importaNome(csvNome("AGRO FORTE|0000802|10/11/2026|10,00")).linhas.single()
        assertEquals(AMBIGUO, amb.situacao); assertNull(amb.docEntry)
        assertTrue(amb.mensagem!!.contains("filial 6 Filial 6 serie 1") && amb.mensagem!!.contains("filial 18 Filial 18 serie 2"), amb.mensagem)
        nota(7203, 803, 6, "1", 1 to "10.00|2026-11-10", nome = "NOME TOTALMENTE DIFERENTE")
        val dif = importaNome(csvNome("PAGADOR QUALQUER|0000803|10/11/2026|10,00")).linhas.single()
        assertEquals(NAO_ENCONTRADO, dif.situacao); assertNull(dif.docEntry)
        assertEquals("NF 803 existe, mas o nome do parceiro nao confere (filial 6 Filial 6 serie 1)", dif.mensagem)
        val vazio = importaNome(csvNome("|0000803|10/11/2026|10,00")).linhas.single()
        assertEquals(NAO_ENCONTRADO, vazio.situacao); assertTrue(vazio.mensagem!!.contains("nome do pagador ausente"))
        assertEquals(NAO_ENCONTRADO, importaNome(csvNome("X|0000999|10/11/2026|10,00")).linhas.single().situacao)
        assertTrue(importaNome(csvNome("NOME TOTALMENTE DIFERENTE|0000803-5|10/11/2026|10,00")).linhas.single().mensagem!!.contains("nao tem a parcela 5"))
    }

    @Test
    fun `COMPLETO segue igual - nome so desempata, nao e criterio`() {
        nota(7301, 804, 6, "1", 1 to "10.00|2026-11-10", nome = "NOME TOTALMENTE DIFERENTE")
        val c = importa(csvNome("PAGADOR QUALQUER|0000804|10/11/2026|10,00")).linhas.single()
        assertEquals(ENCONTRADO, c.situacao); assertNull(c.mensagem)
        assertEquals(NAO_ENCONTRADO, importaNome(csvNome("PAGADOR QUALQUER|0000804|10/11/2026|10,00")).linhas.single().situacao)
    }

    @Test
    fun `parcela com erro do Excel ou separador pendurado e NAO_ENCONTRADO, nunca a NF inteira`() {
        nota(8301, 818, 6, "1", 1 to "10.00|2026-12-10", nome = "ALFA COMERCIO LTDA")
        val xlsx = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Numero da nota", "B1" to "Parcela", "C1" to "Nome do parceiro"),
            2 to mapOf("A2" to 818, "B2" to ErroXlsx("#N/A"), "C2" to "ALFA COMERCIO"),
            3 to mapOf("A3" to ErroXlsx("#VALUE!"), "B3" to 1, "C3" to "ALFA COMERCIO"),
            4 to mapOf("A4" to 818, "B4" to 1, "C4" to "ALFA COMERCIO")))
        val l = importaNome(xlsx).linhas
        assertEquals(listOf(NAO_ENCONTRADO, NAO_ENCONTRADO, ENCONTRADO), l.map { it.situacao })
        assertEquals("linha 2: parcela ilegivel", l[0].mensagem)
        assertEquals("linha 3: numero da nota ilegivel", l[1].mensagem)
        assertNull(l[0].docEntry)
        assertEquals(listOf(8301), importaNome(xlsx).documentos.map { it.docEntry })
        val pendurado = importaNome("Numero da nota,Nome do parceiro\n818-,ALFA COMERCIO\n".toByteArray())
        assertEquals("linha 2: parcela ilegivel", pendurado.linhas.single().mensagem); assertTrue(pendurado.documentos.isEmpty())
        assertEquals("linha 2: parcela ilegivel", importa(csvSimples("818-|10/12/2026|10,00")).linhas.single().mensagem)
    }

    @Test
    fun `formula sem valor em cache na parcela e ilegivel, com cache vale o valor`() {
        nota(8501, 818, 6, "1", 1 to "10.00|2026-12-10", 2 to "10.00|2027-01-10", nome = "ALFA COMERCIO LTDA")
        val xlsx = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Numero da nota", "B1" to "Parcela", "C1" to "Nome do parceiro"),
            2 to mapOf("A2" to 818, "B2" to FormulaXlsx("A2-817", null), "C2" to "ALFA COMERCIO"),
            3 to mapOf("A3" to 818, "B3" to FormulaXlsx("A3-816", "2"), "C3" to "ALFA COMERCIO"),
            4 to mapOf("A4" to 818, "B4" to FormulaXlsx("1+0", ""), "C4" to "ALFA COMERCIO")))
        val r = importaNome(xlsx)
        assertEquals(listOf(NAO_ENCONTRADO, ENCONTRADO, NAO_ENCONTRADO), r.linhas.map { it.situacao })
        assertEquals("linha 2: parcela ilegivel", r.linhas[0].mensagem); assertEquals("linha 4: parcela ilegivel", r.linhas[2].mensagem)
        assertEquals(2, r.linhas[1].parcela)
        assertEquals(listOf(8501), r.documentos.map { it.docEntry })
    }

    @Test
    fun `parcela com texto compartilhado sem valor ou fora da tabela, ou formula t=s sem cache, e ilegivel (NAO_ENCONTRADO fora do lote)`() {
        nota(8801, 818, 6, "1", 1 to "10.00|2026-12-10", nome = "ALFA COMERCIO LTDA")
        val xlsx = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Numero da nota", "B1" to "Parcela", "C1" to "Nome do parceiro"),
            2 to mapOf("A2" to 818, "B2" to CrusXlsx("t=\"s\"", "<f>1</f>"), "C2" to "ALFA COMERCIO"),
            3 to mapOf("A3" to 818, "B3" to CrusXlsx("t=\"s\"", "<v>99</v>"), "C3" to "ALFA COMERCIO"),
            4 to mapOf("A4" to 818, "B4" to 1, "C4" to "ALFA COMERCIO")))
        val r = importaNome(xlsx)
        assertEquals(listOf(NAO_ENCONTRADO, NAO_ENCONTRADO, ENCONTRADO), r.linhas.map { it.situacao })
        assertEquals(listOf("linha 2: parcela ilegivel", "linha 3: parcela ilegivel"), r.linhas.take(2).map { it.mensagem })
        assertEquals(listOf(8801), r.documentos.map { it.docEntry })
        assertTrue(r.linhas.take(2).all { it.docEntry == null })
    }

    @Test
    fun `pagador com erro do Excel ou formula sem cache e ilegivel, e nunca vira nome (REF)`() {
        nota(8601, 840, 6, "1", 1 to "10.00|2026-12-10", nome = "REF COMERCIO LTDA")
        nota(8602, 840, 18, "1", 1 to "10.00|2026-12-10", nome = "REF")
        val nomeXlsx = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Numero da nota", "B1" to "Nome do parceiro"),
            2 to mapOf("A2" to 840, "B2" to ErroXlsx("#REF!")),
            3 to mapOf("A3" to 840, "B3" to FormulaXlsx("B1", null))))
        val l = importaNome(nomeXlsx).linhas
        assertEquals(listOf(NAO_ENCONTRADO, NAO_ENCONTRADO), l.map { it.situacao })
        assertEquals(listOf("linha 2: nome do parceiro ilegivel", "linha 3: nome do parceiro ilegivel"), l.map { it.mensagem })
        // COMPLETO: duas notas batem em valor+vencimento e o pagador ilegivel nao desempata (AMBIGUO)
        val completo = PlanilhaXlsxTest.xlsx(mapOf(
            1 to mapOf("A1" to "Sacado", "B1" to "Seu Número", "C1" to "Vencimento", "D1" to "Valor (R$)"),
            2 to mapOf("A2" to ErroXlsx("#REF!"), "B2" to "840 1", "C2" to "10/12/2026", "D2" to 10)))
        val c = importa(completo).linhas.single()
        assertEquals(AMBIGUO, c.situacao); assertNull(c.docEntry)
        assertFalse(NotaFiscalLoteImportacaoService.nomesParecidos("#REF!", "REF COMERCIO LTDA"))
        assertFalse(NotaFiscalLoteImportacaoService.nomesParecidos("REF", "#N/A"))
    }

    @Test
    fun `prefixo NF-e e aceito, separador pendurado continua rejeitado`() {
        val c = NotaFiscalLoteImportacaoService.Companion::chaveNota
        assertEquals(818 to null, c("NF-e 818")); assertEquals(818 to 2, c("NF-e 818-2")); assertEquals(818 to null, c("NFe 818"))
        assertEquals(818 to 2, c("nf-e 818 2")); assertEquals(818 to null, c("NF-e818"))
        listOf("818-", "NF-e 818-", "NF-e", "NF-e -", "NF-e 818x").forEach { assertNull(c(it), it) }
    }

    @Test
    fun `serial de data fora do intervalo e ilegivel, sem excecao`() {
        listOf("1e20", "NaN", "Infinity", "-Infinity", "-5", "0", "0.5", "2958466", "99999999999").forEach {
            assertNull(NotaFiscalLoteImportacaoService.data(it), it)
        }
        assertEquals("9999-12-31", NotaFiscalLoteImportacaoService.data("2958465")); assertEquals("2030-01-31", NotaFiscalLoteImportacaoService.data("47514"))
        nota(8701, 850, 6, "1", 1 to "10.00|2026-12-10", nome = "ALFA COMERCIO LTDA")
        val l = importa(csvSimples("850 1|1e20|10,00")).linhas.single()
        assertEquals(DIVERGENTE, l.situacao); assertEquals("vencimento ilegivel: nao conferido", l.mensagem)
    }

    @Test
    fun `data impossivel e ilegivel (31 02), data valida segue`() {
        assertNull(NotaFiscalLoteImportacaoService.data("31/02/2026")); assertNull(NotaFiscalLoteImportacaoService.data("32/01/2026"))
        assertNull(NotaFiscalLoteImportacaoService.data("29/02/2027")); assertEquals("2028-02-29", NotaFiscalLoteImportacaoService.data("29/02/2028"))
        assertEquals("2026-02-28", NotaFiscalLoteImportacaoService.data("28/02/2026"))
        nota(8401, 830, 6, "1", 1 to "10.00|2026-12-10", nome = "ALFA COMERCIO LTDA")
        val l = importa(csvSimples("830 1|31/02/2026|10,00")).linhas.single()
        assertEquals(DIVERGENTE, l.situacao); assertEquals("vencimento ilegivel: nao conferido", l.mensagem)
    }

    @Test
    fun `coluna Nosso numero informativa e a do cabecalho Nosso Numero`() {
        nota(3001, 200, 6, "1", 1 to "500.00|2026-11-10")
        assertEquals("3-8", importa(csvSimples("200 1|10/11/2026|500,00")).linhas.single().nossoNumero)
    }

    @Test
    fun `ignora o nosso numero e nao usa o filtro da tela nem consulta boleto`() {
        cenario()
        importa()
        assertFalse(sqls.contains("boletos"))
        assertEquals(listOf("numeros", "listagem"), sqls)
        // a unica ida a listagem e a de documentos achados, por DocEntry, sem filtro
        assertEquals(listOf(-1), paramsListagem!!["filiais"])
    }

    @Test
    fun `windows-1252 e utf-8 dao o mesmo resultado`() {
        cenario()
        val acentuado = csv().replace("CLIENTE UM DE TESTE", "JOÃO DA CONCEIÇÃO")
        val utf8 = importa(acentuado.toByteArray(Charsets.UTF_8))
        val w1252 = importa(acentuado.toByteArray(Charset.forName("windows-1252")))
        assertEquals(utf8.linhas.map { it.situacao }, w1252.linhas.map { it.situacao })
        assertEquals("JOÃO DA CONCEIÇÃO", w1252.linhas[0].pagador)
        assertEquals("JOÃO DA CONCEIÇÃO", utf8.linhas[0].pagador)
        // cabecalho com acento ("Número") foi reconhecido nos dois
        assertEquals(8, w1252.linhas.size)
    }

    @Test
    fun `planilha sem a coluna Seu numero e recusada com a mensagem do modo`() {
        val e = assertThrows(IllegalArgumentException::class.java) { importa("a,b\n1,2\n".toByteArray()) }
        assertTrue(e.message!!.contains("Seu numero"))
    }

    @Test
    fun `chave da nota a partir do Seu Numero`() {
        val c = NotaFiscalLoteImportacaoService.Companion::chaveNota
        assertEquals(3956 to 1, c("3956 1         "))
        assertEquals(5647 to 2, c("0005647-2"))
        assertEquals(830 to 2, c("0000830-02"))
        assertEquals(5521 to null, c("0005521"))
        assertEquals(870 to null, c("0000870"))
        assertEquals(6515 to 2, c("0006515-002-02"))
        assertNull(c("0006515-002-03"))
        assertNull(c("abc")); assertNull(c("")); assertNull(c(null)); assertNull(c("1 2 3")); assertNull(c("0"))
        // referencia incompleta (separador pendurado, simbolo depois do numero) e ilegivel, nao "NF inteira"
        listOf("818-", "818 -", "818/", "818-1-", "818.", "818 #N/A", "818-x").forEach { assertNull(c(it), it) }
        assertEquals(818 to null, c("818")); assertEquals(818 to null, c("NF 818")); assertEquals(818 to 3, c("818 - 3"))
    }

    @Test
    fun `SQL por numero so le, tem so parametro nomeado e a elegibilidade da tela`() {
        val sql = SqlResource.carregar("odbc/nota-fiscal-lote-por-numero.sql")
        assertTrue(sql.contains("N.\"Serial\" IN (:numeros)") && sql.contains("N.\"Model\" = 39") && sql.contains("KeyNfe"))
        assertFalse(Regex("(?i)\\b(insert|update|delete|merge|drop|alter)\\b").containsMatchIn(sql.lineSequence().filterNot { it.startsWith("--") }.joinToString("\n")))
    }

    @Test
    fun `modo NOSSO_NUMERO aceita o mesmo csv pelos sinonimos de coluna`() {
        val e = assertThrows(IllegalArgumentException::class.java) { servico.importar("a,b\n1,2\n".toByteArray()) }
        assertTrue(e.message!!.contains("Nosso numero"))
    }

    @Test
    fun `linha NF sozinha no meio da planilha e dado, nao cabecalho, e as seguintes mantem o mapa de colunas`() {
        val csv = "Numero da nota,Parcela,Nome do parceiro\n818,1,ALFA COMERCIO\nNF\n819,2,BETA COMERCIO\n"
        val l = importaNome(csv.toByteArray()).linhas
        // sem a correcao "NF" refazia o mapa e a linha 4 perdia parcela/parceiro
        assertEquals(listOf(2, 3, 4), l.map { it.linha })
        assertEquals(NAO_ENCONTRADO, l[1].situacao)
        assertEquals("819 2", l[2].beneficiario)
        assertEquals("BETA COMERCIO", l[2].pagador)
    }

    @Test
    fun `planilha de uma coluna so (Numero da nota) ainda e reconhecida`() {
        val l = importaNome("Numero da nota\n818\n819\n".toByteArray()).linhas
        assertEquals(listOf(2, 3), l.map { it.linha })
        assertEquals(listOf("818", "819"), l.map { it.beneficiario })
    }

    @Test
    fun `cabecalho de uma coluna repetido no meio nao e dado nem conta no limite de 200`() {
        val dados = (1..200).map { 1000 + it }
        val csv = (listOf("Numero da nota") + dados.take(100) + "Numero da nota" + dados.drop(100)).joinToString("\n")
        val l = importaNome(csv.toByteArray()).linhas
        assertEquals(dados.map { it.toString() }, l.map { it.beneficiario })
    }
}
