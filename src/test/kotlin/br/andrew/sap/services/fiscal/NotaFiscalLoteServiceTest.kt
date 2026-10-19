package br.andrew.sap.services.fiscal

import br.andrew.sap.model.bankplus.Boleto
import br.andrew.sap.model.fiscal.NotaFiscalLoteArquivo
import br.andrew.sap.model.fiscal.NotaFiscalLoteFiltro
import br.andrew.sap.model.fiscal.StatusSapNota
import br.andrew.sap.model.fiscal.CategoriaDocumentoLote
import br.andrew.sap.model.fiscal.TipoDocumentoLote
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
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.util.zip.ZipInputStream

class NotaFiscalLoteServiceTest {

    // Linha como o sap-odbc devolve: decimal vem como string.
    private fun nota(docEntry: Int, numero: Int?, cardCode: String = "C0001", serie: String? = "2",
                     statusSap: String = "FECHADO", total: Int = 1) = mapOf(
        "DocEntry" to docEntry, "DocNum" to docEntry + 1000, "Numero" to numero, "Serie" to serie,
        "DocDate" to "2026-09-30", "CardCode" to cardCode, "CardName" to "Cliente $cardCode",
        "BPLId" to 2, "BPLName" to "Filial 2", "DocTotal" to "150.50", "SlpCode" to 7, "SlpName" to "Joao",
        "VencimentoInicial" to "2026-10-22", "VencimentoFinal" to "2027-02-19", "Parcelas" to 5,
        "ChaveAcesso" to "1126090905408700015455002000000${docEntry}", "StatusSefazId" to 4,
        "StatusSefaz" to "Autorizada", "StatusSap" to statusSap, "Linha" to 1, "Total" to total)

    // Adiantamento de venda futura como a perna AD do SQL devolve: sem serie, chave nem SEFAZ.
    private fun adiantamento(docEntry: Int, numero: Int, contrato: Int = 412, cardCode: String = "C0001") =
        nota(docEntry, numero, cardCode, serie = null).toMutableMap().also {
            it["Tipo"] = "AD"; it["Contrato"] = contrato; it["ChaveAcesso"] = null
            it["StatusSefazId"] = null; it["StatusSefaz"] = null; it["StatusSap"] = "ABERTO"
        }

    private val consultas = mutableListOf<Pair<String, Map<String, Any?>>>()

    // O mock filtra por :docEntries como o SQL faria; o resto dos filtros e do banco.
    private fun odbc(vararg notas: Map<String, Any?>) = mock(OdbcClient::class.java, Answer { inv ->
        if (inv.method.name != "consultar") return@Answer null
        @Suppress("UNCHECKED_CAST")
        val params = inv.arguments[1] as Map<String, Any?>
        consultas.add(inv.arguments[0] as String to params)
        // cada perna so traz o que esta na sua lista (NF em docEntries, AD em adiantamentos)
        val filtradas = if (params["todosDocs"] == 0)
            notas.filter {
                val lista = if (it["Tipo"] == "AD") params["adiantamentos"] else params["docEntries"]
                it["DocEntry"] in (lista as List<*>)
            }
        else notas.toList()
        // pagina como o WHERE "Linha" > :inicio AND "Linha" <= :fim
        val linhas = filtradas.drop(params["inicio"] as Int).take((params["fim"] as Int) - (params["inicio"] as Int))
        QueryResponse(rows = linhas, rowCount = linhas.size)
    })

    private val chamadasTax = mutableListOf<String>()

    private fun taxNfe(consultaFalha: Set<Int> = setOf(), xmlFalha: Set<String> = setOf(),
                       atrasoMs: Map<Int, Long> = mapOf(), falhaInterna: Map<Int, Throwable> = mapOf()) =
        mock(TaxNfeService::class.java, Answer { inv ->
            when (inv.method.name) {
                "consultar" -> {
                    val docEntry = inv.arguments[0] as Int
                    synchronized(chamadasTax) { chamadasTax.add("consultar $docEntry") }
                    atrasoMs[docEntry]?.let { Thread.sleep(it) }
                    falhaInterna[docEntry]?.let { throw it }
                    if (docEntry in consultaFalha) throw Exception("Documento fiscal não encontrado para docEntry $docEntry")
                    DocumentoFiscal("lote-$docEntry", "55")
                }
                "pdf" -> "%PDF ${(inv.arguments[0] as DocumentoFiscal).batchId}".toByteArray()
                "xml" -> {
                    val batch = (inv.arguments[0] as DocumentoFiscal).batchId!!
                    if (batch in xmlFalha) throw Exception("TaxPlus respondeu 500 ao buscar o xml")
                    "<nfeProc>$batch</nfeProc>".toByteArray()
                }
                else -> null
            }
        })

