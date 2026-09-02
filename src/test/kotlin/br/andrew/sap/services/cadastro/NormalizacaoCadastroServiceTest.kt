package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.stock.ItemsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NormalizacaoCadastroServiceTest {

    private val itemsService = mock<ItemsService>()
    private val localidadeService = mock<LocalidadeService>()
    private val businessPartnersService = mock<BusinessPartnersService>()

    private val sqlQueriesService = mock<SqlQueriesService>()

    private val service = NormalizacaoCadastroService(
        sqlQueriesService, itemsService, localidadeService, businessPartnersService, "PAC")

    //A view ja devolve so as divergencias, nos tres cadastros, no mesmo formato.
    private fun cenario(vararg linhas: Triple<String, String, String>) {
        val valores = linhas.map { mapOf("tipo" to it.first, "codigo" to it.second, "nome" to it.third) }
        whenever(sqlQueriesService.execute(any<String>(), any<List<Parameter>>()))
            .doReturn(OData(linkedMapOf("value" to OData().mapper.writeValueAsString(valores))))
    }

    private fun item(codigo: String, nome: String) = Triple("item", codigo, nome)
    private fun localidade(codigo: String, nome: String) = Triple("localidade", codigo, nome)
    private fun cliente(codigo: String, nome: String) = Triple("cliente", codigo, nome)

    @Test
    fun `acha nome com minuscula nos tres cadastros`() {
        cenario(
            item("PAC0000069", "Ox Beef"),
            localidade("20", "Manicore"),
            cliente("CLI001", "Mauro Carreta"))

        val previa = service.previa()

        assertEquals(3, previa.size)
        assertEquals("OX BEEF", previa.single { it.tipo == "item" }.novo)
        assertEquals("MANICORE", previa.single { it.tipo == "localidade" }.novo)
        assertEquals("MAURO CARRETA", previa.single { it.tipo == "cliente" }.novo)
    }

    @Test
    fun `ignora quem ja esta em maiusculo`() {
        //a view nao deveria devolver esses, mas se o UPPER do HANA discordar do Java a
        //reconferencia no Kotlin descarta - o banco e filtro, nao autoridade
        cenario(
            cliente("CLI001", "MAURO CARRETA"),
            cliente("CLI002", "FRISACRE LTDA"))

        assertTrue(service.previa().isEmpty())
    }

    /** So caixa alta: acento e pontuacao ficam como estao. */
    @Test
    fun `mantem acento e pontuacao`() {
        cenario(cliente("CLI001", "Jose D'Avila & Cia Ltda."))

        assertEquals("JOSE D'AVILA & CIA LTDA.", service.previa().single().novo)
    }

    @Test
    fun `previa nao grava nada`() {
        cenario(cliente("CLI001", "Mauro Carreta"))

        service.previa()

        verify(businessPartnersService, never()).update(any(), any<String>())
    }

    /** O PATCH leva so o campo do nome - mandar a entidade inteira derruba o Service Layer. */
    @Test
    fun `aplicar envia patch minimo com o campo do nome`() {
        cenario(cliente("CLI001", "Mauro Carreta"))

        service.aplicar()

        val corpo = argumentCaptor<Any>()
        val id = argumentCaptor<String>()
        verify(businessPartnersService).update(corpo.capture(), id.capture())
        assertEquals("""{"CardName":"MAURO CARRETA"}""", corpo.firstValue)
        assertEquals("'CLI001'", id.firstValue, "chave de texto tem que ir entre aspas")
    }

    /** Nome com aspas quebraria um JSON concatenado a mao. */
    @Test
    fun `aplicar escapa o json do nome`() {
        cenario(cliente("CLI001", """Posto "Bom Dia" Ltda"""))

        service.aplicar()

        val corpo = argumentCaptor<Any>()
        verify(businessPartnersService).update(corpo.capture(), any<String>())
        assertEquals("""{"CardName":"POSTO \"BOM DIA\" LTDA"}""", corpo.firstValue)
    }

    /**
     * Localidade tem codigo numerico mas chave alfanumerica no UDO. Sem as aspas o update saia
     * como Locais(230) e o Service Layer recusava com "203 - Error in query syntax".
     */
    @Test
    fun `codigo numerico de localidade vai entre aspas`() {
        cenario(localidade("230", "Manicore"))

        service.aplicar()

        val id = argumentCaptor<String>()
        verify(localidadeService).update(any(), id.capture())
        assertEquals("'230'", id.firstValue)
    }

    /**
     * Falha num cadastro nao pode abortar os outros: sao milhares independentes, e parar no
     * primeiro erro deixaria a base pela metade sem relatorio do que passou.
     */
    @Test
    fun `falha em um cadastro nao interrompe os demais`() {
        cenario(
            localidade("20", "Manicore"),
            cliente("CLI001", "Mauro Carreta"))
        whenever(localidadeService.update(any(), any<String>())).doThrow(RuntimeException("Locais(20) travado"))

        val resultado = service.aplicar()

        val localidade = resultado.single { it.tipo == "localidade" }
        val cliente = resultado.single { it.tipo == "cliente" }
        assertFalse(localidade.aplicado!!)
        assertEquals("Locais(20) travado", localidade.erro)
        assertTrue(cliente.aplicado!!, "o cliente tinha que ser processado mesmo com a localidade falhando")
    }
}
