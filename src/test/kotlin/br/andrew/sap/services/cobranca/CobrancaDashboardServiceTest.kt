package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.cobranca.CobrancaAgregadoSap
import br.andrew.sap.model.cobranca.CobrancaRecuperadoDiaSap
import br.andrew.sap.model.cobranca.CobrancaRecuperadoSap
import br.andrew.sap.model.cobranca.CobrancaTrabalhadosSap
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate

class CobrancaDashboardServiceTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val service = CobrancaDashboardService(sqlQueriesService)

    private val hoje = LocalDate.of(2026, 8, 3)
    private val de = LocalDate.of(2026, 8, 1)
    private val ate = LocalDate.of(2026, 8, 3)

    private val vendedor = User(
        "60", "Fulano", UserOriginEnum.SalePerson, "fulano",
        bussinesPlace = listOf(), roles = listOf("vendedor")
    )
    private val admin = User(
        "1", "Admin", UserOriginEnum.EmployeesInfo, "admin",
        bussinesPlace = listOf(), roles = listOf("admin")
    )

    // getAll e inline: ele expande pra execute(...)!!.tryGetNextValues(), entao quem e
    // mockado aqui e o execute. E como tem "!!", qualquer view nao mockada estoura NPE -
    // por isso todas as dez comecam vazias e cada teste sobrescreve so a que lhe interessa.
    @BeforeEach
    fun todasAsViewsVaziasPorPadrao() {
        listOf(
            "cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql",
            "cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql",
            "cobranca-recuperado-diario.sql", "cobranca-recuperado-diario-adiantamento.sql",
            "cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql",
            "cobranca-promessa-vencida.sql", "cobranca-promessa-vencida-adiantamento.sql",
            "cobranca-trabalhados.sql", "cobranca-trabalhados-adiantamento.sql",
        ).forEach { view ->
            whenever(sqlQueriesService.execute(eq(view), any<List<Parameter>>())).thenReturn(odata())
        }
    }

    @Test
    fun `as quatro faixas de aging viram janelas de vencimento sem buraco e sem sobreposicao`() {
        // Faixa de atraso nao sai de CASE WHEN (o parser do SQLQueries nao aceita): cada
        // faixa e uma janela de DueDate. Se as bordas nao encostarem exatamente, titulo
        // desaparece do total ou e contado duas vezes - e o total do dashboard mente.
        service.resumo(admin, de = de, ate = ate, hoje = hoje)

        val janelas = janelasDeVencimento()
        assertEquals(4, janelas.size)
        // Comeca em 1 dia de atraso: parcela que vence hoje (2026-08-03) nao esta em atraso, e a
        // tela de titulos ja exclui essas por padrao - com 0 aqui, clicar na faixa abriria uma
        // lista menor que o card.
        assertEquals("2026-07-04" to "2026-08-02", janelas[0])
        assertEquals("2026-06-04" to "2026-07-03", janelas[1])
        assertEquals("2026-05-05" to "2026-06-03", janelas[2])
        assertEquals("1900-01-01" to "2026-05-04", janelas[3])
    }

    @Test
    fun `periodo anterior tem a mesma duracao e termina um dia antes do periodo pedido`() {
        // Sem isso o delta compararia janelas de tamanhos diferentes - um mes cheio contra tres
        // dias - e a variacao percentual viraria ficcao.
        service.resumo(admin, de = LocalDate.of(2026, 8, 1), ate = LocalDate.of(2026, 8, 31), hoje = hoje)

        val janelas = janelasDeRecuperado()
        assertEquals(2, janelas.size)
        assertEquals("2026-08-01" to "2026-08-31", janelas[0])
        // 31 dias contra 31 dias (1 a 31 de julho), terminando na vespera de 1/ago.
        assertEquals("2026-07-01" to "2026-07-31", janelas[1])
    }

    @Test
    fun `periodo anterior de um dia so tambem tem um dia so`() {
        service.resumo(admin, de = LocalDate.of(2026, 8, 3), ate = LocalDate.of(2026, 8, 3), hoje = hoje)

        assertEquals("2026-08-02" to "2026-08-02", janelasDeRecuperado()[1])
    }

    @Test
    fun `resumo devolve as datas da janela anterior pra tela poder rotular a comparacao`() {
        val resumo = service.resumo(admin, de = LocalDate.of(2026, 8, 1), ate = LocalDate.of(2026, 8, 31), hoje = hoje)

        assertEquals("2026-07-01", resumo.DeAnterior)
        assertEquals("2026-07-31", resumo.AteAnterior)
    }

    @Test
    fun `recuperado do periodo e do anterior nao se misturam`() {
        // As duas chamadas usam a MESMA view, so mudando :de/:ate - se o Kotlin trocasse os
        // resultados, o delta apareceria invertido e ninguem notaria.
        val captor = argumentCaptor<List<Parameter>>()
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado.sql"), captor.capture()))
            .thenReturn(odata(recuperado(cobrador = "MARCELA", valor = "1000.00", documentos = 2)))
            .thenReturn(odata(recuperado(cobrador = "MARCELA", valor = "400.00", documentos = 1)))

        val resumo = service.resumo(admin, de = de, ate = ate, hoje = hoje)

        assertEquals(BigDecimal("1000.00"), resumo.Recuperado)
        assertEquals(BigDecimal("400.00"), resumo.RecuperadoAnterior)
    }

    @Test
    fun `faixa aberta usa piso largo em vez de omitir o parametro`() {
        service.resumo(admin, de = de, ate = ate, hoje = hoje)

        // A view recebe sempre vencimentoDe e vencimentoAte; a faixa "+90" nao pode
        // simplesmente nao mandar o piso, senao o parametro fica sem valor no SAP.
        assertEquals("1900-01-01", janelasDeVencimento().last().first)
    }

    @Test
    fun `saldo desconta o pago e sobrevive a PaidToDate ausente`() {
        // O SQL nao subtrai (sem IFNULL, um PaidToDate nulo envenenaria a soma do grupo
        // inteiro): a view devolve Total e Pago separados e a conta acontece em Kotlin.
        whenever(sqlQueriesService.execute(eq("cobranca-carteira.sql"), any<List<Parameter>>()))
            .thenReturn(odata(agregado(total = "100.00", pago = "30.00", parcelas = 2)))
        whenever(sqlQueriesService.execute(eq("cobranca-carteira-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odata(agregado(total = "50.00", pago = null, parcelas = 1)))

        val resumo = service.resumo(admin, de = de, ate = ate, hoje = hoje)

        // 4 faixas x (100-30) + 4 faixas x (50-0) = 4 x 120
        assertEquals(BigDecimal("480.00"), resumo.CarteiraSaldo)
        assertEquals(12, resumo.CarteiraParcelas)
    }

    @Test
    fun `nota fiscal e adiantamento somam no mesmo total`() {
        // UNION nao passa no parser do SQLQueries, entao sao views separadas - se o Kotlin
        // esquecer de somar uma delas o total do dashboard nao fecha com a tela de titulos.
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado.sql"), any<List<Parameter>>()))
            .thenReturn(odata(recuperado(cobrador = "MARCELA", valor = "1000.00", documentos = 3)))
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odata(recuperado(cobrador = "MARCELA", valor = "250.00", documentos = 1)))

        val resumo = service.resumo(admin, de = de, ate = ate, hoje = hoje)

        assertEquals(BigDecimal("1250.00"), resumo.Recuperado)
        assertEquals(4, resumo.RecuperadoDocumentos)
        assertEquals(1, resumo.PorCobrador.size)
        assertEquals(BigDecimal("1250.00"), resumo.PorCobrador.first().Recuperado)
    }

    @Test
    fun `vendedor comum tem o escopo forcado para o proprio codigo`() {
        service.resumo(vendedor, vendedor = 99, de = de, ate = ate, hoje = hoje)

        val parametros = primeirosParametros("cobranca-carteira.sql")
        assertEquals(60, parametros["vendedor"])
        assertEquals(-1, parametros["vendedorIsFilter"])
    }

    @Test
    fun `admin sem escolher vendedor nao restringe`() {
        service.resumo(admin, de = de, ate = ate, hoje = hoje)

        val parametros = primeirosParametros("cobranca-carteira.sql")
        assertEquals(Int.MAX_VALUE, parametros["vendedorIsFilter"])
        assertEquals(Int.MAX_VALUE, parametros["filialIsFilter"])
    }

    @Test
    fun `porCobrador junta esforco e resultado, e nao perde quem trabalhou sem recuperar`() {
        // O caso que importa pro gestor: distinguir "trabalhou e nao recuperou" de "nao
        // trabalhou". Se a juncao fosse so pelas linhas de recuperado, a LAURA sumiria.
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado.sql"), any<List<Parameter>>()))
            .thenReturn(odata(recuperado(cobrador = "MARCELA", valor = "800.00", documentos = 2)))
        whenever(sqlQueriesService.execute(eq("cobranca-trabalhados.sql"), any<List<Parameter>>()))
            .thenReturn(odata(trabalhados("MARCELA", 5), trabalhados("LAURA", 7)))

        val porCobrador = service.resumo(admin, de = de, ate = ate, hoje = hoje).PorCobrador

        assertEquals(listOf("MARCELA", "LAURA"), porCobrador.map { it.Cobrador })
        assertEquals(5, porCobrador.first { it.Cobrador == "MARCELA" }.TitulosTrabalhados)
        val laura = porCobrador.first { it.Cobrador == "LAURA" }
        assertEquals(7, laura.TitulosTrabalhados)
        assertEquals(BigDecimal.ZERO, laura.Recuperado)
    }

    @Test
    fun `cobrador e status ausentes ganham rotulo em vez de virar chave vazia`() {
        whenever(sqlQueriesService.execute(eq("cobranca-carteira.sql"), any<List<Parameter>>()))
            .thenReturn(odata(agregado(total = "10.00", pago = "0.00", parcelas = 1, status = null)))
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado.sql"), any<List<Parameter>>()))
            .thenReturn(odata(recuperado(cobrador = null, valor = "5.00", documentos = 1)))

        val resumo = service.resumo(admin, de = de, ate = ate, hoje = hoje)

        assertEquals("Sem acompanhamento", resumo.PorStatus.first().Status)
        assertEquals("Sem cobrador", resumo.PorCobrador.first().Cobrador)
    }

    @Test
    fun `evolucao devolve um ponto por mes, inclusive mes sem pagamento`() {
        // Mes zerado tem que aparecer: buraco na serie faz o leitor achar que o mes nao
        // foi medido, em vez de entender que nao entrou nada.
        whenever(sqlQueriesService.execute(eq("cobranca-recuperado-diario.sql"), any<List<Parameter>>()))
            .thenReturn(odata(dia("20260715", "100.00"), dia("20260720", "50.00"), dia("20260802", "700.00")))

        val meses = service.evolucao(admin, meses = 3, hoje = hoje)

        assertEquals(listOf("2026-06", "2026-07", "2026-08"), meses.map { it.Mes })
        assertEquals(listOf("jun/26", "jul/26", "ago/26"), meses.map { it.Rotulo })
        assertEquals(BigDecimal.ZERO, meses[0].Recuperado)
        assertEquals(BigDecimal("150.00"), meses[1].Recuperado)
        assertEquals(BigDecimal("700.00"), meses[2].Recuperado)
    }

    @Test
    fun `evolucao busca a janela inteira numa consulta so, nao uma por mes`() {
        service.evolucao(admin, meses = 6, hoje = hoje)

        verify(sqlQueriesService, times(1))
            .execute(eq("cobranca-recuperado-diario.sql"), any<List<Parameter>>())
        val parametros = primeirosParametros("cobranca-recuperado-diario.sql")
        assertEquals("2026-03-01", parametros["de"])
        assertEquals("2026-08-03", parametros["ate"])
    }

    @Test
    fun `evolucao limita o numero de meses pedido`() {
        service.evolucao(admin, meses = 999, hoje = hoje)

        val parametros = primeirosParametros("cobranca-recuperado-diario.sql")
        // 24 meses e o teto: sem limite um ?meses=9999 viraria uma varredura da base inteira.
        assertEquals("2024-09-01", parametros["de"])
    }

    @Test
    fun `promessa vencida e sem acao usam a data de hoje, nao o periodo pedido`() {
        // Carteira, promessa e "sem acao" sao foto de HOJE; recuperado e trabalhado sao do
        // periodo. Trocar um pelo outro faria o card de SLA responder pelo mes passado.
        service.resumo(admin, de = LocalDate.of(2026, 1, 1), ate = LocalDate.of(2026, 1, 31), hoje = hoje)

        assertEquals("2026-08-03", primeirosParametros("cobranca-sem-acao.sql")["data"])
        assertEquals("2026-08-03", primeirosParametros("cobranca-promessa-vencida.sql")["data"])
        assertEquals("2026-01-01", primeirosParametros("cobranca-recuperado.sql")["de"])
        assertEquals("2026-01-31", primeirosParametros("cobranca-recuperado.sql")["ate"])
    }

    @Test
    fun `filial por saldo vem ordenada da maior para a menor`() {
        whenever(sqlQueriesService.execute(eq("cobranca-carteira.sql"), any<List<Parameter>>()))
            .thenReturn(
                odata(
                    agregado(total = "10.00", pago = "0.00", parcelas = 1, bplId = 2, bplName = "Vilhena"),
                    agregado(total = "90.00", pago = "0.00", parcelas = 1, bplId = 6, bplName = "Serra Verde"),
                )
            )

        val porFilial = service.resumo(admin, de = de, ate = ate, hoje = hoje).PorFilial

        assertEquals(listOf(6, 2), porFilial.map { it.BPLId })
        assertTrue(porFilial.first().Saldo > porFilial.last().Saldo)
    }

    private fun janelasDeVencimento(): List<Pair<String, String>> {
        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService, times(4)).execute(eq("cobranca-carteira.sql"), captor.capture())
        return captor.allValues.map { parametros ->
            val mapa = parametros.associate { it.key to it.value }
            mapa["vencimentoDe"].toString() to mapa["vencimentoAte"].toString()
        }
    }

    // As duas janelas (periodo pedido e anterior) na ordem em que foram pedidas ao SAP.
    private fun janelasDeRecuperado(): List<Pair<String, String>> {
        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService, times(2)).execute(eq("cobranca-recuperado.sql"), captor.capture())
        return captor.allValues.map { parametros ->
            val mapa = parametros.associate { it.key to it.value }
            mapa["de"].toString() to mapa["ate"].toString()
        }
    }

    private fun primeirosParametros(view: String): Map<String, Any> {
        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService, org.mockito.kotlin.atLeastOnce()).execute(eq(view), captor.capture())
        return captor.firstValue.associate { it.key to it.value }
    }

    private fun agregado(
        total: String?,
        pago: String?,
        parcelas: Int,
        status: String? = "1 - NÃO INICIADO",
        cobrador: String? = null,
        bplId: Int? = 6,
        bplName: String? = "Fazenda Serra Verde",
    ) = CobrancaAgregadoSap(
        BPLId = bplId, BPLName = bplName,
        U_Status = status, U_Cobrador = cobrador,
        Total = total?.let { BigDecimal(it) }, Pago = pago?.let { BigDecimal(it) },
        Parcelas = parcelas,
    )

    private fun recuperado(cobrador: String?, valor: String, documentos: Int) = CobrancaRecuperadoSap(
        BPLId = 6, BPLName = "Fazenda Serra Verde", U_Cobrador = cobrador,
        Recuperado = BigDecimal(valor), Documentos = documentos,
    )

    private fun trabalhados(usuario: String, titulos: Int) = CobrancaTrabalhadosSap(usuario, titulos)

    private fun dia(docDate: String, valor: String) =
        CobrancaRecuperadoDiaSap(DocDate = docDate, Recuperado = BigDecimal(valor), Documentos = 1)

    private fun odata(vararg linhas: Any): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = linhas.toList()
        return OData(backing)
    }
}