    private fun boleto(id: Int, parcela: Int?, status: String = "Confirmado") = Boleto().also {
        it.id = id; it.numeroDaParcela = parcela; it.statusDescricao = status
    }

    private val chamadasBankPlus = mutableListOf<String>()

    // boletos por DocEntry; pdfFalha = ids de boleto cujo pdf falha
    private fun bankPlus(boletos: Map<Int, List<Boleto>> = mapOf(), listaFalha: Set<Int> = setOf(), pdfFalha: Set<Int> = setOf(),
                         listaFalhaHttp: Set<Int> = setOf(), empresasFalha: Boolean = false) =
        mock(BankPlusService::class.java, Answer { inv ->
            when (inv.method.name) {
                "getEmpresas" -> {
                    synchronized(chamadasBankPlus) { chamadasBankPlus.add("empresas prazo=${inv.arguments.getOrNull(0)}") }
                    if (empresasFalha) throw org.springframework.web.client.ResourceAccessException("Read timed out")
                    listOf<Any>()
                }
                "getBoletosBy" -> {
                    if (inv.arguments.size != 4) return@Answer null
                    val docEntry = (inv.arguments[1] as String).toInt()
                    val origem = if (inv.arguments[2] == OrigemBoletoEnum.adiantamento) " origem=adiantamento" else ""
                    synchronized(chamadasBankPlus) { chamadasBankPlus.add("boletos filial=${inv.arguments[0]} doc=$docEntry prazo=${inv.arguments[3]}$origem") }
                    if (docEntry in listaFalhaHttp) throw HttpServerErrorException.create(HttpStatus.INTERNAL_SERVER_ERROR,
                        "erro", org.springframework.http.HttpHeaders(), "{\"stack\":\"segredo-interno\"}".toByteArray(), null)
                    if (docEntry in listaFalha) throw Exception("BankPlus fora")
                    boletos[docEntry] ?: listOf<Boleto>()
                }
                "getPdf" -> {
                    val id = (inv.arguments[0] as String).toInt()
                    // o lote sempre pede a leitura com prazo
                    if (inv.arguments[1] != true) throw IllegalStateException("getPdf sem prazo")
                    if (id in pdfFalha) throw Exception("500")
                    "%PDF boleto $id".toByteArray()
                }
                else -> null
            }
        })

    private fun servico(odbc: OdbcClient, tax: TaxNfeService = taxNfe(), maximo: Int = 200, bank: BankPlusService = bankPlus(),
                        prazoSegundos: Long = 300) =
        NotaFiscalLoteService(odbc, tax, bank, maximo, 2, prazoSegundos)

