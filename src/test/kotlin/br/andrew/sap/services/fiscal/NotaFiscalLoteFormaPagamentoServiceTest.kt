package br.andrew.sap.services.fiscal

import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.ATUALIZADO
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.ERRO
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.NAO_ENCONTRADO
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.NAO_PERMITIDO
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.SEM_CONFIRMACAO
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.SEM_MUDANCA
import br.andrew.sap.model.fiscal.SituacaoTrocaFormaPagamento.VINCULADO_E_ATUALIZADO
import br.andrew.sap.model.fiscal.CategoriaDocumentoLote
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.model.fiscal.TrocaFormaPagamentoRequest
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.documents.DownPaymentService
import br.andrew.sap.services.documents.InvoiceService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.ConnectException
import java.util.Collections
import java.util.concurrent.ThreadPoolExecutor

class NotaFiscalLoteFormaPagamentoServiceTest {

    private val formasAtivas = listOf("AVISTA", "BB-RC-BOL-1199", "pix-vencimento")

    private fun doc(tipo: String, docEntry: Int, cardCode: String, forma: String?, numero: Int = docEntry + 500,
                    categoria: String = tipo, statusSap: String = "ABERTO") = mapOf(
        "Tipo" to tipo, "Categoria" to categoria, "DocEntry" to docEntry, "Numero" to numero, "CardCode" to cardCode,
        "CardName" to "Cliente $cardCode", "BPLId" to 2, "FormaPagamento" to forma, "StatusSap" to statusSap,
        "Linha" to 1, "Total" to 1)

    private val consultasCrd2 = Collections.synchronizedList(mutableListOf<Map<String, Any?>>())

