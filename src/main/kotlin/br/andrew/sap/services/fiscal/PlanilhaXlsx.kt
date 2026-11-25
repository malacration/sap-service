package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.ImportacaoInvalidaException
import br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException
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

    // Marcas que o leitor devolve no lugar de celula com erro do Excel ou formula sem valor em cache.
    private val MARCAS_ILEGIVEL = setOf("#N/A", "#VALUE!", "#REF!", "#DIV/0!", "#NAME?", "#NUM!", "#NULL!", "#SPILL!", "#CALC!",
        "#GETTING_DATA", "#ERRO", "#FORMULA")

    fun ilegivel(texto: String?): Boolean = texto?.trim()?.uppercase() in MARCAS_ILEGIVEL

    /** Linhas da primeira aba: numero da linha (1-based) -> celulas por coluna (0-based). */
    fun ler(arquivo: ByteArray): Map<Int, Map<Int, String>> {
        if (arquivo.size > TETO_ARQUIVO)
            throw ArquivoGrandeDemaisException("A planilha e grande demais (limite de ${TETO_ARQUIVO / 1024 / 1024} MB).")
        val partes = descompacta(arquivo)
        val textos = partes["xl/sharedStrings.xml"]?.let(::textosCompartilhados) ?: listOf()
        val aba = primeiraAba(partes) ?: throw ImportacaoInvalidaException("A planilha nao tem nenhuma aba.")
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
                            throw ArquivoGrandeDemaisException("A planilha e grande demais.")
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
            throw ImportacaoInvalidaException("Nao deu para abrir o arquivo como .xlsx.")
        }
        if (partes.isEmpty())
            throw ImportacaoInvalidaException("O arquivo nao e uma planilha .xlsx (se for .xls, salve como .xlsx).")
        return partes
    }

    // A primeira aba na ordem do workbook.xml. So se workbook.xml E rels NAO existem cai na sheet1; existindo so um, existindo
    // e malformado (ou sem aba/destino), o arquivo e invalido (400), sem chute.
    private fun primeiraAba(partes: Map<String, ByteArray>): ByteArray? {
        val livroXml = partes["xl/workbook.xml"]
        val relsXml = partes["xl/_rels/workbook.xml.rels"]
        if (livroXml == null && relsXml == null)
            return partes["xl/worksheets/sheet1.xml"]
                ?: partes.entries.filter { it.key.startsWith("xl/worksheets/sheet") }.minByOrNull { it.key }?.value
        // So um dos dois: arquivo incompleto (nao chuta a aba).
        if (livroXml == null || relsXml == null)
            throw ImportacaoInvalidaException("Arquivo .xlsx incompleto (falta workbook.xml ou workbook.xml.rels).")
        val semAba = ImportacaoInvalidaException("A planilha nao tem nenhuma aba.")
        val primeira = elementos(xml(livroXml).documentElement, "sheet").firstOrNull() ?: throw semAba
        val id = primeira.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id")
        val destino = elementos(xml(relsXml).documentElement, "Relationship").firstOrNull { it.getAttribute("Id") == id }
            ?.getAttribute("Target")?.removePrefix("/xl/")?.removePrefix("/") ?: throw semAba
        return partes["xl/$destino"] ?: throw semAba
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
        // Formula sem valor em cache: ilegivel qualquer que seja o tipo da celula (s, str, b, n...), antes de interpretar o tipo.
        if (elementos(c, "f").isNotEmpty() && v.isNullOrBlank()) return "#FORMULA"
        return when (c.getAttribute("t")) {
            // Texto compartilhado: sem <v> e celula vazia (null); com <v> que nao resolve (nao numerico / fora da tabela) e ilegivel.
            "s" -> if (v.isNullOrBlank()) null else textos.getOrNull(v.trim().toIntOrNull() ?: -1) ?: "#ERRO"
            "inlineStr" -> elementos(c, "t").joinToString("") { it.textContent }
            // Celula com erro do Excel (#N/A, #VALUE!...): fica como marca de erro, nunca como vazia (quem le a chave
            // ou o nome trata como ilegivel em vez de "sem parcela"/nome).
            "e" -> v?.trim()?.uppercase()?.takeIf { it in MARCAS_ILEGIVEL } ?: "#ERRO"
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

    // XML malformado (ou com DOCTYPE) e arquivo invalido: erro de entrada (400), nao 500.
    private fun xml(conteudo: ByteArray) = try {
        DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            isXIncludeAware = false
            isExpandEntityReferences = false
        }.newDocumentBuilder().parse(ByteArrayInputStream(conteudo))
    } catch (e: org.xml.sax.SAXException) {
        throw ImportacaoInvalidaException("Nao deu para ler o conteudo da planilha (arquivo .xlsx danificado).")
    }
}
