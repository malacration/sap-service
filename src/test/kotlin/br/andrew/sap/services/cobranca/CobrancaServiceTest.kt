package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaRemocaoLog
import br.andrew.sap.model.cobranca.CobrancaTituloVendedorSap
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.sistema.Session
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
    private val service = CobrancaService(env, restTemplate, authService, consultaService, logService)

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

    @Test
    fun `quem registrou remove a propria linha e o historico volta sem ela`() {
        // O escopo do vendedor e conferido antes de mexer no historico (mesma regra da leitura):
        // sem isso o mock default devolve SlpCode nulo e a remocao para no 403.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaDaNilvia = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Nilvia", U_Cobrador = "Nilvia",
            U_Status = "3 - SEM CONTATO", U_Acao = "4 - LIGAÇÃO", U_Observacao = "ligou, nao atendeu",
        ).also { it.LineId = 1 }
        val linhaDoFulano = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "9 - ACORDO ENVIADO", U_Observacao = "linha errada",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_Status = "9 - ACORDO ENVIADO", U_Cobrador = "Fulano de Tal",
            historico = mutableListOf(linhaDaNilvia, linhaDoFulano),
        )
        val depoisDoPatch = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_Status = "3 - SEM CONTATO", U_Cobrador = "Nilvia",
            historico = mutableListOf(linhaDaNilvia),
        )

        var chamadasGet = 0
        var corpoPatch: Map<*, *>? = null
        var cabecalhosPatch: org.springframework.http.HttpHeaders? = null
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
                        cabecalhosPatch = request.headers
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val historico = service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals(1, historico.size)
        assertEquals(1, historico.first().LineId)
        assertEquals(
            "true", cabecalhosPatch?.getFirst("B1S-ReplaceCollectionsOnPatch"),
            "sem esse header o Service Layer faz merge da colecao e a linha omitida continua no SAP",
        )

        val historicoEnviado = corpoPatch!!["COB_TITULO_LCollection"] as List<*>
        assertEquals(1, historicoEnviado.size, "o PATCH substitui a colecao - a linha removida some por nao ser reenviada")
        assertEquals(1, (historicoEnviado.first() as CobrancaHistorico).LineId)
    }

    @Test
    fun `cabecalho volta pra acao que sobrou, senao a tela mostra status de linha apagada`() {
        // O escopo do vendedor e conferido antes de mexer no historico (mesma regra da leitura):
        // sem isso o mock default devolve SlpCode nulo e a remocao para no 403.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaAntiga = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "3 - SEM CONTATO", U_Acao = "4 - LIGAÇÃO", U_Observacao = "ligou, nao atendeu",
        ).also { it.LineId = 1 }
        val linhaNova = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "9 - ACORDO ENVIADO", U_Ocorrencia = "2 - PROMESSA", U_Observacao = "duplicada",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_Status = "9 - ACORDO ENVIADO", U_Ocorrencia = "2 - PROMESSA", U_DataAcao = "2026-07-27",
            historico = mutableListOf(linhaAntiga, linhaNova),
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        // O mock aplica a substituicao da colecao, como o SAP faz com o header
                        // B1S-ReplaceCollectionsOnPatch - a releitura do service confere isso.
                        @Suppress("UNCHECKED_CAST")
                        existente.historico = (corpoPatch!!["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals("3 - SEM CONTATO", corpoPatch!!["U_Status"])
        assertEquals("4 - LIGAÇÃO", corpoPatch!!["U_Acao"])
        assertEquals("2026-07-01", corpoPatch!!["U_DataAcao"])
        // A linha que sobrou nao tinha ocorrencia: precisa limpar o cabecalho, nao manter a da
        // linha apagada. A UDT guarda ausencia como string vazia, nao null.
        assertEquals("", corpoPatch!!["U_Ocorrencia"])
    }

    @Test
    fun `remover linha do meio nao zera campo que uma acao anterior ainda sustenta`() {
        // O cabecalho e o acumulado das acoes: registrarAcao so grava o campo que o cobrador
        // mexeu. Refazer o cabecalho a partir da linha mais recente (que aqui so tem observacao)
        // apagaria o status e a acao que a linha 1 registrou - o titulo voltaria pra
        // "1 - NAO INICIADO" na grade e sumiria do filtro de status.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val primeira = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "3 - SEM CONTATO", U_Acao = "4 - LIGAÇÃO",
        ).also { it.LineId = 1 }
        val doMeio = CobrancaHistorico(
            U_Data = "2026-07-15", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "9 - ACORDO ENVIADO",
        ).also { it.LineId = 2 }
        val ultima = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Observacao = "cliente pediu boleto novo",
        ).also { it.LineId = 3 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_Status = "9 - ACORDO ENVIADO", U_Acao = "4 - LIGAÇÃO",
            historico = mutableListOf(primeira, doMeio, ultima),
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        // O mock aplica a substituicao da colecao, como o SAP faz com o header
                        // B1S-ReplaceCollectionsOnPatch - a releitura do service confere isso.
                        @Suppress("UNCHECKED_CAST")
                        existente.historico = (corpoPatch!!["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals("3 - SEM CONTATO", corpoPatch!!["U_Status"], "status da linha 1 continua valendo")
        assertEquals("4 - LIGAÇÃO", corpoPatch!!["U_Acao"])
        assertEquals("cliente pediu boleto novo", corpoPatch!!["U_Observacao"])
        assertEquals("2026-07-27", corpoPatch!!["U_DataAcao"], "quem cobrou por ultimo continua sendo a linha 3")
    }

    @Test
    fun `remover a ultima linha apaga o registro - titulo volta pra nao iniciado`() {
        // O escopo do vendedor e conferido antes de mexer no historico (mesma regra da leitura):
        // sem isso o mock default devolve SlpCode nulo e a remocao para no 403.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        // Registro vazio continuaria contando como titulo trabalhado no dashboard (que olha a
        // presenca em @COB_TITULO) e aparecendo nos filtros de rastreado sem nenhuma acao.
        val unicaLinha = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "9 - ACORDO ENVIADO",
        ).also { it.LineId = 1 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            historico = mutableListOf(unicaLinha),
        )

        val urls = mutableListOf<String>()
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.DELETE -> {
                        // request.url estoura pra RequestEntity montada com URI template (e o que
                        // EntitiesService.delete usa); o toString traz a URL crua do jeito que foi.
                        urls.add(request.toString())
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val historico = service.removerHistorico(cobradora, "NF", 500, 1, 1)

        assertTrue(historico.isEmpty())
        assertEquals(1, urls.size)
        assertTrue(
            urls.first().contains("/COB_TITULO('NF-500-1')"),
            "Code e alfanumerico: precisa ir entre aspas na URL, senao o SAP responde 400",
        )
    }

    @Test
    fun `cobrador nao remove linha de outro cobrador`() {
        // O escopo do vendedor e conferido antes de mexer no historico (mesma regra da leitura):
        // sem isso o mock default devolve SlpCode nulo e a remocao para no 403.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaDeOutro = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Nilvia", U_Cobrador = "Nilvia", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(linhaDeOutro))))
                    )
                    else -> throw AssertionError("Nao pode escrever nada: ${request.method}")
                }
            }

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
    }

    @Test
    fun `acesso total nao autoriza apagar o historico de outro cobrador`() {
        // Escopo (quem ve o titulo) e autoria (quem registrou a acao) sao coisas diferentes: o
        // historico e a prova do que foi combinado com o cliente, so o autor pode remover.
        val linhaDaNilvia = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Nilvia", U_Cobrador = "Nilvia", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(linhaDaNilvia))))
                    )
                    else -> throw AssertionError("Nao pode escrever nada: ${request.method}")
                }
            }

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(admin, "NF", 500, 1, 1)
        }
    }

    @Test
    fun `vendedor comum nao remove historico de titulo de outro vendedor`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1)).thenReturn(CobrancaTituloVendedorSap(SlpCode = 99))

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
        verify(restTemplate, never()).exchange(any<RequestEntity<*>>(), eq(OData::class.java))
    }

    @Test
    fun `linha que nao existe mais no historico para em CobrancaException, sem escrever no SAP`() {
        // O escopo do vendedor e conferido antes de mexer no historico (mesma regra da leitura):
        // sem isso o mock default devolve SlpCode nulo e a remocao para no 403.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenReturn(
                ResponseEntity.ok(
                    odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                        U_InstlmntID = 1, historico = mutableListOf())))
                )
            )

        assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 7)
        }
        // Corrida entre dois cobradores com o modal aberto: a segunda remocao nao pode mandar
        // PATCH nem DELETE pro SAP.
        verify(restTemplate, never()).exchange(
            org.mockito.kotlin.argThat<RequestEntity<*>> { method != HttpMethod.GET },
            eq(OData::class.java),
        )
    }

    @Test
    fun `autoria vale pelo id, nao pelo nome - homonimo nao apaga linha do outro`() {
        // Dois cobradores chamados igual existem de verdade no cadastro; antes o U_Usuario batia
        // e um apagava a acao do outro. Mesmo nome, SlpCode diferente: tem que barrar.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaDoOutroFulano = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "77", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(linhaDoOutroFulano))))
                    )
                    else -> throw AssertionError("Nao pode escrever nada: ${request.method}")
                }
            }

        assertThrows(ResponseStatusException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
    }

    @Test
    fun `cobrador renomeado no SAP continua removendo a propria linha`() {
        // O nome mudou no OSLP depois da acao; o id nao muda, entao a linha segue sendo dele.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaComNomeAntigo = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano", U_Cobrador = "Fulano",
            U_UsuarioId = "60", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        val urls = mutableListOf<String>()
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(linhaComNomeAntigo))))
                    )
                    HttpMethod.DELETE -> {
                        urls.add(request.toString())
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        assertTrue(service.removerHistorico(cobradora, "NF", 500, 1, 1).isEmpty())
        assertEquals(1, urls.size)
    }

    @Test
    fun `linha antiga sem id ainda e reconhecida pelo nome de quem escreveu`() {
        // Historico gravado antes do U_UsuarioId existir: travar pelo id deixaria tudo o que ja
        // esta no SAP sem poder ser removido por ninguem.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val linhaLegada = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(linhaLegada))))
                    )
                    HttpMethod.DELETE -> ResponseEntity.ok(OData())
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        assertTrue(service.removerHistorico(cobradora, "NF", 500, 1, 1).isEmpty())
    }

    @Test
    fun `a leitura do historico diz quais linhas o usuario pode remover`() {
        // A tela nao tem como decidir isso sozinha: no login por Keycloak o token do navegador
        // nao carrega o User.id do SAP.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val minha = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "9 - ACORDO ENVIADO",
        ).also { it.LineId = 2 }
        val deOutro = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Nilvia", U_Cobrador = "Nilvia",
            U_UsuarioId = "45", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenReturn(
                ResponseEntity.ok(
                    odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                        U_InstlmntID = 1, historico = mutableListOf(deOutro, minha))))
                )
            )

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
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val prometeuDia5 = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_DataPromessa = "2026-07-05", U_Observacao = "prometeu dia 5",
        ).also { it.LineId = 1 }
        val prometeuDia20 = CobrancaHistorico(
            U_Data = "2026-07-10", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_DataPromessa = "2026-07-20", U_Observacao = "remarcou pro dia 20",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_DataPromessa = "2026-07-20", historico = mutableListOf(prometeuDia5, prometeuDia20),
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        // O mock aplica a substituicao da colecao, como o SAP faz com o header
                        // B1S-ReplaceCollectionsOnPatch - a releitura do service confere isso.
                        @Suppress("UNCHECKED_CAST")
                        existente.historico = (corpoPatch!!["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertEquals("2026-07-05", corpoPatch!!["U_DataPromessa"])
    }

    @Test
    fun `sem promessa sobrando o cabecalho e limpo com null, nao com string vazia`() {
        // Campo db_Date: o Service Layer recusa "". E limpar importa - a view de promessa vencida
        // (cobranca-promessa-vencida.sql) le U_DataPromessa do cabecalho, entao data orfa mantem o
        // titulo no KPI de promessa vencida pra sempre.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val semPromessa = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Observacao = "ligou, sem compromisso",
        ).also { it.LineId = 1 }
        val prometeu = CobrancaHistorico(
            U_Data = "2026-07-10", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_DataPromessa = "2026-07-20",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_DataPromessa = "2026-07-20", historico = mutableListOf(semPromessa, prometeu),
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        // O mock aplica a substituicao da colecao, como o SAP faz com o header
                        // B1S-ReplaceCollectionsOnPatch - a releitura do service confere isso.
                        @Suppress("UNCHECKED_CAST")
                        existente.historico = (corpoPatch!!["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertTrue(corpoPatch!!.containsKey("U_DataPromessa"), "o campo tem que ir no PATCH pra ser limpo")
        assertEquals(null, corpoPatch!!["U_DataPromessa"])
    }

    @Test
    fun `remover linha que nao prometeu nada nao encosta na promessa do cabecalho`() {
        // Linha gravada antes do U_DataPromessa existir na UDT tem o campo nulo mesmo tendo
        // prometido de verdade: recompor sempre apagaria promessa legitima de registro antigo.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val legadaComPromessaNoCabecalho = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "8 - EM NEGOCIAÇÃO",
        ).also { it.LineId = 1 }
        val outraLegada = CobrancaHistorico(
            U_Data = "2026-07-10", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Observacao = "novo contato",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            U_DataPromessa = "2026-07-20",
            historico = mutableListOf(legadaComPromessaNoCabecalho, outraLegada),
        )

        var corpoPatch: Map<*, *>? = null
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        corpoPatch = request.body as Map<*, *>
                        // O mock aplica a substituicao da colecao, como o SAP faz com o header
                        // B1S-ReplaceCollectionsOnPatch - a releitura do service confere isso.
                        @Suppress("UNCHECKED_CAST")
                        existente.historico = (corpoPatch!!["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        assertFalse(corpoPatch!!.containsKey("U_DataPromessa"), "nao mexer e diferente de limpar")
    }

    @Test
    fun `SAP que ignora a remocao nao pode virar sucesso na tela`() {
        // Caso real: PATCH sem o header B1S-ReplaceCollectionsOnPatch responde 200 e mantem a
        // linha. A tela dizia "removido", a grade recarregava e o historico voltava inteiro.
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val primeira = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        val segunda = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "9 - ACORDO ENVIADO",
        ).also { it.LineId = 2 }
        // Mesmo registro nas duas leituras: o SAP nao apagou nada.
        val intacto = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            historico = mutableListOf(primeira, segunda),
        )

        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(intacto)))
                    HttpMethod.PATCH -> ResponseEntity.ok(OData())
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        val erro = assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 2)
        }
        assertTrue(erro.message!!.contains("continua"), "a mensagem tem que dizer que a linha ficou: ${erro.message}")
    }

    @Test
    fun `remocao grava a auditoria em COB_TITULO_LOG antes de apagar a linha`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val primeira = CobrancaHistorico(
            U_Data = "2026-07-01", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "3 - SEM CONTATO",
        ).also { it.LineId = 1 }
        val segunda = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "9 - ACORDO ENVIADO", U_Observacao = "linha errada",
        ).also { it.LineId = 2 }
        val existente = CobrancaRegistro(
            Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500, U_InstlmntID = 1,
            historico = mutableListOf(primeira, segunda),
        )
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(odataLista(listOf(existente)))
                    HttpMethod.PATCH -> {
                        @Suppress("UNCHECKED_CAST")
                        val corpo = request.body as Map<*, *>
                        existente.historico = (corpo["COB_TITULO_LCollection"] as List<CobrancaHistorico>).toMutableList()
                        ResponseEntity.ok(OData())
                    }
                    else -> throw AssertionError("Metodo inesperado: ${request.method}")
                }
            }

        service.removerHistorico(cobradora, "NF", 500, 1, 2)

        // Confere o conteudo e nao a instancia: a linha volta do OData desserializada de novo.
        val captor = org.mockito.kotlin.argumentCaptor<CobrancaHistorico>()
        verify(logService).registrarRemocao(eq("NF-500-1"), captor.capture(), eq(cobradora))
        assertEquals(2, captor.firstValue.LineId)
        assertEquals("9 - ACORDO ENVIADO", captor.firstValue.U_Status)
        assertEquals("linha errada", captor.firstValue.U_Observacao, "a auditoria precisa levar o conteudo apagado")
    }

    @Test
    fun `auditoria que falha impede a remocao - nada e apagado sem rastro`() {
        whenever(consultaService.buscarTituloParaEscopo("NF", 500, 1))
            .thenReturn(CobrancaTituloVendedorSap(SlpCode = 60, CardCode = "CLI0007196"))
        val unica = CobrancaHistorico(
            U_Data = "2026-07-27", U_Usuario = "Fulano de Tal", U_Cobrador = "Fulano de Tal",
            U_UsuarioId = "60", U_Status = "9 - ACORDO ENVIADO",
        ).also { it.LineId = 1 }
        whenever(restTemplate.exchange(any<RequestEntity<*>>(), eq(OData::class.java)))
            .thenAnswer { invocation ->
                val request = invocation.getArgument<RequestEntity<*>>(0)
                when (request.method) {
                    HttpMethod.GET -> ResponseEntity.ok(
                        odataLista(listOf(CobrancaRegistro(Code = "NF-500-1", U_Tipo = "NF", U_DocEntry = 500,
                            U_InstlmntID = 1, historico = mutableListOf(unica))))
                    )
                    else -> throw AssertionError("Nao pode apagar sem auditoria: ${request.method}")
                }
            }
        whenever(logService.registrarRemocao(any(), any(), any()))
            .thenThrow(CobrancaException("UDT de auditoria indisponivel"))

        assertThrows(CobrancaException::class.java) {
            service.removerHistorico(cobradora, "NF", 500, 1, 1)
        }
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
