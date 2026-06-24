package br.andrew.sap.model.sap.documents.base

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Centraliza o fuso e o formato usados nos campos de controle do PIX
 * (U_pix_consultar_ate, U_pix_proxima_consulta_em).
 *
 * Os timestamps sao timezone-aware (OffsetDateTime) no fuso da JVM
 * (ZoneId.systemDefault()) e serializados com offset explicito
 * (ex.: 2026-06-28T23:59:59-04:00), para que o front converta sem ambiguidade
 * e para manter a ordenacao lexicografica usada nas comparacoes em SQL.
 */
object PixControleData {
    val ZONA: ZoneId = ZoneId.systemDefault()

    private val FORMATO: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")

    fun agora(): OffsetDateTime = OffsetDateTime.now(ZONA)

    fun formatar(valor: OffsetDateTime): String = valor.format(FORMATO)
}
