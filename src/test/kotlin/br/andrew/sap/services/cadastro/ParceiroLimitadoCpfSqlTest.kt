package br.andrew.sap.services.cadastro

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * View do /business-partners/search-fornecedor (e do search-vendedor, que apesar do nome usa a mesma).
 * A busca de transportadora da ordem de carregamento depende dela trazer so fornecedor: ela nasceu como
 * busca de cliente ('C') e virou 'S' em 1c41c16. Voltar para cliente faria a tela listar clientes como
 * transportadora sem nenhum erro - se o search-vendedor precisar de cliente, ele ganha uma view propria.
 *
 * Regras documentadas aqui porque o .sql nao pode ter comentario (CLAUDE.md).
 */
class ParceiroLimitadoCpfSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/parceiro-limitado-cpf.sql"))

    @Test
    fun `sem comentario, que quebraria a view ao ser achatada em uma linha`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }

    @Test
    fun `filtra so fornecedor`() {
        assertTrue(sql.contains("\"OCRD\".\"CardType\" = 'S'"))
        assertFalse(sql.contains("\"CardType\" = 'C'"))
    }
}
