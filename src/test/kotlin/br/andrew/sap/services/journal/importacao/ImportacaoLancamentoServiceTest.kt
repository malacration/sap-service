package br.andrew.sap.services.journal.importacao

import JournalEntry
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchRecusadoException
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.journal.JournalEntriesService
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoCadastros.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.web.client.ResourceAccessException

class ImportacaoLancamentoServiceTest {

    private val cadastros = mock<ImportacaoLancamentoCadastros> {
        on { filiais(any()) } doReturn mapOf(2 to Filial(2, "Matriz", false))
        on { contas(any()) } doReturn listOf("D", "C").associateWith { Conta(it, it, false, false, false, null, null) }
        on { rateios(any()) } doReturn emptyMap()
        on { referencias(any()) } doReturn emptyMap()
    }
    private val logService = mock<ImportacaoLancamentoLogService> {
        on { porHash(any()) } doReturn emptyList()
    }
    private val journal = mock<JournalEntriesService>()
    private val batch = mock<BatchService>()
    private val service = ImportacaoLancamentoService(ImportacaoLancamentoValidador(cadastros), cadastros, logService, journal, batch, 500)

    private val csv = ("Filial;Tipo;Data;Debito;Credito;Valor;Historico;Grupo;Centro;Ref1;Ref2;Ref3\r\n" +
        "2;;05/10/2026;D;C;10,00;Primeiro;;;;;\r\n" +
        "2;;06/10/2026;D;C;20,50;Segundo;;;;;").toByteArray()

    private fun hash() = service.validar(csv, "a.csv").hash

    private fun criado(jdtNum: Int, contentId: Int) = BatchResponse(
        201, "Content-ID: $contentId\r\nHTTP/1.1 201 Created\r\nContent-Type: application/json\r\n\r\n" +
            "{\"JdtNum\": $jdtNum, \"Number\": ${jdtNum + 700000}, \"Memo\": \"x\"}", true, contentId = contentId,
    )

    private fun importado(code: String) = ImportacaoLancamentoLog(Code = code, U_Status = ImportacaoLancamentoLog.IMPORTADO)

    private fun importar(hash: String = hash(), forcar: Boolean = false, conteudo: ByteArray = csv) =
        service.importar(conteudo, "a.csv", hash, forcar, "Fulano", "SalePerson:60")

    @Test
    fun `previa soma o arquivo e libera a importacao`() {
        val previa = service.validar(csv, "a.csv")
        assertTrue(previa.podeImportar)
        assertEquals(2, previa.totalLinhas)
        assertEquals("30.50", previa.totalValor.toPlainString())
        assertEquals(64, previa.hash.length)
        verifyNoInteractions(batch)
    }

    @Test
    fun `resumo por conta soma debito e credito de cada conta com o nome do plano de contas`() {
        val tres = (String(csv) + "\r\n2;;07/10/2026;C;D;5,00;Terceiro;;;;;").toByteArray()
        val resumo = service.validar(tres, "a.csv").resumoPorConta
        assertEquals(listOf("C", "D"), resumo.map { it.conta })
        val d = resumo.single { it.conta == "D" }
        assertEquals("D", d.nome)
        assertEquals("30.50", d.debito.toPlainString())
        assertEquals("5.00", d.credito.toPlainString())
        assertEquals("25.50", d.saldo.toPlainString())
        assertEquals(3, d.linhas)
        val c = resumo.single { it.conta == "C" }
        assertEquals("-25.50", c.saldo.toPlainString())
        // debito total = credito total = total do arquivo
        assertEquals(0, resumo.fold(java.math.BigDecimal.ZERO) { s, it -> s + it.saldo }.signum())
    }

