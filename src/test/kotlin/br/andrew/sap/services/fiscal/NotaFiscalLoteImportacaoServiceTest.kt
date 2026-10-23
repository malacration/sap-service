package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.DIVERGENTE
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.ENCONTRADO
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.FORA_DA_TELA
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao.NAO_ENCONTRADO
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import br.andrew.sap.services.odbc.SqlResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer

class NotaFiscalLoteImportacaoServiceTest {

    // Boletos da BankPlus (IV_IB_BillOfExchange) e documentos que a tela mostra.
    private val boletos = mutableListOf<Map<String, Any?>>()
    private val naTela = mutableListOf<Map<String, Any?>>()
    private val consultas = mutableListOf<String>()

    private fun boleto(nosso: String, docType: Int, docEntry: Int, parcela: Int, documento: String, valor: String, status: Int = 3) =
        mapOf("NossoNumero" to nosso, "DocType" to docType, "DocEntry" to docEntry, "Parcela" to parcela,
            "Documento" to documento, "Valor" to valor, "Status" to status).also { boletos.add(it) }

    private fun doc(tipo: String, docEntry: Int, numero: Int) = mapOf("Tipo" to tipo, "DocEntry" to docEntry, "Numero" to numero,
        "Parcela" to 1, "Linha" to 1, "Total" to 1).also { naTela.add(it) }

