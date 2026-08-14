package br.andrew.sap.services.cobranca

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class CobrancaDashboardSqlTest {

    private val pasta = Path.of("src/main/resources/views/cobranca")

    private val views = listOf(
        "cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql",
        "cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql",
        "cobranca-recuperado-diario.sql", "cobranca-recuperado-diario-adiantamento.sql",
        "cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql",
        "cobranca-promessa-vencida.sql", "cobranca-promessa-vencida-adiantamento.sql",
        "cobranca-trabalhados.sql", "cobranca-trabalhados-adiantamento.sql",
    ).associateWith { Files.readString(pasta.resolve(it)) }

    @Test
    fun `nenhuma view do dashboard usa construcao sem precedente no parser do SQLQueries`() {
        // IFNULL/CASE WHEN/DAYS_BETWEEN ja quebraram o provisionamento em producao
        // ("mismatched input '.' expecting FROM") e nao aparecem em nenhuma das 46 views do
        // projeto. MIN/MAX tambem nao tem precedente - por isso a serie mensal agrupa por
        // data crua e monta o mes em Kotlin em vez de pedir o menor/maior no SQL.
        val proibidas = listOf(
            "CASE", "IFNULL", "COALESCE", "DAYS_BETWEEN", "CURRENT_DATE",
            "CAST(", "UNION", "MIN(", "MAX(",
        )
        views.forEach { (nome, sql) ->
            val maiuscula = sql.uppercase()
            proibidas.forEach { proibida ->
                assertFalse(
                    maiuscula.contains(proibida),
                    "$nome usa '$proibida', que nao tem precedente nas views do projeto"
                )
            }
        }
    }

    @Test
    fun `nenhuma view do dashboard tem comentario SQL`() {
        // Comentario com "--" ja foi removido a mao de view deste projeto; nao arriscar.
        views.forEach { (nome, sql) ->
            assertFalse(sql.contains("--"), "$nome tem comentario SQL")
        }
    }

    @Test
    fun `carteira devolve Total e Pago separados, sem subtrair no SQL`() {
        // Sem IFNULL, um PaidToDate nulo envenenaria sum(InsTotal - PaidToDate) do grupo
        // inteiro. As duas somas voltam cruas e o saldo e calculado em Kotlin.
        listOf("cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("sum(P.\"InsTotal\")   AS \"Total\""), nome)
            assertTrue(sql.contains("sum(P.\"PaidToDate\") AS \"Pago\""), nome)
            assertFalse(sql.contains("InsTotal\" -"), "$nome nao deve subtrair dentro do SQL")
        }
    }

    @Test
    fun `carteira conta parcela, nao documento distinto`() {
        // A carteira e somada nas QUATRO faixas de aging. Com count(DISTINCT DocEntry),
        // uma nota com parcelas em faixas diferentes seria contada uma vez em cada faixa e
        // o total do card sairia inflado. Uma linha do join OINV x INV6 e uma parcela,
        // entao count da coluna da parcela e a contagem certa - e parcela e a unidade que o
        // time usa (as 213 linhas da planilha eram parcelas, nao notas).
        listOf(
            "cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql",
            "cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql",
            "cobranca-promessa-vencida.sql", "cobranca-promessa-vencida-adiantamento.sql",
        ).forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("count(P.\"InstlmntID\") AS \"Parcelas\""), nome)
            assertFalse(
                sql.contains("count(DISTINCT"),
                "$nome nao pode contar documento distinto: o total soma as quatro faixas"
            )
        }
    }

    @Test
    fun `recuperado diario so conta pagamento que veio DEPOIS de existir acao de cobranca`() {
        // Este EXISTS e o que separa "a cobranca trouxe de volta" de "o cliente pagou
        // sozinho". Sem ele o dashboard credita ao time todo pagamento de titulo que por
        // acaso tem registro - ou seja, mente a favor de quem esta sendo medido.
        listOf("cobranca-recuperado-diario.sql", "cobranca-recuperado-diario-adiantamento.sql").forEach { nome ->
            assertTrue(
                views.getValue(nome).contains(
                    "EXISTS(SELECT 1 FROM \"@COB_TITULO_L\" H WHERE H.\"Code\" = C.\"Code\" AND H.\"U_Data\" <= r.\"DocDate\")"
                ),
                "$nome perdeu o EXISTS que exige acao registrada antes do pagamento"
            )
        }
    }

    @Test
    fun `recuperado por cobrador so conta pagamento que veio DEPOIS de existir acao, e atribui ao cobrador daquela acao`() {
        // O INNER JOIN em H (em vez de C) exige uma acao registrada antes do pagamento -
        // sem nenhuma linha de historico anterior, o join nao produz linha nenhuma. E o
        // NOT EXISTS auto-referenciado pega so a acao MAIS RECENTE antes do pagamento, pra
        // atribuir a recuperacao a quem era responsavel naquele momento, nao a quem e o
        // cobrador atual do titulo (C."U_Cobrador" muda se o titulo for reatribuido depois).
        listOf("cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("INNER JOIN \"@COB_TITULO_L\" H"), "$nome nao junta com o historico")
            assertTrue(sql.contains("H.\"Code\" = C.\"Code\" AND H.\"U_Data\" <= r.\"DocDate\""), nome)
            assertTrue(
                sql.contains("H2.\"U_Data\" > H.\"U_Data\" OR (H2.\"U_Data\" = H.\"U_Data\" AND H2.\"LineId\" > H.\"LineId\")"),
                "$nome nao restringe a linha de historico mais recente antes do pagamento"
            )
            assertTrue(sql.contains("H.\"U_Cobrador\""), "$nome deve agrupar pelo cobrador da linha de historico, nao do mestre")
            assertFalse(
                sql.contains("C.\"U_Cobrador\""),
                "$nome nao pode mais ler o cobrador do registro mestre - ele e mutavel e reflete a acao mais recente"
            )
        }
    }

    @Test
    fun `recuperado liga pagamento a parcela pelo mesmo join de parcelas-pagas`() {
        // RCT2."DocNum" e o DocEntry do ORCT (nao o numero do documento) - o join invertido
        // silenciosamente casaria linhas erradas. O precedente e views/parcelas-pagas.sql.
        listOf("cobranca-recuperado.sql", "cobranca-recuperado-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("INNER JOIN ORCT r ON r.\"DocEntry\" = l.\"DocNum\""), nome)
            assertTrue(sql.contains("C.\"U_InstlmntID\" = l.\"InstId\""), nome)
            assertTrue(sql.contains("(r.\"Canceled\" = 'N' OR r.\"Canceled\" IS NULL)"), nome)
        }
    }

    @Test
    fun `cada par de view usa o InvType e o Tipo certo pra nao misturar nota com adiantamento`() {
        // ODPI e OINV tem contadores de DocEntry independentes (ObjType 13 x 203); sem o
        // par InvType/U_Tipo, pagamento de adiantamento entraria como recuperacao de nota.
        assertTrue(views.getValue("cobranca-recuperado.sql").contains("l.\"InvType\" = 13"))
        assertTrue(views.getValue("cobranca-recuperado.sql").contains("C.\"U_Tipo\" = 'NF'"))
        assertTrue(views.getValue("cobranca-recuperado-adiantamento.sql").contains("l.\"InvType\" = 203"))
        assertTrue(views.getValue("cobranca-recuperado-adiantamento.sql").contains("C.\"U_Tipo\" = 'AD'"))
    }

    @Test
    fun `sem-acao procura ausencia de registro na UDT, nao status vazio`() {
        // "Ninguem tocou" e C."Code" IS NULL (nao existe registro). Um titulo COM registro
        // mas sem status preenchido e outra coisa - e ja aparece no grupo de status.
        listOf("cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("LEFT JOIN \"@COB_TITULO\" C"), "$nome precisa de LEFT JOIN")
            assertTrue(sql.contains("C.\"Code\" IS NULL"), nome)
        }
    }

    @Test
    fun `promessa vencida exige parcela ainda aberta`() {
        // Promessa vencida so e quebra de SLA se o titulo continua em aberto; se pagou
        // depois da data prometida, a promessa foi cumprida com atraso, nao quebrada.
        listOf("cobranca-promessa-vencida.sql", "cobranca-promessa-vencida-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("P.\"Status\" = 'O'"), nome)
            assertTrue(sql.contains("C.\"U_DataPromessa\" <= :data"), nome)
            assertTrue(sql.contains("INNER JOIN \"@COB_TITULO\" C"), "$nome nao pode ser LEFT JOIN")
        }
    }

    @Test
    fun `trabalhados conta titulo distinto, nao linha de historico`() {
        // count(DISTINCT Code) = titulos tocados. Cinco ligacoes no mesmo titulo nao e
        // cinco vezes o trabalho de cinco titulos - e count(DISTINCT ...) e a unica forma
        // de count com precedente no projeto (carregamento/peso-ordem-carregamento.sql).
        listOf("cobranca-trabalhados.sql", "cobranca-trabalhados-adiantamento.sql").forEach { nome ->
            val sql = views.getValue(nome)
            assertTrue(sql.contains("count(DISTINCT H.\"Code\") AS \"Titulos\""), nome)
            assertTrue(sql.contains("H.\"U_Data\" >= :de"), nome)
            assertTrue(sql.contains("H.\"U_Data\" <= :ate"), nome)
        }
    }

    @Test
    fun `toda view do dashboard aceita o filtro opcional de filial e vendedor`() {
        // Filtro dentro de card de grafico e anti-padrao: a tela tem UMA linha de filtro e
        // todos os numeros obedecem o mesmo recorte. Se uma view ignorasse filial, um card
        // mostraria a empresa inteira ao lado de outro filtrado - e ninguem notaria.
        views.forEach { (nome, sql) ->
            assertTrue(sql.contains(":filialIsFilter"), "$nome nao filtra filial")
            assertTrue(sql.contains(":vendedorIsFilter"), "$nome nao filtra vendedor")
        }
    }

    @Test
    fun `carteira e sem-acao olham so parcela aberta`() {
        listOf(
            "cobranca-carteira.sql", "cobranca-carteira-adiantamento.sql",
            "cobranca-sem-acao.sql", "cobranca-sem-acao-adiantamento.sql",
        ).forEach { nome ->
            assertTrue(views.getValue(nome).contains("P.\"Status\" = 'O'"), nome)
            assertTrue(views.getValue(nome).contains("\"CANCELED\" = 'N'"), nome)
        }
    }
}
