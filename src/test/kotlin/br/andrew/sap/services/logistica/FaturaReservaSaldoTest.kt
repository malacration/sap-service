package br.andrew.sap.services.logistica

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path

/** As views de fatura de reserva trazem uma linha por retirada; o saldo e o peso saem em CarregamentoService. */
class FaturaReservaSaldoTest {

    private fun row(baseLine: Int, openQty: String, retirada: String?, ordemAberta: Int?) = mapOf(
        "DocEntry" to 256990, "BaseLine" to baseLine, "OpenQty" to openQty,
        "Retirada" to retirada, "OrdemAberta" to ordemAberta,
        "PesoLinha" to "1500", "QtdFatura" to "60",
    )

    @Test
    fun `saldo desconta so retirada de ordem aberta`() {
        val saldos = CarregamentoService.comSaldo(listOf(
            row(0, "50", "10", 11),
            row(0, "50", "5", 12),
            row(0, "50", "30", null),
            row(1, "200", null, null),
        )).associate { (linha, saldo) -> linha["BaseLine"] to saldo }

        assertEquals(0, BigDecimal("35").compareTo(saldos[0]))
        assertEquals(0, BigDecimal("200").compareTo(saldos[1]))
    }

    @Test
    fun `peso e proporcional a quantidade da linha`() {
        assertEquals(0, BigDecimal("250").compareTo(CarregamentoService.peso(row(0, "60", null, null), BigDecimal("10"))))
    }

    @Test
    fun `sem localidade mostra cidade e UF da entrega`() {
        assertEquals("MINISTRO ANDREAZZA", CarregamentoService.localidade(mapOf("Name" to "MINISTRO ANDREAZZA", "CityS" to "X")))
        assertEquals("PORTO VELHO - RO", CarregamentoService.localidade(mapOf("Name" to null, "CityS" to "PORTO VELHO", "StateS" to "RO")))
        assertEquals("RO", CarregamentoService.localidade(mapOf("CityS" to "", "StateS" to "RO")))
        assertEquals(null, CarregamentoService.localidade(mapOf<String, Any?>()))
    }

    @Test
    fun `filtro de cidade ignora acento e caixa`() {
        assertTrue(CarregamentoService.cidadeConfere("CANDEIAS DO JAMARI", "candeias"))
        assertTrue(CarregamentoService.cidadeConfere("Humaitá", "humaita"))
        assertTrue(CarregamentoService.cidadeConfere(null, ""))
        assertFalse(CarregamentoService.cidadeConfere(null, "porto"))
    }

    @Test
    fun `views de fatura de reserva nao fazem conta no SELECT`() {
        //SQLQueries recusa conta e CASE ("Invalid SQL syntax") e a view nao sobe no boot.
        listOf(
            "carregamento/fatura-reserva-saldo.sql",
            "carregamento/fatura-reserva-search.sql",
            "carregamento/ord-carregamento-faturas.sql",
            "carregamento/pedidoeestoque-faturas.sql",
            "carregamento/peso-ordem-carregamento-faturas.sql",
            "PainelInterativoCarregamento/faturas-reserva-carregamento.sql",
        ).forEach { view ->
            val sql = Files.readString(Path.of("src/main/resources/views/$view"))
            val select = sql.substringBefore("FROM")
            assertFalse(Regex("CASE|\\s[-*/+]\\s").containsMatchIn(select), "$view faz conta no SELECT")
        }
    }

    @Test
    fun `observacao leva a ordem na frente do texto de origem e cabe nos 254 caracteres`() {
        assertEquals("Ordem de carregamento 19. Baseado em Cotacoes de vendas 19220. Based On Sales Orders 79644.",
            CarregamentoService.observacaoComOrdem(19, "Baseado em Cotacoes de vendas 19220.", "Based On Sales Orders 79644."))
        assertEquals("Ordem de carregamento 19.", CarregamentoService.observacaoComOrdem(19, "  "))
        assertEquals("Ordem de carregamento 19. Based On A/R Invoices 69805.",
            CarregamentoService.observacaoComOrdem(19, null, "Based On A/R Invoices 69805."))

        //Texto longo: corta o meio, nunca a ordem nem a referencia ao documento de origem.
        val longa = CarregamentoService.observacaoComOrdem(19, "x".repeat(400), "Based On Sales Orders 79644.")
        assertEquals(254, longa.length)
        assertTrue(longa.startsWith("Ordem de carregamento 19. x"))
        assertTrue(longa.endsWith("x Based On Sales Orders 79644."))
    }

    @Test
    fun `frete da retirada na tela e proporcional ao total da NF, igual a entrega`() {
        //NF 69805: frete 3800 em 400 sacos; retirada de 40 = 380 (entrega 28642 gravou 380).
        assertEquals(BigDecimal("380.00"), CarregamentoService.freteProporcional(BigDecimal("3800"), BigDecimal("40"), BigDecimal("400")))
        assertEquals(BigDecimal("660.00"), CarregamentoService.freteProporcional(BigDecimal("5100"), BigDecimal("110"), BigDecimal("850")))
    }

