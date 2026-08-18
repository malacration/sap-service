package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaRegistro
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
    private val service = CobrancaService(env, restTemplate, authService, consultaService)

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

    private fun odataLista(value: List<Any?>): OData {
        val backing = LinkedHashMap<String, Any?>()
        backing["value"] = value
        return OData(backing)
    }

    private fun odataFlat(vararg pares: Pair<String, Any?>): OData {
        return OData(LinkedHashMap(pares.toMap()))
    }
}
