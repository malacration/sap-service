package br.andrew.sap.services.fiscal

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Leitor minimo de .xlsx (a primeira aba), sem dependencia: o arquivo e um zip de XMLs. Le so o
 * valor das celulas - texto compartilhado, texto inline, numero e o texto ja calculado de formula.
 * Data vem como numero (serial do Excel); quem le converte com [dataDoExcel].
 *
 * .xls (formato binario antigo) nao e lido: quem tiver um salva como .xlsx.
 *
 * Arquivo vindo do usuario: o parser recusa DOCTYPE (sem XXE) e o descompactado tem teto, contra
 * zip-bomba.
 */
object PlanilhaXlsx {

    private const val TETO_DESCOMPACTADO = 20L * 1024 * 1024
    // A planilha do banco tem dezenas de KB; isto e so o teto contra abuso.
    const val TETO_ARQUIVO = 10 * 1024 * 1024

    /** Linhas da primeira aba: numero da linha (1-based) -> celulas por coluna (0-based). */
    fun ler(arquivo: ByteArray): Map<Int, Map<Int, String>> {
        if (arquivo.size > TETO_ARQUIVO)
            throw IllegalArgumentException("A planilha e grande demais (limite de ${TETO_ARQUIVO / 1024 / 1024} MB).")
        val partes = descompacta(arquivo)
        val textos = partes["xl/sharedStrings.xml"]?.let(::textosCompartilhados) ?: listOf()
        val aba = primeiraAba(partes) ?: throw IllegalArgumentException("A planilha nao tem nenhuma aba.")
        return linhas(aba, textos)
    }

    /** Serial do Excel (dias desde 1899-12-30, sistema 1900) para data. */
    fun dataDoExcel(serial: Double): java.time.LocalDate =
        java.time.LocalDate.of(1899, 12, 30).plusDays(serial.toLong())

    private fun descompacta(arquivo: ByteArray): Map<String, ByteArray> {
        val partes = mutableMapOf<String, ByteArray>()
        var total = 0L
        try {
            ZipInputStream(ByteArrayInputStream(arquivo)).use { zip ->
                var entrada = zip.nextEntry
                val buffer = ByteArray(8192)
                while (entrada != null) {
                    val nome = entrada.name
                    val guarda = !entrada.isDirectory && (nome == "xl/sharedStrings.xml" || nome == "xl/workbook.xml" ||
                            nome == "xl/_rels/workbook.xml.rels" || nome.startsWith("xl/worksheets/sheet"))
                    // Toda entrada e lida e conta no teto, inclusive as ignoradas: pular uma entrada
                    // tambem descompacta, e uma comprimida enorme sairia do teto.
                    val saida = if (guarda) ByteArrayOutputStream() else null
                    while (true) {
                        val lidos = zip.read(buffer)
                        if (lidos < 0) break
                        total += lidos
                        if (total > TETO_DESCOMPACTADO)
                            throw IllegalArgumentException("A planilha e grande demais.")
                        saida?.write(buffer, 0, lidos)
                    }
                    if (saida != null)
                        partes[nome] = saida.toByteArray()
                    entrada = zip.nextEntry
                }
            }
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("Nao deu para abrir o arquivo como .xlsx.")
        }
        if (partes.isEmpty())
            throw IllegalArgumentException("O arquivo nao e uma planilha .xlsx (se for .xls, salve como .xlsx).")
        return partes
    }

    // A primeira aba na ordem do workbook.xml; sem ele, a sheet1.
    private fun primeiraAba(partes: Map<String, ByteArray>): ByteArray? {
        val alvo = runCatching {
            val livro = xml(partes.getValue("xl/workbook.xml"))
            val primeira = elementos(livro.documentElement, "sheet").first()
            val id = primeira.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id")
            val rels = xml(partes.getValue("xl/_rels/workbook.xml.rels"))
            val destino = elementos(rels.documentElement, "Relationship").first { it.getAttribute("Id") == id }
                .getAttribute("Target").removePrefix("/xl/").removePrefix("/")
            "xl/$destino"
        }.getOrNull()
        return alvo?.let { partes[it] } ?: partes["xl/worksheets/sheet1.xml"]
            ?: partes.entries.filter { it.key.startsWith("xl/worksheets/sheet") }.minByOrNull { it.key }?.value
    }

    private fun textosCompartilhados(conteudo: ByteArray): List<String> =
        elementos(xml(conteudo).documentElement, "si").map { si -> elementos(si, "t").joinToString("") { it.textContent } }

    private fun linhas(aba: ByteArray, textos: List<String>): Map<Int, Map<Int, String>> {
        val linhas = sortedMapOf<Int, Map<Int, String>>()
        elementos(xml(aba).documentElement, "row").forEachIndexed { i, row ->
            val numero = row.getAttribute("r").toIntOrNull() ?: (i + 1)
            val celulas = mutableMapOf<Int, String>()
            elementos(row, "c").forEachIndexed { j, c ->
                val coluna = coluna(c.getAttribute("r")) ?: j
                valor(c, textos)?.takeIf { it.isNotBlank() }?.let { celulas[coluna] = it.trim() }
            }
            if (celulas.isNotEmpty())
                linhas[numero] = celulas
        }
        return linhas
    }

    private fun valor(c: Element, textos: List<String>): String? {
        val v = elementos(c, "v").firstOrNull()?.textContent
        return when (c.getAttribute("t")) {
            "s" -> v?.toIntOrNull()?.let { textos.getOrNull(it) }
            "inlineStr" -> elementos(c, "t").joinToString("") { it.textContent }
            "e" -> null
            else -> v
        }
    }

    // "B11" -> 1
    private fun coluna(referencia: String): Int? {
        val letras = referencia.takeWhile { it.isLetter() }.uppercase()
        if (letras.isEmpty()) return null
        return letras.fold(0) { acc, ch -> acc * 26 + (ch - 'A' + 1) } - 1
    }

    private fun elementos(pai: Element, nome: String): List<Element> {
        val lista = pai.getElementsByTagNameNS("*", nome)
        return (0 until lista.length).map { lista.item(it) }.filter { it.nodeType == Node.ELEMENT_NODE }.map { it as Element }
    }

    private fun xml(conteudo: ByteArray) = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
        isXIncludeAware = false
        isExpandEntityReferences = false
    }.newDocumentBuilder().parse(ByteArrayInputStream(conteudo))
}
