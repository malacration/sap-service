package br.andrew.sap.services.fiscal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class PlanilhaXlsxTest {

    companion object {
        /** Monta um .xlsx minimo: [linhas] = numero da linha -> celulas ("B11" -> valor). Texto vai como compartilhado. */
        fun xlsx(linhas: Map<Int, Map<String, Any>>, sheetXmlExtra: String = ""): ByteArray {
            val textos = mutableListOf<String>()
            val rows = linhas.entries.joinToString("") { (n, celulas) ->
                "<row r=\"$n\">" + celulas.entries.joinToString("") { (ref, v) ->
                    when (v) {
                        is Number -> "<c r=\"$ref\"><v>$v</v></c>"
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
}
