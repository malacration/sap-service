package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.financeiro.contaspagar.CodigoConsultaPagamento.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

/** Dados ficticios: CNPJs, CardCodes e valores nao correspondem a registros reais. */
class ConsultaPagamentoParcelaServiceTest {

    private class FakeSap : ContasPagarSapGateway {
        var filiais = mapOf(FILIAL to listOf(2))
        var fornecedores = mapOf(FORNECEDOR to listOf("FOR0000001"))
        var notas = listOf(nota())
        var parcelas = listOf(parcela(1, "100.00", "0"))
        var pagamentos = listOf<PagamentoNotaEntradaSap>()
        var numeroConsultado: Int? = null
        val numerosConsultados = mutableListOf<Int>()
        var chaveConsultada: String? = null
        var parcelaPagamentos: Int? = null

        override fun filiaisPorCnpj(cnpj: String) = filiais[cnpj].orEmpty()
        override fun fornecedoresPorCnpj(cnpj: String) = fornecedores[cnpj].orEmpty()
        override fun notasEntrada(numero: Int, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap> {
            numeroConsultado = numero
            numerosConsultados += numero
            return notas.filter { it.sequenceSerial == numero }
        }
        override fun notasPorChave(chave: String, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap> {
            chaveConsultada = chave
            return notas.filter { it.chaveAcesso == chave }
        }
        override fun parcelas(docEntry: Int) = parcelas
        // Devolve as aplicacoes de todas as parcelas de proposito: o servico filtra de novo pela parcela.
        override fun pagamentos(docEntry: Int, parcela: Int): List<PagamentoNotaEntradaSap> {
            parcelaPagamentos = parcela
            return pagamentos
        }
    }

    private val sap = FakeSap()
    private var service = ConsultaPagamentoParcelaService(sap, BigDecimal("0.01"))

    private fun consulta(
        numero: String = "000123",
        serie: String? = null,
        parcela: String? = "1",
        valor: String = "100.00",
        filial: String = "12.345.678/0001-90",
        fornecedor: String = FORNECEDOR,
        chave: String? = null,
        tipo: String? = null,
    ) = service.consultar(ConsultaPagamentoParcelaRequest(filial, fornecedor, numero, serie, parcela, BigDecimal(valor), chave, tipo))

    @Test
    fun `parcela quitada e coberta por pagamento efetuado e PAGO com dados do pagamento`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"))
        sap.pagamentos = listOf(pagamento(500, 1, "100.00", "2026-11-10T00:00:00Z"))
        val r = consulta()
        assertEquals(PAGO, r.codigo)
        assertEquals(123, sap.numeroConsultado, "zeros a esquerda nao fazem parte do Serial")
        assertEquals(10, r.nota!!.docEntry)
        assertEquals(BigDecimal("100.00"), r.parcela!!.valorPagamentos)
        assertEquals(BigDecimal("0.00"), r.parcela!!.saldo)
        assertEquals("2026-11-10", r.pagamentos.single().data)
        assertEquals(500, r.pagamentos.single().docEntry)
    }

    @Test
    fun `soma pagamentos parciais da mesma parcela e ignora os de outra parcela`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"), parcela(2, "100.00", "0"))
        sap.pagamentos = listOf(
            pagamento(501, 1, "60.00", "2026-11-01"),
            pagamento(502, 1, "40.00", "2026-11-05"),
            pagamento(503, 2, "999.00", "2026-11-05"),
        )
        val r = consulta()
        assertEquals(PAGO, r.codigo)
        assertEquals(listOf(501, 502), r.pagamentos.map { it.docEntry })
        // A parcela 2 recebeu um pagamento (VPM2) mas PaidToDate segue zero: nao e PAGO.
        assertEquals(PAGO_PARCIAL, consulta(parcela = "2").codigo)
        assertEquals(2, sap.parcelaPagamentos, "pagamentos consultados so da parcela pedida")
    }

    @Test
    fun `pagamento parcial e PAGO_PARCIAL`() {
        sap.parcelas = listOf(parcela(1, "100.00", "40.00"))
        sap.pagamentos = listOf(pagamento(501, 1, "40.00"))
        val r = consulta()
        assertEquals(PAGO_PARCIAL, r.codigo)
        assertEquals(BigDecimal("60.00"), r.parcela!!.saldo)
    }

    @Test
    fun `sem baixa nem pagamento e EM_ABERTO`() {
        assertEquals(EM_ABERTO, consulta().codigo)
    }