    // Um so ODBC falso responde as tres consultas: formas (OPYM), cadastro do parceiro (CRD2) e
    // documentos (perna NF/AD por lista).
    private fun odbc(vararg docs: Map<String, Any?>) = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        val sql = inv.arguments[0] as String
        @Suppress("UNCHECKED_CAST")
        val p = inv.arguments[1] as Map<String, Any?>
        Thread.sleep(atrasoOdbc)
        if (sql.contains("OPYM"))
            return@Answer QueryResponse(rows = formasAtivas.map { mapOf("Codigo" to it, "Descricao" to "Forma $it") })
        if (sql.contains("CRD2")) {
            consultasCrd2.add(p)
            val com = (p["parceiros"] as List<*>).filter { p["forma"] in cadastro[it].orEmpty() }
            return@Answer QueryResponse(rows = com.map { mapOf("CardCode" to it) })
        }
        val linhas = docs.filter {
            val lista = if (it["Tipo"] == "AD") p["adiantamentos"] else p["docEntries"]
            it["DocEntry"] in (lista as List<*>)
        }
        QueryResponse(rows = linhas)
    })

    // Formas no cadastro de cada parceiro; o PATCH falha como o SAP quando a forma nao esta la.
    private val cadastro = Collections.synchronizedMap(mutableMapOf<String, MutableSet<String>>())
    private val patches = Collections.synchronizedList(mutableListOf<String>())
    private val vinculos = Collections.synchronizedList(mutableListOf<String>())
    // Erro fixo por "tipo-docEntry", para simular outras recusas do SAP.
    private val recusas = mutableMapOf<String, Exception>()
    // false = o SAP grava a forma mesmo fora do cadastro do parceiro (como fez no HOMOLOG).
    private var exigeVinculo = true
    private var vinculoFalha: Exception? = null

    private fun erroSap(mensagem: String) = HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "Bad Request",
        HttpHeaders(), """{"error":{"code":-5002,"message":{"lang":"en-us","value":"$mensagem"}}}""".toByteArray(), Charsets.UTF_8)

    private fun servicoDocumento(tipo: String, cardCodes: Map<Int, String>) = Answer { inv ->
        if (inv.method.name != "update") return@Answer null
        val docEntry = (inv.arguments[1] as String).toInt()
        @Suppress("UNCHECKED_CAST")
        val forma = (inv.arguments[0] as Map<String, Any?>)["PaymentMethod"] as String
        atrasos["$tipo-$docEntry"]?.let { Thread.sleep(it) }
        recusas["$tipo-$docEntry"]?.let { throw it }
        val cardCode = cardCodes.getValue(docEntry)
        if (exigeVinculo && forma !in cadastro.getOrPut(cardCode) { mutableSetOf() })
            throw erroSap("1250000129 - Linked payment method $forma is inactive or is no longer linked with business partner $cardCode")
        patches.add("$tipo-$docEntry=$forma")
        null
    }

    private val bpService = mock(BusinessPartnersService::class.java, Answer { inv ->
        if (inv.method.name != "addPaymentMethod") return@Answer null
        val cardCode = inv.arguments[0] as String
        val forma = inv.arguments[1] as String
        vinculoFalha?.let { throw it }
        vinculos.add("$cardCode+$forma")
        cadastro.getOrPut(cardCode) { mutableSetOf() }.add(forma)
        null
    })

    private fun bankPlus(ativos: Map<Int, List<Boleto>> = mapOf(), falha: Boolean = false, atraso: Long = 0) =
        mock(BankPlusService::class.java, Answer { inv ->
            if (inv.method.name != "getBoletosBy" || inv.arguments.size != 4) return@Answer null
            Thread.sleep(atraso)
            if (falha) throw ResourceAccessException("Read timed out")
            ativos[(inv.arguments[1] as String).toInt()] ?: listOf<Boleto>()
        })

    // Atraso do PATCH por "tipo-docEntry" (ms), para o teste de prazo.
    private val atrasos = mutableMapOf<String, Long>()
    // Atraso de cada consulta ao sap-odbc (ms).
    private var atrasoOdbc = 0L

    private fun servico(vararg docs: Map<String, Any?>, bank: BankPlusService = bankPlus(), prazo: Long = 300): NotaFiscalLoteFormaPagamentoService {
        val odbc = odbc(*docs)
        val nf = docs.filter { it["Tipo"] == "NF" }.associate { it["DocEntry"] as Int to it["CardCode"] as String }
        val ad = docs.filter { it["Tipo"] == "AD" }.associate { it["DocEntry"] as Int to it["CardCode"] as String }
        val lote = NotaFiscalLoteService(odbc, mock(TaxNfeService::class.java), bank, 200, 2)
        return NotaFiscalLoteFormaPagamentoService(odbc, lote,
            mock(InvoiceService::class.java, servicoDocumento("NF", nf)),
            mock(DownPaymentService::class.java, servicoDocumento("AD", ad)),
            bpService, bank, maximo = 200, paralelismo = 2, prazoSegundos = prazo)
    }

    private fun troca(s: NotaFiscalLoteFormaPagamentoService, forma: String, nf: List<Int> = listOf(), ad: List<Int> = listOf()) =
        s.trocar(TrocaFormaPagamentoRequest(forma, nf, ad), "teste")

    @Test
    fun `nota e adiantamento com a forma ja no cadastro sao atualizados no servico certo`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA"), doc("AD", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10), listOf(10))
        assertEquals(listOf(ATUALIZADO, ATUALIZADO), r.map { it.situacao })
        assertEquals(setOf("NF-10=pix-vencimento", "AD-10=pix-vencimento"), patches.toSet())
        assertEquals(listOf(TipoDocumentoLote.NF, TipoDocumentoLote.AD), r.map { it.tipo })
        assertEquals("AVISTA", r[0].formaAnterior)
        assertTrue(vinculos.isEmpty())
    }

    @Test
    fun `forma fora do cadastro do parceiro e vinculada e a troca e repetida`() {
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(VINCULADO_E_ATUALIZADO, r.situacao)
        assertEquals(listOf("C1+pix-vencimento"), vinculos)
        assertEquals(listOf("NF-10=pix-vencimento"), patches)
    }

    @Test
    fun `varios documentos do mesmo parceiro vinculam a forma uma vez so`() {
        val docs = (1..6).map { doc("NF", it, "C1", "AVISTA") } + doc("AD", 7, "C1", "AVISTA") + doc("NF", 8, "C2", "AVISTA")
        val r = troca(servico(*docs.toTypedArray()), "pix-vencimento", (1..6).toList() + 8, listOf(7))
        assertEquals(listOf("C1+pix-vencimento", "C2+pix-vencimento"), vinculos.sorted())
        assertEquals(8, patches.size)
        assertTrue(r.all { it.situacao == ATUALIZADO || it.situacao == VINCULADO_E_ATUALIZADO })
        assertEquals(2, r.count { it.situacao == VINCULADO_E_ATUALIZADO })
    }

    @Test
    fun `outra recusa do SAP vira erro com a mensagem dele, sem vincular nada`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        recusas["NF-10"] = erroSap("Document is already closed")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(ERRO, r.situacao)
        assertEquals("Document is already closed", r.mensagem)
        assertTrue(vinculos.isEmpty())
    }

    @Test
    fun `se depois de vincular o SAP ainda recusa, o erro e o da segunda tentativa`() {
        // o vinculo entra, mas o documento tem outra trava
        val s = servico(doc("NF", 10, "C1", "AVISTA"))
        recusas.clear()
        val invoice = mock(InvoiceService::class.java, Answer { inv ->
            if (inv.method.name != "update") return@Answer null
            if (vinculos.isEmpty()) throw erroSap("Linked payment method pix-vencimento is inactive or is no longer linked with business partner C1")
            throw erroSap("Field PaymentMethod cannot be updated")
        })
        val comTrava = NotaFiscalLoteFormaPagamentoService(s.odbc, s.lote, invoice, s.downPaymentService, bpService,
            s.bankPlusService, 200, 2)
        val r = troca(comTrava, "pix-vencimento", listOf(10)).single()
        assertEquals(ERRO, r.situacao)
        assertEquals("Field PaymentMethod cannot be updated", r.mensagem)
        assertEquals(listOf("C1+pix-vencimento"), vinculos)
    }

    @Test
    fun `conexao que cai depois de enviar o PATCH fica sem confirmacao, sem detalhe interno`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        recusas["NF-10"] = ResourceAccessException("I/O error on PATCH request for https://10.117.163.230:50000")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(SEM_CONFIRMACAO, r.situacao)
        assertEquals("a conexao com o SAP caiu durante a gravacao (sem resposta do servico): confira no SAP", r.mensagem)
    }

    @Test
    fun `SAP fora do ar antes de enviar o PATCH e erro`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        recusas["NF-10"] = ResourceAccessException("I/O error on PATCH request", ConnectException("Connection refused"))
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(ERRO, r.situacao)
        assertEquals("sem resposta do servico", r.mensagem)
    }

    @Test
    fun `conexao que cai no login da sessao, antes do PATCH, e erro`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        recusas["NF-10"] = ResourceAccessException("I/O error on POST request for \"https://10.117.163.230:50000/b1s/v1/Login\": Connection reset")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(ERRO, r.situacao)
        assertEquals("sem resposta do servico", r.mensagem)
    }

    @Test
    fun `conexao que cai no PATCH repetido depois de vincular fica sem confirmacao`() {
        val s = servico(doc("NF", 10, "C1", "AVISTA"))
        val invoice = mock(InvoiceService::class.java, Answer { inv ->
            if (inv.method.name != "update") return@Answer null
            if (vinculos.isEmpty()) throw erroSap("Linked payment method pix-vencimento is inactive or is no longer linked with business partner C1")
            throw ResourceAccessException("I/O error on PATCH request")
        })
        val r = troca(NotaFiscalLoteFormaPagamentoService(s.odbc, s.lote, invoice, s.downPaymentService, bpService,
            s.bankPlusService, 200, 2), "pix-vencimento", listOf(10)).single()
        assertEquals(SEM_CONFIRMACAO, r.situacao)
        assertEquals(listOf("C1+pix-vencimento"), vinculos)
    }

    @Test
    fun `documento que ja esta com a forma nao e gravado, mas o parceiro recebe a forma se nao tiver`() {
        val r = troca(servico(doc("NF", 10, "C1", "pix-vencimento")), "pix-vencimento", listOf(10)).single()
        assertEquals(SEM_MUDANCA, r.situacao)
        assertTrue(patches.isEmpty())
        assertEquals(listOf("C1+pix-vencimento"), vinculos)
        assertEquals("forma vinculada ao parceiro", r.mensagem)
    }

    @Test
    fun `SAP aceita a troca sem vinculo e o parceiro e vinculado depois`() {
        exigeVinculo = false
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA"), doc("AD", 11, "C1", "AVISTA")), "pix-vencimento", listOf(10), listOf(11))
        assertEquals(listOf(VINCULADO_E_ATUALIZADO, ATUALIZADO), r.map { it.situacao })
        assertEquals("forma vinculada ao parceiro", r[0].mensagem)
        assertNull(r[1].mensagem)
        // vincular deu certo: nao e pendencia
        assertFalse(r.any { it.pendencia })
        assertEquals(listOf("C1+pix-vencimento"), vinculos)
        assertEquals(setOf("NF-10=pix-vencimento", "AD-11=pix-vencimento"), patches.toSet())
    }

    @Test
    fun `parceiro que ja tem a forma nao e vinculado de novo, e o cadastro e consultado uma vez so`() {
        exigeVinculo = false
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        troca(servico(doc("NF", 10, "C1", "AVISTA"), doc("NF", 11, "C2", "AVISTA"), doc("NF", 12, "C2", "AVISTA")),
            "pix-vencimento", listOf(10, 11, 12))
        assertEquals(listOf("C2+pix-vencimento"), vinculos)
        assertEquals(1, consultasCrd2.size)
        assertEquals(setOf("C1", "C2"), (consultasCrd2.single()["parceiros"] as List<*>).toSet())
    }

    @Test
    fun `falha ao vincular depois de trocar nao desfaz a troca e avisa`() {
        exigeVinculo = false
        vinculoFalha = erroSap("Business partner is locked")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "pix-vencimento", listOf(10)).single()
        assertEquals(ATUALIZADO, r.situacao)
        assertEquals("forma NAO vinculada ao parceiro (Business partner is locked)", r.mensagem)
        assertTrue(r.pendencia)
        assertEquals(listOf("NF-10=pix-vencimento"), patches)
    }

    @Test
    fun `documento que nao existe ou nao entra na tela volta como nao encontrado, na ordem pedida`() {
        cadastro["C1"] = mutableSetOf("AVISTA")
        val r = troca(servico(doc("NF", 10, "C1", "pix-vencimento")), "AVISTA", listOf(99, 10), listOf(55))
        assertEquals(listOf(NAO_ENCONTRADO, ATUALIZADO, NAO_ENCONTRADO), r.map { it.situacao })
        assertEquals(listOf(99, 10, 55), r.map { it.docEntry })
        assertTrue(r[2].mensagem!!.startsWith("Adiantamento DocEntry 55"))
    }

    @Test
    fun `forma vazia, inexistente ou inativa e recusada antes de gravar qualquer coisa`() {
        val s = servico(doc("NF", 10, "C1", "AVISTA"))
        listOf("", "  ", "BB-RC-AUTO", "NAO-EXISTE").forEach { forma ->
            assertThrows(Exception::class.java) { troca(s, forma, listOf(10)) }
        }
        assertThrows(Exception::class.java) { troca(s, "AVISTA") }
        assertTrue(patches.isEmpty())
    }

    @Test
    fun `sair do boleto com boleto ativo na BankPlus avisa`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val ativo = Boleto().also { it.id = 1; it.statusDescricao = "Confirmado" }
        val cancelado = Boleto().also { it.id = 2; it.statusDescricao = "Aguardando Cancelamento" }
        val r = troca(servico(doc("NF", 10, "C1", "BB-RC-BOL-1199"), bank = bankPlus(mapOf(10 to listOf(ativo, cancelado)))),
            "pix-vencimento", listOf(10)).single()
        assertEquals(ATUALIZADO, r.situacao)
        assertTrue(r.boletoAtivo)
        assertEquals("ainda tem 1 boleto(s) ativo(s) na BankPlus", r.mensagem)
        // boleto ativo tem aviso proprio (boletoAtivo)
        assertFalse(r.pendencia)
    }

    @Test
    fun `sem boleto ativo nao avisa`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val r = troca(servico(doc("NF", 10, "C1", "BB-RC-BOL-1199")), "pix-vencimento", listOf(10)).single()
        assertFalse(r.boletoAtivo)
        assertNull(r.mensagem)
    }

    @Test
    fun `boleto antigo ativo avisa mesmo indo para outra forma de boleto ou saindo de uma forma que nao e boleto`() {
        // BB -> Sicoob (forma que nem esta no boleto.ids) e Pix -> AVISTA depois de um boleto -> Pix
        cadastro["C1"] = mutableSetOf("BB-RC-BOL-1199", "AVISTA")
        val ativo = Boleto().also { it.id = 1; it.statusDescricao = "Confirmado" }
        val s = servico(doc("NF", 10, "C1", "pix-vencimento"), bank = bankPlus(mapOf(10 to listOf(ativo))))
        assertTrue(troca(s, "AVISTA", listOf(10)).single().boletoAtivo)
        val r = troca(servico(doc("NF", 11, "C1", "AVISTA"), bank = bankPlus(mapOf(11 to listOf(ativo)))), "BB-RC-BOL-1199", listOf(11)).single()
        assertTrue(r.boletoAtivo)
    }

    @Test
    fun `documento sem mudanca nao consulta a BankPlus`() {
        cadastro["C1"] = mutableSetOf("AVISTA")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA"), bank = bankPlus(falha = true)), "AVISTA", listOf(10)).single()
        assertEquals(SEM_MUDANCA, r.situacao)
        assertNull(r.mensagem)
    }

    @Test
    fun `prazo estourado nao comeca PATCH novo e diz o que ficou sem confirmacao`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        cadastro["C2"] = mutableSetOf("pix-vencimento")
        // C1: o 10 trava alem do prazo, o 11 (mesmo parceiro, na fila) nem comeca; C2 passa
        atrasos["NF-10"] = 3_000
        val inicio = System.nanoTime()
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA"), doc("NF", 11, "C1", "AVISTA"), doc("NF", 12, "C2", "AVISTA"), prazo = 1),
            "pix-vencimento", listOf(10, 11, 12))
        val segundos = (System.nanoTime() - inicio) / 1e9
        assertTrue(segundos < 2.5, "levou ${segundos}s")
        assertEquals(listOf(SEM_CONFIRMACAO, ERRO, ATUALIZADO), r.map { it.situacao })
        assertTrue(r[0].mensagem!!.contains("confira no SAP"), r[0].mensagem)
        assertTrue(r[1].mensagem!!.contains("nada foi gravado"), r[1].mensagem)
        // o 11 nao pode ser gravado depois da resposta
        Thread.sleep(2_500)
        assertFalse(patches.any { it.startsWith("NF-11") }, patches.toString())
    }

    @Test
    fun `prazo que acaba na conferencia do boleto nao esconde o PATCH ja gravado`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val inicio = System.nanoTime()
        val r = troca(servico(doc("NF", 10, "C1", "BB-RC-BOL-1199"), bank = bankPlus(atraso = 3_000), prazo = 1),
            "pix-vencimento", listOf(10)).single()
        assertTrue((System.nanoTime() - inicio) / 1e9 < 2.5)
        assertEquals(ATUALIZADO, r.situacao)
        assertEquals("o prazo da troca (1s) acabou antes de conferir o vinculo ao parceiro e o boleto", r.mensagem)
        assertTrue(r.pendencia)
        assertEquals(listOf("NF-10=pix-vencimento"), patches)
    }

    @Test
    fun `prazo conta desde as consultas de preparo`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        // formas + documentos + CRD2 = 3 consultas de 400ms: o prazo de 1s acaba antes do PATCH
        atrasoOdbc = 400
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA"), prazo = 1), "pix-vencimento", listOf(10)).single()
        assertEquals(ERRO, r.situacao)
        assertTrue(r.mensagem!!.contains("nada foi gravado"), r.mensagem)
        assertTrue(patches.isEmpty())
    }

    @Test
    fun `PATCH pendurado nao soma threads a cada nova troca`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        cadastro["C2"] = mutableSetOf("pix-vencimento")
        cadastro["C3"] = mutableSetOf("pix-vencimento")
        // os dois PATCHs da primeira troca prendem as 2 threads do servico alem do prazo
        atrasos["NF-10"] = 3_000
        atrasos["NF-20"] = 3_000
        val s = servico(doc("NF", 10, "C1", "AVISTA"), doc("NF", 20, "C2", "AVISTA"), doc("NF", 30, "C3", "AVISTA"), prazo = 1)
        val inicio = System.nanoTime()
        assertEquals(listOf(SEM_CONFIRMACAO, SEM_CONFIRMACAO), troca(s, "pix-vencimento", listOf(10, 20)).map { it.situacao })
        // a segunda troca nao ganha thread nova: espera a vez e o prazo acaba antes
        val segunda = troca(s, "pix-vencimento", listOf(30)).single()
        assertEquals(ERRO, segunda.situacao)
        assertTrue(segunda.mensagem!!.contains("nada foi gravado"), segunda.mensagem)
        // e o grupo dela nao fica esperando na fila
        val pool = s.javaClass.getDeclaredField("pool").apply { isAccessible = true }.get(s) as ThreadPoolExecutor
        assertEquals(0, pool.queue.size)
        // e nao grava quando as threads soltam
        Thread.sleep((3_500 - (System.nanoTime() - inicio) / 1_000_000).coerceAtLeast(0))
        assertFalse(patches.any { it.startsWith("NF-30") }, patches.toString())
        s.encerrar()
    }

    @Test
    fun `SAP pede o vinculo, mas outra troca vinculou primeiro, entao so atualizado`() {
        // o addPaymentMethod desta falha por duplicado porque a outra troca vinculou antes
        val concorrente = mock(BusinessPartnersService::class.java, Answer { inv ->
            if (inv.method.name != "addPaymentMethod") return@Answer null
            cadastro.getOrPut(inv.arguments[0] as String) { mutableSetOf() }.add(inv.arguments[1] as String)
            throw erroSap("Payment method already exists for business partner")
        })
        val base = servico(doc("NF", 10, "C1", "AVISTA"))
        val s = NotaFiscalLoteFormaPagamentoService(base.odbc, base.lote, base.invoiceService, base.downPaymentService, concorrente,
            base.bankPlusService, 200, 2)
        val r = troca(s, "pix-vencimento", listOf(10)).single()
        assertEquals(ATUALIZADO, r.situacao)
        assertNull(r.mensagem)
        assertEquals(listOf("NF-10=pix-vencimento"), patches)
    }

    @Test
    fun `vinculo que falha porque outra troca ja vinculou nao vira aviso`() {
        exigeVinculo = false
        // a outra troca vincula no meio do caminho e o addPaymentMethod desta falha por duplicado
        val concorrente = mock(BusinessPartnersService::class.java, Answer { inv ->
            if (inv.method.name != "addPaymentMethod") return@Answer null
            cadastro.getOrPut(inv.arguments[0] as String) { mutableSetOf() }.add(inv.arguments[1] as String)
            throw erroSap("Payment method already exists for business partner")
        })
        val base = servico(doc("NF", 10, "C1", "AVISTA"))
        val s = NotaFiscalLoteFormaPagamentoService(base.odbc, base.lote, base.invoiceService, base.downPaymentService, concorrente,
            base.bankPlusService, 200, 2)
        val r = troca(s, "pix-vencimento", listOf(10)).single()
        assertEquals(ATUALIZADO, r.situacao)
        assertNull(r.mensagem)
    }

    @Test
    fun `BankPlus fora na conferencia do boleto nao desfaz a troca`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val r = troca(servico(doc("NF", 10, "C1", "BB-RC-BOL-1199"), bank = bankPlus(falha = true)), "pix-vencimento", listOf(10)).single()
        assertEquals(ATUALIZADO, r.situacao)
        assertFalse(r.boletoAtivo)
        assertTrue(r.mensagem!!.startsWith("nao deu para conferir boleto na BankPlus"), r.mensagem)
        assertTrue(r.pendencia)
    }

    @Test
    fun `resultado traz o nome do parceiro e a categoria`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA", categoria = "NF_FUTURA")), "pix-vencimento", listOf(10)).single()
        assertEquals("Cliente C1", r.cardName)
        assertEquals(CategoriaDocumentoLote.NF_FUTURA, r.categoria)
    }

    @Test
    fun `recusa em documento fechado vem explicada`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        recusas["NF-11"] = erroSap("[OINV.PeyMethod] , 'Field cannot be updated (ODBC -1029)'")
        val r = troca(servico(doc("NF", 11, "C1", "AVISTA", statusSap = "FECHADO")), "pix-vencimento", listOf(11)).single()
        assertEquals(ERRO, r.situacao)
        assertTrue(r.mensagem!!.startsWith("documento fechado: o SAP nao deixa trocar a forma"), r.mensagem)
        assertTrue(r.mensagem!!.endsWith("'Field cannot be updated (ODBC -1029)'"), r.mensagem)
    }

    @Test
    fun `NF de venda futura antiga nem e tentada, e o resto do lote segue`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        cadastro["C2"] = mutableSetOf("pix-vencimento")
        // se fosse tentada, o SAP recusaria
        recusas["NF-10"] = erroSap("[OINV.PeyMethod] , 'Field cannot be updated (ODBC -1029)'")
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA", categoria = "NF_FUTURA"), doc("NF", 11, "C1", "AVISTA"),
            doc("AD", 12, "C2", "AVISTA")), "pix-vencimento", listOf(10, 11), listOf(12))
        assertEquals(listOf(NAO_PERMITIDO, ATUALIZADO, ATUALIZADO), r.map { it.situacao })
        assertEquals("NF de venda futura do padrao antigo (fatura de reserva com entrega): o SAP nao deixa trocar a forma", r[0].mensagem)
        assertEquals(CategoriaDocumentoLote.NF_FUTURA, r[0].categoria)
        assertEquals(setOf("NF-11=pix-vencimento", "AD-12=pix-vencimento"), patches.toSet())
        assertTrue(vinculos.isEmpty())
    }

    @Test
    fun `NF de venda futura antiga ja com a forma e sem mudanca, nao bloqueio`() {
        cadastro["C1"] = mutableSetOf("pix-vencimento")
        val r = troca(servico(doc("NF", 10, "C1", "pix-vencimento", categoria = "NF_FUTURA")), "pix-vencimento", listOf(10)).single()
        assertEquals(SEM_MUDANCA, r.situacao)
        assertTrue(patches.isEmpty())
    }

    @Test
    fun `lote so de NF de venda futura antiga nao consulta o cadastro nem grava`() {
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA", categoria = "NF_FUTURA")), "pix-vencimento", listOf(10)).single()
        assertEquals(NAO_PERMITIDO, r.situacao)
        assertTrue(consultasCrd2.isEmpty())
        assertTrue(patches.isEmpty())
    }

    @Test
    fun `nao encontrado vem sem nome nem categoria`() {
        val r = troca(servico(doc("NF", 10, "C1", "AVISTA")), "AVISTA", listOf(99)).single()
        assertNull(r.cardName)
        assertNull(r.categoria)
    }

    @Test
    fun `formas vem da OPYM de recebimento ativo`() {
        assertEquals(formasAtivas, servico().formas().map { it.codigo })
    }
}
