package br.andrew.sap.services.fiscal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import br.andrew.sap.controllers.invent.NotaFiscalLoteController
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// Celula com erro do Excel (t="e"): #N/A, #VALUE!...
data class ErroXlsx(val texto: String)

// Celula com formula; cache = valor em <v> (nulo = sem <v>, "" = <v> vazio)
data class FormulaXlsx(val formula: String, val cache: String?)

// Celula crua: atributos (ex.: t="s") e corpo (ex.: <f>1</f><v>9</v>)
data class CrusXlsx(val atributos: String, val corpo: String)

class PlanilhaXlsxTest {

    companion object {
        /** Monta um .xlsx minimo: [linhas] = numero da linha -> celulas ("B11" -> valor). Texto vai como compartilhado. */
        fun xlsx(linhas: Map<Int, Map<String, Any>>, sheetXmlExtra: String = ""): ByteArray {
            val textos = mutableListOf<String>()
            val rows = linhas.entries.joinToString("") { (n, celulas) ->
                "<row r=\"$n\">" + celulas.entries.joinToString("") { (ref, v) ->
                    when (v) {
                        is Number -> "<c r=\"$ref\"><v>$v</v></c>"
                        is ErroXlsx -> "<c r=\"$ref\" t=\"e\"><v>${v.texto}</v></c>"
                        is CrusXlsx -> "<c r=\"$ref\" ${v.atributos}>${v.corpo}</c>"
                        is FormulaXlsx -> "<c r=\"$ref\"><f>${v.formula}</f>" + (v.cache?.let { "<v>$it</v>" } ?: "") + "</c>"
                        else -> { textos.add(v.toString()); "<c r=\"$ref\" t=\"s\"><v>${textos.size - 1}</v></c>" }
                    }
                } + "</row>"
            }
            val ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
            val partes = mapOf(
                "xl/workbook.xml" to "<workbook xmlns=\"$ns\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                    "<sheets><sheet name=\"Planilha1\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>",
                "xl/_rels/workbook.xml.rels" to "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                    "<Relationship Id=\"rId1\" Type=\"worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>",
                "xl/sharedStrings.xml" to "<sst xmlns=\"$ns\">" + textos.joinToString("") { "<si><t>${it.replace("&", "&amp;")}</t></si>" } + "</sst>",
                "xl/worksheets/sheet1.xml" to "$sheetXmlExtra<worksheet xmlns=\"$ns\"><sheetData>$rows</sheetData></worksheet>",
            )
            val saida = ByteArrayOutputStream()
            ZipOutputStream(saida).use { zip ->
                partes.forEach { (nome, conteudo) -> zip.putNextEntry(ZipEntry(nome)); zip.write(conteudo.toByteArray()); zip.closeEntry() }
            }
            return saida.toByteArray()
        }
    }

    @Test
    fun `le texto compartilhado, numero e a coluna pela referencia`() {
        val linhas = PlanilhaXlsx.ler(xlsx(mapOf(10 to mapOf("D10" to "Nosso número"), 11 to mapOf("D11" to "35158540000021002-5", "G11" to 16088.38, "H11" to 46342))))
        assertEquals("Nosso número", linhas[10]!![3])
        assertEquals("35158540000021002-5", linhas[11]!![3])
        assertEquals("16088.38", linhas[11]!![6])
        assertEquals(LocalDate.of(2026, 11, 16), PlanilhaXlsx.dataDoExcel(linhas[11]!![7]!!.toDouble()))
    }

    @Test
    fun `arquivo que nao e xlsx e recusado com mensagem`() {
        assertThrows(IllegalArgumentException::class.java) { PlanilhaXlsx.ler("Cart;Var;Nosso numero".toByteArray()) }
    }

    @Test
    fun `entrada ignorada tambem conta no teto do descompactado (zip-bomba)`() {
        val ok = xlsx(mapOf(1 to mapOf("A1" to 1)))
        // junta ao xlsx valido uma entrada fora das lidas: 25 MB de zeros, que comprimem para quase nada
        val saida = ByteArrayOutputStream()
        ZipOutputStream(saida).use { zip ->
            java.util.zip.ZipInputStream(ok.inputStream()).use { orig ->
                generateSequence { orig.nextEntry }.forEach { e -> zip.putNextEntry(ZipEntry(e.name)); zip.write(orig.readBytes()); zip.closeEntry() }
            }
            zip.putNextEntry(ZipEntry("xl/media/bomba.bin")); zip.write(ByteArray(25 * 1024 * 1024)); zip.closeEntry()
        }
        assertTrue(saida.size() < 1024 * 1024)
        val erro = assertThrows(IllegalArgumentException::class.java) { PlanilhaXlsx.ler(saida.toByteArray()) }
        assertTrue(erro.message!!.contains("grande demais"))
    }

