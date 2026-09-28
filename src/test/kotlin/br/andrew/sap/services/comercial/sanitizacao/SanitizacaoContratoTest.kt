package br.andrew.sap.services.comercial.sanitizacao

import br.andrew.sap.services.batch.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.math.BigDecimal

internal fun exemploSanitizacao(): ReclassificacaoSanitizacao {
    val valor = BigDecimal("100")
    val pernas = listOf(
        PernaSanitizacao(0, "CLIENTES", "C001", 1, BigDecimal.ZERO, valor, -valor, emptyList(), false),
        PernaSanitizacao(1, "CONTROLE", "C001", 1, valor, BigDecimal.ZERO, BigDecimal.ZERO, listOf(9), false),
    )
    return ReclassificacaoSanitizacao(10, 20, 1, "C001", "Cliente", DocumentoSanitizacao(30, 40, valor), "Cancelada",
        emptyList(), pernas, listOf(ApropriacaoSanitizacao(50, 60, 70, false, BigDecimal.ZERO, valor, 20, "C001", 1)),
        listOf(ReconciliacaoSanitizacao(9, 0, "2026-01-01", listOf(
            ParticipanteSanitizacao(10, 1, 30, 10, valor), ParticipanteSanitizacao(70, 0, 13, 50, valor)))),
        emptyList(), listOf("Cancelar reconciliação interna 9", "Cancelar apropriação 60", "Estornar 10"))
}

class SanitizacaoContratoRegrasTest {
    private val item = exemploSanitizacao()
    private fun avaliar(pernas: List<PernaSanitizacao> = item.pernas, cancelada: Boolean = true,
        integral: Boolean = false, ambiguo: Boolean = false, estornos: Int = 0,
        apps: List<ApropriacaoSanitizacao> = item.apropriacoes) =
        avaliarSanitizacao(pernas, item.nota, cancelada, integral, ambiguo, estornos, apps, 20, "C001", 1, "CONTROLE")

    @Test fun `uma perna sem reconciliacao e nota cancelada permite corrigir`() { assertTrue(avaliar().isEmpty()) }
    @Test fun `as duas pernas sem reconciliacao tambem permitem corrigir`() {
        assertTrue(avaliar(item.pernas.map { it.copy(reconciliacoes = emptyList()) }).isEmpty())
    }
    @Test fun `devolucao integral permite corrigir`() { assertTrue(avaliar(cancelada = false, integral = true).isEmpty()) }
    @Test fun `devolucao parcial somente sinaliza`() {
        assertTrue(avaliar(cancelada = false).any { it.contains("parcial") })
    }
    @Test fun `ambas pernas reconciliadas nao sofrem correcao`() {
        assertTrue(avaliar(item.pernas.map { it.copy(reconciliacoes = listOf(9)) }).any { it.contains("duas pernas possuem") })
    }
    @Test fun `vfdv existente ou origem ambigua impedem duplicacao`() {
        assertFalse(avaliar(estornos = 1).isEmpty())
        assertFalse(avaliar(ambiguo = true).isEmpty())
    }
    @Test fun `apropriacao de outro contrato ou filial impede cancelamento`() {
        assertFalse(avaliar(apps = item.apropriacoes.map { it.copy(contrato = 999) }).isEmpty())
        assertFalse(avaliar(apps = item.apropriacoes.map { it.copy(filial = 2) }).isEmpty())
    }
    @Test fun `conta valor ou estrutura inesperados bloqueiam`() {
        assertFalse(avaliar(item.pernas.take(1)).isEmpty())
        assertFalse(avaliar(item.pernas.map { it.copy(parceiro = "OUTRO") }).isEmpty())
        assertFalse(avaliar(item.pernas.map { it.copy(credito = BigDecimal("120")) }).isEmpty())
    }
}

class SanitizacaoContratoServiceTest {
    private val consulta = mock<SanitizacaoContratoConsulta>()
    private val lancamentos = mock<SanitizacaoContratoLancamentos>()
    private val service = SanitizacaoContratoService(consulta, lancamentos)
    private val item = exemploSanitizacao()
    private val data = "2026-09-24"

    private fun previa(): PreviaSanitizacao {
        whenever(consulta.dataCorrente()).thenReturn(data)
        whenever(consulta.buscar()).thenReturn(listOf(item))
        whenever(consulta.buscar(10)).thenReturn(listOf(item))
        whenever(consulta.verificar(item)).thenReturn(ResultadoSanitizacao(10, "APLICADO", "ok", 11))
        return service.previa("admin")
    }

