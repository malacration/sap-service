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
    authService: AuthService
) : EntitiesService<Autorizador>(env, restTemplate, authService) {

    override fun path() = "/b1s/v1/autorizador"

    fun getTodos(): List<Autorizador> {
        return getAll(Autorizador::class.java)
    }

    fun criar(autorizador: Autorizador): Autorizador {
        return save(autorizador).tryGetValue()
    }

    fun remover(id: Int) {
        delete("'$id'")
    }

    fun podeAutorizar(motivo: String, usuario: String): Boolean {
        return getTodos().any { it.U_motivo == motivo && it.U_usuario == usuario }
    }
}
