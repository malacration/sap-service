package br.andrew.sap.services.cadastro

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * A view e o filtro da varredura: ela devolve so os cadastros cujo nome tem minuscula, nos tres
 * tipos, em vez de o middleware paginar a base inteira pelo Service Layer.
 *
 * As regras dela sao documentadas e validadas aqui porque o proprio .sql nao pode ter comentario:
 * o Query.kt achata o SQL numa linha so antes de subir pro Service Layer, e um "--" comentaria
 * todo o resto da query (ver CLAUDE.md).
 */
class NormalizacaoDivergenteSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/${NormalizacaoCadastroService.VIEW_DIVERGENTES}"))

    @Test
    fun `sem comentario, que quebraria a view ao ser achatada em uma linha`() {
        assertFalse(sql.contains("--"), "comentario de linha comenta o resto da query inteira")
        assertFalse(sql.contains("/*"), "comentario de bloco corrompe o payload enviado ao SAP")
    }

    @Test
    fun `cobre os tres cadastros, cada um na sua tabela`() {
        assertTrue(sql.contains("\"OITM\""), "produtos")
        assertTrue(sql.contains("\"@RO_LOCAIS\""), "localidades - UDO Locais")
        assertTrue(sql.contains("\"OCRD\""), "clientes")
        assertTrue(Regex("UNION ALL").findAll(sql).count() == 2, "os tres em uma consulta so")
    }

    /** O filtro tem que estar no banco - e ele que evita paginar a base inteira. */
    @Test
    fun `compara com UPPER em cada um dos tres`() {
        assertTrue(sql.contains("\"OITM\".\"ItemName\" <> UPPER(\"OITM\".\"ItemName\")"))
        assertTrue(sql.contains("\"@RO_LOCAIS\".\"Name\" <> UPPER(\"@RO_LOCAIS\".\"Name\")"))
        assertTrue(sql.contains("\"OCRD\".\"CardName\" <> UPPER(\"OCRD\".\"CardName\")"))
    }

    /** Nome vazio nao e divergencia e nao pode entrar no relatorio. */
    @Test
    fun `descarta nome nulo ou vazio`() {
        assertTrue(Regex("IFNULL").findAll(sql).count() == 3)
    }

    /** Produto so do prefixo de venda, o mesmo filtro da calculadora. */
    @Test
    fun `filtra produto pelo prefixo parametrizado`() {
        assertTrue(sql.contains("\"OITM\".\"ItemCode\" LIKE :prefixo"))
    }

    /** As colunas tem que casar com o CadastroDivergente que o Jackson desserializa. */
    @Test
    fun `devolve tipo, codigo e nome`() {
        assertTrue(sql.contains("AS \"tipo\""))
        assertTrue(sql.contains("AS \"codigo\""))
        assertTrue(sql.contains("AS \"nome\""))
    }
}
