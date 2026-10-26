package br.andrew.sap.services.logistica

import br.andrew.sap.model.logistica.DetalheNecessidade
import br.andrew.sap.model.logistica.TipoDocumentoNecessidade
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.math.BigDecimal

class NecessidadeProducaoServiceTest {

    private val sqlDepositos = SqlResource.carregar("odbc/necessidade-producao-depositos.sql")
    private val sqlNecessidade = SqlResource.carregar("odbc/necessidade-producao.sql")
    private val sqlPedidos = SqlResource.carregar("odbc/necessidade-producao-pedidos.sql")
    private val sqlSemPrevisao = SqlResource.carregar("odbc/necessidade-producao-sem-previsao.sql")

    private val depositos = listOf(
        deposito("500.01", 2, "120"),
        deposito("500.08", 2, "5"),
        deposito("501.06", 4, "30"),
        deposito("502.06", 11, "0"),
    )

    private fun deposito(whs: String, filial: Int, estoque: String) =
        mapOf("WhsCode" to whs, "WhsName" to "DEP $whs", "BPLId" to filial.toString(), "BPLName" to "F$filial", "Estoque" to estoque)

    private fun linha(item: String, estoque: String, pedido: String, vf: String = "0", sf: String = "0") = mapOf(
        "ItemCode" to item, "ItemName" to "PRODUTO $item", "Grupo" to "PREMIUM", "Unidade" to "SACA 30KG",
        "Linha" to "premium", "LinhaNome" to "OX PREMIUM", "LinhaOrdem" to "4",
        "Estoque" to estoque, "Pedido" to pedido, "ContratoVendaFutura" to vf, "SimplesFaturamento" to sf,
    )

    private val odbc = mock<OdbcClient> {
        on { consultar(eq(sqlDepositos), any(), any()) } doReturn QueryResponse(rows = depositos)
        on { consultar(eq(sqlNecessidade), any(), any()) } doReturn QueryResponse(rows = listOf(
            linha("PAC0000072", "233", "225"),
            linha("PAC0000115", "870", "1445", vf = "1000", sf = "758"),
            linha("PAC0000077", "168", "150"),
        ))
        on { consultar(eq(sqlPedidos), any(), any()) } doReturn QueryResponse(rows = listOf(
            documento("PEDIDO", 71746, saldo = "100", contrato = null),
            documento("PEDIDO", 72619, saldo = "5", contrato = "29"),
        ))
        on { consultar(eq(sqlSemPrevisao), any(), any()) } doReturn QueryResponse(rows = listOf(
            documento("VENDA_FUTURA", 29, saldo = "30", contrato = "29", deposito = null),
            documento("SIMPLES_FATURAMENTO", 9001, saldo = "1278", contrato = null),
        ))
    }

    private fun documento(tipo: String, doc: Int, saldo: String, contrato: String?, deposito: String? = "500.01") = mapOf(
        "Tipo" to tipo, "DocEntry" to doc.toString(), "DocNum" to doc.toString(), "Data" to "2026-04-14",
        "Previsao" to null, "CardCode" to "CLI1", "CardName" to "CLIENTE", "BPLId" to "2", "BPLName" to "Matriz",
        "Deposito" to deposito, "Vendedor" to "Dulce", "ContratoVendaFutura" to contrato,
        "Quantidade" to "80", "Entregue" to "50", "EmPedido" to "0", "Saldo" to saldo,
    )

    private fun parametros(sql: String): Map<String, Any?> {
        val captor = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(eq(sql), captor.capture(), any())
        return captor.firstValue
    }

    private fun service(excluidos: List<String> = listOf("500.08"), utilizacoes: List<Int> = listOf(16)) =
        NecessidadeProducaoService(odbc, excluidos, utilizacoes)

    private fun parametrosDaNecessidade(): Map<String, Any?> {
        val captor = argumentCaptor<Map<String, Any?>>()
        verify(odbc).consultar(eq(sqlNecessidade), captor.capture(), any())
        return captor.firstValue
    }

