package br.andrew.sap.services.odbc

import org.springframework.core.io.ClassPathResource

/**
 * Carrega um .sql de `resources/odbc/` para enviar ao sap-odbc.
 *
 * Tira os comentarios ANTES de enviar: o ReadOnlySqlValidator do sap-odbc recusa
 * instrucao que contenha comentario (defesa contra esconder trecho da query de quem
 * a le, inclusive da auditoria). O arquivo pode e deve ser comentado; o que viaja, nao.
 *
 * Nada a ver com a proibicao de comentario nas views do Service Layer (ver CLAUDE.md): la o
 * problema e o achatamento em linha unica, que faz o `--` comentar o resto da query.
 * Aqui a quebra de linha e preservada e o comentario e removido de verdade.
 *
 * So comentario de LINHA (dois hifens) e removido. Comentario de BLOCO (barra-asterisco)
 * sobreviveria a limpeza e cairia na recusa do validador: nao use nos .sql desta pasta.
 */
object SqlResource {

    fun carregar(caminho: String): String {
        val bruto = ClassPathResource(caminho).inputStream.bufferedReader().use { it.readText() }
        return bruto.lineSequence()
            .map { linha -> linha.substringBefore("--").trimEnd() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }
}