    @Test
    fun `historico junta as versoes do mesmo salvamento e mostra o estado final`() {
        fun v(log: Int, acao: String, qtd: Int, placa: String? = null) =
            mapOf("LogInst" to log, "Usuario" to "joao", "Acao" to acao, "Status" to "Aberto", "Placa" to placa, "QtdRetirada" to qtd)
        //Ordem 35 do HOMOLOG2: criar grava cabecalho (0) e retiradas (3); a edicao, cabecalho e retiradas (5).
        val linhas = CarregamentoService.agruparHistorico(listOf(
            v(1, "Criada", 0), v(2, "Criada", 3), v(3, "Logística alterada", 3, "ABC1D23"),
            v(4, "Alterada", 3, "ABC1D23"), v(5, "Alterada", 5, "ABC1D23"), v(6, "Cancelada", 5, "ABC1D23")))

        assertEquals(listOf(2, 3, 5, 6), linhas.map { it["LogInst"] })
        assertEquals(5, linhas[2]["QtdRetirada"])
    }

    @Test
    fun `historico nao junta salvamentos de minutos diferentes nem mostra operacao que nao terminou`() {
        fun v(log: Int, acao: String, hora: Int, transp: String? = null) = mapOf("LogInst" to log, "Usuario" to "joao",
            "Acao" to acao, "Status" to "Aberto", "Transportadora" to transp, "UpdateDate" to "2026-10-29", "UpdateTime" to hora)
        //Duas alteracoes de logistica so da transportadora, em minutos diferentes: continuam duas linhas.
        val duas = CarregamentoService.agruparHistorico(listOf(v(1, "Logística alterada", 1300, "T1"), v(2, "Logística alterada", 1305, "T2")))
        assertEquals(listOf(1, 2), duas.map { it["LogInst"] })

        //Alteracao concluida: "em andamento" some e fica so o resultado.
        val ok = CarregamentoService.agruparHistorico(listOf(v(1, "Alteração" + CarregamentoService.EM_ANDAMENTO, 1300),
            v(2, "Alteração" + CarregamentoService.EM_ANDAMENTO, 1300), v(3, "Alterada", 1300)))
        assertEquals(listOf("Alterada"), ok.map { it["Acao"] })

        //Operacao que caiu no meio (nem o fim foi registrado): continua "em andamento", mesmo com outra acao depois.
        val caiu = CarregamentoService.agruparHistorico(listOf(v(1, "Criada", 1200), v(2, "Alteração" + CarregamentoService.EM_ANDAMENTO, 1300)))
        assertEquals(listOf("Criada", "Alteração em andamento"), caiu.map { it["Acao"] })
        val caiuEDepoisCancelou = CarregamentoService.agruparHistorico(listOf(v(1, "Criada", 1200),
            v(2, "Alteração" + CarregamentoService.EM_ANDAMENTO, 1300), v(3, "Cancelada", 1310)))
        assertEquals(listOf("Criada", "Alteração em andamento", "Cancelada"), caiuEDepoisCancelou.map { it["Acao"] })

        //Recusada: a tentativa em andamento some e fica o motivo.
        val recusada = CarregamentoService.agruparHistorico(listOf(v(1, "Criada", 1200),
            v(2, "Alteração" + CarregamentoService.EM_ANDAMENTO, 1300), v(3, "Alteração recusada: 400 - titulo vencido", 1300)))
        assertEquals(listOf("Criada", "Alteração recusada: 400 - titulo vencido"), recusada.map { it["Acao"] })
    }

    @Test
    fun `frete da tela nao passa do que resta na NF`() {
        //NF 39012: frete 1020 ja baixado inteiro, retirada de 10 de 200 daria 51 e a entrega leva 0.
        assertEquals(BigDecimal("51.00"), CarregamentoService.freteProporcional(BigDecimal("1020"), BigDecimal("10"), BigDecimal("200")))
        assertEquals(0, BigDecimal.ZERO.compareTo(CarregamentoService.freteExibido(BigDecimal("1020"), BigDecimal("1020"), BigDecimal("10"), BigDecimal("200"))))
        //Resta 30 do frete: a retirada que daria 51 mostra 30; a que daria 10,20 continua 10,20.
        assertEquals(0, BigDecimal("30").compareTo(CarregamentoService.freteExibido(BigDecimal("1020"), BigDecimal("990"), BigDecimal("10"), BigDecimal("200"))))
        assertEquals(0, BigDecimal("10.20").compareTo(CarregamentoService.freteExibido(BigDecimal("1020"), BigDecimal("990"), BigDecimal("2"), BigDecimal("200"))))
    }

    @Test
    fun `frete exibido por linha nao reparte o teto entre linhas que podem nao ser retiradas`() {
        //Duas linhas de 10 com resta 10: cada uma mostra 10 (se so uma for retirada, a entrega leva 10). Repartir
        //em 5 e 5 mostraria menos do que a entrega cobra.
        val linha = CarregamentoService.freteExibido(BigDecimal("20"), BigDecimal("10"), BigDecimal("50"), BigDecimal("100"))
        assertEquals(0, BigDecimal("10").compareTo(linha))
    }

    @Test
    fun `frete unitario sem arredondar nao zera frete pequeno ao ampliar a retirada`() {
        //Frete 1,00 em 1000 unidades: 1 unidade = 0,001, que arredondada em centavos vira 0,00. A tela recebe o
        //unitario sem arredondar: 700 unidades dao 0,70, nao 0,00.
        assertEquals(BigDecimal("0.00"), CarregamentoService.freteProporcional(BigDecimal("1"), BigDecimal("1"), BigDecimal("1000")))
        val unitario = BigDecimal("1").divide(BigDecimal("1000"), 10, java.math.RoundingMode.HALF_UP)
        assertEquals(BigDecimal("0.0010000000"), unitario)
        assertEquals(BigDecimal("0.70"), CarregamentoService.freteProporcional(BigDecimal("1"), BigDecimal("700"), BigDecimal("1000")))
    }
}