    @Test
    fun `deposito excluido por configuracao nao aparece nem entra na soma`() {
        assertEquals(listOf("500.01", "501.06", "502.06"), service().depositos("PAC", "PAC9").map { it.whsCode })

        service().consultar("PAC", "PAC9", listOf(), listOf("500.01", "500.08"))

        assertEquals(listOf("500.01"), parametrosDaNecessidade()["depositos"])
    }

    @Test
    fun `filial recorta so a venda e nao limita os depositos do estoque`() {
        val resultado = service().consultar("PAC", "PAC9", listOf(11), listOf("500.01", "502.06"))

        val params = parametrosDaNecessidade()
        assertEquals(listOf("500.01", "502.06"), params["depositos"])
        assertEquals(listOf(11), params["filiais"])
        assertEquals(listOf(11), resultado.filiais)
    }

    @Test
    fun `sem filial marcada a venda soma todas as filiais com deposito, menos a filial zero`() {
        val fixture = depositos + deposito("teste", 0, "1")
        val odbcSemFilial = mock<OdbcClient> {
            on { consultar(eq(sqlDepositos), any(), any()) } doReturn QueryResponse(rows = fixture)
            on { consultar(eq(sqlNecessidade), any(), any()) } doReturn QueryResponse(rows = listOf())
        }
        val resultado = NecessidadeProducaoService(odbcSemFilial, listOf("500.08"), listOf(16))
            .consultar("PAC", "PAC9", listOf(), listOf())

        assertEquals(listOf(2, 4, 11), resultado.filiais)
        assertEquals(listOf("500.01", "501.06", "502.06", "teste"), resultado.depositos)
    }

    @Test
    fun `deposito fora da faixa ou filial desconhecida e recusado`() {
        val deposito = assertThrows<NecessidadeProducaoException> {
            service().consultar("PAC", "PAC9", listOf(), listOf("999.99"))
        }
        assertTrue(deposito.message.contains("Nenhum deposito"))

        val filial = assertThrows<NecessidadeProducaoException> {
            service().consultar("PAC", "PAC9", listOf(99), listOf())
        }
        assertTrue(filial.message.contains("Nenhuma filial"))
    }

    @Test
    fun `estoque vai pelo deposito e a venda pela filial do documento`() {
        assertTrue(sqlNecessidade.contains("estoque.\"WhsCode\" IN (:depositos)"))
        assertTrue(sqlNecessidade.contains("pedido.\"BPLId\" IN (:filiais)"))
        assertTrue(sqlNecessidade.contains("nota.\"BPLId\" IN (:filiais)"))
        assertTrue(sqlNecessidade.contains("contrato.\"U_filial\" IN (:filiais)"))
        assertEquals(1, Regex(":depositos").findAll(sqlNecessidade.lineSequence().filterNot { it.trim().startsWith("--") }.joinToString("\n")).count())
    }

    @Test
    fun `ordena pelo a fabricar, menor primeiro, e soma o sem previsao`() {
        val linhas = service().consultar("PAC", "PAC9", listOf(), listOf()).linhas

        assertEquals(listOf("PAC0000115", "PAC0000072", "PAC0000077"), linhas.map { it.itemCode })
        assertEquals(BigDecimal("-575"), linhas[0].aFabricar)
        assertEquals(BigDecimal("1758"), linhas[0].semPrevisao)
        assertEquals("OX PREMIUM", linhas[0].linhaNome)
        assertEquals(4, linhas[0].linhaOrdem)
    }

    @Test
    fun `manda exatamente os binds que o sql usa - o sap-odbc recusa parametro sobrando`() {
        val placeholder = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)")
        service().consultar("PAC", "PAC9", listOf(), listOf())

