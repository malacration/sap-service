package br.andrew.sap.services.journal.importacao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class LancamentoCsvParserTest {

    private val cabecalho = "Filial;Tipo;Data;Debito;Credito;Valor;Historico;Grupo;Centro;Ref1;Ref2;Ref3"

    private fun ler(vararg linhas: String) = LancamentoCsvParser.ler((listOf(cabecalho) + linhas).joinToString("\r\n"), "UTF-8")

    private fun linha(texto: String) = ler(texto).linhas.single()

    @Test
    fun `le o layout do journal save coluna por coluna`() {
        val l = linha("2;X;05/10/2026;1.1.01;2.1.01;1.234,56;Folha outubro;501;CC10;REF-1;R2;R3")
        assertEquals(emptyList<String>(), l.erros)
        assertEquals(2, l.filial)
        assertEquals(LocalDate.of(2026, 10, 5), l.dataLancamento)
        assertEquals("1.1.01", l.contaDebito)
        assertEquals("2.1.01", l.contaCredito)
        assertEquals(BigDecimal("1234.56"), l.valor)
        assertEquals("Folha outubro", l.historico)
        assertEquals("501", l.grupoEconomico)
        assertEquals("CC10", l.centroCusto)
        assertEquals(listOf("REF-1", "R2", "R3"), listOf(l.referencia, l.referencia2, l.referencia3))
    }

    @Test
    fun `referencia e rateio vazios viram null e nao string vazia`() {
        val l = linha("2;;05/10/2026;1.1.01;2.1.01;10,00;Hist;;;;;")
        assertTrue(l.erros.isEmpty())
        assertNull(l.grupoEconomico)
        assertNull(l.centroCusto)
        assertNull(l.referencia)
    }

    @Test
    fun `data invalida nao rola para outro dia e ano com dois digitos e recusado`() {
        // O readCsv usa SimpleDateFormat leniente: 31/02 vira 03/03 e "26" vira o ano 26.
        assertTrue(linha("2;;31/02/2026;A;B;10,00;H;;").erros.any { it.startsWith("Data inválida") })
        assertTrue(linha("2;;05/10/26;A;B;10,00;H;;").erros.any { it.startsWith("Data inválida") })
        assertEquals(LocalDate.of(2026, 3, 5), linha("2;;5/3/2026;A;B;10,00;H;;").dataLancamento)
    }

    @Test
    fun `valor em formato brasileiro com e sem milhar`() {
        assertEquals(BigDecimal("1234.56") to null, LancamentoCsvParser.valor("1.234,56"))
        assertEquals(BigDecimal("1234.56") to null, LancamentoCsvParser.valor("1234,56"))
        assertEquals(BigDecimal("1234.56") to null, LancamentoCsvParser.valor("1234.56"))
        assertEquals(BigDecimal("1234.56") to null, LancamentoCsvParser.valor("R$ 1.234,56"))
        assertEquals(BigDecimal("1.50") to null, LancamentoCsvParser.valor("1.5"))
    }

    @Test
    fun `valor ambiguo zero negativo ou com centavos demais e erro`() {
        assertTrue(LancamentoCsvParser.valor("1.234").second!!.startsWith("Valor ambíguo"))
        assertEquals("O valor tem que ser maior que zero.", LancamentoCsvParser.valor("0,00").second)
        assertTrue(LancamentoCsvParser.valor("-10,00").second!!.startsWith("Valor negativo"))
        assertTrue(LancamentoCsvParser.valor("10,123").second!!.contains("mais de 2 casas"))
        assertTrue(LancamentoCsvParser.valor("abc").second!!.startsWith("Valor inválido"))
        assertEquals("Valor não informado.", LancamentoCsvParser.valor("").second)
    }

    @Test
    fun `milhar so vale com grupos de tres digitos`() {
        // "1.23,45" virava 123,45 calado
        assertTrue(LancamentoCsvParser.valor("1.23,45").second!!.startsWith("Valor inválido"))
        assertTrue(LancamentoCsvParser.valor("12.3456,00").second!!.startsWith("Valor inválido"))
        assertEquals(BigDecimal("12345.67") to null, LancamentoCsvParser.valor("12.345,67"))
        assertEquals(BigDecimal("1234567.89") to null, LancamentoCsvParser.valor("1.234.567,89"))
    }

    @Test
    fun `aspas malformadas ou sem fechar viram erro em vez de cortar o historico calado`() {
        val malformada = linha("2;;05/10/2026;A;B;10,00;\"H\"oops;501;CC")
        assertEquals(listOf("Aspas malformadas: depois de fechar as aspas, o campo tem que terminar no ';'."), malformada.erros)
        // campo com quebra de linha dentro das aspas chega aqui partido em duas linhas fisicas
        val leitura = ler("2;;05/10/2026;A;B;10,00;\"Linha um", "linha dois\";501;CC")
        assertTrue(leitura.linhas.first().erros.single().startsWith("Aspas sem fechamento"))
        assertTrue(leitura.linhas.none { it.erros.isEmpty() })
        // uma aspa sozinha numa linha nao e linha em branco
        val aspaSolta = ler("2;;05/10/2026;A;B;10,00;H;;", "\"", "2;;06/10/2026;A;B;20,00;H;;")
        assertEquals(listOf(2, 3, 4), aspaSolta.linhas.map { it.linha })
        assertTrue(aspaSolta.linhas[1].erros.single().startsWith("Aspas sem fechamento"))
    }

    @Test
    fun `ponto e virgula no historico entre aspas e aceito`() {
        val l = linha("2;;05/10/2026;A;B;10,00;\"Pgto; NF 10 \"\"urgente\"\"\";501;CC")
        assertTrue(l.erros.isEmpty(), l.erros.toString())
        assertEquals("Pgto; NF 10 \"urgente\"", l.historico)
        assertEquals("501", l.grupoEconomico)
    }

    @Test
    fun `ponto e virgula no historico sem aspas desloca as colunas e vira erro`() {
        val l = linha("2;;05/10/2026;A;B;10,00;Pgto; NF 10;501;CC;R1;R2;R3")
        assertTrue(l.erros.any { it.contains("no máximo 12") }, l.erros.toString())
    }

    @Test
    fun `colunas vazias a mais no fim da linha sao ignoradas`() {
        assertTrue(linha("2;;05/10/2026;A;B;10,00;H;501;CC;;;;;;").erros.isEmpty())
    }

    @Test
    fun `linha curta da um erro so em vez de um por coluna deslocada`() {
        assertEquals(1, linha("2;;05/10/2026;A;B;10,00").erros.size)
    }

    @Test
    fun `campos obrigatorios contas iguais e historico longo`() {
        val erros = linha("x;;;A;A;10,00;${"h".repeat(255)};;").erros
        assertTrue(erros.contains("Filial inválida: 'x'."))
        assertTrue(erros.contains("Data não informada."))
        assertTrue(erros.contains("A conta de débito e a de crédito são a mesma (A)."))
        assertTrue(erros.contains("Histórico com 255 caracteres; o SAP aceita até 254."))
    }

    @Test
    fun `historico de 60 caracteres que o curl antigo importava continua aceito`() {
        // Caso real: lancamento 1499715 no HOMOLOG2, importado pelo /journal/save.
        val historico = "Saída de insumos - custo ggf remessa ind.ord. producao 64351"
        assertEquals(60, historico.length)
        assertTrue(linha("2;;01/08/2026;4.1.1.003.00002;4.9.1.001.00001;3.106,15;$historico;500;50000205;64351;GGF;REMESSA").erros.isEmpty())
        assertTrue(linha("2;;01/08/2026;A;B;10,00;${"h".repeat(254)};;").erros.isEmpty())
    }

    @Test
    fun `arquivo sem cabecalho nao perde o primeiro lancamento calado`() {
        val leitura = LancamentoCsvParser.ler("2;;05/10/2026;A;B;10,00;H;;\r\n2;;06/10/2026;A;B;20,00;H;;", "UTF-8")
        assertTrue(leitura.erros.single().contains("cabeçalho"))
        // primeira linha com data errada tambem e lancamento, nao cabecalho
        val dataErrada = LancamentoCsvParser.ler("2;;31/02/2026;A;B;10,00;H;;\r\n2;;06/10/2026;A;B;20,00;H;;", "UTF-8")
        assertTrue(dataErrada.erros.single().contains("cabeçalho"))
    }

    @Test
    fun `linhas em branco e de separadores sao ignoradas mantendo o numero da linha no arquivo`() {
        val leitura = ler("", "2;;05/10/2026;A;B;10,00;H;;", ";;;;;;;;", "2;;06/10/2026;A;B;20,00;H;;", "")
        assertEquals(listOf(3, 5), leitura.linhas.map { it.linha })
    }

    @Test
    fun `arquivo vazio ou so com cabecalho`() {
        assertEquals(listOf("O arquivo está vazio."), LancamentoCsvParser.ler("\r\n", "UTF-8").erros)
        assertEquals(listOf("O arquivo não tem nenhum lançamento além do cabeçalho."), ler().erros)
    }

    @Test
    fun `csv salvo pelo Excel em Windows-1252 nao estraga o acento`() {
        val bytes = "$cabecalho\r\n2;;05/10/2026;A;B;10,00;Histórico ação;;".toByteArray(charset("windows-1252"))
        val leitura = LancamentoCsvParser.ler(bytes)
        assertEquals("Windows-1252", leitura.codificacao)
        assertEquals("Histórico ação", leitura.linhas.single().historico)
    }

    @Test
    fun `utf-8 com BOM`() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "$cabecalho\n2;;05/10/2026;A;B;10,00;Histórico;;".toByteArray()
        val leitura = LancamentoCsvParser.ler(bytes)
        assertEquals("UTF-8", leitura.codificacao)
        assertTrue(leitura.erros.isEmpty())
        assertEquals("Histórico", leitura.linhas.single().historico)
    }

    @Test
    fun `vira o mesmo lancamento de duas pernas que o journal save envia`() {
        val entry = linha("2;;05/10/2026;1.1.01;2.1.01;1.234,56;Folha;501;CC10;REF-1;;").paraLancamento()
        val json = ObjectMapper().registerModule(KotlinModule.Builder().build()).readTree(
            ObjectMapper().registerModule(KotlinModule.Builder().build()).writeValueAsString(entry)
        )
        assertEquals("2026-10-05", json["TaxDate"].asText())
        assertEquals("2026-10-05", json["ReferenceDate"].asText())
        assertEquals("Folha", json["Memo"].asText())
        assertEquals("REF-1", json["Reference"].asText())
        assertFalse(json.has("Reference2"))
        val linhas = json["JournalEntryLines"]
        assertEquals(2, linhas.size())
        assertEquals("1.1.01", linhas[0]["AccountCode"].asText())
        assertEquals(1234.56, linhas[0]["Debit"].asDouble())
        assertEquals("2.1.01", linhas[1]["AccountCode"].asText())
        assertEquals(1234.56, linhas[1]["Credit"].asDouble())
        linhas.forEach {
            assertEquals(2, it["BPLID"].asInt())
            assertEquals("501", it["CostingCode"].asText())
            assertEquals("CC10", it["CostingCode2"].asText())
        }
    }
}
