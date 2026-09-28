package br.andrew.sap.services.comercial.sanitizacao

import br.andrew.sap.controllers.comercial.SanitizacaoContratoController
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.QueryResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.web.server.ResponseStatusException

class SanitizacaoContratoConsultaTest {
    private val odbc = mock<OdbcClient>()
    private val consulta = SanitizacaoContratoConsulta(odbc, 7, "CONTROLE")

    @Test fun `decimal ODBC texto e conciliacao da linha um nao fecha a linha zero`() {
        whenever(odbc.consultar(any(), any(), any())).thenAnswer { invocation ->
            val sql = invocation.arguments[0] as String
            val rows = when {
                sql.startsWith("WITH") -> listOf(mapOf(
                    "TransId" to 10, "DocEntry" to 20, "DocNum" to 30, "Contrato" to 40,
                    "BPLId" to 1, "CardCode" to "C1", "CardName" to "Cliente", "DocTotal" to "100.000000",
                    "CANCELED" to "Y", "DevolucaoEntry" to null, "ReclassificacoesOrigem" to 1,
                    "DevolucaoIntegral" to 0, "EstornosDevolucao" to 0,
                ))
                sql.contains("FROM \"JDT1\"") -> listOf(0, 1).map { linha -> mapOf(
                    "TransId" to 10, "Line_ID" to linha, "Account" to if (linha == 0) "CLIENTES" else "CONTROLE",
                    "ShortName" to "C1", "BPLId" to 1, "Debit" to if (linha == 1) "100.000000" else "0.000000",
                    "Credit" to if (linha == 0) "100.000000" else "0.000000",
                    "BalDueDeb" to "0.000000", "BalDueCred" to if (linha == 0) "100.000000" else "0.000000",
                    "FCDebit" to "0.000000", "FCCredit" to "0.000000", "BalFcDeb" to "0.000000", "BalFcCred" to "0.000000",
                ) }
                sql.contains("FROM \"OITR\"") -> listOf(mapOf(
                    "ReconNum" to 9, "ReconType" to 0, "ReconDate" to "2026-01-01", "TransId" to 10,
                    "TransRowId" to 1, "SrcObjTyp" to 30, "SrcObjAbs" to 10, "ReconSum" to "100.000000",
                ), mapOf(
                    "ReconNum" to 8, "ReconType" to 16, "ReconDate" to "2026-01-01", "TransId" to 10,
                    "TransRowId" to 5, "SrcObjTyp" to 203, "SrcObjAbs" to 5, "ReconSum" to "100.000000",
                ))
                else -> emptyList()
            }
            QueryResponse(rows = rows)
        }
        val item = consulta.buscar().single()
        assertTrue(item.podeAplicar)
        assertEquals(emptyList<Int>(), item.pernas[0].reconciliacoes)
        assertEquals(listOf(9), item.pernas[1].reconciliacoes)
        assertEquals(0, item.nota.valor.compareTo("100".toBigDecimal()))
        // A de adiantamento (tipo 16) aparece na prévia, mas não é cancelada explicitamente.
        assertEquals(setOf(8, 9), item.reconciliacoes.map { it.numero }.toSet())
        assertEquals(listOf("Cancelar reconciliação interna 9", "Estornar reclassificação 10"), item.acoes)
    }

    @Test fun `sucesso exige estorno e cancelamento na data dos originais`() {
        // Caso real do contrato 108 em HMG: o Cancel lançou na data do original, não na de hoje.
        val rows = listOf(
            mapOf("Tipo" to "ESTORNO", "Id" to 11, "Data" to "2026-01-13T00:00", "Original" to "2026-01-13T00:00"),
            mapOf("Tipo" to "APROPRIACAO", "Id" to 51, "Data" to "2026-06-16T00:00", "Original" to "2026-06-16T00:00"),
        )
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = rows))
        val resultado = consulta.verificar(exemploSanitizacao())
        assertEquals("APLICADO", resultado.status)
        assertEquals(11, resultado.estorno)
        assertEquals(listOf(51), resultado.cancelamentos)
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = listOf(
            rows[0], rows[1] + ("Data" to "2026-09-28T00:00"))))
        assertEquals("CONFERIR", consulta.verificar(exemploSanitizacao()).status)
    }

    @Test fun `resposta vazia ou reconciliacao ainda ativa nao e sucesso`() {
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse())
        assertEquals("CONFERIR", consulta.verificar(exemploSanitizacao()).status)
        whenever(odbc.consultar(any(), any(), any())).thenReturn(QueryResponse(rows = listOf(
            mapOf("Tipo" to "ESTORNO", "Id" to 11, "Data" to "2026-09-24", "Original" to "2026-09-24"),
            mapOf("Tipo" to "APROPRIACAO", "Id" to 51, "Data" to "2026-09-24", "Original" to "2026-09-24"),
            mapOf("Tipo" to "PENDENCIA", "Id" to 9, "Data" to "2026-09-24", "Original" to null),
        )))
        assertEquals("CONFERIR", consulta.verificar(exemploSanitizacao()).status)
    }
}

class SanitizacaoContratoControllerTest {
    @Test fun `ambas rotas exigem administrador inclusive acesso direto`() {
        val service = mock<SanitizacaoContratoService>()
        val controller = SanitizacaoContratoController(service)
        val user = User("1", "Vendedor", UserOriginEnum.SalePerson, "vendedor", bussinesPlace = listOf(1), roles = listOf("vendedor"))
        assertEquals(403, assertThrows(ResponseStatusException::class.java) { controller.previa(user) }.statusCode.value())
        assertEquals(403, assertThrows(ResponseStatusException::class.java) {
            controller.aplicar(user, AplicarSanitizacao("token", 10))
        }.statusCode.value())
        verifyNoInteractions(service)
    }
}
