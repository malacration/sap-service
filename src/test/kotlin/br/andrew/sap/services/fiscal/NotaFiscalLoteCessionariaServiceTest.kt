package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.CessionariaEditRequest
import br.andrew.sap.model.fiscal.CessionariaInvalidaException
import br.andrew.sap.model.fiscal.CessionariaRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.exceptions.SapGenericException
import br.andrew.sap.model.fiscal.CessionariaUdo
import br.andrew.sap.model.sap.sistema.ErrorMsg
import br.andrew.sap.model.sap.sistema.SapError
import br.andrew.sap.model.sap.sistema.SapMessage
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus

class NotaFiscalLoteCessionariaServiceTest {

    private fun erro(r: CessionariaRequest) =
        assertThrows(CessionariaInvalidaException::class.java) { NotaFiscalLoteCessionariaService.validar(r) }

    @Test
    fun `monta a linha do UDO com Code so digitos, CNPJ formatado e ativa`() {
        val u = NotaFiscalLoteCessionariaService.validar(CessionariaRequest("  Securitizadora X ", "11.222.333/0001-81", " (65) 3000-0000 ", " Securitizadora X Ltda "))
        assertEquals("11222333000181", u.Code)
        assertEquals("Securitizadora X", u.Name)
        assertEquals("11.222.333/0001-81", u.U_Cnpj)
        assertEquals("(65) 3000-0000", u.U_Telefone)
        assertEquals("Securitizadora X Ltda", u.U_NomePdf)
        assertEquals("Y", u.U_Ativo)
    }

    @Test
    fun `telefone vazio vira nulo e CNPJ aceita so digitos`() {
        val u = NotaFiscalLoteCessionariaService.validar(CessionariaRequest("X", "11222333000181", "  ", "X Ltda"))
        assertEquals(null, u.U_Telefone)
        assertEquals("11.222.333/0001-81", u.U_Cnpj)
    }

    @Test
    fun `rejeita nome e CNPJ ausentes ou invalidos com 400`() {
        listOf(
            CessionariaRequest(null, "11222333000181", null, "X Ltda"),
            CessionariaRequest("  ", "11222333000181", null, "X Ltda"),
            CessionariaRequest("X", null, null, "X Ltda"),
            CessionariaRequest("X", "11222333000182", null, "X Ltda"),
            CessionariaRequest("X", "11111111111111", null, "X Ltda"),
            CessionariaRequest("X", "123", null, "X Ltda"),
            CessionariaRequest("X", "11222333000181", "1".repeat(21), "X Ltda"),
            CessionariaRequest("X", "11222333000181", null, null),
            CessionariaRequest("X", "١١٢٢٢٣٣٣٠٠٠١٨١", null, "X Ltda"),
            CessionariaRequest("X", "11.222.333/0001-81abc", null, "X Ltda"),
            CessionariaRequest("X", "CNPJ 11222333000181", null, "X Ltda"),
            CessionariaRequest("X", "11222333000181", null, "   "),
            CessionariaRequest("X", "11222333000181", null, "n".repeat(255)),
        ).forEach { assertEquals(HttpStatus.BAD_REQUEST, erro(it).status, it.cnpj) }
    }

    @Test
    fun `digito verificador do CNPJ`() {
        assertTrue(NotaFiscalLoteCessionariaService.cnpjValido("11222333000181"))
        assertFalse(NotaFiscalLoteCessionariaService.cnpjValido("11222333000180"))
    }

    private fun servico(falha: Exception?) = object : NotaFiscalLoteCessionariaService(mock(), mock(), mock()) {
        override fun get(filter: Filter, order: OrderBy, page: Pageable) = OData().also { it["value"] = listOf<Any>() }
        override fun save(entry: CessionariaUdo): OData { falha?.let { throw it }; return OData() }
    }
    private val pedido = CessionariaRequest("X", "11.222.333/0001-81", null, "X Ltda")

    @Test
    fun `chave repetida no save do SAP vira 409, outro erro do SAP segue como esta`() {
        fun sapErro(msg: String, codigo: String = "-2035") = SapGenericException(SapError(ErrorMsg(codigo, SapMessage("pt", msg))))
        val conflito = assertThrows(CessionariaInvalidaException::class.java) {
            servico(sapErro("This entry already exists in the following tables")).criar(pedido)
        }
        assertEquals(HttpStatus.CONFLICT, conflito.status)
        // codigo -2035 vale mesmo com a mensagem em outro idioma
        assertEquals(HttpStatus.CONFLICT, assertThrows(CessionariaInvalidaException::class.java) {
            servico(sapErro("Esta entrada ja foi registrada nas tabelas a seguir")).criar(pedido)
        }.status)
        // sem o codigo, o texto continua como reserva
        assertEquals(HttpStatus.CONFLICT, assertThrows(CessionariaInvalidaException::class.java) {
            servico(sapErro("Duplicate key value", codigo = "-1")).criar(pedido)
        }.status)
        // erro sem relacao: o mesmo erro do SAP segue (mesmo com "ja existe" so no codigo errado)
        val outro = sapErro("Field cannot be updated", codigo = "-5002")
        assertSame(outro, assertThrows(SapGenericException::class.java) { servico(outro).criar(pedido) })
        assertEquals("11222333000181", servico(null).criar(pedido).codigo)
    }

