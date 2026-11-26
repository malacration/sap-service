package br.andrew.sap.services.odbc

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

/**
 * Uma pagina de um .sql que expoe "Linha" (posicao, filtrada por :inicio < Linha <= :fim) e "Total"
 * em toda linha. O tamanho pedido e limitado a [maxSize] (nunca recusa); [maxRows] e o teto do
 * sap-odbc para a resposta e, sem ele, e o proprio tamanho da pagina (modos agrupados trazem mais
 * linhas que o tamanho, porque paginam por grupo).
 */
fun OdbcClient.consultarPagina(sql: String, params: Map<String, Any?>, page: Pageable, maxSize: Int, maxRows: Int? = null): Page<Map<String, Any?>> {
    val pagina = PageRequest.of(page.pageNumber, page.pageSize.coerceIn(1, maxSize))
    val inicio = pagina.offset
    val linhas = consultar(sql, params + mapOf("inicio" to inicio, "fim" to inicio + pagina.pageSize), maxRows ?: pagina.pageSize).rows
    // Pagina alem da ultima volta vazia e sem total: conta de novo pela primeira linha (primeiro grupo, nos agrupados: por isso o mesmo maxRows).
    val total = linhas.firstOrNull()?.let(::total)
        ?: if (inicio == 0L) 0L else consultar(sql, params + mapOf("inicio" to 0L, "fim" to 1L), maxRows ?: 1).rows.firstOrNull()?.let(::total) ?: 0L
    return PageImpl(linhas, pagina, total)
}

private fun total(linha: Map<String, Any?>): Long = linha["Total"].toString().toBigDecimal().toLong()