    @Test
    fun `grava o log ENVIANDO antes e o PATCH para IMPORTADO no mesmo changeset dos lancamentos`() {
        val lote = argumentCaptor<BatchList>()
        whenever(batch.run(lote.capture())).thenReturn(listOf(criado(101, 1), criado(102, 2), criado(0, 3)))

        val resultado = importar()

        assertEquals("IMPORTADO", resultado.status)
        assertEquals(listOf(101, 102), resultado.lancamentos)
        // cada linha do arquivo com o lancamento que gerou (linha 1 do arquivo e o cabecalho)
        assertEquals(listOf(Triple(2, 101, 700101), Triple(3, 102, 700102)),
            resultado.porLinha.map { Triple(it.linha, it.transacao, it.numero) })
        val itens = lote.firstValue.toList()
        assertEquals(3, itens.size)
        assertTrue(itens.take(2).all { it.method == BatchMethod.POST })
        assertEquals(listOf("Primeiro", "Segundo"), itens.take(2).map { (it.payload as JournalEntry).memo })

        // o log nasce ENVIANDO numa chamada antes do lote...
        val inicio = argumentCaptor<ImportacaoLancamentoLog>()
        inOrder(logService, batch) {
            verify(logService).registrarInicio(inicio.capture())
            verify(batch).run(any<BatchList>())
        }
        val log = inicio.firstValue
        assertEquals(ImportacaoLancamentoLog.ENVIANDO, log.U_Status)
        // ...e vira IMPORTADO no mesmo changeset: so muda se os lancamentos entrarem
        val status = itens.last()
        assertEquals(BatchMethod.PATCH, status.method)
        assertSame(logService, status.service)
        val patch = status.payload as ImportacaoLancamentoStatus
        assertEquals("'${log.Code}'", patch.getId())
        assertEquals(mapOf("U_Status" to "IMPORTADO"), patch.campos)
        assertEquals("Fulano", log.U_Usuario)
        assertEquals("SalePerson:60", log.U_UsuarioId)
        assertEquals(2, log.U_Linhas)
        assertEquals("30.50", log.U_Total!!.toPlainString())
        assertEquals("N", log.U_Forcado)
        val registrados = argumentCaptor<List<LancamentoGerado>>()
        verify(logService).registrarLancamentos(eq(log.Code!!), registrados.capture())
        assertEquals(listOf(2 to 700101, 3 to 700102), registrados.firstValue.map { it.linha to it.numero })
    }

    @Test
    fun `historico traz os lancamentos que cada importacao gerou`() {
        whenever(logService.ultimasImportadas(any())).thenReturn(listOf(
            ImportacaoLancamentoLog(Code = "N", U_Usuario = "Fulano", U_Data = "2026-10-06T00:00:00Z", U_Hora = "15:00",
                U_Linhas = 2, U_Forcado = "Y",
                U_Lancamentos = "[{\"linha\":2,\"numero\":793886,\"transacao\":1499715},{\"linha\":3,\"numero\":793887,\"transacao\":1499716}]"),
            ImportacaoLancamentoLog(Code = "A", U_Lancamentos = "101, 102"),
            ImportacaoLancamentoLog(Code = "V", U_Status = ImportacaoLancamentoLog.IMPORTADO),
        ))
        val historico = service.historico()
        assertEquals(listOf("N", "A", "V"), historico.map { it.id })
        val (novo, antigo, semNumeros) = historico
        assertEquals("2026-10-06", novo.data)
        assertTrue(novo.forcado)
        assertEquals(listOf(Triple(2, 793886, 1499715), Triple(3, 793887, 1499716)),
            novo.lancamentos.map { Triple(it.linha, it.numero, it.transacao) })
        // formato antigo: so as transacoes
        assertEquals(listOf(101, 102), antigo.lancamentos.map { it.transacao })
        assertTrue(semNumeros.lancamentos.isEmpty())
    }

