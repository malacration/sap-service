package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.cobranca.CobrancaAdiantamentoSap
import br.andrew.sap.model.cobranca.CobrancaTituloSap
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CobrancaConsultaServiceTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val service = CobrancaConsultaService(sqlQueriesService)

    private val vendedor = User(
        "60", "Fulano", UserOriginEnum.SalePerson, "fulano",
        bussinesPlace = listOf(), roles = listOf("vendedor")
    )
    private val admin = User(
        "1", "Admin", UserOriginEnum.EmployeesInfo, "admin",
        bussinesPlace = listOf(), roles = listOf("admin")
    )

    @BeforeEach
    fun semAdiantamentosPorPadrao() {
        // A maioria dos testes aqui e sobre faturas; sem isso o mock devolve null pro
        // getAll<CobrancaAdiantamentoSap> (viewName nao mockado) e todo teste quebra com NPE.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odataComAdiantamentos())
    }

    @Test
    fun `vendedor comum tem o filtro de vendedor forcado para o proprio codigo`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(vendedor)

        val parametros = capturarParametros()
        assertEquals(60, parametros["vendedor"])
        assertEquals(-1, parametros["vendedorIsFilter"])
    }

    @Test
    fun `admin sem escolher vendedor nao tem restricao de vendedor`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin)

        val parametros = capturarParametros()
        assertEquals(Int.MAX_VALUE, parametros["vendedorIsFilter"])
    }

    @Test
    fun `filial e cliente informados viram filtro obrigatorio`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, filiais = listOf(6), cliente = "CLI0007196")

        val parametros = capturarParametros()
        assertEquals(6, parametros["filial"])
        assertEquals(-1, parametros["filialIsFilter"])
        assertEquals("CLI0007196", parametros["cliente"])
        assertEquals("", parametros["clienteIsFilter"])
    }

    @Test
    fun `mais de uma filial vira uma consulta por filial, cada uma com o filtro ligado`() {
        // A view aceita um :filial so (lista fixa de BPLId nao tem precedente no parser do
        // SQLQueries) - multi-selecao na tela virou consulta repetida, e o resultado e a uniao.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, filiais = listOf(6, 7))

        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService, times(2)).execute(eq("cobranca-titulos.sql"), captor.capture())
        val filiaisConsultadas = captor.allValues.map { parametros -> parametros.first { it.key == "filial" }.value }
        assertEquals(listOf(6, 7), filiaisConsultadas)
        captor.allValues.forEach { parametros ->
            assertEquals(-1, parametros.first { it.key == "filialIsFilter" }.value)
        }
    }

    @Test
    fun `filial repetida na selecao nao vira consulta repetida no SAP`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, filiais = listOf(6, 6))

        verify(sqlQueriesService, times(1)).execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())
    }

    @Test
    fun `valor com acento nao vai pro SQLQueries - o SAP responde Parameter error`() {
        // Testado contra o Service Layer: status='8 - EM NEGOCIACAO' devolve 200, mas
        // status='8 - EM NEGOCIAÇÃO' devolve 400 code 704 "Parameter error." - e nao existe
        // encoding que resolva (UTF-8 e Latin-1 falham igual). O filtro entao sai do SQL e fica
        // so em passaNosFiltrosLocais, que compara o valor original.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, status = "8 - EM NEGOCIAÇÃO")

        val parametros = capturarParametros()
        assertEquals("~", parametros["status"])
        assertEquals(Int.MAX_VALUE, parametros["statusIsFilter"])
    }

    @Test
    fun `valor sem acento continua filtrando no SQL, com espaco e tudo`() {
        // Espaco o SAP aceita numa boa - so o nao-ASCII quebra. Nao pode virar filtro-em-Kotlin
        // no atacado, senao o laco de paginacao varre a base de 20 em 20 sem necessidade.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, status = "3 - SEM CONTATO", cobrador = "Pedro Colombo")

        val parametros = capturarParametros()
        assertEquals("3 - SEM CONTATO", parametros["status"])
        assertEquals(-1, parametros["statusIsFilter"])
        assertEquals("Pedro Colombo", parametros["cobrador"])
        assertEquals(-1, parametros["cobradorIsFilter"])
    }

    @Test
    fun `cobrador com acento no nome tambem sai do SQL em vez de estourar`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, cobrador = "Nilvia Conceição", situacao = "2 - A RECEBER")

        val parametros = capturarParametros()
        assertEquals("~", parametros["cobrador"])
        assertEquals(Int.MAX_VALUE, parametros["cobradorIsFilter"])
        // situacao sem acento segue no SQL
        assertEquals("2 - A RECEBER", parametros["situacao"])
        assertEquals(-1, parametros["situacaoIsFilter"])
    }

    @Test
    fun `filtro com acento ainda e aplicado - so muda o lugar onde a comparacao acontece`() {
        // O que nao pode acontecer e "saiu do SQL" virar "nao filtra nada" e a tela mostrar
        // titulo que nao casa com o status escolhido.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(
                titulo(diasAtraso = 30, status = "8 - EM NEGOCIAÇÃO"),
                titulo(diasAtraso = 30, status = "3 - SEM CONTATO"),
            ))

        val resultado = service.listar(admin, status = "8 - EM NEGOCIAÇÃO")

        assertEquals(1, resultado.size)
        assertEquals("8 - EM NEGOCIAÇÃO", resultado.first().U_Status)
    }

    @Test
    fun `filtrar por NAO INICIADO traz o titulo que ninguem trabalhou ainda`() {
        // "1 - NAO INICIADO" e rotulo que a tela exibe quando U_Status esta vazio - o titulo nunca
        // trabalhado nem tem registro na UDT. Comparar o texto literal nao acha nada, e era isso
        // que fazia o filtro voltar vazio.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(
                titulo(diasAtraso = 30, status = null),
                titulo(diasAtraso = 30, status = "8 - EM NEGOCIAÇÃO"),
            ))

        val resultado = service.listar(admin, status = "1 - NÃO INICIADO", incluirSemStatus = true)

        assertEquals(1, resultado.size)
        assertEquals(null, resultado.first().U_Status)
    }

    @Test
    fun `NAO INICIADO tambem casa com quem tem esse status gravado de verdade`() {
        // O valor existe no dominio (o seeder cria "1 - NAO INICIADO"), entao alguem pode ter
        // escolhido ele no modal. Os dois casos aparecem iguais na tela e devem vir juntos.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(
                titulo(diasAtraso = 30, status = null),
                titulo(diasAtraso = 30, status = "1 - NÃO INICIADO"),
                titulo(diasAtraso = 30, status = "3 - SEM CONTATO"),
            ))

        val resultado = service.listar(admin, status = "1 - NÃO INICIADO", incluirSemStatus = true)

        assertEquals(2, resultado.size)
    }

    @Test
    fun `incluirSemStatus desliga o filtro de status no SQL, senao o SAP descarta o U_Status nulo`() {
        // A view compara C."U_Status" = :status; com LEFT JOIN, U_Status nulo nunca casa - o SAP
        // ja teria jogado fora as linhas que esse filtro quer antes do Kotlin ver.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, status = "1 - NAO INICIADO SEM ACENTO", incluirSemStatus = true)

        val parametros = capturarParametros()
        assertEquals("~", parametros["status"])
        assertEquals(Int.MAX_VALUE, parametros["statusIsFilter"])
    }

    @Test
    fun `sem incluirSemStatus o status vazio nao entra no resultado`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(
                titulo(diasAtraso = 30, status = null),
                titulo(diasAtraso = 30, status = "3 - SEM CONTATO"),
            ))

        val resultado = service.listar(admin, status = "3 - SEM CONTATO")

        assertEquals(1, resultado.size)
        assertEquals("3 - SEM CONTATO", resultado.first().U_Status)
    }

    @Test
    fun `lista de filiais vazia e o mesmo que nao filtrar filial`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, filiais = emptyList())

        val parametros = capturarParametros()
        assertEquals(Int.MAX_VALUE, parametros["filialIsFilter"])
    }

    @Test
    fun `filial e cliente nao informados nao restringem`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin)

        val parametros = capturarParametros()
        assertEquals(Int.MAX_VALUE, parametros["filialIsFilter"])
        assertEquals("~", parametros["clienteIsFilter"])
    }

    @Test
    fun `diasAtrasoMin desloca o corte de vencimento no proprio SQL, sem parametro novo`() {
        // Atraso >= N e o mesmo que DueDate <= hoje - N. Resolver isso no :data (que a view ja
        // tem) evita que o laco de paginacao puxe pagina do SAP so pra descartar localmente.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, data = LocalDate.of(2026, 7, 28), diasAtrasoMin = 10)

        assertEquals("2026-07-18", capturarParametros()["data"])
    }

    @Test
    fun `sem diasAtrasoMin o corte de vencimento continua sendo a data pedida`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, data = LocalDate.of(2026, 7, 28), diasAtrasoMin = null)

        assertEquals("2026-07-28", capturarParametros()["data"])
    }

    @Test
    fun `situacaoSap vira filtro de Status da parcela no SQL`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, situacaoSap = "ABERTO")

        val parametros = capturarParametros()
        assertEquals("O", parametros["statusParcela"])
        assertEquals("", parametros["statusParcelaIsFilter"])
    }

    @Test
    fun `situacaoSap PAGO filtra a parcela fechada no SQL`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, situacaoSap = "PAGO")

        assertEquals("C", capturarParametros()["statusParcela"])
    }

    @Test
    fun `sem situacaoSap o filtro de Status da parcela fica desligado`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, situacaoSap = null)

        val parametros = capturarParametros()
        assertEquals("~", parametros["statusParcela"])
        assertEquals("~", parametros["statusParcelaIsFilter"])
    }

    @Test
    fun `intervalo de vencimento vai pro SQL com sentinela larga quando desligado`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin)

        val parametros = capturarParametros()
        assertEquals("1900-01-01", parametros["vencimentoDe"])
        assertEquals("9999-12-31", parametros["vencimentoAte"])
    }

    @Test
    fun `intervalo de vencimento informado vai pro SQL`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(
            admin,
            vencimentoDe = LocalDate.of(2026, 1, 1),
            vencimentoAte = LocalDate.of(2026, 6, 30),
        )

        val parametros = capturarParametros()
        assertEquals("2026-01-01", parametros["vencimentoDe"])
        assertEquals("2026-06-30", parametros["vencimentoAte"])
    }

    @Test
    fun `status cobrador e situacao viram filtro no SQL com escape em coluna nao-nula`() {
        // Esses tres vem do LEFT JOIN da UDT (nulos pra titulo nunca acompanhado), entao o
        // "desligado" nao pode ser testado na propria coluna - o escape e o DocEntry, que nunca
        // e nulo. Ligado => -1 (escape falso, so a igualdade vale, e nulo nao casa com nada).
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, status = "3 - SEM CONTATO", cobrador = "Marcela", situacao = "2 - A RECEBER")

        val parametros = capturarParametros()
        assertEquals("3 - SEM CONTATO", parametros["status"])
        assertEquals(-1, parametros["statusIsFilter"])
        assertEquals("Marcela", parametros["cobrador"])
        assertEquals(-1, parametros["cobradorIsFilter"])
        assertEquals("2 - A RECEBER", parametros["situacao"])
        assertEquals(-1, parametros["situacaoIsFilter"])
    }

    @Test
    fun `sem status cobrador e situacao o escape deixa passar tambem o titulo nunca acompanhado`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin)

        val parametros = capturarParametros()
        // Int.MAX_VALUE torna "DocEntry < :xIsFilter" verdadeiro pra toda linha - sem isso o
        // titulo com U_Status nulo (a maioria) desapareceria da lista.
        assertEquals(Int.MAX_VALUE, parametros["statusIsFilter"])
        assertEquals(Int.MAX_VALUE, parametros["cobradorIsFilter"])
        assertEquals(Int.MAX_VALUE, parametros["situacaoIsFilter"])
    }

    @Test
    fun `filtros de dias em atraso e status sao aplicados depois da consulta ao SAP`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComTitulos(
                    titulo(diasAtraso = 3, status = "8 - EM NEGOCIAÇÃO"),
                    titulo(diasAtraso = 20, status = "3 - SEM CONTATO"),
                )
            )

        val resultado = service.listar(admin, diasAtrasoMin = 10)

        assertEquals(1, resultado.size)
        assertEquals(20L, resultado.first().DiasAtraso)
    }

    @Test
    fun `por padrao (situacaoSap ABERTO) titulo ja quitado no SAP nao aparece`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComTitulos(
                    titulo(diasAtraso = 3, status = null, statusParcela = "C"), // quitado no SAP
                    titulo(diasAtraso = 5, status = null, statusParcela = "O"), // em aberto
                )
            )

        val resultado = service.listar(admin, situacaoSap = "ABERTO")

        assertEquals(1, resultado.size)
        assertEquals("ABERTO", resultado.first().SituacaoSap)
    }

    @Test
    fun `situacaoSap null (Todos) traz tanto pago quanto aberto`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComTitulos(
                    titulo(diasAtraso = 3, status = null, statusParcela = "C"),
                    titulo(diasAtraso = 5, status = null, statusParcela = "O"),
                )
            )

        val resultado = service.listar(admin, situacaoSap = null)

        assertEquals(2, resultado.size)
    }

    @Test
    fun `saldo negativo (rateio, adiantamento vinculado etc) nao classifica como pago se a parcela ainda esta aberta no SAP`() {
        // Bug reportado: InsTotal - PaidToDate pode dar negativo (parcela aparentemente
        // "paga a mais") mesmo com a parcela ainda 'O' (aberta) no SAP - a situacao tem que
        // seguir o status oficial da parcela, nunca o saldo calculado.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComTitulos(
                    titulo(diasAtraso = 5, status = null, paidToDate = BigDecimal("110.00"), statusParcela = "O"),
                )
            )

        val resultado = service.listar(admin, situacaoSap = null)

        assertEquals(1, resultado.size)
        assertEquals("ABERTO", resultado.first().SituacaoSap)
    }

    @Test
    fun `adiantamento entra na lista e usa o numero do contrato de venda futura como NF`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odataComAdiantamentos(adiantamento(diasAtraso = 5, contratoDocNum = 777)))

        val resultado = service.listar(admin)

        assertEquals(1, resultado.size)
        assertEquals("AD", resultado.first().Tipo)
        assertEquals(777, resultado.first().DocNum)
    }

    @Test
    fun `fatura e adiantamento com o mesmo DocEntry nao se confundem e saem ordenados por vencimento`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(titulo(diasAtraso = 5, status = null)))
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odataComAdiantamentos(adiantamento(diasAtraso = 10, contratoDocNum = null)))

        val resultado = service.listar(admin)

        assertEquals(2, resultado.size)
        // O adiantamento (10 dias de atraso, vence antes) precisa vir primeiro.
        assertEquals("AD", resultado[0].Tipo)
        assertEquals("NF", resultado[1].Tipo)
        assertEquals(1, resultado[0].DocEntry)
        assertEquals(1, resultado[1].DocEntry)
    }

    @Test
    fun `filtro tipo NF nao busca adiantamento`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>()))
            .thenReturn(odataComTitulos(titulo(diasAtraso = 5, status = null)))

        val resultado = service.listar(admin, tipo = "NF")

        assertEquals(1, resultado.size)
        assertEquals("NF", resultado.first().Tipo)
        verify(sqlQueriesService, never()).execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>())
    }

    @Test
    fun `filtro tipo AD nao busca fatura`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(odataComAdiantamentos(adiantamento(diasAtraso = 5, contratoDocNum = null)))

        val resultado = service.listar(admin, tipo = "AD")

        assertEquals(1, resultado.size)
        assertEquals("AD", resultado.first().Tipo)
        verify(sqlQueriesService, never()).execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())
    }

    @Test
    fun `adiantamento para de puxar pagina do SAP assim que ja tem o suficiente pra pagina pedida`() {
        // Antes o adiantamento vinha de getAll: varria a view ate o fim em toda chamada, mesmo
        // pra montar so a primeira tela. Com 20 linhas por pagina no SAP, isso era uma ida e
        // volta a mais a cada 20 adiantamentos em aberto - custo fixo que crescia sozinho.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComAdiantamentos(
                    adiantamento(diasAtraso = 30, contratoDocNum = null),
                    adiantamento(diasAtraso = 20, contratoDocNum = null),
                    proximaPagina = "pagina-2-do-adiantamento",
                )
            )

        val resultado = service.listar(admin, pagina = 0, tamanhoPagina = 2)

        assertEquals(2, resultado.size)
        verify(sqlQueriesService, never()).nextLink("pagina-2-do-adiantamento")
    }

    @Test
    fun `adiantamento continua puxando pagina nova enquanto faltar linha pra completar a pagina`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())
        whenever(sqlQueriesService.execute(eq("cobranca-titulos-adiantamento.sql"), any<List<Parameter>>()))
            .thenReturn(
                odataComAdiantamentos(
                    adiantamento(diasAtraso = 30, contratoDocNum = null),
                    proximaPagina = "pagina-2-do-adiantamento",
                )
            )
        whenever(sqlQueriesService.nextLink("pagina-2-do-adiantamento"))
            .thenReturn(
                odataComAdiantamentos(
                    adiantamento(diasAtraso = 20, contratoDocNum = null),
                    adiantamento(diasAtraso = 10, contratoDocNum = null),
                )
            )

        val resultado = service.listar(admin, pagina = 0, tamanhoPagina = 3)

        assertEquals(3, resultado.size)
        // Mais antigo primeiro: o corte por pagina nao pode bagunçar a ordem do merge.
        assertEquals(listOf(30L, 20L, 10L), resultado.map { it.DiasAtraso })
    }

    @Test
    fun `semAcompanhamento ligado e desligado manda so o IsFilter, porque nao tem valor a comparar`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, semAcompanhamento = true)

        assertEquals(-1, capturarParametros()["semAcompanhamentoIsFilter"])
    }

    @Test
    fun `sem semAcompanhamento o filtro fica desligado e o titulo acompanhado continua vindo`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin)

        assertEquals(Int.MAX_VALUE, capturarParametros()["semAcompanhamentoIsFilter"])
    }

    @Test
    fun `promessaVencidaAte informada vira data de corte no SQL`() {
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, promessaVencidaAte = LocalDate.of(2026, 8, 3))

        val parametros = capturarParametros()
        assertEquals("2026-08-03", parametros["promessaVencidaAte"])
        assertEquals(-1, parametros["promessaVencidaIsFilter"])
    }

    @Test
    fun `sem promessaVencidaAte o parametro de data ainda vai, mas o filtro esta desligado`() {
        // A view sempre pede os dois parametros; quem desliga a condicao e o IsFilter.
        whenever(sqlQueriesService.execute(eq("cobranca-titulos.sql"), any<List<Parameter>>())).thenReturn(odataVazia())

        service.listar(admin, data = LocalDate.of(2026, 8, 3))

        val parametros = capturarParametros()
        assertEquals("2026-08-03", parametros["promessaVencidaAte"])
        assertEquals(Int.MAX_VALUE, parametros["promessaVencidaIsFilter"])
    }

    private fun capturarParametros(): Map<String, Any> {
        val captor = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService).execute(eq("cobranca-titulos.sql"), captor.capture())
        return captor.firstValue.associate { it.key to it.value }
    }

    private fun titulo(
        diasAtraso: Int,
        status: String?,
        paidToDate: BigDecimal = BigDecimal.ZERO,
        statusParcela: String = "O",
    ): CobrancaTituloSap {
        // DueDate relativo a hoje: DiasAtraso e calculado em Kotlin (CobrancaTituloSap.toDto),
        // nao vem pronto do SAP - por isso o teste monta a data em vez de fixar o numero.
        val dueDate = LocalDate.now().minusDays(diasAtraso.toLong()).format(DateTimeFormatter.BASIC_ISO_DATE)
        return CobrancaTituloSap(
            DocEntry = 1, DocNum = 1, Serial = "1", Series = 1,
            BPLId = 6, BPLName = "Fazenda Serra Verde", CardCode = "CLI001", CardName = "Cliente Teste",
            Telefone = "6699998888", Celular = null,
            DocDate = "20260701", DocTotal = BigDecimal("100.00"),
            SlpCode = 60, SlpName = "Vendedor Teste",
            InstlmntID = 1, InsTotal = BigDecimal("100.00"), PaidToDate = paidToDate,
            DueDate = dueDate, StatusParcela = statusParcela,
            U_Status = status, U_Cobrador = null, U_Acao = null, U_Situacao = null,
            U_Ocorrencia = null, U_Observacao = null, U_DataAcao = null, U_DataPromessa = null,
        )
    }

    private fun adiantamento(diasAtraso: Int, contratoDocNum: Int?, statusParcela: String = "O"): CobrancaAdiantamentoSap {
        val dueDate = LocalDate.now().minusDays(diasAtraso.toLong()).format(DateTimeFormatter.BASIC_ISO_DATE)
        return CobrancaAdiantamentoSap(
            DocEntry = 1, DocNumAdiantamento = 501, ContratoDocNum = contratoDocNum,
            BPLId = 6, BPLName = "Fazenda Serra Verde", CardCode = "CLI001", CardName = "Cliente Teste",
            Telefone = "6699998888", Celular = null,
            DocDate = "20260701", DocTotal = BigDecimal("50.00"),
            SlpCode = 60, SlpName = "Vendedor Teste",
            InstlmntID = 1, InsTotal = BigDecimal("50.00"), PaidToDate = BigDecimal.ZERO,
            DueDate = dueDate, StatusParcela = statusParcela,
            U_Status = null, U_Cobrador = null, U_Acao = null, U_Situacao = null,
            U_Ocorrencia = null, U_Observacao = null, U_DataAcao = null, U_DataPromessa = null,
        )
    }

    private fun odataVazia() = odataComTitulos()

    private fun odataComTitulos(vararg titulos: CobrancaTituloSap): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = titulos.toList()
        return OData(backing)
    }

    private fun odataComAdiantamentos(
        vararg adiantamentos: CobrancaAdiantamentoSap,
        proximaPagina: String? = null,
    ): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = adiantamentos.toList()
        proximaPagina?.let { backing["odata.nextLink"] = it }
        return OData(backing)
    }
}