    private val odbc = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        val sql = inv.arguments[0] as String
        @Suppress("UNCHECKED_CAST")
        val p = inv.arguments[1] as Map<String, Any?>
        // so a consulta dos boletos tem :nossosNumeros (a da lista tambem cita IV_IB_BillOfExchange)
        val ehBoletos = sql.contains(":nossosNumeros")
        consultas.add(if (ehBoletos) "boletos" else "documentos")
        if (ehBoletos)
            return@Answer QueryResponse(rows = boletos.filter { it["NossoNumero"] in (p["nossosNumeros"] as List<*>) })
        QueryResponse(rows = naTela.filter {
            val lista = if (it["Tipo"] == "AD") p["adiantamentos"] else p["docEntries"]
            it["DocEntry"] in (lista as List<*>)
        })
    })

    private val servico = NotaFiscalLoteImportacaoService(odbc,
        NotaFiscalLoteService(odbc, mock(TaxNfeService::class.java), mock(BankPlusService::class.java), 200, 2))

    // Layout da "Consulta de titulos" do BB: totais em cima, cabecalho na linha 10.
    private fun planilha(vararg titulos: List<Any>): ByteArray {
        val linhas = mutableMapOf<Int, Map<String, Any>>(
            3 to mapOf("A3" to "Beneficiário"),
            8 to mapOf("A8" to "REAL", "B8" to 5064628.68),
            10 to mapOf("B10" to "Cart.", "C10" to "Var.", "D10" to "Nosso número", "E10" to "Nome pagador",
                "F10" to "Nro beneficiário", "G10" to "Valor", "H10" to "Vencimento", "I10" to "Situação"))
        titulos.forEachIndexed { i, t ->
            val n = 11 + i
            linhas[n] = mapOf("B$n" to 17, "C$n" to 19, "D$n" to t[0], "E$n" to "Pagador", "F$n" to t[1], "G$n" to t[2],
                "H$n" to 46342, "I$n" to "Normal")
        }
        linhas[11 + titulos.size + 1] = mapOf("B${12 + titulos.size}" to "#VALUE!")
        return PlanilhaXlsxTest.xlsx(linhas)
    }

    @Test
    fun `nosso numero com digito acha a NF e a parcela, e parcelas do mesmo documento viram um so`() {
        boleto("35158540000021002", 13, 250976, 2, "5715", "16088.38")
        boleto("35158540000021003", 13, 250976, 3, "5715", "16093.2")
        doc("NF", 250976, 5715)
        val r = servico.importar(planilha(listOf("35158540000021002-5", "005715 2", 16088.38), listOf("35158540000021003-3", "005715 3", 16093.2)))
        assertEquals(listOf(ENCONTRADO, ENCONTRADO), r.linhas.map { it.situacao })
        assertEquals(listOf(2, 3), r.linhas.map { it.parcela })
        assertEquals(listOf(11, 12), r.linhas.map { it.linha })
        assertEquals("2026-11-16", r.linhas[0].vencimento)
        assertEquals(listOf(250976), r.documentos.map { it.docEntry })
        // uma ida para os boletos e uma para os documentos
        assertEquals(listOf("boletos", "documentos"), consultas)
    }

    @Test
    fun `adiantamento confere o DocEntry no Nro beneficiario`() {
        boleto("35158540000011882", 203, 4055, 1, "0", "1500")
        doc("AD", 4055, 4100)
        val r = servico.importar(planilha(listOf("35158540000011882-1", "4055", 1500)))
        assertEquals(ENCONTRADO, r.linhas.single().situacao)
        assertEquals(TipoDocumentoLote.AD, r.documentos.single().tipo)
    }

    @Test
    fun `Nro beneficiario ou valor que nao batem entram com aviso`() {
        boleto("35158540000021002", 13, 250976, 2, "5715", "16088.38")
        doc("NF", 250976, 5715)
        val r = servico.importar(planilha(listOf("35158540000021002-5", "005715 3", 100))).linhas.single()
        assertEquals(DIVERGENTE, r.situacao)
        assertTrue(r.mensagem!!.contains("parcela 3, a BankPlus diz parcela 2"), r.mensagem)
        assertTrue(r.mensagem!!.contains("valor na planilha 100, na BankPlus 16088.38"), r.mensagem)
    }

    @Test
    fun `Nro beneficiario ou valor ilegiveis nao saem como conferidos`() {
        boleto("35158540000021002", 13, 250976, 2, "5715", "16088.38")
        boleto("35158540000021003", 13, 250976, 3, "5715", "16093.2")
        doc("NF", 250976, 5715)
        val r = servico.importar(planilha(listOf("35158540000021002-5", "sem numero", 16088.38), listOf("35158540000021003-3", "005715 3", "abc"))).linhas
        assertEquals(listOf(DIVERGENTE, DIVERGENTE), r.map { it.situacao })
        assertTrue(r[0].mensagem!!.contains("Nro beneficiario 'sem numero' ilegivel"), r[0].mensagem)
        assertTrue(r[1].mensagem!!.contains("valor da planilha ilegivel"), r[1].mensagem)
    }

    @Test
    fun `NF sem a parcela no Nro beneficiario nao sai como conferida`() {
        boleto("35158540000021002", 13, 250976, 2, "5715", "16088.38")
        doc("NF", 250976, 5715)
        listOf("005715", "005715 abc").forEach { beneficiario ->
            val r = servico.importar(planilha(listOf("35158540000021002-5", beneficiario, 16088.38))).linhas.single()
            assertEquals(DIVERGENTE, r.situacao, beneficiario)
            assertTrue(r.mensagem!!.contains("ilegivel: nao conferido"), r.mensagem)
        }
    }

    @Test
    fun `nosso numero desconhecido e documento fora da tela ficam de fora do lote`() {
        boleto("35158540000021004", 13, 251066, 1, "5729", "2449")
        val r = servico.importar(planilha(listOf("99999999999999999-1", "000001 1", 10), listOf("35158540000021004-9", "005729 1", 2449)))
        assertEquals(listOf(NAO_ENCONTRADO, FORA_DA_TELA), r.linhas.map { it.situacao })
        assertNull(r.linhas[0].docEntry)
        assertEquals(251066, r.linhas[1].docEntry)
        assertTrue(r.documentos.isEmpty())
    }

    @Test
    fun `boleto reemitido usa o que nao esta cancelado`() {
        boleto("35158540000021005", 13, 1, 1, "10", "50", status = NotaFiscalLoteImportacaoService.STATUS_CANCELADO)
        boleto("35158540000021005", 13, 2, 1, "20", "50")
        doc("NF", 2, 20)
        val r = servico.importar(planilha(listOf("35158540000021005-0", "000020 1", 50))).linhas.single()
        assertEquals(2, r.docEntry)
        assertEquals(ENCONTRADO, r.situacao)
    }

    @Test
    fun `planilha sem coluna Nosso numero e recusada`() {
        val sem = PlanilhaXlsxTest.xlsx(mapOf(1 to mapOf("A1" to "Cliente", "B1" to "Valor"), 2 to mapOf("A2" to "X", "B2" to 1)))
        assertThrows(IllegalArgumentException::class.java) { servico.importar(sem) }
        assertTrue(consultas.isEmpty())
    }

    @Test
    fun `nosso numero, valor e data aceitam o que o banco e o Excel mandam`() {
        assertEquals("35158540000021002", NotaFiscalLoteImportacaoService.nossoNumero("35158540000021002-5"))
        assertEquals("35158540000021002", NotaFiscalLoteImportacaoService.nossoNumero(" 35158540000021002 "))
        assertEquals("16088.38".toBigDecimal(), NotaFiscalLoteImportacaoService.valor("16.088,38"))
        assertEquals("16088.38".toBigDecimal(), NotaFiscalLoteImportacaoService.valor("16088.38"))
        assertEquals("2026-11-16", NotaFiscalLoteImportacaoService.data("16/11/2026"))
        assertEquals("2026-11-16", NotaFiscalLoteImportacaoService.data("46342"))
    }

    @Test
    fun `SQL dos boletos so le e so tem o parametro da lista`() {
        val sql = SqlResource.carregar("odbc/nota-fiscal-lote-boletos.sql")
        assertEquals(setOf("nossosNumeros"), Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)").findAll(sql.replace(Regex("'[^']*'"), "''")).map { it.groupValues[1] }.toSet())
        assertTrue(sql.trimStart().startsWith("SELECT"))
    }
}
