package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sistema.Autorizador
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

//tabela de roteamento motivo -> usuario (cadastro simples, mesmo espirito do
//LiberaPara da Comissao)
@Service
class AutorizadorService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val regraService: RegraAutorizacaoService
) : EntitiesService<Autorizador>(env, restTemplate, authService) {

    override fun path() = "/b1s/v1/autorizador"

    fun getTodos(): List<Autorizador> {
        return getAll(Autorizador::class.java)
    }

    /**
     * O Code e gerado aqui: @AUTORIZADOR e bott_MasterData e o service layer recusa POST sem
     * codigo ("Enter valid code [@AUTORIZADOR.Code]"), mesmo com ManageSeries ligado no UDO -
     * a numeracao automatica so vale quando existe uma serie configurada no SAP para o objeto.
     * Gerar aqui deixa o cadastro funcionando sem depender de configuracao no cliente SAP.
     *
     * Codigo em sequencia sobre os existentes. A tabela e de baixo volume (roteamento
     * motivo -> usuario), entao a corrida entre dois cadastros simultaneos e improvavel - e se
     * acontecer o proprio SAP recusa o codigo duplicado, que e uma falha limpa.
     */
    fun criar(autorizador: Autorizador): Autorizador {
        if(autorizador.U_motivo.isBlank() || autorizador.U_usuario.isBlank())
            throw Exception("Motivo e usuario sao obrigatorios para cadastrar um autorizador")

        //O select da tela ja oferece so motivo valido, mas a trava tem que estar aqui: motivo
        //que nenhuma regra produz gera autorizador inutil, e o documento fica pendente sem
        //ninguem que possa aprovar - falha silenciosa, que so aparece quando alguem trava.
        val motivosValidos = regraService.motivos()
        if(!motivosValidos.contains(autorizador.U_motivo))
            throw Exception("Motivo '${autorizador.U_motivo}' nao existe no motor de regras. " +
                "Motivos disponiveis: ${motivosValidos.joinToString(", ")}")

        val existentes = getTodos()
        if(existentes.any { it.U_motivo == autorizador.U_motivo && it.U_usuario == autorizador.U_usuario })
            throw Exception("O usuario ${autorizador.U_usuario} ja e autorizador do motivo ${autorizador.U_motivo}")

        autorizador.Code = proximoCodigo(existentes)
        //Name espelha o Code de proposito: em UDT o Name costuma ser unico, e montar a partir de
        //motivo+usuario colidiria assim que dois cadastros truncassem no mesmo texto.
        autorizador.Name = autorizador.Code
        return save(autorizador).tryGetValue()
    }

    private fun proximoCodigo(existentes: List<Autorizador>): String {
        return ((existentes.mapNotNull { it.Code?.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
    }

    fun remover(id: String) {
        delete("'$id'")
    }

    fun podeAutorizar(motivo: String, usuario: String): Boolean {
        return getTodos().any { it.U_motivo == motivo && it.U_usuario == usuario }
    }
}