    @Test
    fun `casa cada linha com a resposta pelo Content-ID mesmo fora de ordem`() {
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(criado(0, 3), criado(102, 2), criado(101, 1)))
        val resultado = importar()
        assertEquals("IMPORTADO", resultado.status)
        assertEquals(listOf(2 to 101, 3 to 102), resultado.porLinha.map { it.linha to it.transacao })
    }

    @Test
    fun `Content-ID inesperado nao e reconhecido e vai para a releitura do log`() {
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(criado(101, 1), criado(102, 7), criado(0, 3)))
        whenever(logService.porCode(any())).thenAnswer { importado(it.arguments[0] as String) }
        val resultado = importar()
        assertEquals("IMPORTADO", resultado.status)
        assertTrue(resultado.porLinha.isEmpty())
        verify(logService, never()).registrarLancamentos(any(), any())
    }

    @Test
    fun `resposta sem Content-ID nao casa por posicao - confirma pelo log, sem numeros por linha`() {
        val semId = { jdt: Int -> BatchResponse(201, "HTTP/1.1 201 Created\r\n\r\n{\"JdtNum\": $jdt, \"Number\": 1}", true) }
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(semId(102), semId(101), semId(0)))
        whenever(logService.porCode(any())).thenAnswer { importado(it.arguments[0] as String) }
        val resultado = importar()
        assertEquals("IMPORTADO", resultado.status)
        assertTrue(resultado.porLinha.isEmpty())
    }

    @Test
    fun `resposta sem o Numero busca pelo numero da transacao`() {
        val semNumero = { jdt: Int, id: Int -> BatchResponse(201, "Content-ID: $id\r\nHTTP/1.1 201 Created\r\n\r\n{\"JdtNum\": $jdt}", true, contentId = id) }
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(semNumero(101, 1), criado(102, 2), semNumero(0, 3)))
        whenever(cadastros.numeros(setOf(101))).thenReturn(mapOf(101 to 793886))
        val resultado = importar()
        assertEquals(listOf(793886, 700102), resultado.porLinha.map { it.numero })

        // sap-odbc fora: a linha fica so com a transacao, a importacao continua IMPORTADO
        whenever(cadastros.numeros(any())).thenThrow(RuntimeException("odbc fora"))
        whenever(logService.porHash(any())).thenReturn(emptyList())
        val outro = (String(csv) + "\r\n").toByteArray()
        val semOdbc = service.importar(outro, "b.csv", service.validar(outro, "b.csv").hash, false, "Fulano", "SalePerson:60")
        assertEquals("IMPORTADO", semOdbc.status)
        assertNull(semOdbc.porLinha.first().numero)
    }

    @Test
    fun `arquivo diferente do validado e recusado sem enviar nada`() {
        val e = assertThrows<ImportacaoLancamentoException> { importar(hash = "outro") }
        assertEquals(HttpStatus.CONFLICT, e.status)
        verifyNoInteractions(batch)
    }

    @Test
    fun `arquivo com erro nao importa nenhuma linha`() {
        val ruim = (String(csv) + "\r\n2;;31/02/2026;D;C;5,00;Terceiro;;;;;").toByteArray()
        val e = assertThrows<ImportacaoLancamentoException> {
            importar(hash = service.validar(ruim, "a.csv").hash, conteudo = ruim)
        }
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.status)
        assertEquals(1, e.previa!!.linhasComErro)
        verifyNoInteractions(batch)
    }

    @Test
    fun `arquivo ja importado so entra de novo se confirmar a reimportacao`() {
        whenever(logService.porHash(any())).thenReturn(listOf(
            ImportacaoLancamentoLog(Code = "X", U_Usuario = "Beltrano", U_Data = "2026-10-01T00:00:00Z", U_Hora = "09:00",
                U_Status = ImportacaoLancamentoLog.IMPORTADO),
            // tentativa recusada do mesmo arquivo nao conta como importacao anterior
            ImportacaoLancamentoLog(Code = "R", U_Usuario = "Ciclano", U_Status = ImportacaoLancamentoLog.RECUSADO),
        ))
        assertEquals(listOf("X"), service.validar(csv, "a.csv").importacoesAnteriores.map { it.id })
        val e = assertThrows<ImportacaoLancamentoException> { importar() }
        assertEquals(HttpStatus.CONFLICT, e.status)
        assertTrue(e.message.contains("Beltrano em 2026-10-01 09:00"))
        verifyNoInteractions(batch)

        whenever(batch.run(any<BatchList>())).thenReturn(listOf(criado(101, 1), criado(102, 2), criado(0, 3)))
        assertEquals("IMPORTADO", importar(forcar = true).status)
        val inicio = argumentCaptor<ImportacaoLancamentoLog>()
        verify(logService).registrarInicio(inicio.capture())
        assertEquals("Y", inicio.firstValue.U_Forcado)
    }

    @Test
    fun `linhas que ja existem no SAP exigem confirmacao mesmo sem registro de importacao`() {
        // Arquivo importado pelo curl antigo: nao ha hash em @LC_IMPORTACAO, mas os lancamentos estao la.
        whenever(cadastros.iguais(any(), any(), any())).thenReturn(listOf(
            LancamentoExistente(1499715, "R1", java.time.LocalDate.of(2026, 10, 5), "D", "10.00".toBigDecimal())
        ))
        val comReferencia = (String(csv).replace("Primeiro;;;;;", "Primeiro;;;R1;;")).toByteArray()
        val previa = service.validar(comReferencia, "a.csv")
        assertTrue(previa.podeImportar)
        assertEquals(1, previa.linhasJaNoSap)
        assertTrue(previa.exigeConfirmacao)

        val e = assertThrows<ImportacaoLancamentoException> {
            importar(hash = previa.hash, conteudo = comReferencia)
        }
        assertEquals(HttpStatus.CONFLICT, e.status)
        assertTrue(e.message.startsWith("1 lançamento(s) do arquivo já existem no SAP."))
        verifyNoInteractions(batch)
    }

    @Test
    fun `SAP recusou o lote - nada gravado e o log vira RECUSADO`() {
        doAnswer { throw BatchRecusadoException("400 - Period is locked") }.whenever(batch).run(any<BatchList>())
        whenever(logService.porCode(any())).thenAnswer {
            ImportacaoLancamentoLog(Code = it.arguments[0] as String, U_Status = ImportacaoLancamentoLog.ENVIANDO)
        }
        val e = assertThrows<ImportacaoLancamentoException> { importar() }
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.status)
        assertTrue(e.message.contains("nenhum lançamento foi gravado"))
        assertTrue(e.message.contains("Period is locked"))
        verify(logService).marcar(any(), eq(ImportacaoLancamentoLog.RECUSADO))
    }

    @Test
    fun `falha ao registrar o inicio nao envia o lote`() {
        whenever(logService.registrarInicio(any())).thenThrow(ResourceAccessException("SL fora"))
        val e = assertThrows<ImportacaoLancamentoException> { importar() }
        assertEquals(HttpStatus.BAD_GATEWAY, e.status)
        assertTrue(e.message.contains("nenhum lançamento foi enviado"))
        verifyNoInteractions(batch)
    }

    @Test
    fun `pedido recusado pelo Service Layer antes de processar tambem e recusa definitiva`() {
        whenever(batch.run(any<BatchList>())).thenThrow(
            org.springframework.web.client.HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request")
        )
        whenever(logService.porCode(any())).thenReturn(null)
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, assertThrows<ImportacaoLancamentoException> { importar() }.status)
    }

    @Test
    fun `timeout sem log IMPORTADO ainda nao e recusa - fica em conferencia, com o log ENVIANDO no SAP`() {
        // o changeset pode estar em andamento no SAP
        whenever(batch.run(any<BatchList>())).thenThrow(ResourceAccessException("Read timed out"))
        whenever(logService.porCode(any())).thenAnswer {
            ImportacaoLancamentoLog(Code = it.arguments[0] as String, U_Status = ImportacaoLancamentoLog.ENVIANDO)
        }
        val resultado = importar()
        assertEquals("CONFERIR", resultado.status)
        verify(logService, never()).marcar(any(), any())
    }

    @Test
    fun `tentativa ENVIANDO recente barra o mesmo arquivo mesmo depois de um restart`() {
        // a trava vem do SAP, nao da memoria: um servico novo (restart ou outra instancia) tambem barra
        val enviando = ImportacaoLancamentoLog.novo("Fulano", "SalePerson:60", "a.csv", hash(), 2, "30.50".toBigDecimal(), false)
        whenever(logService.emEnvio(any())).thenReturn(listOf(enviando))
        val reiniciado = ImportacaoLancamentoService(ImportacaoLancamentoValidador(cadastros), cadastros, logService, journal, batch, 500)
        val e = assertThrows<ImportacaoLancamentoException> {
            reiniciado.importar(csv, "a.csv", hash(), true, "Fulano", "SalePerson:60")
        }
        assertEquals(HttpStatus.CONFLICT, e.status)
        assertTrue(e.message.contains("ainda pode estar entrando"), e.message)
        verifyNoInteractions(batch)
        verify(logService, never()).registrarInicio(any())
    }

    @Test
    fun `tentativa ENVIANDO antiga nao trava - ela nao entrou`() {
        whenever(logService.emEnvio(any())).thenReturn(listOf(ImportacaoLancamentoLog(
            Code = "20260101080000-abcd1234", U_Status = ImportacaoLancamentoLog.ENVIANDO,
        )))
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(criado(101, 1), criado(102, 2), criado(0, 3)))
        assertEquals("IMPORTADO", importar().status)
    }

    @Test
    fun `tentativa que entrou deixa de ser ENVIANDO e cai na confirmacao de reimportacao`() {
        whenever(logService.porHash(any())).thenReturn(listOf(
            ImportacaoLancamentoLog(Code = "X", U_Usuario = "Fulano", U_Status = ImportacaoLancamentoLog.IMPORTADO)
        ))
        val e = assertThrows<ImportacaoLancamentoException> { importar() }
        assertTrue(e.message.contains("já foi importado"), e.message)
        verifyNoInteractions(batch)
    }

    @Test
    fun `falha no login antes do lote nao e recusa do SAP`() {
        // AuthService lanca Exception simples quando o login volta sem corpo
        doAnswer { throw Exception("Login sem resposta") }.whenever(batch).run(any<BatchList>())
        whenever(logService.porCode(any())).thenReturn(null)
        assertEquals("CONFERIR", importar().status)
    }

    @Test
    fun `resposta nao reconhecida e releitura com erro - conferir com o id, sem erro 500`() {
        whenever(batch.run(any<BatchList>())).thenReturn(emptyList())
        whenever(logService.porCode(any())).thenThrow(ResourceAccessException("SL fora"))
        val resultado = importar()
        assertEquals("CONFERIR", resultado.status)
        assertTrue(resultado.id.isNotBlank())
    }

    @Test
    fun `resposta completa do lote dispensa reler o log`() {
        whenever(batch.run(any<BatchList>())).thenReturn(listOf(criado(101, 1), criado(102, 2), criado(0, 3)))
        assertEquals(listOf(101, 102), importar().lancamentos)
        verify(logService, never()).porCode(any())
    }

    @Test
    fun `resposta perdida mas o log ja e IMPORTADO - a importacao entrou`() {
        whenever(batch.run(any<BatchList>())).thenThrow(ResourceAccessException("Read timed out"))
        whenever(logService.porCode(any())).thenAnswer { importado(it.arguments[0] as String) }
        val resultado = importar()
        assertEquals("IMPORTADO", resultado.status)
        assertTrue(resultado.lancamentos.isEmpty())
    }

    @Test
    fun `sem erro no lote mas sem log - pede conferencia em vez de afirmar sucesso`() {
        // Resposta que o parser do batch nao reconhece vira lista vazia, sem erro.
        whenever(batch.run(any<BatchList>())).thenReturn(emptyList())
        whenever(logService.porCode(any())).thenReturn(null)
        assertEquals("CONFERIR", importar().status)
        verify(logService, never()).registrarLancamentos(any(), any())
    }

    @Test
    fun `arquivo acima do limite nem consulta os cadastros`() {
        val pequeno = ImportacaoLancamentoService(ImportacaoLancamentoValidador(cadastros), cadastros, logService, journal, batch, 1)
        val previa = pequeno.validar(csv, "a.csv")
        assertFalse(previa.podeImportar)
        assertTrue(previa.erros.single().contains("limite por importação é 1"))
        verifyNoInteractions(cadastros)
    }
}