    @Test fun `previa nao efetiva lancamentos`() { previa(); verifyNoInteractions(lancamentos) }
    @Test fun `confirmacao executa somente o item e repeticao nao duplica`() {
        val p = previa()
        val pedido = AplicarSanitizacao(p.id, 10)
        val primeiro = service.aplicar("admin", pedido)
        assertEquals("APLICADO", primeiro.status)
        assertEquals(primeiro, service.aplicar("admin", pedido))
        verify(lancamentos, times(1)).aplicar(item)
        verify(consulta).verificar(item)
    }
    @Test fun `token desconhecido outro usuario e item nao exibido sao recusados`() {
        val p = previa()
        assertThrows(IllegalArgumentException::class.java) { service.aplicar("admin", AplicarSanitizacao("inventado", 10)) }
        assertThrows(IllegalArgumentException::class.java) { service.aplicar("outro", AplicarSanitizacao(p.id, 10)) }
        assertThrows(IllegalArgumentException::class.java) { service.aplicar("admin", AplicarSanitizacao(p.id, 999)) }
        verifyNoInteractions(lancamentos)
    }
    @Test fun `mudanca nos vinculos desde a previa impede lancamentos`() {
        val p = previa()
        whenever(consulta.buscar(10)).thenReturn(listOf(item.copy(reconciliacoes = emptyList())))
        assertEquals("REJEITADO", service.aplicar("admin", AplicarSanitizacao(p.id, 10)).status)
        verifyNoInteractions(lancamentos)
    }
    @Test fun `falha apos envio nao gera retry automatico`() {
        val p = previa()
        doThrow(RuntimeException("timeout")).whenever(lancamentos).aplicar(item)
        val pedido = AplicarSanitizacao(p.id, 10)
        assertEquals("CONFERIR", service.aplicar("admin", pedido).status)
        assertEquals("CONFERIR", service.aplicar("admin", pedido).status)
        verify(lancamentos, times(1)).aplicar(item)
    }
    @Test fun `item estornado apos previa nao executa novamente`() {
        val p = previa()
        whenever(consulta.buscar(10)).thenReturn(emptyList())
        assertEquals("REJEITADO", service.aplicar("admin", AplicarSanitizacao(p.id, 10)).status)
        verifyNoInteractions(lancamentos)
    }
    @Test fun `item bloqueado nao pode ser forjado pelo cliente`() {
        whenever(consulta.dataCorrente()).thenReturn(data)
        whenever(consulta.buscar()).thenReturn(listOf(item.copy(impedimentos = listOf("Parcial"))))
        val p = service.previa("admin")
        assertThrows(IllegalArgumentException::class.java) { service.aplicar("admin", AplicarSanitizacao(p.id, 10)) }
        verifyNoInteractions(lancamentos)
    }
}

class SanitizacaoContratoLancamentosTest {
    @Test fun `cancelamentos e estorno seguem em um unico changeset na ordem correta`() {
        val batch = mock<BatchService>()
        val invoices = mock<br.andrew.sap.services.documents.InvoiceService>()
        val journals = mock<br.andrew.sap.services.journal.JournalEntriesService>()
        whenever(invoices.path()).thenReturn("/b1s/v1/Invoices")
        whenever(journals.path()).thenReturn("/b1s/v1/JournalEntries")
        whenever(batch.run(any<BatchList>())).thenAnswer { invocation ->
            (invocation.arguments[0] as BatchList).map { BatchResponse(204, null, true) }
        }
        val service = SanitizacaoContratoLancamentos(batch, invoices, journals, mock(), mock(), mock())
        val exemplo = exemploSanitizacao()
        // Uma apropriação já cancelada não pode ser cancelada outra vez, e a reconciliação de
        // adiantamento (tipo 16, do sistema) é desfeita pelo SAP ao cancelar a apropriação.
        val adiantamento = ReconciliacaoSanitizacao(8, 16, "2026-01-01", listOf(
            ParticipanteSanitizacao(80, 1, 203, 5, BigDecimal("100")), ParticipanteSanitizacao(70, 1, 13, 50, BigDecimal("100"))))
        val item = exemplo.copy(apropriacoes = exemplo.apropriacoes + exemplo.apropriacoes.first().copy(docEntry = 51, cancelada = true),
            reconciliacoes = listOf(adiantamento) + exemplo.reconciliacoes)
        service.aplicar(item)
        val captor = argumentCaptor<BatchList>()
        verify(batch, times(1)).run(captor.capture())
        val ops = captor.firstValue.toList()
        assertEquals(3, ops.size)
        assertEquals("/b1s/v1/InternalReconciliationsService_Cancel", ops[0].service.path())
        assertEquals(mapOf("InternalReconciliationParams" to mapOf("ReconNum" to 9)), ops[0].payload)
        assertEquals("POST /b1s/v1/Invoices(50)/Cancel", ops[1].method.getHttp(ops[1].service, ops[1].payload as BatchId))
        assertEquals("POST /b1s/v1/JournalEntries(10)/Cancel", ops[2].method.getHttp(ops[2].service, ops[2].payload as BatchId))
        assertFalse(ops[1].method.temCorpo(), "Cancel nativo não aceita DocDate da nota original")
    }
}