    // Le o zip do temporario e apaga, como o controller faz ao enviar.
    private fun conteudo(arquivo: NotaFiscalLoteArquivo): Map<String, String> {
        val entradas = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(Files.readAllBytes(arquivo.caminho))).use { z ->
            generateSequence { z.nextEntry }.forEach { entradas[it.name] = String(z.readBytes()) }
        }
        Files.delete(arquivo.caminho)
        return entradas
    }

    @Test
    fun `pdf e xml juntos vao em pastas separadas com o numero da nota no nome`() {
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502, "C0002"))).zip(listOf(10, 11), listOf(), true, true)

        val zip = conteudo(arquivo)
        assertEquals(setOf("danfe/NF501-S2-C0001.pdf", "xml/NF501-S2-C0001.xml",
            "danfe/NF502-S2-C0002.pdf", "xml/NF502-S2-C0002.xml"), zip.keys)
        assertEquals("<nfeProc>lote-10</nfeProc>", zip["xml/NF501-S2-C0001.xml"])
        assertEquals(0, arquivo.notasComErro)
        assertTrue(arquivo.nome.matches(Regex("notas-fiscais-\\d{8}-\\d{4}\\.zip")))
    }

    @Test
    fun `consulta a TaxPlus uma vez por nota mesmo pedindo os dois arquivos`() {
        servico(odbc(nota(10, 501))).zip(listOf(10, 10), listOf(), true, true)
        assertEquals(listOf("consultar 10"), chamadasTax)
    }

    @Test
    fun `so xml fica na raiz do zip`() {
        val zip = conteudo(servico(odbc(nota(10, 501))).zip(listOf(10), listOf(), false, true))
        assertEquals(setOf("NF501-S2-C0001.xml"), zip.keys)
    }

    @Test
    fun `falha de uma nota nao derruba o lote e vai para erros txt`() {
        val tax = taxNfe(consultaFalha = setOf(11), xmlFalha = setOf("lote-12"))
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502), nota(12, 503)), tax)
            .zip(listOf(10, 11, 12, 99), listOf(), true, true)

        val zip = conteudo(arquivo)
        assertTrue("danfe/NF501-S2-C0001.pdf" in zip && "xml/NF501-S2-C0001.xml" in zip)
        assertTrue("danfe/NF503-S2-C0001.pdf" in zip)
        assertFalse("xml/NF503-S2-C0001.xml" in zip)
        assertFalse(zip.keys.any { it.contains("NF502") && !it.endsWith(".txt") })
        val erros = zip["erros.txt"]!!
        assertTrue(erros.contains("NF DocEntry 99: nao encontrada ou nao foi emitida para a SEFAZ"), erros)
        assertTrue(erros.contains("NF502-S2-C0001: consulta na TaxPlus falhou"), erros)
        assertTrue(erros.contains("NF503-S2-C0001: xml nao baixado"), erros)
        // 11 sem nada, 12 sem xml, 99 fora da consulta
        assertEquals(3, arquivo.notasComErro)
    }

    @Test
    fun `nota cancelada tambem baixa`() {
        val zip = conteudo(servico(odbc(nota(10, 501, statusSap = "CANCELADO"))).zip(listOf(10), listOf(), false, true))
        assertEquals(setOf("NF501-S2-C0001.xml"), zip.keys)
    }

    @Test
    fun `mesmo numero em notas diferentes nao sobrescreve arquivo`() {
        val zip = conteudo(servico(odbc(nota(10, 501), nota(20, 501))).zip(listOf(10, 20), listOf(), true, false))
        assertEquals(setOf("NF501-S2-C0001.pdf", "NF501-S2-C0001-2.pdf"), zip.keys)
    }

    @Test
    fun `nota sem numero usa o docEntry`() {
        val zip = conteudo(servico(odbc(nota(10, null, serie = null))).zip(listOf(10), listOf(), true, false))
        assertEquals(setOf("NFdoc10-C0001.pdf"), zip.keys)
    }

    @Test
    fun `recusa lote acima do teto, vazio ou sem tipo de arquivo`() {
        val s = servico(odbc(nota(10, 501)), maximo = 2)
        assertThrows(Exception::class.java) { s.zip(listOf(1, 2, 3), listOf(), true, true) }
        assertThrows(Exception::class.java) { s.zip(listOf(), listOf(), true, true) }
        assertThrows(Exception::class.java) { s.zip(listOf(10), listOf(), false, false, false) }
    }

    @Test
    fun `boleto vai um pdf por parcela na pasta boleto, ao lado do danfe e do xml`() {
        val bank = bankPlus(mapOf(10 to listOf(boleto(900, 2), boleto(901, 1))))
        val arquivo = servico(odbc(nota(10, 501)), bank = bank).zip(listOf(10), listOf(), true, true, true)

        val zip = conteudo(arquivo)
        assertEquals(setOf("danfe/NF501-S2-C0001.pdf", "xml/NF501-S2-C0001.xml",
            "boleto/NF501-S2-C0001-parcela1.pdf", "boleto/NF501-S2-C0001-parcela2.pdf"), zip.keys)
        assertEquals("%PDF boleto 901", zip["boleto/NF501-S2-C0001-parcela1.pdf"])
        // empresas uma vez antes do paralelo; boletos pela filial da nota (BPLId), como o /invoice/{id}/boletos
        // e sempre pela leitura com prazo, que so o lote usa
        assertEquals(listOf("empresas prazo=true", "boletos filial=2 doc=10 prazo=true"), chamadasBankPlus)
        assertEquals(0, arquivo.notasComErro)
        assertEquals(0, arquivo.notasSemBoleto)
    }

    @Test
    fun `so boleto fica na raiz e nem consulta a TaxPlus`() {
        val bank = bankPlus(mapOf(10 to listOf(boleto(900, 1))))
        val zip = conteudo(servico(odbc(nota(10, 501)), bank = bank).zip(listOf(10), listOf(), false, false, true))
        assertEquals(setOf("NF501-S2-C0001-parcela1.pdf"), zip.keys)
        assertTrue(chamadasTax.isEmpty())
    }

    @Test
    fun `nota sem boleto e aviso, nao erro`() {
        val bank = bankPlus(mapOf(10 to listOf(boleto(900, 1))))
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), bank = bank).zip(listOf(10, 11), listOf(), false, false, true)

        val zip = conteudo(arquivo)
        assertEquals(setOf("NF501-S2-C0001-parcela1.pdf", "avisos.txt"), zip.keys)
        assertTrue(zip["avisos.txt"]!!.contains("NF502-S2-C0001: sem boleto na BankPlus"))
        assertEquals(0, arquivo.notasComErro)
        assertEquals(1, arquivo.notasSemBoleto)
    }

    @Test
    fun `boleto cancelado ou aguardando cancelamento fica fora do zip`() {
        val bank = bankPlus(mapOf(
            10 to listOf(boleto(900, 1, "Aguardando Cancelamento"), boleto(901, 1, "Confirmado")),
            11 to listOf(boleto(902, 1, "Cancelado"))))
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), bank = bank).zip(listOf(10, 11), listOf(), false, false, true)

        val zip = conteudo(arquivo)
        assertEquals("%PDF boleto 901", zip["NF501-S2-C0001-parcela1.pdf"])
        assertFalse(zip.keys.any { it.startsWith("NF502") })
        assertTrue(zip["avisos.txt"]!!.contains("NF502-S2-C0001: boleto cancelado, nao incluido (Cancelado)"))
        assertEquals(1, arquivo.notasSemBoleto)
        assertEquals(0, arquivo.notasComErro)
    }

    @Test
    fun `falha da BankPlus e erro e nao derruba o danfe`() {
        val bank = bankPlus(mapOf(11 to listOf(boleto(910, 1), boleto(911, 2))), listaFalha = setOf(10), pdfFalha = setOf(911))
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), bank = bank).zip(listOf(10, 11), listOf(), true, false, true)

        val zip = conteudo(arquivo)
        assertTrue("danfe/NF501-S2-C0001.pdf" in zip)
        assertTrue("boleto/NF502-S2-C0001-parcela1.pdf" in zip)
        assertFalse("boleto/NF502-S2-C0001-parcela2.pdf" in zip)
        val erros = zip["erros.txt"]!!
        assertTrue(erros.contains("NF501-S2-C0001: consulta de boleto na BankPlus falhou"), erros)
        assertTrue(erros.contains("NF502-S2-C0001: boleto-parcela2 nao baixado"), erros)
        assertEquals(2, arquivo.notasComErro)
        assertEquals(0, arquivo.notasSemBoleto)
    }

    @Test
    fun `dois boletos da mesma parcela nao se sobrescrevem`() {
        val bank = bankPlus(mapOf(10 to listOf(boleto(900, 1), boleto(901, 1))))
        val zip = conteudo(servico(odbc(nota(10, 501)), bank = bank).zip(listOf(10), listOf(), false, false, true))
        assertEquals(setOf("NF501-S2-C0001-parcela1.pdf", "NF501-S2-C0001-parcela1-2.pdf"), zip.keys)
    }

    @Test
    fun `BankPlus fora no inicio vira erro de boleto sem uma chamada por nota`() {
        val bank = bankPlus(mapOf(10 to listOf(boleto(900, 1))), empresasFalha = true)
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), bank = bank).zip(listOf(10, 11), listOf(), true, false, true)

        assertEquals(listOf("empresas prazo=true"), chamadasBankPlus)
        val zip = conteudo(arquivo)
        // o DANFE sai; o boleto de cada nota vira erro
        assertTrue("danfe/NF501-S2-C0001.pdf" in zip && "danfe/NF502-S2-C0001.pdf" in zip)
        assertTrue(zip["erros.txt"]!!.contains("NF501-S2-C0001: boleto nao buscado: BankPlus indisponivel"), zip["erros.txt"])
        assertEquals(2, arquivo.notasComErro)
    }

    @Test
    fun `sem boleto marcado nem carrega as empresas da BankPlus`() {
        conteudo(servico(odbc(nota(10, 501))).zip(listOf(10), listOf(), true, false, false))
        assertTrue(chamadasBankPlus.isEmpty())
    }

    @Test
    fun `nota que passa do prazo vira erro e o resto do zip sai`() {
        val tax = taxNfe(atrasoMs = mapOf(11 to 30_000L))
        val inicio = System.nanoTime()
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), tax, prazoSegundos = 1).zip(listOf(10, 11), listOf(), true, false)
        val segundos = (System.nanoTime() - inicio) / 1_000_000_000.0

        assertTrue(segundos < 10, "levou ${segundos}s")
        val zip = conteudo(arquivo)
        assertTrue("NF501-S2-C0001.pdf" in zip)
        assertTrue(zip["erros.txt"]!!.contains("NF502-S2-C0001: tempo esgotado"), zip["erros.txt"])
        assertEquals(1, arquivo.notasComErro)
    }

    @Test
    fun `nome sem sufixo segue a ordem pedida, nao a de quem termina primeiro`() {
        // 10 e 20 tem o mesmo numero; 10 demora, 20 termina antes
        val tax = taxNfe(atrasoMs = mapOf(10 to 300L))
        val zip = conteudo(servico(odbc(nota(10, 501), nota(20, 501)), tax).zip(listOf(10, 20), listOf(), true, false))
        assertEquals("%PDF lote-10", zip["NF501-S2-C0001.pdf"])
        assertEquals("%PDF lote-20", zip["NF501-S2-C0001-2.pdf"])
    }

    @Test
    fun `erros txt sai na ordem pedida`() {
        val tax = taxNfe(consultaFalha = setOf(10, 11), atrasoMs = mapOf(10 to 200L))
        val erros = conteudo(servico(odbc(nota(10, 501), nota(11, 502)), tax).zip(listOf(10, 11), listOf(), true, false))["erros.txt"]!!
        assertTrue(erros.indexOf("NF501") < erros.indexOf("NF502"), erros)
    }

    @Test
    fun `corpo da resposta de erro das APIs nao vai para o zip`() {
        val bank = bankPlus(listaFalhaHttp = setOf(10))
        val tax = taxNfe(falhaInterna = mapOf(11 to IOException("connect 10.117.163.209:5000 recusado")))
        val zip = conteudo(servico(odbc(nota(10, 501), nota(11, 502)), tax, bank = bank).zip(listOf(10, 11), listOf(), true, false, true))
        val erros = zip["erros.txt"]!!
        assertTrue(erros.contains("NF501-S2-C0001: consulta de boleto na BankPlus falhou (HTTP 500)"), erros)
        assertTrue(erros.contains("NF502-S2-C0001: consulta na TaxPlus falhou (sem resposta do servico)"), erros)
        assertFalse(erros.contains("segredo-interno") || erros.contains("10.117"), erros)
    }

    @Test
    fun `falha inesperada numa nota nao derruba o lote`() {
        val tax = taxNfe(falhaInterna = mapOf(10 to IllegalStateException("bug")))
        val arquivo = servico(odbc(nota(10, 501), nota(11, 502)), tax).zip(listOf(10, 11), listOf(), true, false)
        val zip = conteudo(arquivo)
        assertTrue("NF502-S2-C0001.pdf" in zip)
        assertTrue(zip["erros.txt"]!!.contains("NF501-S2-C0001: consulta na TaxPlus falhou (erro interno (IllegalStateException))"), zip["erros.txt"])
    }

    @Test
    fun `adiantamento baixa so o boleto, com a origem de adiantamento na BankPlus`() {
        val bank = bankPlus(mapOf(77 to listOf(boleto(900, 1))))
        val tax = taxNfe()
        val arquivo = servico(odbc(adiantamento(77, 5831)), tax, bank = bank).zip(listOf(), listOf(77), true, true, true)

        val zip = conteudo(arquivo)
        assertEquals(setOf("boleto/AD5831-VF412-C0001-parcela1.pdf", "avisos.txt"), zip.keys)
        assertTrue(zip["avisos.txt"]!!.contains("AD5831-VF412-C0001: adiantamento nao emite NF-e"), zip["avisos.txt"])
        assertTrue(chamadasTax.isEmpty())
        assertTrue(chamadasBankPlus.contains("boletos filial=2 doc=77 prazo=true origem=adiantamento"), chamadasBankPlus.toString())
        assertEquals(0, arquivo.notasComErro)
    }

    @Test
    fun `NF e adiantamento com o mesmo DocEntry sao documentos diferentes`() {
        val arquivo = servico(odbc(nota(10, 501), adiantamento(10, 5831))).zip(listOf(10), listOf(10), true, false)
        val zip = conteudo(arquivo)
        assertTrue("NF501-S2-C0001.pdf" in zip, zip.keys.toString())
        assertTrue(zip["avisos.txt"]!!.contains("AD5831"), zip["avisos.txt"])
        assertEquals(0, arquivo.notasComErro)
        val params = consultas.single().second
        assertEquals(listOf(10), params["docEntries"])
        assertEquals(listOf(10), params["adiantamentos"])
    }

    @Test
    fun `adiantamento que nao e de venda futura aparece como nao encontrado`() {
        val arquivo = servico(odbc()).zip(listOf(), listOf(88), false, false, true)
        assertTrue(conteudo(arquivo)["erros.txt"]!!.contains("Adiantamento DocEntry 88: nao encontrado ou nao e de venda futura"))
        assertEquals(1, arquivo.notasComErro)
    }

    @Test
    fun `o teto conta notas e adiantamentos juntos`() {
        val s = servico(odbc(), maximo = 2)
        assertThrows(Exception::class.java) { s.zip(listOf(1, 2), listOf(3), true, false) }
    }

    @Test
    fun `filtro de categoria liga a perna certa do SQL`() {
        val s = servico(odbc())
        fun p(c: CategoriaDocumentoLote?) = s.parametros(NotaFiscalLoteFiltro(categoria = c), listOf(), listOf(), 0, 50)
        assertEquals(listOf("AD", "AD"), p(CategoriaDocumentoLote.AD).let { listOf(it["tipo"], it["categoria"]) })
        // NF futura antiga e NF de saida estao na mesma perna (OINV); a categoria separa
        assertEquals(listOf("NF", "NF_FUTURA"), p(CategoriaDocumentoLote.NF_FUTURA).let { listOf(it["tipo"], it["categoria"]) })
        assertEquals(listOf("NF", "NF"), p(CategoriaDocumentoLote.NF).let { listOf(it["tipo"], it["categoria"]) })
        assertEquals(listOf("", ""), p(null).let { listOf(it["tipo"], it["categoria"]) })
        // listagem: as duas listas com -1 e todosDocs ligado
        val p = s.parametros(NotaFiscalLoteFiltro(), listOf(), listOf(), 0, 50)
        assertEquals(1, p["todosDocs"])
        assertEquals(listOf(-1), p["adiantamentos"])
    }

    @Test
    fun `o JSON da listagem nao leva a chave interna`() {
        // a tela tem getter `chave`; um campo `chave` no JSON quebrava o Object.assign e a lista vinha vazia
        val n = servico(odbc(nota(10, 501))).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20)).content.single()
        val json = com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(n)
        assertFalse(json.contains("\"chave\""), json)
        assertTrue(json.contains("\"tipo\":\"NF\""), json)
    }

    @Test
    fun `NF de venda futura antiga vem com a categoria propria`() {
        val linha = nota(10, 4086).toMutableMap().also { it["Categoria"] = "NF_FUTURA" }
        val n = servico(odbc(linha)).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20)).content.single()
        assertEquals(TipoDocumentoLote.NF, n.tipo)
        assertEquals(CategoriaDocumentoLote.NF_FUTURA, n.categoria)
        // linha sem a coluna (ex.: mock antigo) cai no tipo
        assertEquals(CategoriaDocumentoLote.NF, servico(odbc(nota(11, 1))).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20)).content.single().categoria)
    }

    @Test
    fun `adiantamento mapeia tipo, contrato e forma de pagamento`() {
        val n = servico(odbc(adiantamento(77, 5831).also { it["FormaPagamento"] = "BB-RC-BOL-1199" }))
            .listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20)).content.single()
        assertEquals(TipoDocumentoLote.AD, n.tipo)
        assertEquals(412, n.contrato)
        assertEquals("BB-RC-BOL-1199", n.formaPagamento)
        assertNull(n.chaveAcesso)
    }

    @Test
    fun `download busca so os docEntries pedidos e sem os filtros da tela`() {
        servico(odbc(nota(10, 501))).zip(listOf(10, 11), listOf(), true, false)
        val params = consultas.single().second
        assertEquals(0, params["todosDocs"])
        assertEquals(listOf(10, 11), params["docEntries"])
        assertEquals(1, params["todasFiliais"])
        assertEquals("", params["statusSap"])
        assertEquals(-1, params["statusSefaz"])
        assertEquals(0, params["inicio"])
        assertEquals(2, params["fim"])
    }

    @Test
    fun `filtros da tela viram parametros com sentinela quando vazios`() {
        val s = servico(odbc())
        val cheio = s.parametros(NotaFiscalLoteFiltro(
            dataInicial = "2026-09-01", dataFinal = "2026-09-30", filial = listOf(2, 6), cardCode = " C0001 ",
            salesPersonCode = 7, numeroDe = 500, numeroAte = 600, vencimentoDe = "2026-10-01", vencimentoAte = "2026-10-31",
            statusSap = StatusSapNota.CANCELADO, statusSefaz = 4), listOf(), listOf(), 0, 50)
        assertEquals("2026-09-01", cheio["dataInicial"])
        assertEquals(0, cheio["todasFiliais"])
        assertEquals(listOf(2, 6), cheio["filiais"])
        assertEquals("C0001", cheio["cardCode"])
        assertEquals(7, cheio["vendedor"])
        assertEquals(500, cheio["numeroDe"])
        assertEquals(600, cheio["numeroAte"])
        assertEquals(0, cheio["todosVencimentos"])
        assertEquals("2026-10-01", cheio["vencimentoDe"])
        assertEquals("2026-10-31", cheio["vencimentoAte"])
        assertEquals("CANCELADO", cheio["statusSap"])
        assertEquals(4, cheio["statusSefaz"])

        val vazio = s.parametros(NotaFiscalLoteFiltro(), listOf(), listOf(), 0, 50)
        // IN () e erro de sintaxe: a lista vai com -1 e o flag desliga o filtro
        assertEquals(1, vazio["todasFiliais"])
        assertEquals(listOf(-1), vazio["filiais"])
        assertEquals(listOf(-1), vazio["docEntries"])
        assertEquals("1900-01-01", vazio["dataInicial"])
        assertEquals("2999-12-31", vazio["dataFinal"])
        assertEquals("", vazio["cardCode"])
        assertEquals(-1, vazio["vendedor"])
        assertEquals(-1, vazio["numeroDe"])
        assertEquals(-1, vazio["numeroAte"])
        // sem vencimento na tela o flag desliga; as datas vao largas porque DATE nao compara com ''
        assertEquals(1, vazio["todosVencimentos"])
        assertEquals("1900-01-01", vazio["vencimentoDe"])
        assertEquals("2999-12-31", vazio["vencimentoAte"])
    }

    @Test
    fun `um lado so da faixa ja liga o filtro`() {
        val s = servico(odbc())
        val soDe = s.parametros(NotaFiscalLoteFiltro(vencimentoDe = "2026-10-01", numeroDe = 500), listOf(), listOf(), 0, 50)
        assertEquals(0, soDe["todosVencimentos"])
        assertEquals("2026-10-01", soDe["vencimentoDe"])
        assertEquals("2999-12-31", soDe["vencimentoAte"])
        assertEquals(500, soDe["numeroDe"])
        assertEquals(-1, soDe["numeroAte"])
        val soAte = s.parametros(NotaFiscalLoteFiltro(vencimentoAte = "2026-10-31"), listOf(), listOf(), 0, 50)
        assertEquals(0, soAte["todosVencimentos"])
        assertEquals("1900-01-01", soAte["vencimentoDe"])
    }

    @Test
    fun `faixa invertida e recusada com mensagem`() {
        val s = servico(odbc())
        listOf(
            NotaFiscalLoteFiltro(dataInicial = "2026-09-30", dataFinal = "2026-09-01"),
            NotaFiscalLoteFiltro(vencimentoDe = "2026-10-31", vencimentoAte = "2026-10-01"),
            NotaFiscalLoteFiltro(numeroDe = 600, numeroAte = 500),
        ).forEach { filtro -> assertThrows(Exception::class.java) { s.parametros(filtro, listOf(), listOf(), 0, 50) } }
        // de = ate e uma NF ou um dia so
        s.parametros(NotaFiscalLoteFiltro(numeroDe = 501, numeroAte = 501, vencimentoDe = "2026-10-01", vencimentoAte = "2026-10-01"), listOf(), listOf(), 0, 50)
    }

    @Test
    fun `data fora do formato e recusada antes de ir ao banco`() {
        assertThrows(Exception::class.java) {
            servico(odbc()).parametros(NotaFiscalLoteFiltro(dataInicial = "30/09/2026"), listOf(), listOf(), 0, 50)
        }
    }

    @Test
    fun `listagem pagina pelo ROW_NUMBER com o teto de pagina`() {
        servico(odbc(), maximo = 50).listar(NotaFiscalLoteFiltro(), PageRequest.of(2, 500))
        // pagina pedida com 500 vira 50 (teto); pagina 2 = linhas 101 a 150
        assertEquals(100, consultas.first().second["inicio"])
        assertEquals(150, consultas.first().second["fim"])
    }

    @Test
    fun `listagem le o total da consulta e mapeia a linha`() {
        val pagina = servico(odbc(nota(10, 501, total = 1))).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20))

        assertEquals(1, pagina.totalElements)
        assertEquals(1, consultas.size)
        val n = pagina.content.single()
        assertEquals(501, n.numero)
        assertEquals("2026-09-30", n.data)
        assertEquals("Joao", n.vendedor)
        assertEquals(StatusSapNota.FECHADO, n.statusSap)
        assertEquals("Autorizada", n.statusSefaz)
        assertEquals(4, n.statusSefazId)
        assertEquals(0, "150.50".toBigDecimal().compareTo(n.total))
        assertEquals("2026-10-22", n.vencimentoInicial)
        assertEquals("2027-02-19", n.vencimentoFinal)
        assertEquals(5, n.parcelas)
        assertTrue(n.chaveAcesso!!.startsWith("1126"))
    }

    @Test
    fun `pagina alem da ultima volta vazia com o total de verdade`() {
        val pagina = servico(odbc(nota(10, 501, total = 1))).listar(NotaFiscalLoteFiltro(), PageRequest.of(3, 20))
        assertTrue(pagina.content.isEmpty())
        assertEquals(1, pagina.totalElements)
        // a recontagem pede so a primeira linha
        assertEquals(0, consultas.last().second["inicio"])
        assertEquals(1, consultas.last().second["fim"])
    }

    @Test
    fun `filtro sem nota nenhuma nao reconta`() {
        val pagina = servico(odbc()).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20))
        assertEquals(0, pagina.totalElements)
        assertEquals(1, consultas.size)
    }

    @Test
    fun `vendedor -1 do SAP vira sem vendedor`() {
        val linha = nota(10, 501).toMutableMap().also { it["SlpCode"] = -1; it["SlpName"] = null }
        val n = servico(odbc(linha)).listar(NotaFiscalLoteFiltro(), PageRequest.of(0, 20)).content.single()
        assertNull(n.vendedorCodigo)
        assertNull(n.vendedor)
    }
}
