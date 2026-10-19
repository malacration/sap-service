package br.andrew.sap.schedules.futura

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * A entrega ja reclassificada nao pode ganhar um segundo VFET. Caso real: contrato 585, NF 73782,
 * VFEC 1502448 e 1516918 - o segundo consumiu de novo adiantamento do contrato.
 */
class DecisaoReclassificacaoTest {

    @Test
    fun `sem reclassificacao valida lanca o VFET`() {
        assertEquals(DecisaoReclassificacao.Criar, DecisaoReclassificacao.de(listOf()))
    }

    @Test
    fun `VFET ainda nao conciliado e reaproveitado`() {
        assertEquals(
            DecisaoReclassificacao.Reutilizar(10),
            DecisaoReclassificacao.de(listOf(ReclassificacaoAtiva(10, "VFET"))),
        )
    }

    @Test
    fun `entrega ja conciliada nao e reclassificada de novo`() {
        assertEquals(
            DecisaoReclassificacao.Pular(1502448),
            DecisaoReclassificacao.de(listOf(ReclassificacaoAtiva(1502448, "VFEC"))),
        )
    }

    @Test
    fun `conciliada prevalece sobre VFET pendente da mesma entrega`() {
        assertEquals(
            DecisaoReclassificacao.Pular(1502448),
            DecisaoReclassificacao.de(listOf(ReclassificacaoAtiva(1516918, "VFET"), ReclassificacaoAtiva(1502448, "VFEC"))),
        )
    }

    @Test
    fun `consulta identifica a entrega pelo Ref1 e Memo e ignora lancamento estornado`() {
        val sql = Files.readString(Path.of("src/main/resources/odbc/reclassificacao-entrega-ativa.sql"))

        assertTrue(sql.contains("lancamento.\"Ref1\" = :referencia"))
        assertTrue(sql.contains("lancamento.\"Memo\" = :memo"))
        assertTrue(sql.contains("lancamento.\"TransCode\" IN ('VFET', 'VFEC')"))
        assertTrue(sql.contains("lancamento.\"StornoToTr\" IS NULL"))
        assertTrue(sql.contains("estorno.\"StornoToTr\" = lancamento.\"TransId\""))
    }
}
