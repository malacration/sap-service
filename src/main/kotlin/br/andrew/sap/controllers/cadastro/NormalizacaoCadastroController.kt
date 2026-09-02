package br.andrew.sap.controllers.cadastro

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.cadastro.CadastroParaNormalizar
import br.andrew.sap.services.cadastro.NormalizacaoCadastroService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Rotina administrativa que passa para maiusculo o nome de produto, localidade e cliente.
 *
 * Dois passos separados de proposito: a previa mostra o que mudaria sem gravar nada, e so depois
 * de conferir a pessoa aplica. Sao milhares de cadastros e nao ha desfazer.
 */
@RestController
@RequestMapping("normalizacao-cadastro")
class NormalizacaoCadastroController(val service: NormalizacaoCadastroService) {

    @GetMapping("previa")
    fun previa(auth: Authentication): List<CadastroParaNormalizar> {
        exigirAdmin(auth)
        return service.previa()
    }

    @PostMapping("aplicar")
    fun aplicar(auth: Authentication): List<CadastroParaNormalizar> {
        exigirAdmin(auth)
        return service.aplicar()
    }

    //O rules.yml ja da /** so pro admin, mas a checagem aqui deixa a regra explicita junto do
    //endpoint - e uma rotina de escrita em massa, nao da pra depender de leitura de config.
    //Mesma mensagem que o RoleBasedAuthorizationFilter usa.
    private fun exigirAdmin(auth: Authentication) {
        if (auth !is User || "admin" !in auth.roles)
            throw Exception("Você não tem permissão para acessar este recurso.")
    }
}
