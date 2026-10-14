package br.andrew.sap.services.journal.importacao

import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.OrderBy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.data.domain.Pageable

class ImportacaoLancamentoLogServiceTest {

    private fun pagina(vararg logs: Pair<String, String?>, proxima: Boolean = false) = OData(linkedMapOf<String, Any?>(
        "value" to logs.map { (code, status) -> mapOf("Code" to code, "U_Status" to status) },
    )).also { if (proxima) it["odata.nextLink"] = "LC_IMPORTACAO?\$skip=1" }

    /** Serve as paginas em ordem, sem Service Layer: o get abre a primeira, cada next a seguinte. */
    private class Paginado(private val paginas: List<OData>) : ImportacaoLancamentoLogService(mock(), mock(), mock()) {
        var lidas = 0
        override fun get(filter: Filter, order: OrderBy, page: Pageable): OData = paginas[0].also { lidas = 1 }
        override fun next(odata: OData): OData = paginas[lidas++]
    }

    @Test
    fun `historico pula as tentativas e pagina ate juntar as importacoes`() {
        // 50 tentativas recusadas na frente nao podem esconder o historico
        val logs = Paginado(listOf(
            pagina("E1" to "ENVIANDO", "R1" to "RECUSADO", "I1" to "IMPORTADO", proxima = true),
            pagina("I2" to null, "R2" to "RECUSADO", proxima = true),
            pagina("I3" to "IMPORTADO", proxima = true),
            pagina("I4" to "IMPORTADO"),
        ))
        // linha sem status e anterior ao campo e conta como importada
        assertEquals(listOf("I1", "I2", "I3"), logs.ultimasImportadas(3).map { it.Code })
        assertEquals(3, logs.lidas)
    }

    @Test
    fun `historico para na ultima pagina mesmo sem juntar a quantidade`() {
        val logs = Paginado(listOf(pagina("I1" to "IMPORTADO", "R1" to "RECUSADO")))
        assertEquals(listOf("I1"), logs.ultimasImportadas(50).map { it.Code })
    }

    @Test
    fun `consulta por hash le todas as paginas`() {
        // a importacao que entrou pode estar alem da primeira pagina (20 linhas, sem ordem)
        val logs = Paginado(listOf(
            pagina("R1" to "RECUSADO", "R2" to "RECUSADO", proxima = true),
            pagina("I1" to "IMPORTADO"),
        ))
        assertEquals(listOf("R1", "R2", "I1"), logs.porHash("abc").map { it.Code })
    }
}
