package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.nio.file.Files
import java.nio.file.Path

class InadimplenciaRetiradaVendaFuturaTest {

    private val sqlQueriesService = mock<SqlQueriesService>()
    private val service = ContratoVendaFuturaService(mock(), mock(), sqlQueriesService, mock(), mock())

    @Test
    fun `considera inadimplente quando existe titulo vencido do contrato`() {
        doReturn(odataComTitulo()).`when`(sqlQueriesService)
            .execute(any<String>(), any<List<Parameter>>())

        assertTrue(service.temTituloVencidoNoContrato("CLI001", 42))

        val nome = argumentCaptor<String>()
        val parametros = argumentCaptor<List<Parameter>>()
        verify(sqlQueriesService).execute(nome.capture(), parametros.capture())
        assertTrue(nome.firstValue == "cliente-em-atraso-venda-futura.sql")
        assertTrue(parametros.firstValue.any { it.toString() == "cardCode='CLI001'" })
        assertTrue(parametros.firstValue.any { it.toString() == "idContrato=42" })
    }

    @Test
    fun `permite retirada quando contrato nao tem titulo vencido`() {
        doReturn(OData(linkedMapOf("value" to "[]"))).`when`(sqlQueriesService)
            .execute(any<String>(), any<List<Parameter>>())

        assertFalse(service.temTituloVencidoNoContrato("CLI001", 42))
    }

    @Test
    fun `nao consulta nem bloqueia quando funcionalidade esta desativada`() {
        val consulta = mock<SqlQueriesService>()
        val desativado = ContratoVendaFuturaService(mock(), mock(), consulta, mock(), mock(), false)

        assertFalse(desativado.temTituloVencidoNoContrato("CLI001", 42))
        verifyNoInteractions(consulta)
    }

    @Test
    fun `sql restringe o titulo ao adiantamento do contrato atual`() {
        val sql = Files.readString(
            Path.of("src/main/resources/views/cliente-em-atraso-venda-futura.sql")
        )

        assertTrue(sql.contains("ODPI dp"))
        assertTrue(sql.contains("INNER JOIN DPI6 p"))
        assertTrue(sql.contains("dp.\"U_venda_futura\" = :idContrato"))
        assertTrue(sql.contains("dp.\"CANCELED\" = 'N'"))
        assertTrue(sql.contains("dp.\"CardCode\" = :cardCode"))
        assertTrue(sql.contains("p.\"Status\" = 'O'"))
        assertTrue(sql.contains("p.\"DueDate\" <= :dataLimite"))
        assertFalse(sql.contains("--"))
        assertFalse(sql.contains("/*"))
    }

    private fun odataComTitulo() = OData(linkedMapOf(
        "value" to """[{"TransId":123,"ShortName":"CLI001","DueDate":"20260901"}]"""
    ))
}
