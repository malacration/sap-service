package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaRegistroPatch
import br.andrew.sap.model.cobranca.CobrancaRemocaoLog
import br.andrew.sap.model.cobranca.CobrancaTituloVendedorSap
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.sistema.Session
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.batch.BatchIdOnly
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchResponse
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.security.AuthService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.util.concurrent.atomic.AtomicReference

class CobrancaServiceTest {

    private val restTemplate = mock<RestTemplate>()
    private val env = SapEnvrioment("https://sap.local", "manager", "senha", "SBODEMO")
    private val authService = AuthService(env, restTemplate)
    private val consultaService = mock<CobrancaConsultaService>()
    private val logService = mock<CobrancaLogService>()
    private val batchService = mock<BatchService>()
    private val service = CobrancaService(env, restTemplate, authService, consultaService, logService, batchService)

    private val cobradora = User(
        "60", "Fulano de Tal", UserOriginEnum.SalePerson, "fulano",
        bussinesPlace = listOf(), roles = listOf("vendedor")
    )
    private val admin = User(
        "1", "Admin", UserOriginEnum.EmployeesInfo, "admin",
        bussinesPlace = listOf(), roles = listOf("admin")
    )

    @BeforeEach
    fun mockLogin() {
        whenever(restTemplate.postForEntity(any<URI>(), any(), eq(Session::class.java)))
            .thenReturn(ResponseEntity.ok(Session("sessao-1", "10.0", 30)))
        whenever(consultaService.buscarTituloParaEscopo(any(), any(), any()))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = null, CardCode = "CLI0007196"))
    }

    @Test
    fun `gera o Code a partir do tipo, docEntry e da parcela`() {
        assertEquals("NF-500-1", CobrancaRegistro.code("NF", 500, 1))
        assertEquals("AD-500-1", CobrancaRegistro.code("AD", 500, 1))
    }

    @Test
    fun `recusa tipo de titulo invalido`() {
        val req = CobrancaAcaoRequest(observacao = "teste")
        assertThrows(CobrancaException::class.java) {
            service.registrarAcao("XX", 500, 1, req, cobradora)
        }
        verify(restTemplate, never()).exchange(any<RequestEntity<*>>(), eq(OData::class.java))
    }

    @Test
    fun `recusa registrar acao sem observacao e sem ocorrencia`() {
        val req = CobrancaAcaoRequest(status = "8 - EM NEGOCIAÇÃO")

        assertThrows(CobrancaException::class.java) {
            service.registrarAcao("NF", 500, 1, req, cobradora)
        }
        verify(restTemplate, never()).exchange(any<RequestEntity<*>>(), eq(OData::class.java))
    }

    @Test
    fun `cria o registro quando ele ainda nao existe e carimba o cobrador e a data`() {
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    HttpMethod.POST -> ResponseEntity.ok(odataFlat("Code" to "NF-500-1"))
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val req = CobrancaAcaoRequest(status = "8 - EM NEGOCIAÇÃO", observacao = "cliente prometeu pagar")
        val registro = service.registrarAcao("NF", 500, 1, req, cobradora)

        assertEquals("NF-500-1", registro.Code)

        val captor = org.mockito.kotlin.argumentCaptor<RequestEntity<*>>()
        verify(restTemplate, org.mockito.Mockito.times(2)).exchange(captor.capture(), eq(OData::class.java))
        val corpoCriado = captor.allValues.first { it.method == HttpMethod.POST }.body as CobrancaRegistro

        assertEquals("Fulano de Tal", corpoCriado.U_Cobrador)
        assertEquals("CLI0007196", corpoCriado.U_CardCode)
        assertEquals(1, corpoCriado.historico.size)
        assertEquals("Fulano de Tal", corpoCriado.historico.first().U_Usuario)
        assertEquals("60", corpoCriado.historico.first().U_UsuarioId, "sem o id a autoria volta a depender do nome")
    }

    @Test
    fun `grava o CardCode do titulo ao criar o registro pela primeira vez`() {
        // U_CardCode e um campo declarado no schema (CobrancaConfiguration, ligado a
        // BusinessPartners) mas nunca era preenchido aqui - achado investigando um caso real
        // onde a linha ficava com Status/Acao certos no historico mas o cabecalho incompleto.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0009999"))
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    HttpMethod.POST -> ResponseEntity.ok(odataFlat("Code" to "NF-500-1"))
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val req = CobrancaAcaoRequest(observacao = "cliente prometeu pagar")
        service.registrarAcao("NF", 500, 1, req, cobradora)

        val captor = org.mockito.kotlin.argumentCaptor<RequestEntity<*>>()
        verify(restTemplate, org.mockito.Mockito.times(2)).exchange(captor.capture(), eq(OData::class.java))
        val corpoCriado = captor.allValues.first { it.method == HttpMethod.POST }.body as CobrancaRegistro

        assertEquals("CLI0009999", corpoCriado.U_CardCode)
    }

    @Test
    fun `SAP devolvendo U_DocEntry nulo nao derruba a acao do cobrador`() {
        // U_DocEntry e SMALLINT no HANA (teto 32767) e DocEntry de OINV ja passa de 150 mil: o
        // Service Layer aceita o POST e devolve 200 com o campo nulo, sem erro. Quem registrou a
        // cobranca nao tem culpa nem o que fazer a respeito - a acao vale, o alerta fica no log.
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    // resposta do SAP sem U_DocEntry, exatamente como em producao
                    HttpMethod.POST -> ResponseEntity.ok(odataFlat("Code" to "NF-141516-1", "U_Tipo" to "NF"))
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val req = CobrancaAcaoRequest(observacao = "cobrado via whatsapp")
        val registro = service.registrarAcao("NF", 141516, 1, req, cobradora)

        assertEquals("NF-141516-1", registro.Code)
    }

    @Test
    fun `atualiza o registro existente sem apagar o historico anterior e ignora cobrador que o cliente nao pode enviar`() {
        val linhaAntiga = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Nilvia", U_Cobrador = "Nilvia", U_Status = "3 - SEM CONTATO"
        ).also { it.LineId = 1 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1, U_Status = "3 - SEM CONTATO",
            historico = mutableListOf(linhaAntiga)
        )
        val depoisDoPatch = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1, U_Status = "9 - ACORDO ENVIADO",
            U_Cobrador = "Fulano de Tal",
            historico = mutableListOf(
                linhaAntiga,
                CobrancaHistorico("2026-07-27", "Fulano de Tal", "Fulano de Tal", U_Status = "9 - ACORDO ENVIADO")
                    .also { it.LineId = 2 }
            )
        )

        var chamadasGet = 0
        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> {
                        chamadasGet++
                        ResponseEntity.ok(odataLista(if (chamadasGet == 1) listOf(existente) else listOf(depoisDoPatch)))
                    }
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val req = CobrancaAcaoRequest(status = "9 - ACORDO ENVIADO", observacao = "cliente confirmou pagamento")
        val registro = service.registrarAcao("NF", 500, 1, req, cobradora)

        assertEquals("9 - ACORDO ENVIADO", registro.U_Status)
        assertEquals(2, registro.historico.size)
        assertEquals("Fulano de Tal", corpoPatch!!["U_Cobrador"])

        val historicoEnviado = corpoPatch!!["COB_TITULO_LCollection"] as List<*>
        assertEquals(2, historicoEnviado.size, "a linha antiga precisa ser reenviada, senao o PATCH apaga o historico")
    }

    @Test
    fun `registro legado com U_DocEntry nulo e reposto na proxima acao, nao fica preso pra sempre`() {
        // Caso real (NF-141597-1): registro criado quando a coluna ainda era SMALLINT ficou com
        // U_DocEntry nulo. Como o Code esta certo, toda acao nova cai no caminho de atualizacao,
        // que so mandava os campos mexidos - o titulo nunca voltava a casar com a view.
        val existente = CobrancaRegistro(
            Code = "NF-141597-1", U_Tipo = "NF", U_DocEntry = null, U_InstlmntID = 1,
            U_CardCode = null, U_Status = "8 - EM NEGOCIAÇÃO", historico = mutableListOf(),
        )
        whenever(consultaService.buscarTituloParaEscopo("NF", 141597, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007777"))

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.registrarAcao("NF", 141597, 1, CobrancaAcaoRequest(observacao = "novo contato"), cobradora)

        assertEquals(141597, corpoPatch!!["U_DocEntry"])
        assertEquals("CLI0007777", corpoPatch!!["U_CardCode"])
    }

    @Test
    fun `registro integro nao paga consulta extra ao SAP so pra conferir campo`() {
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_CardCode = "CLI0007196", historico = mutableListOf(),
        )
        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.registrarAcao("NF", 500, 1, CobrancaAcaoRequest(observacao = "contato"), cobradora)

        assertFalse(corpoPatch!!.containsKey("U_DocEntry"), "nao deve reenviar campo que ja esta certo")
        assertFalse(corpoPatch!!.containsKey("U_CardCode"))
        verify(consultaService, never()).buscarTituloParaEscopo(any(), any(), any())
    }

    @Test
    fun `campo deixado em branco fica de fora do PATCH em vez de ser enviado como null`() {
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_Status = "3 - SEM CONTATO", U_Acao = "4 - LIGAÇÃO",
            historico = mutableListOf()
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        //so envia observacao; status e acao ficam de fora do request (usuario nao mexeu neles)
        val req = CobrancaAcaoRequest(observacao = "cliente ligou de volta")
        service.registrarAcao("NF", 500, 1, req, cobradora)

        assertFalse(corpoPatch!!.containsKey("U_Status"), "campo nao informado nao pode ser enviado (nem como null)")
        assertFalse(corpoPatch!!.containsKey("U_Acao"), "campo nao informado nao pode ser enviado (nem como null)")
        assertEquals("cliente ligou de volta", corpoPatch!!["U_Observacao"])
    }

    @Test
    fun `lote isola erro por item sem interromper os demais`() {
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    HttpMethod.POST -> ResponseEntity.ok(odataFlat("Code" to "NF-1-1"))
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val itens = listOf(
            CobrancaAcaoLoteItem(tipo = "NF", docEntry = 1, instlmntId = 1, status = "8 - EM NEGOCIAÇÃO", observacao = "ok"),
            CobrancaAcaoLoteItem(tipo = "NF", docEntry = 2, instlmntId = 1), // sem observacao nem ocorrencia -> deve falhar
        )

        val resultado = service.registrarAcaoEmLote(itens, cobradora)

        assertEquals(2, resultado.size)
        assertTrue(resultado[0].success)
        assertFalse(resultado[1].success)
        assertEquals(2, resultado[1].docEntry)
        assertTrue(resultado[1].error?.isNotBlank() == true)
    }

    @Test
    fun `duas acoes concorrentes no mesmo titulo nao perdem nenhuma linha de historico`() {
        val estadoAtual = AtomicReference(
            CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1, historico = mutableListOf())
        )

        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> {
                        Thread.sleep(20)
                        ResponseEntity.ok(odataLista(listOf(estadoAtual.get())))
                    }
                    HttpMethod.PATCH -> {
                        val corpo = request.body as Map<*, *>
                        @Suppress("UNCHECKED_CAST")
                        val novoHistorico = corpo["COB_TITULO_LCollection"] as MutableList<CobrancaHistorico>
                        estadoAtual.set(estadoAtual.get().apply { historico = novoHistorico })
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val cobradorA = User("60", "Cobrador A", UserOriginEnum.SalePerson, "a", bussinesPlace = listOf(), roles = listOf("vendedor"))
        val cobradorB = User("61", "Cobrador B", UserOriginEnum.SalePerson, "b", bussinesPlace = listOf(), roles = listOf("vendedor"))

        val threadA = Thread { service.registrarAcao("NF", 500, 1, CobrancaAcaoRequest(observacao = "acao A"), cobradorA) }
        val threadB = Thread { service.registrarAcao("NF", 500, 1, CobrancaAcaoRequest(observacao = "acao B"), cobradorB) }
        threadA.start()
        threadB.start()
        threadA.join()
        threadB.join()

        assertEquals(2, estadoAtual.get().historico.size, "as duas acoes concorrentes devem aparecer no historico final")
    }

    @Test
    fun `vendedor comum ve o historico do proprio titulo`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1)).thenReturn(CobrancaTituloVendedorSap(SlpCode = 60))
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenReturn(ResponseEntity.ok(odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1)))))

        val historico = service.historico(cobradora, "NF", 500, 1)

        assertTrue(historico.isEmpty())
    }

    @Test
    fun `vendedor comum nao ve historico de titulo de outro vendedor`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1)).thenReturn(CobrancaTituloVendedorSap(SlpCode = 99))

        assertThrows(ResponseStatusException::class.java) {
            service.historico(cobradora, "NF", 500, 1)
        }
        verify(restTemplate, never()).exchange(any<RequestEntity<*>>(), eq(OData::class.java))
    }

    @Test
    fun `vendedor comum nao ve historico de titulo que nao existe no SAP`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1)).thenReturn(null)

        assertThrows(ResponseStatusException::class.java) {
            service.historico(cobradora, "NF", 500, 1)
        }
    }

    @Test
    fun `admin ve historico de qualquer titulo, sem consultar o SlpCode`() {
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenReturn(ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>())))

        val historico = service.historico(admin, "NF", 500, 1)

        assertTrue(historico.isEmpty())
        verify(consultaService, never()).buscarTituloParaEscopo(any(), any(), any())
    }

    @Test
    fun `recusa criar registro quando a parcela nao existe no SAP`() {
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    else -> throw AssertionError("Nao deveria criar nada para uma parcela que nao existe: ${request.method}")
                }
            }
        whenever(consultaService.buscarTituloParaEscopo("NF", 999, 1)).thenReturn(null)

        val req = CobrancaAcaoRequest(observacao = "teste")
        assertThrows(CobrancaException::class.java) {
            service.registrarAcao("NF", 999, 1, req, cobradora)
        }
    }

    /**
     * Mock do changeset: guarda os lotes enviados e aplica no registro o efeito do PATCH (o SAP,
     * com B1S-ReplaceCollectionsOnPatch, substitui a colecao), pra releitura do service ver o
     * estado de depois da escrita.
     */
    // Estado do registro no "SAP" do teste: o changeset mexe nele e a releitura do service ve o
    // resultado - inclusive o DELETE, que faz a consulta seguinte nao achar mais nada.
    private var registroNoSap: CobrancaRegistro? = null

    private fun changesetAplicando(existente: CobrancaRegistro): MutableList<BatchList> {
        val enviados = mutableListOf<BatchList>()
        whenever(batchService.run(any<BatchList>())).thenAnswer { invocation ->
            val lote = invocation.getArgument<BatchList>(0)
            enviados.add(lote)
            lote.firstOrNull { it.method == BatchMethod.PATCH }?.let { item ->
                val campos = (item.payload as CobrancaRegistroPatch).campos
                @Suppress("UNCHECKED_CAST")
                existente.historico = (campos["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
            }
            if (lote.any { it.method == BatchMethod.DELETE })
                registroNoSap = null
            emptyList<BatchResponse>()
        }
        return enviados
    }

    // Nenhuma escrita da remocao vai pelo RestTemplate: tudo que nao e GET aqui significa que a
    // operacao saiu do changeset e voltou a ser chamada solta.
    private fun soLeituraDoRegistro(existente: CobrancaRegistro) {
        registroNoSap = existente
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                if (request.method != HttpMethod.GET)
                    throw AssertionError("Escrita fora do changeset: ${request.method}")
                ResponseEntity.ok(odataLista(listOfNotNull(registroNoSap)))
            }
    }

    private fun itemDo(lote: BatchList, metodo: BatchMethod) = lote.first { it.method == metodo }

    private fun camposDoPatch(lote: BatchList) =
        (itemDo(lote, BatchMethod.PATCH).payload as CobrancaRegistroPatch).campos

    private fun linha(
        lineId: Int,
        data: String,
        usuarioId: String? = "60",
        usuario: String = "Fulano de Tal",
        status: String? = null,
        acao: String? = null,
        ocorrencia: String? = null,
        observacao: String? = null,
        dataPromessa: String? = null,
    ) = CobrancaHistorico(
        U_Data = data, U_Usuario = usuario, U_Cobrador = usuario, U_UsuarioId = usuarioId,
        U_Status = status, U_Acao = acao, U_Ocorrencia = ocorrencia, U_Observacao = observacao,
        U_DataPromessa = dataPromessa,
    ).also { it.LineId = lineId }

    private fun registro(vararg linhas: CobrancaHistorico, dataPromessa: String? = null) = CobrancaRegistro(
        Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
        U_DataPromessa = dataPromessa, historico = linhas.toMutableList(),
    )

    private fun escopoDoProprioVendedor() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
    }

    @Test
    fun `auditoria e remocao vao no MESMO changeset - ou as duas ou nenhuma`() {
        // Em chamadas separadas existia a janela em que a auditoria gravava "linha removida" e a
        // remocao falhava depois, deixando registro afirmando remocao que nao houve.
        escopoDoProprioVendedor()
        val daNilvia = linha(1, "2026-07-01", usuarioId = "45", usuario = "Nilvia", status = "3 - SEM CONTATO")
        val minha = linha(2, "2026-07-27", status = "9 - ACORDO ENVIADO", observacao = "linha errada")
        val existente = registro(daNilvia, minha)
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        val historico = service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals(1, enviados.size, "uma unica ida ao SAP: o changeset")
        val lote = enviados.first()
        assertEquals(2, lote.size, "auditoria + remocao no mesmo lote")

        val auditoria = itemDo(lote, BatchMethod.POST).payload as CobrancaRemocaoLog
        assertEquals("NF-500-1", auditoria.U_Registro)
        assertEquals(2, auditoria.U_LineId)
        assertEquals("linha errada", auditoria.U_Observacao, "a auditoria leva o conteudo apagado")

        val patch = itemDo(lote, BatchMethod.PATCH)
        assertEquals(
            "true", patch.headers["B1S-ReplaceCollectionsOnPatch"],
            "sem esse header o Service Layer faz merge da colecao e a linha omitida continua no SAP",
        )
        val historicoEnviado = camposDoPatch(lote)["COB_TITULO_LCollection"] as List<*>
        assertEquals(1, historicoEnviado.size, "a linha removida some por nao ser reenviada")
        assertEquals(1, (historicoEnviado.first() as CobrancaHistorico).LineId)

        assertEquals(1, historico.size)
        assertEquals(1, historico.first().LineId)
    }

    @Test
    fun `cabecalho volta pra acao que sobrou, senao a tela mostra status de linha apagada`() {
        escopoDoProprioVendedor()
        val antiga = linha(1, "2026-07-01", status = "3 - SEM CONTATO", acao = "4 - LIGAÇÃO", observacao = "ligou, nao atendeu")
        val nova = linha(2, "2026-07-27", status = "9 - ACORDO ENVIADO", ocorrencia = "2 - PROMESSA", observacao = "duplicada")
        val existente = registro(antiga, nova)
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        val campos = camposDoPatch(enviados.first())
        assertEquals("3 - SEM CONTATO", campos["U_Status"])
        assertEquals("4 - LIGAÇÃO", campos["U_Acao"])
        assertEquals("2026-07-01", campos["U_DataAcao"])
        // A linha que sobrou nao tinha ocorrencia: precisa limpar o cabecalho, nao manter a da
        // linha apagada. A UDT guarda ausencia como string vazia, nao null.
        assertEquals("", campos["U_Ocorrencia"])
    }

    @Test
    fun `remover linha do meio nao zera campo que uma acao anterior ainda sustenta`() {
        // O cabecalho e o acumulado das acoes: registrarAcao so grava o campo que o cobrador
        // mexeu. Refazer o cabecalho a partir da linha mais recente (que aqui so tem observacao)
        // apagaria o status e a acao que a linha 1 registrou - o titulo voltaria pra
        // "1 - NAO INICIADO" na grade e sumiria do filtro de status.
        escopoDoProprioVendedor()
        val primeira = linha(1, "2026-07-01", status = "3 - SEM CONTATO", acao = "4 - LIGAÇÃO")
        val doMeio = linha(2, "2026-07-15", status = "9 - ACORDO ENVIADO")
        val ultima = linha(3, "2026-07-27", observacao = "cliente pediu boleto novo")
        val existente = registro(primeira, doMeio, ultima)
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        val campos = camposDoPatch(enviados.first())
        assertEquals("3 - SEM CONTATO", campos["U_Status"], "status da linha 1 continua valendo")
        assertEquals("4 - LIGAÇÃO", campos["U_Acao"])
        assertEquals("cliente pediu boleto novo", campos["U_Observacao"])
        assertEquals("2026-07-27", campos["U_DataAcao"], "quem cobrou por ultimo continua sendo a linha 3")
    }

    @Test
    fun `remover a ultima linha apaga o registro - titulo volta pra nao iniciado`() {
        // Registro vazio continuaria contando como titulo trabalhado no dashboard (que olha a
        // presenca em @COB_TITULO) e aparecendo nos filtros de rastreado sem nenhuma acao.
        escopoDoProprioVendedor()
        val existente = registro(linha(1, "2026-07-27", status = "9 - ACORDO ENVIADO"))
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        val historico = service.removerHistorico(cobradora, "NF", 500, 1, 1)

        assertTrue(historico.isEmpty())
        val lote = enviados.first()
        assertEquals(2, lote.size, "auditoria + delete do registro, no mesmo changeset")
        val delete = itemDo(lote, BatchMethod.DELETE)
        assertEquals(
            "'NF-500-1'", (delete.payload as BatchIdOnly).getId(),
            "Code e alfanumerico: precisa ir entre aspas, senao o SAP responde 400",
        )
    }

    @Test
    fun `cobrador nao remove linha de outro cobrador`() {
        escopoDoProprioVendedor()
        val deOutro = linha(1, "2026-07-01", usuarioId = "45", usuario = "Nilvia", status = "3 - SEM CONTATO")
        soLeituraDoRegistro(registro(deOutro))

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
        verify(batchService, never()).run(any<BatchList>())
    }

    @Test
    fun `acesso total nao autoriza apagar o historico de outro cobrador`() {
        // Escopo (quem ve o titulo) e autoria (quem registrou a acao) sao coisas diferentes: o
        // historico e a prova do que foi combinado com o cliente, so o autor pode remover.
        soLeituraDoRegistro(registro(linha(1, "2026-07-01", usuarioId = "45", usuario = "Nilvia")))

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(admin, "NF", 500, 1, 1)
        }
        verify(batchService, never()).run(any<BatchList>())
    }

    @Test
    fun `vendedor comum nao remove historico de titulo de outro vendedor`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1)).thenReturn(CobrancaTituloVendedorSap(SlpCode = 99))

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
        verify(restTemplate, never()).exchange(any<RequestEntity<*>>(), eq(OData::class.java))
        verify(batchService, never()).run(any<BatchList>())
    }

    @Test
    fun `linha que nao existe mais no historico para em CobrancaException, sem escrever no SAP`() {
        escopoDoProprioVendedor()
        soLeituraDoRegistro(registro())

        assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 7)
        }
        // Corrida entre dois cobradores com o modal aberto: a segunda remocao nao pode escrever.
        verify(batchService, never()).run(any<BatchList>())
    }

    @Test
    fun `autoria vale pelo id, nao pelo nome - homonimo nao apaga linha do outro`() {
        // Dois cobradores chamados igual existem de verdade no cadastro; antes o U_Usuario batia
        // e um apagava a acao do outro. Mesmo nome, SlpCode diferente: tem que barrar.
        escopoDoProprioVendedor()
        soLeituraDoRegistro(registro(linha(1, "2026-07-01", usuarioId = "77", status = "3 - SEM CONTATO")))

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
        verify(batchService, never()).run(any<BatchList>())
    }

    @Test
    fun `cobrador renomeado no SAP continua removendo a propria linha`() {
        // O nome mudou no OSLP depois da acao; o id nao muda, entao a linha segue sendo dele.
        escopoDoProprioVendedor()
        val existente = registro(linha(1, "2026-07-01", usuario = "Fulano", status = "3 - SEM CONTATO"))
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        assertTrue(service.removerHistorico(cobradora, "NF", 500, 1, 1).isEmpty())
        assertEquals(1, enviados.size)
    }

    @Test
    fun `linha antiga sem id ainda e reconhecida pelo nome de quem escreveu`() {
        // Historico gravado antes do U_UsuarioId existir: travar pelo id deixaria tudo o que ja
        // esta no SAP sem poder ser removido por ninguem.
        escopoDoProprioVendedor()
        val existente = registro(linha(1, "2026-07-01", usuarioId = null, status = "3 - SEM CONTATO"))
        soLeituraDoRegistro(existente)
        changesetAplicando(existente)

        assertTrue(service.removerHistorico(cobradora, "NF", 500, 1, 1).isEmpty())
    }

    @Test
    fun `a leitura do historico diz quais linhas o usuario pode remover`() {
        // A tela nao tem como decidir isso sozinha: no login por Keycloak o token do navegador
        // nao carrega o User.id do SAP.
        escopoDoProprioVendedor()
        val minha = linha(2, "2026-07-27", status = "9 - ACORDO ENVIADO")
        val deOutro = linha(1, "2026-07-01", usuarioId = "45", usuario = "Nilvia", status = "3 - SEM CONTATO")
        soLeituraDoRegistro(registro(deOutro, minha))

        val linhas = service.historico(cobradora, "NF", 500, 1)

        assertEquals(listOf(2, 1), linhas.map { it.LineId }, "mais recente primeiro")
        assertTrue(linhas.first { it.LineId == 2 }.PodeRemover)
        assertFalse(linhas.first { it.LineId == 1 }.PodeRemover)
    }

    @Test
    fun `a acao guarda a data prometida na propria linha, nao so no cabecalho`() {
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(emptyList<CobrancaRegistro>()))
                    HttpMethod.POST -> ResponseEntity.ok(odataFlat("Code" to "NF-500-1"))
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.registrarAcao(
            "NF", 500, 1,
            CobrancaAcaoRequest(observacao = "cliente prometeu pix", dataPromessa = "2026-08-10"),
            cobradora,
        )

        val captor = org.mockito.kotlin.argumentCaptor<RequestEntity<*>>()
        verify(restTemplate, org.mockito.Mockito.times(2)).exchange(captor.capture(), eq(OData::class.java))
        val criado = captor.allValues.first { it.method == HttpMethod.POST }.body as CobrancaRegistro

        assertEquals("2026-08-10", criado.historico.first().U_DataPromessa)
        assertEquals("2026-08-10", criado.U_DataPromessa, "o cabecalho continua com a promessa vigente")
    }

    @Test
    fun `remover a acao que prometeu volta o cabecalho pra promessa anterior`() {
        escopoDoProprioVendedor()
        val prometeuDia5 = linha(1, "2026-07-01", dataPromessa = "2026-07-05", observacao = "prometeu dia 5")
        val prometeuDia20 = linha(2, "2026-07-10", dataPromessa = "2026-07-20", observacao = "remarcou pro dia 20")
        val existente = registro(prometeuDia5, prometeuDia20, dataPromessa = "2026-07-20")
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals("2026-07-05", camposDoPatch(enviados.first())["U_DataPromessa"])
    }

    @Test
    fun `sem promessa sobrando o cabecalho e limpo com null, nao com string vazia`() {
        // Campo db_Date: o Service Layer recusa "". E limpar importa - a view de promessa vencida
        // (cobranca-promessa-vencida.sql) le U_DataPromessa do cabecalho, entao data orfa mantem o
        // titulo no KPI de promessa vencida pra sempre.
        escopoDoProprioVendedor()
        val semPromessa = linha(1, "2026-07-01", observacao = "ligou, sem compromisso")
        val prometeu = linha(2, "2026-07-10", dataPromessa = "2026-07-20")
        val existente = registro(semPromessa, prometeu, dataPromessa = "2026-07-20")
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        val campos = camposDoPatch(enviados.first())
        assertTrue(campos.containsKey("U_DataPromessa"), "o campo tem que ir no PATCH pra ser limpo")
        assertEquals(null, campos["U_DataPromessa"])
    }

    @Test
    fun `remover linha que nao prometeu nada nao encosta na promessa do cabecalho`() {
        // Linha gravada antes do U_DataPromessa existir na UDT tem o campo nulo mesmo tendo
        // prometido de verdade: recompor sempre apagaria promessa legitima de registro antigo.
        escopoDoProprioVendedor()
        val legada = linha(1, "2026-07-01", status = "8 - EM NEGOCIAÇÃO")
        val outraLegada = linha(2, "2026-07-10", observacao = "novo contato")
        val existente = registro(legada, outraLegada, dataPromessa = "2026-07-20")
        soLeituraDoRegistro(existente)
        val enviados = changesetAplicando(existente)

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertFalse(camposDoPatch(enviados.first()).containsKey("U_DataPromessa"), "nao mexer e diferente de limpar")
    }

    @Test
    fun `SAP que ignora a remocao nao pode virar sucesso na tela`() {
        // O Service Layer responde 200 e mantem a linha em alguns casos. A tela dizia "removido",
        // a grade recarregava e o historico voltava inteiro, sem erro.
        escopoDoProprioVendedor()
        val intacto = registro(
            linha(1, "2026-07-01", status = "3 - SEM CONTATO"),
            linha(2, "2026-07-27", status = "9 - ACORDO ENVIADO"),
        )
        soLeituraDoRegistro(intacto)
        // Changeset que responde sucesso sem aplicar nada.
        whenever(batchService.run(any<BatchList>())).thenReturn(emptyList())

        val erro = assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 2)
        }

        assertTrue(erro.message!!.contains("continua"), "a mensagem tem que dizer que a linha ficou: ${erro.message}")
        // A auditoria foi confirmada junto do PATCH: registro afirmando remocao que nao houve e
        // pior que nao ter registro, entao ele e apagado.
        val captor = org.mockito.kotlin.argumentCaptor<String>()
        verify(logService).delete(captor.capture())
        assertTrue(
            captor.firstValue.startsWith("'NF-500-1-2-"),
            "tem que apagar a auditoria daquela remocao (Code carrega registro-linha-instante): ${captor.firstValue}",
        )
    }

    @Test
    fun `changeset que falha vira erro de negocio e nao mexe na auditoria`() {
        // O changeset e transacional: se a remocao nao acontece, o POST da auditoria e desfeito
        // pelo proprio SAP - nao ha o que compensar. E o texto cru do Service Layer nao pode
        // chegar na tela como erro generico.
        escopoDoProprioVendedor()
        val existente = registro(linha(1, "2026-07-27", status = "9 - ACORDO ENVIADO"))
        soLeituraDoRegistro(existente)
        whenever(batchService.run(any<BatchList>()))
            .thenThrow(RuntimeException("400 - Property 'U_RemovidoPorId' of 'COB_TITULO_LOG' is invalid"))

        val erro = assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }

        assertTrue(erro.message!!.contains("Nada foi apagado"), erro.message)
        assertTrue(erro.message!!.contains("U_RemovidoPorId"), "a causa do SAP tem que continuar visivel")
        verify(logService, never()).delete(any())
    }

    @Test
    fun `SAP que ignora o delete do registro tambem nao vira sucesso`() {
        // O BatchService so acusa erro quando consegue interpretar a resposta: resposta que o
        // parser nao reconhece vira lista vazia e passa por sucesso. Sem conferir a releitura, o
        // ramo do delete dizia "removido" com o registro ainda no SAP.
        escopoDoProprioVendedor()
        val intacto = registro(linha(1, "2026-07-27", status = "9 - ACORDO ENVIADO"))
        soLeituraDoRegistro(intacto)
        whenever(batchService.run(any<BatchList>())).thenReturn(emptyList())

        val erro = assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }

        assertTrue(erro.message!!.contains("não apagou o registro"), erro.message)
        verify(logService).delete(any())
    }

    @Test
    fun `o corpo do PATCH no changeset e o mapa cru dos campos, sem envelope nem id`() {
        // CobrancaRegistroPatch carrega o Code pra URL mas nao pode deixar isso vazar pro corpo: o
        // Service Layer recusa propriedade que nao existe na entidade. O @JsonAnyGetter e o
        // @JsonIgnore herdado de BatchId sao o que garantem isso, e nada mais exercitava esse par.
        val entityService = mock<EntitiesService<*>>()
        whenever(entityService.path()).thenReturn("/b1s/v1/COB_TITULO")
        val patch = CobrancaRegistroPatch(
            "NF-500-1",
            mapOf("U_Status" to "3 - SEM CONTATO", "U_DataPromessa" to null),
        )

        val corpo = BatchService(mock(), mock(), mock(), mock())
            .body("batch-test", BatchList().add(BatchMethod.PATCH, patch, entityService))
            .toString(Charsets.UTF_8)

        assertTrue(corpo.contains("PATCH /b1s/v1/COB_TITULO('NF-500-1')"), corpo)
        assertTrue(corpo.contains("\"U_Status\":\"3 - SEM CONTATO\""), corpo)
        // Promessa tem que ir como null pra ser limpa - se o campo desaparecer, ela fica velha.
        assertTrue(corpo.contains("\"U_DataPromessa\":null"), corpo)
        assertFalse(corpo.contains("campos"), "sem envelope em volta do mapa")
        assertFalse(corpo.contains("\"id\""), "o id e da URL, nao do corpo")
        assertFalse(corpo.contains("NF-500-1\","), "o Code nao pode vazar como propriedade")
    }

    @Test
    fun `o Code da auditoria identifica registro, linha e instante`() {
        val linha = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Nilvia", U_Cobrador = "Nilvia", U_UsuarioId = "45",
            U_Status = "3 - SEM CONTATO", U_Observacao = "ligou",
        ).also { it.LineId = 3 }

        val log = CobrancaRemocaoLog.de(
            "NF-7503-1", linha, "Fulano de Tal", "60",
            java.time.LocalDateTime.of(2026, 8, 19, 13, 40, 12),
        )

        assertEquals("NF-7503-1-3-20260819134012", log.Code)
        assertEquals("2026-08-19", log.U_RemovidoEm)
        assertEquals("13:40", log.U_RemovidoHora)
        // O conteudo apagado tem que estar todo aqui - e a unica copia que sobra.
        assertEquals("Nilvia", log.U_Autor)
        assertEquals("45", log.U_AutorId)
        assertEquals("3 - SEM CONTATO", log.U_Status)
        assertEquals("ligou", log.U_Observacao)
        assertEquals("Fulano de Tal", log.U_RemovidoPor)
        assertEquals("60", log.U_RemovidoPorId)
    }

    private fun odataLista(value: List<Any?>): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = value
        return OData(backing)
    }

    private fun odataFlat(vararg pares: Pair<String, Any?>): OData {
        return OData(LinkedHashMap(pares.toMap()))
    }
}
