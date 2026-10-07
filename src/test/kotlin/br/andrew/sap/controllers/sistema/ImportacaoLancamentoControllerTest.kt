package br.andrew.sap.controllers.sistema

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoException
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoService
import br.andrew.sap.services.journal.importacao.LancamentoCsvParser
import br.andrew.sap.services.journal.importacao.PreviaImportacaoLancamento
import br.andrew.sap.services.journal.importacao.ResultadoImportacaoLancamento
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class ImportacaoLancamentoControllerTest {

    private val service = mock<ImportacaoLancamentoService>()
    private val mvc = MockMvcBuilders.standaloneSetup(ImportacaoLancamentoController(service)).build()
    private val csv = "Filial;;Data\r\n2;;05/10/2026;D;C;10,00;H;;".toByteArray(charset("windows-1252"))

    private fun previa(): PreviaImportacaoLancamento {
        val leitura = LancamentoCsvParser.ler(csv)
        return PreviaImportacaoLancamento("a.csv", "h", leitura.codificacao, leitura.linhas, leitura.erros, emptyList())
    }

    @Test
    fun `validar recebe os bytes crus e devolve a data como texto`() {
        whenever(service.validar(any(), anyOrNull())).thenReturn(previa())
        mvc.perform(post("/journal/importacao/validar").param("arquivo", "a.csv").contentType("text/csv").content(csv))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.linhas[0].data").value("2026-10-05"))
            .andExpect(jsonPath("$.linhas[0].dataLancamento").doesNotExist())
            .andExpect(jsonPath("$.totalLinhas").value(1))
        verify(service).validar(argThat { contentEquals(csv) }, eq("a.csv"))
    }

    @Test
    fun `arquivo de zero bytes vira previa e nao erro 500`() {
        whenever(service.validar(any(), anyOrNull())).thenReturn(previa())
        mvc.perform(post("/journal/importacao/validar").contentType("text/csv"))
            .andExpect(status().isOk)
        verify(service).validar(argThat { isEmpty() }, isNull())
    }

    @Test
    fun `importar usa o usuario logado e recusa com o status e a mensagem do servico`() {
        val usuario = User("60", "Fulano", UserOriginEnum.SalePerson, "fulano", "", "", listOf(), listOf("contabil_importacao"))
        whenever(service.importar(any(), anyOrNull(), any(), any(), any(), any()))
            .thenReturn(ResultadoImportacaoLancamento("X", "IMPORTADO", listOf(101), "ok"))
        mvc.perform(post("/journal/importacao").principal(usuario).param("hash", "h").contentType("text/csv").content(csv))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.lancamentos[0]").value(101))
        verify(service).importar(any(), isNull(), eq("h"), eq(false), eq("Fulano"), eq("SalePerson:60"))

        whenever(service.importar(any(), anyOrNull(), any(), any(), any(), any()))
            .thenThrow(ImportacaoLancamentoException(HttpStatus.UNPROCESSABLE_ENTITY, "O arquivo tem erros.", previa()))
        mvc.perform(post("/journal/importacao").principal(usuario).param("hash", "h").contentType("text/csv").content(csv))
            .andExpect(status().isUnprocessableEntity)
            .andExpect(jsonPath("$.mensagem").value("O arquivo tem erros."))
            .andExpect(jsonPath("$.previa.linhas[0].linha").value(2))
    }
}
