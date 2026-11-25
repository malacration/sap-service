package br.andrew.sap.services.fiscal

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.charset.Charset

class PlanilhaCsvTest {
    @Test
    fun `aspas, virgula decimal dentro das aspas, BOM, linha com quebra e celulas vazias omitidas`() {
        val t = "﻿a,,\"27.750,00\",\"diz \"\"oi\"\"\"\r\n\r\n x ;y,,\"duas\nlinhas\"\r\n"
        val r = PlanilhaCsv.ler(t.toByteArray())
        assertEquals(mapOf(0 to "a", 2 to "27.750,00", 3 to "diz \"oi\""), r[1])
        assertEquals(null, r[2])
        assertEquals(mapOf(0 to "x ;y", 2 to "duas\nlinhas"), r[3])
    }

    @Test
    fun `ponto e virgula como separador e windows-1252`() {
        val r = PlanilhaCsv.ler("Número;Valor\n1;\"2,5\"\n".toByteArray(Charset.forName("windows-1252")))
        assertEquals(mapOf(0 to "Número", 1 to "Valor"), r[1])
        assertEquals(mapOf(0 to "1", 1 to "2,5"), r[2])
    }

    @Test
    fun `xlsx continua sendo lido como xlsx`() {
        val xlsx = PlanilhaXlsxTest.xlsx(mapOf(1 to mapOf("A1" to "Nosso número")))
        assertEquals("Nosso número", NotaFiscalLoteImportacaoService.lerArquivo(xlsx)[1]!![0])
    }

    @Test
    fun `aspas sem fechamento e recusada com a linha onde abriu`() {
        val e = org.junit.jupiter.api.Assertions.assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) {
            PlanilhaCsv.ler("a,b\nc,\"d\ne,f\n".toByteArray())
        }
        assertEquals("CSV invalido: aspas sem fechamento na linha 2", e.message)
        // aspas no meio do texto e caractere comum, nao abre campo
        assertEquals(mapOf(0 to "5\" tubo", 1 to "O\"BRIEN"), PlanilhaCsv.ler("5\" tubo,O\"BRIEN\n".toByteArray())[1])
    }

    @Test
    fun `depois do fecha-aspas so separador, fim de registro ou espaco`() {
        val e = org.junit.jupiter.api.Assertions.assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) {
            PlanilhaCsv.ler("a,b\n\"81\"8,x\n".toByteArray())
        }
        assertEquals("CSV invalido: caractere inesperado depois das aspas na linha 2", e.message)
        org.junit.jupiter.api.Assertions.assertThrows(br.andrew.sap.model.fiscal.ImportacaoInvalidaException::class.java) {
            PlanilhaCsv.ler("\"a\" \"b\",c\n".toByteArray())
        }
        // validos: espaco depois do fecha-aspas, antes do separador e do fim de linha
        assertEquals(mapOf(0 to "81", 1 to "x"), PlanilhaCsv.ler("\"81\" ,x\n".toByteArray())[1])
        assertEquals(mapOf(0 to "81"), PlanilhaCsv.ler("\"81\"\r\n".toByteArray())[1])
        assertEquals(mapOf(0 to "81"), PlanilhaCsv.ler("\"81\"".toByteArray())[1])
    }

    @Test
    fun `separador vem da linha de cabecalho - ponto e virgula com virgula decimal e metadados cheios de virgulas`() {
        val t = "Cedente: A, B, C, D, E, F, G, H\nObs: 1,2,3,4,5,6\n" +
            "Sacado;Seu Número;Vencimento;Valor (R\$)\nFULANO;3956 1;16/11/2026;27750,00\nBELTRANO;0005647-2;01/01/2027;51836,09\n"
        val r = PlanilhaCsv.ler(t.toByteArray())
        assertEquals(mapOf(0 to "FULANO", 1 to "3956 1", 2 to "16/11/2026", 3 to "27750,00"), r[4])
        assertEquals("51836,09", r[5]!![3])
        // cabecalho com virgula e decimal entre aspas
        val v = PlanilhaCsv.ler("Sacado,Seu Número,Valor (R\$)\nFULANO,3956 1,\"27.750,00\"\n".toByteArray())
        assertEquals(mapOf(0 to "FULANO", 1 to "3956 1", 2 to "27.750,00"), v[2])
    }

    @Test
    fun `csv acima do teto de bytes e recusado com 413 e mensagem em portugues`() {
        val e = org.junit.jupiter.api.Assertions.assertThrows(br.andrew.sap.model.fiscal.ArquivoGrandeDemaisException::class.java) {
            PlanilhaCsv.ler(ByteArray(PlanilhaCsv.TETO_CSV + 1) { 'a'.code.toByte() })
        }
        assertEquals("A planilha e grande demais (limite de 1 MB para arquivo CSV).", e.message)
        PlanilhaCsv.ler(ByteArray(PlanilhaCsv.TETO_CSV) { 'a'.code.toByte() })
    }
}