    @Test
    fun `quitada no SAP sem pagamento que cubra e BAIXADA_SEM_PAGAMENTO`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta().codigo)
        sap.pagamentos = listOf(pagamento(501, 1, "90.00"))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta().codigo, "desconto/devolucao nao vira PAGO automatico")
    }

    @Test
    fun `pagamentos cobrem mas PaidToDate nao e PAGO_PARCIAL, nunca PAGO`() {
        sap.parcelas = listOf(parcela(1, "100.00", "50.00"))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00"))
        assertEquals(PAGO_PARCIAL, consulta().codigo)
    }

    @Test
    fun `tolerancia de um centavo so para arredondamento`() {
        sap.parcelas = listOf(parcela(1, "100.00", "99.99", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.99"))
        assertEquals(PAGO, consulta(valor = "100.01").codigo)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "100.02").codigo)
        sap.parcelas = listOf(parcela(1, "100.00", "99.98", "O"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.98"))
        assertEquals(PAGO_PARCIAL, consulta().codigo)
    }

    @Test
    fun `valor divergente vence o status de pagamento e devolve os dados da parcela`() {
        sap.parcelas = listOf(parcela(1, "150.00", "150.00", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "150.00"))
        val r = consulta(valor = "100.00")
        assertEquals(VALOR_DIVERGENTE, r.codigo)
        assertEquals(BigDecimal("150.00"), r.parcela!!.valor)
    }

    @Test
    fun `filial e fornecedor nao encontrados`() {
        sap.filiais = mapOf()
        assertEquals(FILIAL_NAO_ENCONTRADA, consulta().codigo)
        sap.filiais = mapOf(FILIAL to listOf(2))
        sap.fornecedores = mapOf()
        assertEquals(FORNECEDOR_NAO_ENCONTRADO, consulta().codigo)
    }

    @Test
    fun `nota nao encontrada por numero, numero invalido ou fora da faixa do Serial`() {
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = "999").codigo)
        assertEquals(999, sap.numeroConsultado)
        sap.numeroConsultado = null
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = "12A").codigo)
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = "000").codigo)
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = "202600000000123").codigo)
        assertNull(sap.numeroConsultado, "numero invalido nao chega ao SAP")
    }

    @Test
    fun `nota cancelada ou documento de cancelamento nao contam`() {
        sap.notas = listOf(nota(cancelado = "Y"), nota(docEntry = 11, cancelado = "C"), nota(docEntry = 12, cancelado = null))
        assertEquals(NOTA_NAO_ENCONTRADA, consulta().codigo)
    }

    // ---- P1: centavo, total ausente/zero e limites da tolerancia ----

    @Test
    fun `parcela de um centavo sem pagamento nao e PAGO`() {
        sap.parcelas = listOf(parcela(1, "0.01", "0"))
        assertEquals(EM_ABERTO, consulta(valor = "0.01").codigo)
        sap.parcelas = listOf(parcela(1, "0.01", "0.01", "C"))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta(valor = "0.01").codigo, "baixa sem pagamento efetuado")
        sap.pagamentos = listOf(pagamento(501, 1, "0.01"))
        assertEquals(PAGO, consulta(valor = "0.01").codigo)
    }

    @Test
    fun `total da parcela ausente zero ou negativo e VALOR_DIVERGENTE, nunca PAGO`() {
        for (total in listOf(null, "0", "0.00", "-0.01")) {
            sap.parcelas = listOf(ParcelaNotaEntradaSap(1, "2026-11-10", total?.let(::BigDecimal), BigDecimal.ZERO, "C", "R$", "R$"))
            sap.pagamentos = listOf()
            val r = consulta(valor = "0.01")
            assertEquals(VALOR_DIVERGENTE, r.codigo, "total $total")
            assertNull(r.parcela, "sem total valido nao ha dado de parcela para devolver")
            assertEquals(10, r.nota!!.docEntry)
        }
    }

    @Test
    fun `classifica nunca deixa zero cobrir a parcela`() {
        val z = BigDecimal.ZERO
        assertEquals(VALOR_DIVERGENTE, service.classifica(BigDecimal("0.01"), null, z, z).first)
        assertEquals(VALOR_DIVERGENTE, service.classifica(BigDecimal("0.01"), z, z, z).first)
        assertEquals(EM_ABERTO, service.classifica(BigDecimal("0.01"), BigDecimal("0.01"), z, z).first)
        assertEquals(BAIXADA_SEM_PAGAMENTO, service.classifica(BigDecimal("0.01"), BigDecimal("0.01"), BigDecimal("0.01"), BigDecimal("0.009")).first)
        assertEquals(PAGO_PARCIAL, service.classifica(BigDecimal("0.01"), BigDecimal("0.01"), BigDecimal("0.009"), BigDecimal("0.009")).first)
        assertEquals(PAGO, service.classifica(BigDecimal("0.01"), BigDecimal("0.01"), BigDecimal("0.01"), BigDecimal("0.01")).first)
    }

    @Test
    fun `tolerancia configurada vale como limite exato no valor e na quitacao`() {
        service = ConsultaPagamentoParcelaService(sap, BigDecimal("0.05"))
        sap.parcelas = listOf(parcela(1, "100.00", "99.95", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.95"))
        assertEquals(PAGO, consulta(valor = "100.05").codigo)
        assertEquals(PAGO, consulta(valor = "99.95").codigo)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "100.06").codigo)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "99.94").codigo)
        sap.parcelas = listOf(parcela(1, "100.00", "99.94"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.94"))
        assertEquals(PAGO_PARCIAL, consulta().codigo)
        sap.parcelas = listOf(parcela(1, "100.00", "99.95", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.94"))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta().codigo, "pagamentos abaixo do limite")
    }

    @Test
    fun `parcela menor ou igual a tolerancia exige o valor inteiro`() {
        service = ConsultaPagamentoParcelaService(sap, BigDecimal("0.05"))
        sap.parcelas = listOf(parcela(1, "0.03", "0"))
        assertEquals(EM_ABERTO, consulta(valor = "0.03").codigo)
        sap.parcelas = listOf(parcela(1, "0.03", "0.01"))
        sap.pagamentos = listOf(pagamento(501, 1, "0.01"))
        assertEquals(PAGO_PARCIAL, consulta(valor = "0.03").codigo)
        sap.parcelas = listOf(parcela(1, "0.03", "0.03", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "0.03"))
        assertEquals(PAGO, consulta(valor = "0.03").codigo)
    }

    @Test
    fun `tolerancia zero exige igualdade`() {
        service = ConsultaPagamentoParcelaService(sap, BigDecimal.ZERO)
        sap.parcelas = listOf(parcela(1, "100.00", "99.99", "O"))
        sap.pagamentos = listOf(pagamento(501, 1, "99.99"))
        assertEquals(PAGO_PARCIAL, consulta().codigo)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "100.01").codigo)
    }

    @Test
    fun `tolerancia so de arredondamento - fora de 0 a 0,10 e recusada na subida`() {
        ConsultaPagamentoParcelaService(sap, BigDecimal("0.10"))
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("0.11")) }
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("1")) }
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("-0.01")) }
    }

    // ---- P1: moeda ----

    @Test
    fun `nota em moeda estrangeira e MOEDA_NAO_SUPORTADA mesmo quitada`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C", moeda = "USD"))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00"))
        val r = consulta()
        assertEquals(MOEDA_NAO_SUPORTADA, r.codigo)
        assertEquals("USD", r.nota!!.moeda)
        assertNull(r.parcela, "valores em moeda local de nota estrangeira nao sao devolvidos")
        assertTrue(r.pagamentos.isEmpty())
        assertNull(sap.parcelaPagamentos, "nem chega a consultar pagamentos")
    }

    @Test
    fun `moeda desconhecida da nota ou da empresa e MOEDA_NAO_SUPORTADA`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C", moeda = null))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00"))
        assertEquals(MOEDA_NAO_SUPORTADA, consulta().codigo)
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C", moedaLocal = null))
        assertEquals(MOEDA_NAO_SUPORTADA, consulta().codigo)
    }

    @Test
    fun `nota na moeda local segue e devolve a moeda`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00"))
        val r = consulta()
        assertEquals(PAGO, r.codigo)
        assertEquals("R$", r.nota!!.moeda)
    }

    // ---- P2: serie ----

    @Test
    fun `serie zero informada nao casa com nota sem serie`() {
        sap.notas = listOf(nota(serie = null))
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(serie = "0").codigo)
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(serie = "000").codigo)
        sap.notas = listOf(nota(serie = " "))
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(serie = "0").codigo)
        assertEquals(EM_ABERTO, consulta(serie = null).codigo, "sem serie no pedido nao filtra")
        assertEquals(EM_ABERTO, consulta(serie = "  ").codigo, "serie em branco = sem serie")
    }

    @Test
    fun `serie zero escolhe a nota de serie zero e nao cria ambiguidade com a sem serie`() {
        sap.notas = listOf(nota(docEntry = 10, serie = null), nota(docEntry = 11, serie = "0"))
        assertEquals(AMBIGUO, consulta().codigo)
        val r = consulta(serie = "000")
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(11, r.nota!!.docEntry)
    }

    @Test
    fun `normalizacao da serie`() {
        assertNull(ConsultaPagamentoParcelaService.normalizaSerie(null))
        assertNull(ConsultaPagamentoParcelaService.normalizaSerie("  "))
        assertEquals("0", ConsultaPagamentoParcelaService.normalizaSerie("000"))
        assertEquals("0", ConsultaPagamentoParcelaService.normalizaSerie(" 0 "))
        assertEquals("1", ConsultaPagamentoParcelaService.normalizaSerie("001"))
        assertEquals("A1", ConsultaPagamentoParcelaService.normalizaSerie(" a1 "))
    }

    @Test
    fun `serie filtra e compara sem zeros a esquerda`() {
        sap.notas = listOf(nota(docEntry = 10, serie = "1"), nota(docEntry = 11, serie = "2"))
        assertEquals(AMBIGUO, consulta().codigo)
        val r = consulta(serie = "001")
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(10, r.nota!!.docEntry)
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(serie = "3").codigo)
    }

    @Test
    fun `mesma nota repetida na resposta nao e ambigua`() {
        sap.notas = listOf(nota(), nota())
        assertEquals(EM_ABERTO, consulta().codigo)
    }

    @Test
    fun `parcela inexistente, invalida ou nao informada com varias parcelas`() {
        sap.parcelas = listOf(parcela(1, "50.00", "0"), parcela(2, "50.00", "0"))
        assertEquals(PARCELA_NAO_ENCONTRADA, consulta(parcela = "3", valor = "50.00").codigo)
        assertEquals(PARCELA_NAO_ENCONTRADA, consulta(parcela = "x", valor = "50.00").codigo)
        val r = consulta(parcela = null, valor = "50.00")
        assertEquals(PARCELA_NAO_ENCONTRADA, r.codigo)
        assertTrue(r.mensagem.contains("2 parcelas"))
        assertEquals(10, r.nota!!.docEntry)
    }

    @Test
    fun `parcela nao informada com parcela unica usa a unica`() {
        assertEquals(EM_ABERTO, consulta(parcela = null).codigo)
        assertEquals(EM_ABERTO, consulta(parcela = "01").codigo)
    }

    @Test
    fun `pedido malformado e recusado antes de consultar o SAP`() {
        assertThrows<ConsultaInvalidaException> { consulta(filial = "123") }
        assertThrows<ConsultaInvalidaException> { consulta(fornecedor = "1234567800019A") }
        assertThrows<ConsultaInvalidaException> { consulta(valor = "0") }
        assertThrows<ConsultaInvalidaException> { consulta(numero = " ") }
    }

    // ---- Base liquida (retencao na fonte) e pagamento a maior ----

    @Test
    fun `retencao de categoria Fatura ja esta fora do InsTotal e WTSum e ignorado`() {
        // Sem retencao de categoria Pagamento no documento: o InsTotal ja e o liquido.
        sap.parcelas = listOf(parcelaRetencao(1, "90.00", "90.00", wt = "10.00", wtDocumento = null, status = "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "90.00"))
        val r = consulta(valor = "90.00")
        assertEquals(PAGO, r.codigo)
        assertEquals(BigDecimal("90.00"), r.parcela!!.valorLiquido)
        assertEquals(BigDecimal("0.00"), r.parcela!!.retencaoPagamento)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "80.00").codigo, "WTSum nao e descontado de novo")
    }

    @Test
    fun `retencao no pagamento - compara pelo liquido e conta o pago sem a retencao`() {
        sap.parcelas = listOf(parcelaRetencao(1, "100.00", "100.00", wt = "10.00", wtDocumento = "10.00", status = "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00", retido = "10.00"))
        val r = consulta(valor = "90.00")
        assertEquals(PAGO, r.codigo)
        assertEquals(BigDecimal("100.00"), r.parcela!!.valor)
        assertEquals(BigDecimal("10.00"), r.parcela!!.retencaoPagamento)
        assertEquals(BigDecimal("90.00"), r.parcela!!.valorLiquido)
        assertEquals(BigDecimal("90.00"), r.parcela!!.valorPagamentos)
        assertEquals(BigDecimal("0.00"), r.parcela!!.saldo)
        assertEquals(BigDecimal("10.00"), r.pagamentos.single().valorRetido)
        assertEquals(BigDecimal("90.00"), r.pagamentos.single().valorPago)
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "100.00").codigo, "SE manda o liquido; o bruto e outra obrigacao")
    }

    @Test
    fun `retencao no pagamento - pago abaixo do liquido e PAGO_PARCIAL`() {
        sap.parcelas = listOf(parcelaRetencao(1, "100.00", "50.00", wt = "10.00", wtDocumento = "10.00"))
        sap.pagamentos = listOf(pagamento(501, 1, "50.00", retido = "10.00"))
        val r = consulta(valor = "90.00")
        assertEquals(PAGO_PARCIAL, r.codigo)
        assertEquals(BigDecimal("50.00"), r.parcela!!.saldo)
    }

    @Test
    fun `retencao no pagamento so na primeira parcela`() {
        sap.parcelas = listOf(
            parcelaRetencao(1, "50.00", "50.00", wt = "10.00", wtDocumento = "10.00", status = "C"),
            parcelaRetencao(2, "50.00", "50.00", wt = "0.00", wtDocumento = "10.00", status = "C"),
        )
        sap.pagamentos = listOf(pagamento(501, 1, "50.00", retido = "10.00"))
        assertEquals(PAGO, consulta(parcela = "1", valor = "40.00").codigo)
        sap.pagamentos = listOf(pagamento(502, 2, "50.00"))
        assertEquals(PAGO, consulta(parcela = "2", valor = "50.00").codigo)
    }

    @Test
    fun `divisao da retencao no pagamento que nao fecha e RETENCAO_NAO_SUPORTADA, nunca PAGO`() {
        val casos = listOf(
            listOf(parcelaRetencao(1, "100.00", "100.00", wt = "5.00", wtDocumento = "10.00", status = "C")),
            listOf(parcelaRetencao(1, "100.00", "100.00", wt = null, wtDocumento = "10.00", status = "C")),
            listOf(parcelaRetencao(1, "100.00", "100.00", wt = "-10.00", wtDocumento = "-10.00", status = "C")),
            listOf(parcelaRetencao(1, "100.00", "100.00", wt = "10.00", wtDocumento = "10.00", status = "C"),
                parcelaRetencao(2, "100.00", "0", wt = null, wtDocumento = "10.00")),
        )
        sap.pagamentos = listOf(pagamento(501, 1, "100.00", retido = "10.00"))
        for (parcelas in casos) {
            sap.parcelas = parcelas
            val r = consulta(valor = "90.00")
            assertEquals(RETENCAO_NAO_SUPORTADA, r.codigo, parcelas.toString())
            assertNull(r.parcela)
        }
    }

    @Test
    fun `retencao maior ou igual ao InsTotal e VALOR_DIVERGENTE`() {
        sap.parcelas = listOf(parcelaRetencao(1, "10.00", "10.00", wt = "10.00", wtDocumento = "10.00", status = "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "10.00", retido = "10.00"))
        assertEquals(VALOR_DIVERGENTE, consulta(valor = "0.01").codigo)
    }

    @Test
    fun `pagamento a maior por juros ou multa continua PAGO`() {
        sap.parcelas = listOf(parcela(1, "100.00", "112.50", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "112.50"))
        val r = consulta()
        assertEquals(PAGO, r.codigo)
        assertEquals(BigDecimal("112.50"), r.parcela!!.valorPagamentos)
        assertEquals(BigDecimal("0.00"), r.parcela!!.saldo)
        // Dois pagamentos que, somados, passam do liquido.
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "60.00"), pagamento(502, 1, "45.00"))
        assertEquals(PAGO, consulta().codigo)
    }

    @Test
    fun `pagamento a menor continua PAGO_PARCIAL`() {
        sap.parcelas = listOf(parcela(1, "100.00", "95.00"))
        sap.pagamentos = listOf(pagamento(501, 1, "95.00"))
        assertEquals(PAGO_PARCIAL, consulta().codigo)
    }

    @Test
    fun `linha de pagamento com retencao ilegivel ou negativa nao conta como pago`() {
        sap.parcelas = listOf(parcela(1, "100.00", "100.00", "C"))
        sap.pagamentos = listOf(pagamento(501, 1, "100.00", retido = null))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta().codigo)
        sap.pagamentos = listOf(pagamento(501, 1, "100.00", retido = "-5.00"))
        assertEquals(BAIXADA_SEM_PAGAMENTO, consulta().codigo)
    }

    // ---- Chave de acesso, modelo e numero longo de NFS-e ----

    @Test
    fun `chave de acesso encontrada e o primeiro criterio e evita colisao de numero`() {
        val chave = chave44()
        sap.notas = listOf(nota(docEntry = 10, chave = chave), nota(docEntry = 11, modelo = "46"))
        val r = consulta(chave = chave)
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(10, r.nota!!.docEntry)
        assertEquals(CriterioNota.CHAVE_ACESSO, r.nota!!.criterio)
        assertEquals("39", r.nota!!.modelo)
        assertEquals(chave, sap.chaveConsultada)
        assertTrue(sap.numerosConsultados.isEmpty(), "achou pela chave: nao procura pelo numero")
    }

    @Test
    fun `chave com prefixo e espacos e normalizada`() {
        val chave = chave44()
        sap.notas = listOf(nota(chave = chave))
        assertEquals(EM_ABERTO, consulta(chave = "NFe" + chave.chunked(4).joinToString(" ")).codigo)
        assertEquals(chave, sap.chaveConsultada)
        val nfse = chave50("0000000000123")
        sap.notas = listOf(nota(modelo = "46", serie = null, chave = nfse))
        assertEquals(EM_ABERTO, consulta(chave = "NFS$nfse").codigo)
        assertEquals(nfse, sap.chaveConsultada)
    }

    @Test
    fun `chave achada no SAP em nota de outro numero ou modelo e DADOS_NOTA_DIVERGENTES`() {
        val chave = chave44()
        sap.notas = listOf(nota(numero = 124, chave = chave))
        assertEquals(DADOS_NOTA_DIVERGENTES, consulta(chave = chave).codigo)
        sap.notas = listOf(nota(modelo = "45", chave = chave))
        assertEquals(DADOS_NOTA_DIVERGENTES, consulta(chave = chave).codigo)
        sap.notas = listOf(nota(docEntry = 10, chave = chave), nota(docEntry = 11, chave = chave))
        assertEquals(AMBIGUO, consulta(chave = chave).codigo)
    }

    @Test
    fun `chave incoerente com o pedido e DADOS_NOTA_DIVERGENTES sem consultar o SAP`() {
        val casos = listOf(
            consultaSemSap(chave = chave44(numero = 124)),
            consultaSemSap(chave = chave44(serie = "002"), serie = "1"),
            consultaSemSap(chave = chave44(), tipo = "CTE"),
            consultaSemSap(chave = chave44(modelo = "57"), tipo = "Nfe"),
            consultaSemSap(chave = chave44(cnpj = "11111111000111")),
            consultaSemSap(chave = chave50("0000000000999"), tipo = "NFSE"),
            consultaSemSap(chave = "123"),
            consultaSemSap(chave = chave44().dropLast(1) + "X"),
            consultaSemSap(chave = chave44(), numero = "12A"),
        )
        casos.forEachIndexed { i, r -> assertEquals(DADOS_NOTA_DIVERGENTES, r.codigo, "caso $i") }
        assertNull(sap.numeroConsultado)
        assertNull(sap.chaveConsultada)
    }

    private fun consultaSemSap(chave: String, numero: String = "123", serie: String? = null, tipo: String? = null) =
        consulta(chave = chave, numero = numero, serie = serie, tipo = tipo)

    @Test
    fun `chave ausente no SAP segue pelo numero com a serie da chave e descarta nota de outra chave`() {
        val chave = chave44(serie = "002")
        sap.notas = listOf(
            nota(docEntry = 10, serie = "1"),
            nota(docEntry = 11, serie = "2", chave = chave44(serie = "002", codigo = "87654321")),
            nota(docEntry = 12, serie = "002"),
        )
        val r = consulta(chave = chave)
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(12, r.nota!!.docEntry)
        assertEquals(CriterioNota.NUMERO, r.nota!!.criterio)
        assertEquals(listOf(123), sap.numerosConsultados)
    }

    @Test
    fun `tipo filtra pelo modelo configurado e evita colisao CTe x NFS-e`() {
        sap.notas = listOf(nota(docEntry = 10, modelo = "45"), nota(docEntry = 11, modelo = "46"), nota(docEntry = 12, modelo = null))
        assertEquals(10, consulta(tipo = "CTE").nota!!.docEntry)
        assertEquals(11, consulta(tipo = "nfse").nota!!.docEntry)
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(tipo = "NFE").codigo)
        assertEquals(AMBIGUO, consulta().codigo, "sem tipo nem chave: sem filtro de modelo")
        assertEquals(AMBIGUO, consulta(tipo = "NaoDetectado").codigo)
        service = ConsultaPagamentoParcelaService(sap, BigDecimal("0.01"), "39", "45,99", "46,60")
        sap.notas = listOf(nota(docEntry = 10, modelo = "99"))
        assertEquals(10, consulta(tipo = "CTE").nota!!.docEntry)
    }

    @Test
    fun `NFS-e com numero longo casa pelos 9 ultimos so com a chave do SAP confirmando o numero completo`() {
        val numero = "2600000001234"
        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = chave50(numero)))
        val r = consulta(numero = numero, tipo = "NFSE")
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(CriterioNota.NUMERO_TRUNCADO, r.nota!!.criterio)
        assertEquals(listOf(1234), sap.numerosConsultados, "numero acima de Int: so os 9 ultimos")

        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = chave50("2500000001234")))
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = numero, tipo = "NFSE").codigo, "mesmo final de outro ano")

        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = null))
        assertEquals(AMBIGUO, consulta(numero = numero, tipo = "NFSE").codigo, "sem chave no SAP nao confirma")

        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = numero).codigo, "sem tipo: regra dos 9 ultimos nao se aplica")
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = numero, tipo = "NFE").codigo)
    }

    @Test
    fun `NFS-e com chave de 50 digitos acha pela chave mesmo com numero longo`() {
        val numero = "2600000001234"
        val chave = chave50(numero)
        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = chave))
        val r = consulta(numero = numero, chave = chave)
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(CriterioNota.CHAVE_ACESSO, r.nota!!.criterio)
        assertEquals(EM_ABERTO, consulta(numero = "1234", chave = chave).codigo, "pedido com o numero ja truncado")
        sap.notas = listOf(nota(numero = 999, modelo = "46", serie = null, chave = chave))
        assertEquals(DADOS_NOTA_DIVERGENTES, consulta(numero = numero, chave = chave).codigo)
    }

    @Test
    fun `NFS-e com chave e numero curto no pedido ainda exige a confirmacao do numero completo`() {
        val chave = chave50("2600000001234")
        // Chave ainda nao gravada no SAP: a nota sem chave com o mesmo Serial pode ser de outro ano.
        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = null))
        assertEquals(AMBIGUO, consulta(numero = "1234", chave = chave).codigo, "pedido curto")
        assertEquals(listOf(1234), sap.numerosConsultados, "so os 9 ultimos do numero da chave")
        assertEquals(AMBIGUO, consulta(numero = "2600000001234", chave = chave).codigo, "pedido completo: mesmo resultado")
        // Nota do SAP com a chave de 50 digitos de outro numero completo: outro documento.
        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = chave50("2500000001234")))
        assertEquals(NOTA_NAO_ENCONTRADA, consulta(numero = "1234", chave = chave).codigo)
        // Numero que ja cabe em 9 digitos: casamento direto, sem truncamento.
        val curta = chave50("1234")
        sap.notas = listOf(nota(numero = 1234, modelo = "46", serie = null, chave = null))
        val r = consulta(numero = "1234", chave = curta)
        assertEquals(EM_ABERTO, r.codigo)
        assertEquals(CriterioNota.NUMERO, r.nota!!.criterio)
    }

    @Test
    fun `serie de NFS-e nao filtra, so desempata contra SeriesStr preenchida`() {
        sap.notas = listOf(nota(modelo = "46", serie = null))
        assertEquals(EM_ABERTO, consulta(tipo = "NFSE", serie = "00001").codigo, "SeriesStr vazia nao e divergencia")
        sap.notas = listOf(nota(modelo = "46", serie = "700"))
        assertEquals(EM_ABERTO, consulta(tipo = "NFSE", serie = "1").codigo, "serie diferente nao vira NOTA_NAO_ENCONTRADA")
        sap.notas = listOf(nota(docEntry = 10, modelo = "46", serie = null), nota(docEntry = 11, modelo = "46", serie = "000"))
        assertEquals(11, consulta(tipo = "NFSE", serie = "00001").nota!!.docEntry, "3 primeiros caracteres")
        assertEquals(AMBIGUO, consulta(tipo = "NFSE", serie = "5").codigo)
        assertEquals(AMBIGUO, consulta(tipo = "NFSE").codigo)
        sap.notas = listOf(nota(docEntry = 10, modelo = "46", serie = "2"), nota(docEntry = 11, modelo = "60", serie = "002"))
        assertEquals(AMBIGUO, consulta(tipo = "NFSE", serie = "2").codigo)
    }

    @Test
    fun `numero da nota vira Serial com a regra dos 9 ultimos so para NFS-e`() {
        val n = ConsultaPagamentoParcelaService.Companion
        assertEquals(listOf(123), n.numeroNota("000123").seriais)
        assertEquals(listOf(1234), n.numeroNota("2600000001234", TipoNotaEntrada.NFSE).seriais)
        assertEquals(1234, n.numeroNota("2600000001234", TipoNotaEntrada.NFSE).truncado)
        assertEquals(listOf(1, 1000000001), n.numeroNota("1000000001", TipoNotaEntrada.NFSE).seriais)
        assertEquals(listOf(1000000001), n.numeroNota("1000000001").seriais)
        assertEquals(emptyList<Int>(), n.numeroNota("2600000001234").seriais)
        assertEquals(emptyList<Int>(), n.numeroNota("2000000000000", TipoNotaEntrada.NFSE).seriais, "9 ultimos zero")
        assertEquals(emptyList<Int>(), n.numeroNota("12 3").seriais)
        assertEquals(emptyList<Int>(), n.numeroNota("1".repeat(21), TipoNotaEntrada.NFSE).seriais)
    }

    @Test
    fun `modelos configurados precisam ser numericos e de um tipo so`() {
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("0.01"), "39", "45", "39") }
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("0.01"), "", "45", "46") }
        assertThrows<IllegalArgumentException> { ConsultaPagamentoParcelaService(sap, BigDecimal("0.01"), "NFe", "45", "46") }
    }

    companion object {
        const val FILIAL = "12345678000190"
        const val FORNECEDOR = "98765432000100"

        fun nota(docEntry: Int = 10, serie: String? = "1", cancelado: String? = "N", numero: Int = 123,
                 modelo: String? = "39", chave: String? = null) =
            NotaEntradaSap(docEntry, docEntry + 1000, numero, serie, "FOR0000001", 2, "2026-10-01", cancelado, modelo, chave)

        /** Chave de 44 ficticia: cUF 11, AAMM 2610, emitente, modelo, serie, numero, tpEmis 1, codigo, DV 0. */
        fun chave44(cnpj: String = FORNECEDOR, modelo: String = "55", serie: String = "001", numero: Int = 123,
                    codigo: String = "12345678") =
            "112610" + cnpj + modelo + serie + numero.toString().padStart(9, '0') + "1" + codigo + "0"

        /** Chave NFS-e nacional ficticia (50): cMun, ambGer 1, tpInsc 2, emitente, numero (13), AAMM, codigo, DV. */
        fun chave50(numero: String, cnpj: String = FORNECEDOR) =
            "1100205" + "1" + "2" + cnpj + numero.padStart(13, '0') + "2611" + "123456789" + "0"

        fun parcela(numero: Int, total: String, pago: String, status: String = "O",
                    moeda: String? = "R$", moedaLocal: String? = "R$") =
            ParcelaNotaEntradaSap(numero, "2026-11-10", BigDecimal(total), BigDecimal(pago), status, moeda, moedaLocal)

        fun pagamento(docEntry: Int, parcela: Int, valor: String, data: String = "2026-11-10", retido: String? = "0") =
            PagamentoNotaEntradaSap(docEntry, docEntry + 9000, data, parcela, BigDecimal(valor), retido?.let(::BigDecimal))

        /** Parcela de nota com retencao de categoria Pagamento: [wt] na parcela, [wtDocumento] em PCH5 (P). */
        fun parcelaRetencao(numero: Int, total: String, pago: String, wt: String?, wtDocumento: String?, status: String = "O") =
            parcela(numero, total, pago, status).copy(wtSum = wt?.let(::BigDecimal), retencaoPagamentoDocumento = wtDocumento?.let(::BigDecimal))
    }
}
