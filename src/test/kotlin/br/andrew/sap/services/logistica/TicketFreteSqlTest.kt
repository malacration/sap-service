package br.andrew.sap.services.logistica

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

/**
 * As regras da view do relatorio de ticket medio de frete moram aqui: comentario
 * dentro do .sql quebra o provisionamento (ver views/README.md).
 */
class TicketFreteSqlTest {

    private val pasta = Path.of("src/main/resources/views/frete")

    private val sql = Files.readString(pasta.resolve("ticket-medio-frete-localidade.sql"))

    private val sqlQuantidade = Files.readString(pasta.resolve("quantidade-itens-frete-localidade.sql"))

    @Test
    fun `sem comentario SQL`() {
        listOf(sql, sqlQuantidade).forEach {
            assertFalse(it.contains("--"), "comentario quebra a view depois do achatamento em uma linha")
            assertFalse(it.contains("/*"), "comentario de bloco tambem nao e aceito")
        }
    }

    @Test
    fun `sem construcao sem precedente no parser do SQLQueries`() {
        // Mesma lista de CobrancaDashboardSqlTest: essas construcoes ja quebraram o
        // provisionamento em producao e nao aparecem em nenhuma outra view do projeto.
        val proibidas = listOf(
            "CASE", "IFNULL", "COALESCE", "DAYS_BETWEEN", "CURRENT_DATE",
            "CAST(", "UNION", "MIN(", "MAX(",
        )
        listOf(sql, sqlQuantidade).forEach { view ->
            val maiuscula = view.uppercase()
            proibidas.forEach {
                assertFalse(maiuscula.contains(it), "a view usa '$it', sem precedente nas views do projeto")
            }
        }
    }

    @Test
    fun `quantidade sai numa view separada, sobre as mesmas notas`() {
        // Juntar INV1 (linhas do produto) e INV3 (linhas de despesa) no mesmo SELECT
        // multiplicaria as linhas: cada linha de produto apareceria uma vez por linha de
        // despesa, inflando as duas somas. Por isso a quantidade vem de uma view propria,
        // e o recorte de notas com frete e feito com EXISTS em vez de join.
        assertFalse(sqlQuantidade.contains("INV3 E ON"), "a view de quantidade nao pode dar join em INV3")
        assertTrue(sqlQuantidade.contains("EXISTS"))
        assertTrue(sqlQuantidade.contains("E.\"ExpnsCode\" = 1"))
        assertTrue(sqlQuantidade.contains("E.\"LineTotal\" > 0"), "mesmo recorte da view de frete")
        assertTrue(sqlQuantidade.contains("sum(D.\"Quantity\") AS \"Quantidade\""))
        listOf("NS.\"CANCELED\" = 'N'", ":startDate", ":finalDate", ":filial", ":filialIsFilter").forEach {
            assertTrue(sqlQuantidade.contains(it), "a view de quantidade precisa do mesmo recorte da de frete: $it")
        }
    }

    @Test
    fun `frete e a despesa de codigo 1 da nota`() {
        // Mesmo codigo que AdditionalExpenses.frete grava no pedido (expenseCode = 1) e que
        // DocumentForAngular.validaFreteParaEntrega soma pra conferir o valor enviado.
        // Somar TotalExpns do cabecalho pegaria qualquer outra despesa junto.
        assertTrue(sql.contains("\"ExpnsCode\" = 1"), "o frete tem que sair de INV3 com ExpnsCode 1")
        // Linha de frete zerada e nota sem frete: entra no relatorio inflando o denominador
        // (mais notas, mesmo frete) e derruba o ticket medio da localidade.
        assertTrue(sql.contains("E.\"LineTotal\" > 0"), "nota sem valor de frete fica de fora")
        assertTrue(sql.contains("sum(E.\"LineTotal\") AS \"TotalFrete\""))
        assertFalse(sql.contains("TotalExpns"), "TotalExpns inclui despesas que nao sao frete")
    }

    @Test
    fun `localidade vem da nota, nao do cadastro atual do cliente`() {
        // INV12 e a extensao de endereco da propria nota: o resultado do relatorio nao muda
        // quando alguem corrige a localidade do endereco do cliente depois do faturamento.
        assertTrue(sql.contains("INV12"))
        assertTrue(sql.contains("EE.\"U_LocalidadeS\""))
        assertFalse(sql.contains("CRD1"), "CRD1 e o cadastro atual do cliente, nao a foto da nota")
    }

    @Test
    fun `conta nota distinta, nao linha de despesa`() {
        // Uma nota pode ter mais de uma linha de despesa de frete; sem DISTINCT ela contaria
        // duas vezes no denominador e derrubaria o ticket medio da localidade.
        assertTrue(sql.contains("count(DISTINCT NS.\"DocEntry\") AS \"Notas\""))
    }

    @Test
    fun `nao divide dentro do SQL nem inclui nota cancelada`() {
        // A divisao (ticket medio) fica em TicketFreteLocalidade, em BigDecimal com 2 casas.
        assertFalse(sql.contains("/"), "a divisao do ticket medio e feita em Kotlin")
        assertTrue(sql.contains("NS.\"CANCELED\" = 'N'"))
    }
}
