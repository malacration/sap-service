package br.andrew.sap.comercial

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.comercial.painel.Granularidade
import br.andrew.sap.model.comercial.painel.PainelFiltro
import br.andrew.sap.services.comercial.PainelAcessoException
import br.andrew.sap.services.comercial.PainelVendasV2Service
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate

/**
 * Foco: o ESCOPO EFETIVO e a REGRA DE CANCELAMENTO.
 *
 * O painel expõe faturamento consolidado, entao um erro de escopo aqui vaza
 * receita entre filiais ou entre vendedores. E a regra de cancelamento define se
 * um mes ja fechado muda de valor - comportamento que o time definiu
 * explicitamente e que nao pode regredir sem alguem perceber.
 */
class PainelVendasV2ServiceTest {
    @Test
    fun `consultas ODBC nao sao publicadas como SQLQueries do Service Layer`() {
        val resources = org.springframework.core.io.support.PathMatchingResourcePatternResolver()
            .getResources("classpath:/views/**/*.sql")
        assertFalse(resources.any { it.filename in setOf("painel-vendas-kpis.sql", "painel-vendas-evolucao.sql") })
        assertTrue(org.springframework.core.io.ClassPathResource("odbc/painel-vendas-kpis.sql").exists())
    }

    @Test
    fun `comparativo usa o periodo ja alinhado pelo SQL inclusive na semana`() {
        val client = mock<OdbcClient>()
        whenever(client.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = listOf(
            mapOf("SERIE" to "ATUAL", "PERIODO" to "2026-01-05", "FATURAMENTO" to "200", "QTD_FATURAS" to 2),
            mapOf("SERIE" to "ANTERIOR", "PERIODO" to "2026-01-05", "FATURAMENTO" to "150", "QTD_FATURAS" to 1),
            mapOf("SERIE" to "ANTERIOR", "PERIODO" to "2026-01-12", "FATURAMENTO" to "50", "QTD_FATURAS" to 1),
        )))
        val pontos = PainelVendasV2Service(client).evolucao(usuario(), filtro.copy(granularidade = Granularidade.SEMANA))
        assertEquals(listOf("2026-01-05", "2026-01-12"), pontos.map { it.periodo })
        assertEquals("150", pontos.first().anoAnterior!!.toPlainString())
        assertEquals(0, pontos.last().faturamento.signum())
        assertEquals("50", pontos.last().anoAnterior!!.toPlainString())
    }


    private fun usuario(
        id: String = "7",
        filiais: List<Int> = listOf(1, 2),
        roles: List<String> = listOf(),
        origin: UserOriginEnum = UserOriginEnum.SalePerson,
    ) = User(id, "Fulano", origin, "fulano", null, null, filiais, roles)

    private fun clienteQueDevolve(linha: Map<String, Any?>): OdbcClient {
        val client = mock<OdbcClient>()
        whenever(client.consultar(any(), any(), any()))
            .thenReturn(QueryResponse(rows = listOf(linha), rowCount = 1))
        return client
    }

    private val linhaVazia = mapOf<String, Any?>(
        "FATURAMENTO_BRUTO" to "0", "DEVOLUCOES" to "0",
        "FATURAMENTO_LIQUIDO" to "0", "QTD_FATURAS" to 0, "QTD_DEVOLUCOES" to 0,
        "CLIENTES_ATIVOS" to 0, "FILIAIS_COM_VENDA" to 0,
        "VALOR_PRODUTOS" to "0", "QTD_ITENS" to "0",
    )

    /** A serie temporal devolve colunas diferentes das dos KPIs. */
    private val linhaSerie = mapOf<String, Any?>(
        "SERIE" to "ATUAL", "PERIODO" to "2026-01", "FATURAMENTO" to "0", "QTD_FATURAS" to 0,
    )

    private val filtro = PainelFiltro(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))

    /** Remove comentarios `--` para assercoes sobre o SQL realmente executado. */
    private fun semComentarios(sql: String) =
        sql.lineSequence().filterNot { it.trimStart().startsWith("--") }.joinToString("\n")

    // ---------------------------------------------------- escopo de filial

    @Test
    fun `filial nao autorizada e descartada do recorte`() {
        val client = clienteQueDevolve(linhaVazia)
        val service = PainelVendasV2Service(client, listOf())

        service.kpis(usuario(filiais = listOf(1, 2)), filtro.copy(filiais = listOf(1, 99)))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(listOf(1), params.firstValue["filiais"], "a filial 99 nao podia entrar")
    }

    @Test
    fun `sem filial pedida usa todas as autorizadas`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(filiais = listOf(3, 4)), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(listOf(3, 4), params.firstValue["filiais"])
    }

    @Test
    fun `lista de filiais vazia no token nega o acesso`() {
        // Interpretar vazio como "todas" faria um usuario mal provisionado
        // enxergar a empresa inteira. Falha fechado, de proposito.
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(filiais = listOf()), filtro)
        }
    }

    @Test
    fun `pedir apenas filial proibida nao devolve dado de outra`() {
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(filiais = listOf(1)), filtro.copy(filiais = listOf(99)))
        }
    }

    // --------------------------------------------------- escopo de vendedor

    @Test
    fun `vendedor comum nao consegue consultar outro vendedor`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf())
            .kpis(usuario(id = "7", roles = listOf()), filtro.copy(slpCode = 99))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(7, params.firstValue["vendedor"], "devia ter ignorado o slpCode pedido")
        assertEquals(0, params.firstValue["todosVendedores"], "vendedor comum nunca ve todos")
    }

    // ------------------------------------------------------------ periodo

    @Test
    fun `periodo acima do teto da granularidade e recusado`() {
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        val umAno = PainelFiltro(
            LocalDate.of(2025, 1, 1), LocalDate.of(2026, 1, 1),
            granularidade = Granularidade.DIA,
        )
        val ex = assertThrows(PainelAcessoException::class.java) { service.kpis(usuario(), umAno) }
        assertTrue(ex.message.contains("excede"), ex.message)
    }

    @Test
    fun `data final antes da inicial e recusada`() {
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(), filtro.copy(dataInicio = LocalDate.of(2026, 2, 1)))
        }
    }

    // ------------------------------------------------------------ calculo

    @Test
    fun `ticket medio e nulo quando nao ha faturas`() {
        // Acontece de verdade: periodo vazio, ou venda e cancelamento se
        // anulando dentro do mesmo recorte. Melhor vazio que divisao por zero.
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertNull(service.kpis(usuario(), filtro).ticketMedio)
    }

    @Test
    fun `decimal vindo como string nao perde precisao`() {
        // O sap-odbc serializa BigDecimal como string. Ler como Double
        // corromperia centavos em valor financeiro.
        val client = clienteQueDevolve(
            linhaVazia + mapOf("FATURAMENTO_BRUTO" to "12345678901234.56", "QTD_FATURAS" to 2),
        )
        val kpis = PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)
        assertEquals("12345678901234.56", kpis.faturamentoBruto.toPlainString())
        assertEquals("6172839450617.28", kpis.ticketMedio!!.toPlainString())
    }

    // ------------------------------------------- regra de cancelamento (SQL)

    @Test
    fun `o SQL nao filtra CANCELED igual a N`() {
        // Esta e a regra que o time definiu: cancelamento NAO retroage. Filtrar
        // CANCELED='N' faria a venda sumir do mes em que aconteceu.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)

        val sql = argumentCaptor<String>()
        org.mockito.kotlin.verify(client).consultar(sql.capture(), any(), any())
        assertFalse(
            semComentarios(sql.firstValue).contains("\"CANCELED\" = 'N'"),
            "filtrar CANCELED='N' faz cancelamento retroagir no mes original",
        )
        assertTrue(sql.firstValue.contains("'C'"), "o documento de cancelamento precisa ser tratado")
    }

    @Test
    fun `granularidade de semana nao agrupa por numero da semana`() {
        // YEAR()+WEEK() quebra a semana que atravessa a virada do ano em duas.
        val client = clienteQueDevolve(linhaSerie)
        PainelVendasV2Service(client, listOf()).evolucao(
            usuario(), filtro.copy(granularidade = Granularidade.SEMANA),
        )
        val sql = argumentCaptor<String>()
        org.mockito.kotlin.verify(client).consultar(sql.capture(), any(), any())
        // So o SQL executavel importa: os comentarios do arquivo citam YEAR()+WEEK()
        // justamente para explicar por que NAO se usa isso.
        val executavel = semComentarios(sql.firstValue)
        assertFalse(executavel.contains("WEEK("), "agrupar por numero da semana quebra na virada do ano")
        assertTrue(executavel.contains("ADD_DAYS"), "esperado agrupamento pela data de inicio da semana")
    }

    // ----------------------------------- contrato com o validador do sap-odbc

    @Test
    fun `os parametros enviados batem EXATAMENTE com os do SQL`() {
        // O sap-odbc recusa parametro faltando E sobrando. Este teste pega a
        // classe de bug que derrubou os KPIs em producao: a evolucao ganhou
        // :dataInicioAnterior/:dataFimAnterior e o mapa fixo passou a mandar
        // essas datas tambem para a consulta de KPIs, que nao as usa.
        val client = clienteQueDevolve(linhaVazia + linhaSerie)
        val service = PainelVendasV2Service(client, listOf())
        service.kpis(usuario(), filtro)
        service.evolucao(usuario(), filtro)

        val sql = argumentCaptor<String>()
        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client, org.mockito.kotlin.times(2))
            .consultar(sql.capture(), params.capture(), any())

        val placeholder = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)")
        sql.allValues.forEachIndexed { i, consulta ->
            val noSql = placeholder.findAll(consulta).map { it.groupValues[1] }.toSet()
            val enviados = params.allValues[i].keys
            assertEquals(noSql, enviados, "consulta $i: parametros divergentes do SQL")
        }
    }

    @Test
    fun `o comparativo de ano anterior nao vai na consulta de KPIs`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertFalse("dataInicioAnterior" in params.firstValue.keys, "KPIs nao usam o comparativo")
        assertFalse("dataFimAnterior" in params.firstValue.keys)
    }

    @Test
    fun `a evolucao recebe o comparativo de ano anterior`() {
        val client = clienteQueDevolve(linhaSerie)
        PainelVendasV2Service(client, listOf()).evolucao(usuario(), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals("2025-01-01", params.firstValue["dataInicioAnterior"])
        assertEquals("2025-01-31", params.firstValue["dataFimAnterior"])
    }

    @Test
    fun `o SQL enviado nao contem comentarios`() {
        // O ReadOnlySqlValidator do sap-odbc REJEITA comentarios. Os arquivos .sql
        // sao comentados para quem os mantem; os comentarios nao podem viajar.
        // Sem este teste o painel quebraria so em producao - o mock nao valida SQL.
        val client = mock<OdbcClient>()
        whenever(client.consultar(any(), any(), any()))
            .thenReturn(QueryResponse(rows = listOf(linhaVazia + linhaSerie), rowCount = 1))
        val service = PainelVendasV2Service(client, listOf())
        service.kpis(usuario(), filtro)
        service.evolucao(usuario(), filtro)

        val sql = argumentCaptor<String>()
        org.mockito.kotlin.verify(client, org.mockito.kotlin.times(2)).consultar(sql.capture(), any(), any())
        sql.allValues.forEach {
            assertFalse(it.contains("--"), "comentario de linha seria recusado pelo sap-odbc")
            assertFalse(it.contains("/*"), "comentario de bloco seria recusado pelo sap-odbc")
            assertFalse(it.trimEnd().endsWith(";"), "ponto e virgula final e desnecessario")
        }
    }

    @Test
    fun `vendedor_admin NAO ganha todas as filiais`() {
        // superVendedor() trata vendedor_admin como admin PARA VENDEDOR, mas a
        // liberacao de filial e so do papel `admin`.
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(filiais = listOf(), roles = listOf("vendedor_admin")), filtro)
        }
    }

    @Test
    fun `admin que pede um vendedor especifico filtra de verdade`() {
        // `SlpCode < Int.MAX_VALUE` e verdadeiro para qualquer linha: usar isso
        // como filtro fazia o vendedor pedido por um admin ser ignorado.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf())
            .kpis(usuario(roles = listOf("vendedor_admin")), filtro.copy(slpCode = 99))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(99, params.firstValue["vendedor"])
        assertEquals(0, params.firstValue["todosVendedores"], "deveria restringir ao vendedor 99")
    }

    // ------------------------------------------------- admin ve tudo (v2)

    @Test
    fun `admin ve todas as filiais mesmo sem filial cadastrada`() {
        // Decisao do time: o papel admin libera tudo, sem depender de HEM10,
        // do UDO de vendedor ou de papel `filial-N` no Keycloak. E o caso que
        // antes travava o usuario com a mensagem de "sem filial liberada".
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(filiais = listOf(), roles = listOf("admin")), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(1, params.firstValue["todasFiliais"], "admin nao deve ter restricao de filial")
        assertEquals(1, params.firstValue["todosVendedores"], "admin ve todos os vendedores")
    }

    @Test
    fun `a lista de filiais nunca vai vazia para o SQL`() {
        // `IN ()` e erro de sintaxe no HANA: o flag desliga o filtro, a lista
        // continua com um placeholder que nunca casa.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(filiais = listOf(), roles = listOf("admin")), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        val filiais = params.firstValue["filiais"] as List<*>
        assertTrue(filiais.isNotEmpty(), "IN () quebraria a consulta")
    }

    @Test
    fun `admin que filtra filial especifica tem o filtro respeitado`() {
        // Para admin o filtro deixa de ser limite e vira escolha - mas continua valendo.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf())
            .kpis(usuario(filiais = listOf(), roles = listOf("admin")), filtro.copy(filiais = listOf(5, 9)))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(0, params.firstValue["todasFiliais"])
        assertEquals(listOf(5, 9), params.firstValue["filiais"])
    }

    @Test
    fun `admin que e vendedor tambem ve todos por padrao`() {
        // Diferenca DELIBERADA em relacao ao painel v1, onde um admin que fosse
        // SalePerson caia na propria carteira. Aqui admin significa acesso total.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(
            usuario(id = "7", roles = listOf("admin"), origin = UserOriginEnum.SalePerson), filtro,
        )
        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(1, params.firstValue["todosVendedores"])
    }

    @Test
    fun `admin ainda consegue filtrar um vendedor especifico`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf())
            .kpis(usuario(roles = listOf("admin")), filtro.copy(slpCode = 42))

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(42, params.firstValue["vendedor"])
        assertEquals(0, params.firstValue["todosVendedores"])
    }

    @Test
    fun `NAO-admin sem filial continua bloqueado`() {
        // A liberacao vale so para admin: vazio continua negando para os demais,
        // senao um usuario mal provisionado veria a empresa inteira.
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
        assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(filiais = listOf(), roles = listOf("vendedor")), filtro)
        }
    }

    // ------------------------------------------- falha alta em dado inesperado

    @Test
    fun `coluna ausente na resposta falha em vez de virar zero`() {
        // Zero silencioso produz KPI plausivel e falso - pior que erro visivel.
        val client = clienteQueDevolve(linhaVazia - "FATURAMENTO_BRUTO")
        val ex = assertThrows(IllegalStateException::class.java) {
            PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)
        }
        assertTrue(ex.message!!.contains("FATURAMENTO_BRUTO"), ex.message!!)
    }

    @Test
    fun `valor nao numerico falha em vez de virar zero`() {
        val client = clienteQueDevolve(linhaVazia + mapOf("FATURAMENTO_BRUTO" to "N/D"))
        assertThrows(IllegalStateException::class.java) {
            PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)
        }
    }

    // -------------------------------------- allowlist de filiais do ambiente

    @Test
    fun `allowlist do ambiente limita ate o admin`() {
        // E limite de deploy, nao permissao de usuario: nem admin passa dele.
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf(1, 2)).kpis(usuario(roles = listOf("admin")), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(0, params.firstValue["todasFiliais"], "com allowlist nao existe 'todas'")
        assertEquals(listOf(1, 2), params.firstValue["filiais"])
    }

    @Test
    fun `allowlist do ambiente corta filial do usuario`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf(2)).kpis(usuario(filiais = listOf(1, 2, 3)), filtro)

        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(listOf(2), params.firstValue["filiais"], "1 e 3 nao estao no ambiente")
    }

    @Test
    fun `usuario sem intersecao com o ambiente e bloqueado`() {
        val service = PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf(9))
        val ex = assertThrows(PainelAcessoException::class.java) {
            service.kpis(usuario(filiais = listOf(1, 2)), filtro)
        }
        assertTrue(ex.message.contains("ambiente"), ex.message)
    }

    @Test
    fun `allowlist vazia nao restringe nada`() {
        val client = clienteQueDevolve(linhaVazia)
        PainelVendasV2Service(client, listOf()).kpis(usuario(filiais = listOf(4, 7)), filtro)
        val params = argumentCaptor<Map<String, Any?>>()
        org.mockito.kotlin.verify(client).consultar(any(), params.capture(), any())
        assertEquals(listOf(4, 7), params.firstValue["filiais"])
    }

    // ------------------------------------------------------- KPIs novos

    @Test
    fun `ticket medio por cliente divide pelo numero de clientes`() {
        val client = clienteQueDevolve(linhaVazia + mapOf(
            "FATURAMENTO_BRUTO" to "1000.00", "QTD_FATURAS" to 4, "CLIENTES_ATIVOS" to 2))
        val kpis = PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)
        assertEquals("250.00", kpis.ticketMedio!!.toPlainString(), "1000 / 4 notas")
        assertEquals("500.00", kpis.ticketMedioPorCliente!!.toPlainString(), "1000 / 2 clientes")
    }

    @Test
    fun `preco medio divide valor de produto pela quantidade`() {
        val client = clienteQueDevolve(linhaVazia + mapOf(
            "VALOR_PRODUTOS" to "900.00", "QTD_ITENS" to "45"))
        val kpis = PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro)
        assertEquals("20.00", kpis.precoMedio!!.toPlainString())
        assertFalse(kpis.precoMedioConfiavel, "no painel inteiro sempre mistura unidades de medida")
    }

    @Test
    fun `preco medio e nulo quando a quantidade nao e positiva`() {
        // Devolucao maior que venda no recorte: dividir daria preco negativo.
        val client = clienteQueDevolve(linhaVazia + mapOf(
            "VALOR_PRODUTOS" to "-100.00", "QTD_ITENS" to "-5"))
        assertNull(PainelVendasV2Service(client, listOf()).kpis(usuario(), filtro).precoMedio)
    }

    @Test
    fun `ticket por cliente e nulo sem cliente ativo`() {
        assertNull(PainelVendasV2Service(clienteQueDevolve(linhaVazia), listOf())
            .kpis(usuario(), filtro).ticketMedioPorCliente)
    }

    @Test
    fun `granularidade sai de whitelist e nunca do texto do cliente`() {
        val client = clienteQueDevolve(linhaSerie)
        val service = PainelVendasV2Service(client, listOf())
        Granularidade.entries.forEach { g ->
            service.evolucao(usuario(), filtro.copy(granularidade = g))
        }
        val sql = argumentCaptor<String>()
        org.mockito.kotlin.verify(client, org.mockito.kotlin.times(3)).consultar(sql.capture(), any(), any())
        sql.allValues.forEach { assertFalse(it.contains("__TRUNC__"), "fragmento nao substituido") }
    }
}
