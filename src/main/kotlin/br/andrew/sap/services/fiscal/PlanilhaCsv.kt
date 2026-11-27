package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.ImportacaoInvalidaException
import br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Leitor de .csv de exportacao de banco, no mesmo formato de [PlanilhaXlsx.ler]: numero do registro
 * (1-based) -> celulas por coluna (0-based), sem as vazias. Aceita UTF-8 (com ou sem BOM) e
 * Windows-1252 (UTF-8 estrito primeiro; se o arquivo nao for UTF-8 valido, e 1252), virgula ou
 * ponto e virgula, aspas com "" escapado e quebra de linha dentro das aspas.
 */
object PlanilhaCsv {

    // CSV e texto puro (o xlsx e zip): 200 titulos ocupam ~100 KB, entao 1 MB ja sobra e protege a memoria.
    const val TETO_CSV = 1024 * 1024

    fun ler(arquivo: ByteArray): Map<Int, Map<Int, String>> {
        val linhas = linkedMapOf<Int, Map<Int, String>>()
        percorrer(arquivo) { numero, celulas -> linhas[numero] = celulas }
        return linhas
    }

    // Entrega cada registro nao vazio assim que fecha; quem recebe pode lancar para parar a leitura.
    fun percorrer(arquivo: ByteArray, visitante: (Int, Map<Int, String>) -> Unit) {
        if (arquivo.size > TETO_CSV)
            throw ArquivoGrandeDemaisException("A planilha e grande demais (limite de 1 MB para arquivo CSV).")
        val texto = decodifica(arquivo).removePrefix("﻿")
        val separador = separador(texto)
        var numero = 1
        var celulas = mutableMapOf<Int, String>()
        val atual = StringBuilder()
        var coluna = 0
        var aspas = false
        var linhaAspas = 0
        var fechouAspas = false

        fun fechaCelula() {
            atual.toString().trim().takeIf { it.isNotEmpty() }?.let { celulas[coluna] = it }
            atual.setLength(0)
            fechouAspas = false
            coluna++
        }
        fun fechaRegistro() {
            fechaCelula()
            if (celulas.isNotEmpty()) visitante(numero, celulas)
            celulas = mutableMapOf()
            coluna = 0
            numero++
        }

        var i = 0
        while (i < texto.length) {
            val c = texto[i]
            when {
                // Depois do fecha-aspas so cabem separador, fim de registro ou espaco ("81"8 e invalido).
                fechouAspas && c != separador && c != '\r' && c != '\n' && !c.isWhitespace() ->
                    throw ImportacaoInvalidaException("CSV invalido: caractere inesperado depois das aspas na linha $numero")
                aspas && c == '"' && texto.getOrNull(i + 1) == '"' -> { atual.append('"'); i++ }
                // Aspas so abre no comeco da celula; no meio do texto e caractere comum (O"BRIEN, 5").
                c == '"' && aspas -> { aspas = false; fechouAspas = true }
                c == '"' && atual.isBlank() -> { aspas = true; linhaAspas = numero }
                aspas -> atual.append(c)
                c == separador -> fechaCelula()
                c == '\r' -> {}
                c == '\n' -> fechaRegistro()
                else -> atual.append(c)
            }
            i++
        }
        if (aspas) throw ImportacaoInvalidaException("CSV invalido: aspas sem fechamento na linha $linhaAspas")
        if (atual.isNotEmpty() || coluna > 0) fechaRegistro()
    }

    private fun decodifica(arquivo: ByteArray): String = try {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(arquivo)).toString()
    } catch (e: CharacterCodingException) {
        String(arquivo, Charset.forName("windows-1252"))
    }

    // O separador vem da linha de cabecalho (a que tem 2+ titulos conhecidos); sem cabecalho reconhecivel, conta
    // virgulas e pontos e virgulas fora das aspas nas primeiras linhas.
    private fun separador(texto: String): Char {
        for (linha in texto.lineSequence().take(200)) {
            val v = titulosConhecidos(linha, ',')
            val pv = titulosConhecidos(linha, ';')
            if (maxOf(v, pv) >= 2) return if (pv > v) ';' else ','
        }
        var virgulas = 0
        var pontosEVirgulas = 0
        var aspas = false
        for (c in texto.take(8192)) {
            when {
                c == '"' -> aspas = !aspas
                aspas -> {}
                c == ',' -> virgulas++
                c == ';' -> pontosEVirgulas++
            }
        }
        return if (pontosEVirgulas > virgulas) ';' else ','
    }

    private val TITULOS = setOf("nosso numero", "seu numero", "nro beneficiario", "numero da nota", "nota", "nf", "numero nf",
        "nome pagador", "sacado", "nome do parceiro", "parceiro", "cliente", "valor", "valor (r$)", "vencimento", "parcela", "situacao")

    private fun titulosConhecidos(linha: String, separador: Char): Int {
        val celulas = mutableListOf<String>()
        val atual = StringBuilder()
        var aspas = false
        for (c in linha) {
            when {
                c == '"' -> aspas = !aspas
                c == separador && !aspas -> { celulas.add(atual.toString()); atual.setLength(0) }
                else -> atual.append(c)
            }
        }
        celulas.add(atual.toString())
        return celulas.count { NotaFiscalLoteImportacaoService.normaliza(it) in TITULOS }
    }
}