    // editar: o get devolve a linha existente (ou nada); o update (final em EntitiesService) e stubado no spy.
    private fun edicao(existente: CessionariaUdo?) = spy(NotaFiscalLoteCessionariaService(mock(), mock(), mock())).also { s ->
        doReturn(OData().also { it["value"] = listOfNotNull(existente) }).whenever(s).get(any<Filter>(), any<OrderBy>(), any<Pageable>())
        doReturn(null).whenever(s).update(any(), any())
    }
    private val existente = CessionariaUdo("11222333000181", "Antigo", "11.222.333/0001-81", "(65) 3000-0000", "Antigo Ltda", "Y")

    @Test
    fun `editar grava so Name e NomePdf no Code e devolve a cessionaria atualizada`() {
        val s = edicao(existente)
        val r = s.editar("11222333000181", CessionariaEditRequest(" Novo ", " Novo Ltda "))
        verify(s).update(mapOf("Name" to "Novo", "U_NomePdf" to "Novo Ltda"), "'11222333000181'")
        assertEquals("11222333000181", r.codigo)
        assertEquals("Novo", r.nome)
        assertEquals("(65) 3000-0000", r.telefone)
        assertEquals("Novo Ltda", r.nomePdf)
    }

    @Test
    fun `editar com nomePdf vazio ou nulo grava vazio para o termo cair no nome`() {
        listOf("  ", null).forEach {
            val s = edicao(existente)
            val r = s.editar("11222333000181", CessionariaEditRequest("Novo", it))
            verify(s).update(mapOf("Name" to "Novo", "U_NomePdf" to ""), "'11222333000181'")
            assertEquals("", r.nomePdf)
        }
    }

    @Test
    fun `editar sem a chave nomePdf nao envia U_NomePdf e devolve o gravado`() {
        val s = edicao(existente)
        val r = s.editar("11222333000181", CessionariaEditRequest(" Novo "))
        verify(s).update(mapOf("Name" to "Novo"), "'11222333000181'")
        assertEquals("Antigo Ltda", r.nomePdf)
    }

    @Test
    fun `corpo JSON distingue nomePdf ausente, null e texto`() {
        val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        fun le(json: String) = mapper.readValue(json, CessionariaEditRequest::class.java)
        assertFalse(le("""{"nome":"Novo"}""").nomePdfInformado)
        assertTrue(le("""{"nome":"Novo","nomePdf":null}""").nomePdfInformado)
        assertEquals(null, le("""{"nome":"Novo","nomePdf":null}""").nomePdf)
        assertEquals(" X ", le("""{"nome":"Novo","nomePdf":" X "}""").nomePdf)
        assertTrue(le("""{"nome":"Novo","nomePdf":" X "}""").nomePdfInformado)
    }

    // Transporte HTTP mockado (RestTemplate): confere o URL/chave e o corpo que seguiriam no PATCH.
    @Test
    fun `editar CNPJ com zeros a esquerda envia a chave como string entre aspas`() {
        val rest = mock<org.springframework.web.client.RestTemplate>()
        val enviados = mutableListOf<org.springframework.http.RequestEntity<*>>()
        whenever(rest.exchange(any<org.springframework.http.RequestEntity<*>>(), any<Class<OData>>())).thenAnswer {
            enviados.add(it.arguments[0] as org.springframework.http.RequestEntity<*>)
            org.springframework.http.ResponseEntity.ok(OData())
        }
        val env = br.andrew.sap.model.sistema.SapEnvrioment("http://host-falso", "u", "p", "db")
        val servico = object : NotaFiscalLoteCessionariaService(env, rest, br.andrew.sap.mock.Mocks.getAuthService()) {
            override fun get(filter: Filter, order: OrderBy, page: Pageable) = OData().also {
                it["value"] = listOf(CessionariaUdo("00000000000191", "Antigo", "00.000.000/0001-91", null, null, "Y"))
            }
        }
        servico.editar("00000000000191", CessionariaEditRequest("Novo", "Novo Ltda"))
        val patch = enviados.single { it.method == org.springframework.http.HttpMethod.PATCH }
        assertEquals("http://host-falso" + servico.path() + "('00000000000191')", (patch as org.springframework.http.RequestEntity.UriTemplateRequestEntity<*>).uriTemplate)
        assertEquals(mapOf("Name" to "Novo", "U_NomePdf" to "Novo Ltda"), patch.body)
    }

    @Test
    fun `editar rejeita nome vazio e textos acima do limite com 400, sem gravar`() {
        listOf(
            CessionariaEditRequest(null, "X"),
            CessionariaEditRequest("  ", "X"),
            CessionariaEditRequest("n".repeat(101), "X"),
            CessionariaEditRequest("X", "n".repeat(255)),
        ).forEach {
            val s = edicao(existente)
            val e = assertThrows(CessionariaInvalidaException::class.java) { s.editar("11222333000181", it) }
            assertEquals(HttpStatus.BAD_REQUEST, e.status)
            verify(s, never()).update(any(), any())
        }
    }

    @Test
    fun `editar codigo inexistente ou que nao e CNPJ so digitos responde 404, sem gravar`() {
        listOf("11222333000181" to null, "abc" to existente, "" to existente, "1' or '1'='1" to existente).forEach { (codigo, linha) ->
            val s = edicao(linha)
            val e = assertThrows(CessionariaInvalidaException::class.java) { s.editar(codigo, CessionariaEditRequest("X", "X")) }
            assertEquals(HttpStatus.NOT_FOUND, e.status, codigo)
            verify(s, never()).update(any(), any())
        }
    }
}
