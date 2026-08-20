package br.andrew.sap.services.cobranca

import br.andrew.sap.model.cobranca.CobrancaTitulo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CobrancaTelefoneTest {

    // O fallback Phone1 -> Cellular fica em Kotlin porque COALESCE/IFNULL nao tem precedente
    // no parser do SQLQueries do SAP B1 (ver CobrancaTitulosSqlTest) - a view devolve as duas
    // colunas crua e a escolha acontece aqui.

    @Test
    fun `usa o telefone fixo quando o parceiro tem os dois preenchidos`() {
        assertEquals("6634211234", CobrancaTitulo.telefoneDeCobranca("6634211234", "66999998888"))
    }

    @Test
    fun `cai pro celular quando Phone1 vem vazio do OCRD`() {
        // OCRD guarda string vazia, nao null, pra campo nunca preenchido - por isso nao basta
        // testar nulidade.
        assertEquals("66999998888", CobrancaTitulo.telefoneDeCobranca("", "66999998888"))
        assertEquals("66999998888", CobrancaTitulo.telefoneDeCobranca("   ", "66999998888"))
        assertEquals("66999998888", CobrancaTitulo.telefoneDeCobranca(null, "66999998888"))
    }

    @Test
    fun `sem nenhum contato devolve nulo pro JsonInclude NON_EMPTY omitir o campo`() {
        assertNull(CobrancaTitulo.telefoneDeCobranca(null, null))
        assertNull(CobrancaTitulo.telefoneDeCobranca("", " "))
    }
}
