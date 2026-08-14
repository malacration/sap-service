package br.andrew.sap.controllers.autorizacao

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sistema.Autorizacao
import br.andrew.sap.services.autorizacao.AutorizacaoService
import br.andrew.sap.services.autorizacao.AutorizadorService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("autorizacao")
class AutorizacaoController(
    val service: AutorizacaoService,
    val autorizadorService: AutorizadorService
) {

    //fila do usuario logado - so os pendentes que ele tem permissao (via
    //AutorizadorService) pra decidir
    @GetMapping("pendentes")
    fun getPendentes(auth: Authentication): List<Autorizacao> {
        val usuario = (auth as User).userName
        return service.getTodas()
            .filter { it.U_status == "PENDENTE" }
            .filter { autorizadorService.podeAutorizar(it.U_motivo, usuario) }
    }

    //historico completo, pra auditoria
    @GetMapping("")
    fun get(): List<Autorizacao> {
        return service.getTodas()
    }

    @GetMapping("{id}")
    fun getById(@PathVariable id: Int): Autorizacao {
        return service.get(id)
    }

    @PostMapping("{id}/aprovar")
    fun aprovar(@PathVariable id: Int, auth: Authentication): ResponseEntity<Any> {
        val usuario = (auth as User).userName
        val autorizacao = service.get(id)
        if (!autorizadorService.podeAutorizar(autorizacao.U_motivo, usuario))
            return ResponseEntity.status(403).body("Você não está autorizado a decidir sobre o motivo '${autorizacao.U_motivo}'")
        return ResponseEntity.ok(service.aprovar(id, usuario))
    }

    @PostMapping("{id}/rejeitar")
    fun rejeitar(@PathVariable id: Int, @RequestBody(required = false) observacao: String?, auth: Authentication): ResponseEntity<Any> {
        val usuario = (auth as User).userName
        val autorizacao = service.get(id)
        if (!autorizadorService.podeAutorizar(autorizacao.U_motivo, usuario))
            return ResponseEntity.status(403).body("Você não está autorizado a decidir sobre o motivo '${autorizacao.U_motivo}'")
        return ResponseEntity.ok(service.rejeitar(id, usuario, observacao))
    }
}
