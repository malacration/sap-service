package br.andrew.sap.controllers.financeiro

import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoAutomaticoService
import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoLogService
import br.andrew.sap.services.financeiro.retorno.LancamentoRetornoService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

class LancamentoRetornoControllerTest {

    private val service: LancamentoRetornoService = mock(LancamentoRetornoService::class.java)
    private val automatico: LancamentoRetornoAutomaticoService = mock(LancamentoRetornoAutomaticoService::class.java)
    private val historico: LancamentoRetornoLogService = mock(LancamentoRetornoLogService::class.java)
    private val mvc = MockMvcBuilders.standaloneSetup(LancamentoRetornoController(service, automatico, historico)).build()
    private val usuario = UsernamePasswordAuthenticationToken("FRANCISCO", null)

    @Test
    fun retornoSemDescontoVolta404ComAMensagem() {
        //sem isso o null viraria 200 vazio e a tela nao saberia o que aconteceu
        listOf(get("/lancamento-retorno/retornos/1/previa"), post("/lancamento-retorno/retornos/1/lancar").principal(usuario)).forEach {
            mvc.perform(it)
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.mensagem").value("Retorno 1 sem ocorrencia de desconto no BankPlus."))
        }
        verify(service).lancar(1, "MANUAL", "FRANCISCO")
    }

    @Test
    fun listaConverteAsDatasDoFiltro() {
        mvc.perform(get("/lancamento-retorno/retornos").param("de", "2026-02-01").param("ate", "2026-02-28"))
            .andExpect(status().isOk)
        verify(service).listar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28))
    }

    @Test
    fun ligarOAutomaticoGuardaQuemLigou() {
        mvc.perform(put("/lancamento-retorno/automatico").principal(usuario)
            .contentType(MediaType.APPLICATION_JSON).content("""{"ativo": true}"""))
            .andExpect(status().isOk)
        verify(automatico).definir(true, "FRANCISCO")
    }

    @Test
    fun semAsTabelasNoSapAutomaticoVolta503SemCorpo() {
        //com corpo, o ErrorInterceptor do front abriria um alerta toda vez que a tela abre
        org.mockito.kotlin.whenever(automatico.estado()).thenThrow(RuntimeException("Service Not Found"))
        mvc.perform(get("/lancamento-retorno/automatico"))
            .andExpect(status().isServiceUnavailable)
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(""))
    }
}