    @Test
    fun `arquivo acima do teto nem e aberto`() {
        assertThrows(IllegalArgumentException::class.java) { PlanilhaXlsx.ler(ByteArray(PlanilhaXlsx.TETO_ARQUIVO + 1)) }
    }

    @Test
    fun `DOCTYPE e recusado (sem XXE)`() {
        val comDoctype = xlsx(mapOf(1 to mapOf("A1" to 1)), "<?xml version=\"1.0\"?><!DOCTYPE x [<!ENTITY e SYSTEM \"file:///etc/passwd\">]>")
        assertThrows(Exception::class.java) { PlanilhaXlsx.ler(comDoctype) }
    }

    @Test
    fun `celula com erro do Excel fica como texto do erro, nao some como vazia`() {
        val linhas = PlanilhaXlsx.ler(xlsx(mapOf(1 to mapOf("A1" to 818, "B1" to ErroXlsx("#N/A"), "C1" to ErroXlsx("#VALUE!")))))
        assertEquals(mapOf(0 to "818", 1 to "#N/A", 2 to "#VALUE!"), linhas[1])
    }

    @Test
    fun `XML malformado da aba ou das strings compartilhadas e erro de entrada (400), nao 500`() {
        fun zipado(aba: String, strings: String): ByteArray {
            val saida = ByteArrayOutputStream()
            ZipOutputStream(saida).use { zip ->
                mapOf("xl/sharedStrings.xml" to strings, "xl/worksheets/sheet1.xml" to aba)
                    .forEach { (n, c) -> zip.putNextEntry(ZipEntry(n)); zip.write(c.toByteArray()); zip.closeEntry() }
            }
            return saida.toByteArray()
        }
        val ok = "<sst xmlns=\"x\"/>"
        listOf(zipado("<worksheet><sheetData><row r=\"1\"><c r=\"A1\"", ok), zipado("<worksheet><sheetData/></worksheet>", "<sst><si><t>x</sst>"),
            zipado("<!DOCTYPE a [<!ENTITY b \"c\">]><worksheet/>", ok)).forEach { bytes ->
            val e = assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) { PlanilhaXlsx.ler(bytes) }
            // o controller responde 400 para esta excecao
            val r = NotaFiscalLoteController(org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock())
                .onImportacaoInvalida(e)
            assertEquals(400, r.statusCode.value())
        }
    }

    @Test
    fun `formula sem valor em cache vira marca de ilegivel, com cache usa o valor, e erro desconhecido vira #ERRO`() {
        val l = PlanilhaXlsx.ler(xlsx(mapOf(1 to mapOf("A1" to FormulaXlsx("1+1", null), "B1" to FormulaXlsx("1+1", ""),
            "C1" to FormulaXlsx("1+1", "2"), "D1" to ErroXlsx("#FOO")))))[1]!!
        assertEquals(mapOf(0 to "#FORMULA", 1 to "#FORMULA", 2 to "2", 3 to "#ERRO"), l)
        assertTrue(PlanilhaXlsx.ilegivel("#FORMULA") && PlanilhaXlsx.ilegivel(" #ref! ") && !PlanilhaXlsx.ilegivel("REF") && !PlanilhaXlsx.ilegivel(null))
    }

    @Test
    fun `workbook e rels ausentes caem na sheet1, mas presentes e malformados sao erro de entrada`() {
        val ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
        fun zipado(vararg partes: Pair<String, String>): ByteArray {
            val saida = ByteArrayOutputStream()
            ZipOutputStream(saida).use { zip -> partes.forEach { (n, c) -> zip.putNextEntry(ZipEntry(n)); zip.write(c.toByteArray()); zip.closeEntry() } }
            return saida.toByteArray()
        }
        val aba = "xl/worksheets/sheet1.xml" to "<worksheet xmlns=\"$ns\"><sheetData><row r=\"1\"><c r=\"A1\"><v>7</v></c></row></sheetData></worksheet>"
        assertEquals("7", PlanilhaXlsx.ler(zipado(aba))[1]!![0])
        val rels = "xl/_rels/workbook.xml.rels" to "<Relationships/>"
        listOf(zipado(aba, "xl/workbook.xml" to "<workbook><sheets>", rels),
            zipado(aba, "xl/workbook.xml" to "<workbook xmlns=\"$ns\"><sheets><sheet name=\"a\"/></sheets></workbook>", "xl/_rels/workbook.xml.rels" to "<Relationships"),
            zipado(aba, "xl/workbook.xml" to "<workbook xmlns=\"$ns\"><sheets/></workbook>", rels)).forEach { bytes ->
            val e = assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) { PlanilhaXlsx.ler(bytes) }
            assertEquals(400, NotaFiscalLoteController(org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock())
                .onImportacaoInvalida(e).statusCode.value())
        }
    }

    @Test
    fun `formula sem cache e ilegivel em qualquer tipo de celula, e texto compartilhado que nao resolve tambem`() {
        val l = PlanilhaXlsx.ler(xlsx(mapOf(1 to mapOf(
            "A1" to CrusXlsx("t=\"s\"", "<f>1</f>"),            // s + formula sem <v>
            "B1" to CrusXlsx("t=\"str\"", "<f>1</f><v></v>"),   // str + formula, <v> vazio
            "C1" to CrusXlsx("t=\"b\"", "<f>TRUE()</f>"),       // b + formula sem <v>
            "D1" to CrusXlsx("t=\"n\"", "<f>1</f>"),            // n + formula sem <v>
            "E1" to CrusXlsx("t=\"s\"", "<v>99</v>"),           // indice fora da tabela
            "F1" to CrusXlsx("t=\"s\"", "<v>abc</v>"),          // indice nao numerico
            "G1" to CrusXlsx("t=\"s\"", ""),                    // s sem <v> e sem formula: vazia de verdade
            "H1" to CrusXlsx("t=\"str\"", "<f>1</f><v>ok</v>"), // formula com cache: vale o valor
        ))))[1]!!
        assertEquals(mapOf(0 to "#FORMULA", 1 to "#FORMULA", 2 to "#FORMULA", 3 to "#FORMULA", 4 to "#ERRO", 5 to "#ERRO", 7 to "ok"), l)
        assertTrue(PlanilhaXlsx.ilegivel(l[0]) && PlanilhaXlsx.ilegivel(l[4]))
    }

    @Test
    fun `so workbook ou so rels (um dos dois) e arquivo incompleto, e os dois ausentes caem na sheet1`() {
        val ns = "http://schemas.openxmlformats.org/spreadsheetml/2006/main"
        fun zipado(vararg partes: Pair<String, String>): ByteArray {
            val saida = ByteArrayOutputStream()
            ZipOutputStream(saida).use { zip -> partes.forEach { (n, c) -> zip.putNextEntry(ZipEntry(n)); zip.write(c.toByteArray()); zip.closeEntry() } }
            return saida.toByteArray()
        }
        val aba = "xl/worksheets/sheet1.xml" to "<worksheet xmlns=\"$ns\"><sheetData><row r=\"1\"><c r=\"A1\"><v>7</v></c></row></sheetData></worksheet>"
        val livro = "xl/workbook.xml" to "<workbook xmlns=\"$ns\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"><sheets><sheet name=\"a\" r:id=\"rId1\"/></sheets></workbook>"
        val rels = "xl/_rels/workbook.xml.rels" to "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Target=\"worksheets/sheet1.xml\"/></Relationships>"
        assertEquals("7", PlanilhaXlsx.ler(zipado(aba))[1]!![0])
        assertEquals("7", PlanilhaXlsx.ler(zipado(aba, livro, rels))[1]!![0])
        listOf(zipado(aba, livro), zipado(aba, rels)).forEach { bytes ->
            val e = assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) { PlanilhaXlsx.ler(bytes) }
            assertEquals("Arquivo .xlsx incompleto (falta workbook.xml ou workbook.xml.rels).", e.message)
            assertEquals(400, NotaFiscalLoteController(org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock(), org.mockito.kotlin.mock())
                .onImportacaoInvalida(e).statusCode.value())
        }
    }
}
