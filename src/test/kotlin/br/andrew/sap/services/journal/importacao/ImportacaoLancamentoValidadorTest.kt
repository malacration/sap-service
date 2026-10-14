package br.andrew.sap.services.journal.importacao

import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoCadastros.*
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import java.time.LocalDate

class ImportacaoLancamentoValidadorTest {

    private val cadastros = mock<ImportacaoLancamentoCadastros> {
        on { filiais(any()) } doReturn mapOf(
            2 to Filial(2, "Matriz", false),
            9 to Filial(9, "Antiga", true),
        )
        on { contas(any()) } doReturn listOf(
            Conta("D", "Despesa", false, false, false, null, null),
            Conta("C", "Caixa", false, false, false, null, null),
            Conta("T", "Ativo", true, false, false, null, null),
            Conta("CLI", "Clientes", false, true, false, null, null),
            Conta("CONG", "Congelada", false, false, true, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)),
        ).associateBy { it.codigo }
        on { rateios(any()) } doReturn listOf(
            Rateio("501", 1, true),
            Rateio("CC10", 2, true),
            Rateio("CCX", 2, false),
        ).associateBy { it.codigo }
        on { referencias(any()) } doReturn mapOf(
            "JA" to ReferenciaExistente("JA", 777, 3),
            "64351" to ReferenciaExistente("64351", 1499715, 2),
        )
        on { iguais(any(), any(), any()) } doReturn listOf(
            // caso do HOMOLOG2: a ordem de producao 64351 tem um lancamento proprio (outra conta) e o
            // lancamento importado pelo curl (mesma conta de debito e valor da linha do arquivo)
            LancamentoExistente(1425734, "64351", LocalDate.of(2026, 10, 5), "1.1.3.007.00300", "10.00".toBigDecimal()),
            LancamentoExistente(1499715, "64351", LocalDate.of(2026, 10, 5), "D", "10.00".toBigDecimal(), 793886),
        )
    }
    private val validador = ImportacaoLancamentoValidador(cadastros)

    private fun linha(
        numero: Int = 2, filial: Int = 2, debito: String = "D", credito: String = "C",
        grupo: String? = "501", centro: String? = "CC10", referencia: String? = null,
        data: LocalDate = LocalDate.of(2026, 10, 5),
    ) = LinhaLancamento(numero, filial, data, debito, credito, 10.toBigDecimal(), "H", grupo, centro, referencia, null, null)

    @Test
    fun `linha com cadastros validos passa sem erro nem aviso`() {
        val l = linha()
        validador.validar(listOf(l))
        assertEquals(emptyList<String>(), l.erros)
        assertEquals(emptyList<String>(), l.avisos)
    }

    @Test
    fun `filial inexistente ou inativa`() {
        val inexistente = linha(filial = 5)
        val inativa = linha(numero = 3, filial = 9)
        validador.validar(listOf(inexistente, inativa))
        assertEquals(listOf("A filial 5 não existe no SAP."), inexistente.erros)
        assertEquals(listOf("A filial 9 (Antiga) está inativa."), inativa.erros)
    }

    @Test
    fun `conta inexistente titulo associada e congelada na data`() {
        val linhas = listOf(
            linha(debito = "NAO"),
            linha(debito = "T"),
            linha(credito = "CLI"),
            linha(debito = "CONG"),
            linha(debito = "CONG", data = LocalDate.of(2026, 11, 1)),
        )
        validador.validar(linhas)
        assertEquals(listOf("A conta de débito NAO não existe no plano de contas."), linhas[0].erros)
        assertTrue(linhas[1].erros.single().contains("conta título"))
        assertTrue(linhas[2].erros.single().contains("não aceita lançamento manual"))
        assertEquals(listOf("A conta de débito CONG (Congelada) está congelada em 05/10/2026."), linhas[3].erros)
        assertEquals(emptyList<String>(), linhas[4].erros)
    }

    @Test
    fun `rateio inexistente da dimensao errada ou inativo`() {
        val linhas = listOf(linha(grupo = "XX"), linha(grupo = "CC10"), linha(centro = "CCX"))
        validador.validar(linhas)
        assertEquals(listOf("O grupo econômico XX não existe no SAP."), linhas[0].erros)
        assertEquals(listOf("O grupo econômico CC10 é da dimensão 2; esta coluna é da dimensão 1."), linhas[1].erros)
        assertEquals(listOf("O centro de custo CCX está inativo."), linhas[2].erros)
    }

    @Test
    fun `referencia ja usada no SAP repetida no arquivo e linha identica so avisam`() {
        val ja = linha(numero = 2, referencia = "JA")
        val a = linha(numero = 3, referencia = "R")
        val b = linha(numero = 4, referencia = "R")
        validador.validar(listOf(ja, a, b))
        assertEquals(listOf("A referência JA já está na transação 777 (e em mais 2)."), ja.avisos)
        assertTrue(b.avisos.contains("Linha idêntica à linha 3."))
        assertTrue(a.avisos.contains("A referência R se repete nas linhas 3, 4 do arquivo."))
        assertTrue(listOf(ja, a, b).all { it.erros.isEmpty() })
    }

    @Test
    fun `lancamento igual ja no SAP marca a linha e substitui o aviso de referencia`() {
        val igual = linha(referencia = "64351")
        val outraConta = linha(numero = 3, debito = "CONG", referencia = "64351", data = LocalDate.of(2026, 11, 5))
        validador.validar(listOf(igual, outraConta))
        assertEquals(1499715, igual.lancamentoIgual)
        assertEquals(793886, igual.numeroLancamentoIgual)
        assertTrue(igual.avisos.any { it.startsWith("Já existe no SAP um lançamento igual (nº 793886, transação 1499715)") })
        assertTrue(igual.avisos.none { it.contains("já está na transação") }, igual.avisos.toString())
        assertNull(outraConta.lancamentoIgual)
        assertTrue(outraConta.avisos.contains("A referência 64351 já está na transação 1499715 (e em mais 1)."))
        verify(cadastros).iguais(
            setOf("64351"), setOf(LocalDate.of(2026, 10, 5), LocalDate.of(2026, 11, 5)), setOf("D", "CONG")
        )
    }

    @Test
    fun `consulta os cadastros uma vez so com os codigos distintos do arquivo`() {
        validador.validar(listOf(linha(), linha(numero = 3, centro = null)))
        verify(cadastros).contas(setOf("D", "C"))
        verify(cadastros).rateios(setOf("501", "CC10"))
        verify(cadastros).filiais(setOf(2))
        verify(cadastros).referencias(emptySet())
    }

    @Test
    fun `iguais consulta as referencias em blocos com a conta de debito`() {
        val odbc = mock<OdbcClient> { on { consultar(any(), any(), any()) } doReturn QueryResponse() }
        val reais = ImportacaoLancamentoCadastros(odbc)
        reais.iguais((1..150).map { "R$it" }.toSet(), setOf(LocalDate.of(2026, 10, 5)), setOf("D"))
        val parametros = argumentCaptor<Map<String, Any?>>()
        verify(odbc, times(2)).consultar(argThat { contains("FROM \"OJDT\"") }, parametros.capture(), any())
        assertEquals(listOf(100, 50), parametros.allValues.map { (it["referencias"] as List<*>).size })
        assertTrue(parametros.allValues.all { it["contas"] == listOf("D") && it["datas"] == listOf("2026-10-05") })
    }

    @Test
    fun `iguais divide o bloco ao meio quando o sap-odbc trunca`() {
        val odbc = mock<OdbcClient> {
            on { consultar(any(), any(), any()) } doAnswer { invocacao ->
                val refs = (invocacao.arguments[1] as Map<*, *>)["referencias"] as List<*>
                if (refs.size > 2) throw OdbcException("resultado_truncado", "truncado")
                QueryResponse(rows = refs.map {
                    mapOf("TransId" to 1, "Referencia" to it, "Data" to "2026-10-05T00:00", "Conta" to "D", "Debito" to "10.000000")
                })
            }
        }
        val achados = ImportacaoLancamentoCadastros(odbc)
            .iguais(setOf("R1", "R2", "R3", "R4", "R5"), setOf(LocalDate.of(2026, 10, 5)), setOf("D"))
        assertEquals(setOf("R1", "R2", "R3", "R4", "R5"), achados.map { it.referencia }.toSet())

        val umaSo = mock<OdbcClient> {
            on { consultar(any(), any(), any()) } doThrow OdbcException("resultado_truncado", "truncado")
        }
        assertThrows<OdbcException> {
            ImportacaoLancamentoCadastros(umaSo).iguais(setOf("R1"), setOf(LocalDate.of(2026, 10, 5)), setOf("D"))
        }
    }

    @Test
    fun `cadastros nao chama o sap-odbc sem codigo e manda a lista como parametro`() {
        val odbc = mock<OdbcClient> {
            on { consultar(any(), any(), any()) } doReturn QueryResponse(rows = listOf(
                mapOf("OcrCode" to "501", "DimCode" to "1", "Active" to "Y"),
            ))
        }
        val reais = ImportacaoLancamentoCadastros(odbc)
        assertEquals(emptyMap<String, Any>(), reais.referencias(emptySet()))
        verifyNoInteractions(odbc)

        val rateio = reais.rateios(setOf("501")).getValue("501")
        assertEquals(1, rateio.dimensao)
        assertTrue(rateio.ativo)
        verify(odbc).consultar(argThat { contains("FROM \"OOCR\"") }, eq(mapOf("codigos" to listOf("501"))), any())
    }
}
