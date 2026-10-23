package br.andrew.sap.services.cadastro

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * View da trava de CPF/CNPJ duplicado do cadastro de cliente pela tela (cardCodeDoCpfCnpj).
 * Diferente da parceiro.sql, nao pode depender do prefixo do CardCode: cliente com outra serie
 * passaria pela trava. E o documento pode estar gravado com ou sem mascara.
 *
 * Regras documentadas aqui porque o .sql nao pode ter comentario (CLAUDE.md).
 */
class ParceiroClientePorDocumentoSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/parceiro-cliente-por-documento.sql"))

    @Test
    fun `sem comentario, que quebraria a view ao ser achatada em uma linha`() {
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }

    @Test
    fun `nao depende do prefixo do CardCode e filtra so cliente`() {
        assertFalse(sql.contains("CLI"), "prefixo deixaria cliente de outra serie passar pela trava")
        assertTrue(sql.contains("\"CardType\" = 'C'"))
    }

    @Test
    fun `procura o documento com e sem mascara nos dois campos`() {
        for (campo in listOf("TaxId0", "TaxId4"))
            for (parametro in listOf(":comMascara", ":semMascara"))
                assertTrue(sql.contains("documento.\"$campo\" = $parametro"), "$campo x $parametro")
    }
}