        val usados = placeholder.findAll(sqlNecessidade).map { it.groupValues[1] }.toSet()
        assertEquals(usados, parametrosDaNecessidade().keys)
        assertEquals(setOf("itemDe", "itemAte"), placeholder.findAll(sqlDepositos).map { it.groupValues[1] }.toSet())
    }

    @Test
    fun `sem utilizacao configurada o in nao vai vazio`() {
        service(utilizacoes = listOf()).consultar("PAC", "PAC9", listOf(), listOf())
        assertEquals(listOf(-1), parametrosDaNecessidade()["utilizacoes"])
    }

    @Test
    fun `faixa invertida e normalizada e faixa vazia e recusada`() {
        service().consultar(" pac9 ", "PAC", listOf(), listOf())
        val params = parametrosDaNecessidade()
        assertEquals("PAC", params["itemDe"])
        assertEquals("PAC9", params["itemAte"])

        assertThrows<NecessidadeProducaoException> { service().depositos("", "PAC9") }
    }

    @Test
    fun `detalhe do pedido usa o mesmo recorte de filiais do relatorio`() {
        val detalhe = service().detalhe(" pac0000252 ", DetalheNecessidade.PEDIDO, listOf(2))

        val params = parametros(sqlPedidos)
        assertEquals("PAC0000252", params["itemCode"])
        assertEquals(listOf(2), params["filiais"])
        assertEquals(listOf(16), params["utilizacoes"])
        assertEquals(listOf(71746, 72619), detalhe.documentos.map { it.docNum })
        assertEquals(29, detalhe.documentos[1].contratoVendaFutura)
        assertEquals(BigDecimal("105"), detalhe.total)
    }

    @Test
    fun `detalhe do sem previsao traz contrato e nota de simples faturamento`() {
        val detalhe = service().detalhe("PAC0000252", DetalheNecessidade.SEM_PREVISAO, listOf())

        assertEquals(listOf(2, 4, 11), parametros(sqlSemPrevisao)["filiais"])
        assertEquals(
            listOf(TipoDocumentoNecessidade.VENDA_FUTURA, TipoDocumentoNecessidade.SIMPLES_FATURAMENTO),
            detalhe.documentos.map { it.tipo },
        )
        assertEquals(null, detalhe.documentos[0].deposito)
        assertEquals(BigDecimal("1308"), detalhe.total)
    }

    @Test
    fun `sqls do detalhe recebem exatamente os binds que usam`() {
        val placeholder = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)")
        service().detalhe("PAC1", DetalheNecessidade.PEDIDO, listOf())
        service().detalhe("PAC1", DetalheNecessidade.SEM_PREVISAO, listOf())

        for (sql in listOf(sqlPedidos, sqlSemPrevisao))
            assertEquals(placeholder.findAll(sql).map { it.groupValues[1] }.toSet(), parametros(sql).keys)
    }

    @Test
    fun `detalhe repete os filtros da celula que abriu`() {
        assertTrue(sqlPedidos.contains("IFNULL(linha.\"Usage\", -1) NOT IN (:utilizacoes)"))
        assertTrue(sqlSemPrevisao.contains("linha.\"Usage\" IN (:utilizacoes)"))
        assertTrue(sqlSemPrevisao.contains("contrato.\"U_status\" = 'aberto'"))
        assertTrue(sqlSemPrevisao.contains("linha.\"BaseType\" <> 203"))
        assertTrue(sqlSemPrevisao.contains("WHERE vf.\"Saldo\" > 0"))
    }

    @Test
    fun `pedido de simples faturamento sai do pedido e a nota dele entra no sem previsao`() {
        assertTrue(sqlNecessidade.contains("IFNULL(linha.\"Usage\", -1) NOT IN (:utilizacoes)"))
        assertTrue(sqlNecessidade.contains("linha.\"Usage\" IN (:utilizacoes)"))
        assertTrue(sqlNecessidade.contains("contrato.\"U_status\" = 'aberto'"))
        assertTrue(sqlNecessidade.contains("item.\"frozenFor\" = 'N'"))
    }
}

class NecessidadeProducaoJsonTest {
    @Test
    fun `colunas calculadas saem com o nome que o front le`() {
        val json = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper().writeValueAsString(
            br.andrew.sap.model.logistica.NecessidadeProducaoLinha(
                "PAC1", "P", null, null, null, null, null, BigDecimal("10"), BigDecimal("15"), BigDecimal("1"), BigDecimal("2"),
            )
        )
        assertTrue(json.contains("\"aFabricar\":-5"), json)
        assertTrue(json.contains("\"semPrevisao\":3"), json)
        assertTrue(!json.contains("afabricar"), json)
    }
}
