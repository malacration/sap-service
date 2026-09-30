package br.andrew.sap.controllers.authentication

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.acesso.EndpointConhecido
import br.andrew.sap.services.security.acesso.EstadoAtual
import br.andrew.sap.services.security.acesso.PreviaImportacao
import br.andrew.sap.services.security.acesso.RegraDoc
import br.andrew.sap.services.security.acesso.RegrasAcessoNegadoException
import br.andrew.sap.services.security.acesso.RegrasAcessoService
import br.andrew.sap.services.security.acesso.RegrasConflitoException
import br.andrew.sap.services.security.acesso.RegrasDocumento
import br.andrew.sap.services.security.acesso.RegrasNaoEncontradasException
import br.andrew.sap.services.security.acesso.RegrasValidacaoException
import br.andrew.sap.services.security.acesso.ResultadoSimulacao
import br.andrew.sap.services.security.acesso.VersaoCompleta
import br.andrew.sap.services.security.acesso.VersaoResumo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

/**
 * Gestao das regras de acesso (o que antes era o rules.yml). So existe com `fields.acesso=true`.
 *
 * Apenas o perfil `admin` chega aqui: o filtro e default-deny e so o admin tem o curinga. Cada
 * rota confere isso de novo (`exigirAdmin`) para a regra ficar explicita junto do endpoint, e
 * porque um perfil que um dia receba um curinga por engano nao pode ganhar o poder de reescrever
 * as proprias permissoes.
 *
 * Os erros tem handlers proprios abaixo: o handler global transforma qualquer excecao em HTTP 500
 * e ainda manda mensagem para o Telegram, o que nao faz sentido para 400/403/409.
 */
@RestController
@RequestMapping("acesso/regras")
@ConditionalOnProperty(value = ["fields.acesso"], havingValue = "true", matchIfMissing = false)
class RegrasAcessoController(
    private val service: RegrasAcessoService,
    // O qualifier e necessario: o actuator e o spring-data-rest registram outros HandlerMapping.
    @Qualifier("requestMappingHandlerMapping") private val mapeamento: RequestMappingHandlerMapping,
) {

    @GetMapping("")
    fun atual(auth: Authentication): EstadoAtual {
        exigirAdmin(auth)
        return service.atual()
    }

    @PutMapping("/perfis/{perfil}")
    fun salvarPerfil(auth: Authentication, @PathVariable perfil: String, @RequestBody corpo: SalvarPerfilRequest): EstadoAtual =
        service.salvarPerfil(exigirAdmin(auth), perfil, corpo.regras, corpo.baseVersao, corpo.comentario)

    @DeleteMapping("/perfis/{perfil}")
    fun removerPerfil(
        auth: Authentication,
        @PathVariable perfil: String,
        @RequestParam baseVersao: Int,
        @RequestParam(required = false) comentario: String?,
    ): EstadoAtual = service.removerPerfil(exigirAdmin(auth), perfil, baseVersao, comentario)

    @GetMapping("/versoes")
    fun versoes(auth: Authentication): List<VersaoResumo> {
        exigirAdmin(auth)
        return service.versoes()
    }

    @GetMapping("/versoes/{numero}")
    fun versao(auth: Authentication, @PathVariable numero: Int): VersaoCompleta {
        exigirAdmin(auth)
        return service.versao(numero)
    }

    @PostMapping("/versoes/{numero}/restaurar")
    fun restaurar(auth: Authentication, @PathVariable numero: Int, @RequestBody corpo: RestaurarRequest): EstadoAtual =
        service.restaurar(exigirAdmin(auth), numero, corpo.baseVersao, corpo.comentario)

    /** O corpo e o texto do YAML. `simular=true` (o padrao) so devolve a previa, sem gravar. */
    @PostMapping("/importar")
    fun importar(
        auth: Authentication,
        @RequestBody yaml: String,
        @RequestParam(defaultValue = "mesclar") modo: String,
        @RequestParam(defaultValue = "true") simular: Boolean,
        @RequestParam(required = false) baseVersao: Int?,
        @RequestParam(required = false) comentario: String?,
    ): PreviaImportacao = service.importar(exigirAdmin(auth), yaml, modo, simular, baseVersao, comentario)

    @GetMapping("/exportar", produces = [TEXTO])
    fun exportar(auth: Authentication, @RequestParam(required = false) versao: Int?): String {
        exigirAdmin(auth)
        return service.exportar(versao)
    }

    @GetMapping("/arquivo", produces = [TEXTO])
    fun arquivo(auth: Authentication): String {
        exigirAdmin(auth)
        return service.textoDoArquivo()
    }

    @PostMapping("/simular")
    fun simular(auth: Authentication, @RequestBody corpo: SimularRequest): ResultadoSimulacao {
        exigirAdmin(auth)
        return service.simular(corpo.perfis, corpo.metodo, corpo.caminho, corpo.rascunho)
    }

    /** Catalogo dos endpoints reais do sistema, no formato das regras (variavel de caminho vira asterisco). */
    @GetMapping("/endpoints")
    fun endpoints(auth: Authentication): List<EndpointConhecido> {
        exigirAdmin(auth)
        return catalogo()
    }

    /** Quais endpoints reais uma URL de regra alcanca. */
    @GetMapping("/cobertura")
    fun cobertura(auth: Authentication, @RequestParam url: String): List<EndpointConhecido> {
        exigirAdmin(auth)
        return service.cobertura(url, catalogo())
    }

    // As rotas so mudam com um novo deploy: monta uma vez (na primeira consulta) em vez de a cada chamada.
    private val catalogo: List<EndpointConhecido> by lazy { montarCatalogo() }

    private fun catalogo(): List<EndpointConhecido> = catalogo

    private fun montarCatalogo(): List<EndpointConhecido> =
        mapeamento.handlerMethods.keys
            .flatMap { info ->
                val metodos = info.methodsCondition.methods.map { it.name.lowercase() }.ifEmpty { listOf("*") }
                info.patternValues.flatMap { padrao ->
                    metodos.map { EndpointConhecido(padrao.replace(VARIAVEL, "*"), it) }
                }
            }
            .distinct()
            .sortedWith(compareBy({ it.url }, { it.metodo }))

    private fun exigirAdmin(auth: Authentication): User {
        if (auth !is User || "admin" !in auth.roles) throw RegrasAcessoNegadoException()
        return auth
    }

    // ------------------------------------------------------------------ erros

    @ExceptionHandler(RegrasAcessoNegadoException::class)
    fun onNegado(e: RegrasAcessoNegadoException) = erro(HttpStatus.FORBIDDEN, "acesso_negado", e.message)

    @ExceptionHandler(RegrasValidacaoException::class)
    fun onValidacao(e: RegrasValidacaoException): ResponseEntity<Map<String, Any?>> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(mapOf("erro" to "validacao", "mensagem" to e.erros.joinToString("; "), "erros" to e.erros, "avisos" to e.avisos))

    @ExceptionHandler(RegrasConflitoException::class)
    fun onConflito(e: RegrasConflitoException) = erro(HttpStatus.CONFLICT, "conflito", e.message)

    @ExceptionHandler(RegrasNaoEncontradasException::class)
    fun onNaoEncontrado(e: RegrasNaoEncontradasException) = erro(HttpStatus.NOT_FOUND, "nao_encontrado", e.message)

    private fun erro(status: HttpStatus, codigo: String, mensagem: String?): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(status).body(mapOf("erro" to codigo, "mensagem" to mensagem))

    companion object {
        private const val TEXTO = "text/plain;charset=UTF-8"
        private val VARIAVEL = Regex("\\{[^}]*}")
    }
}

data class SalvarPerfilRequest(
    val regras: List<RegraDoc> = emptyList(),
    val baseVersao: Int = 0,
    val comentario: String? = null,
)

data class RestaurarRequest(val baseVersao: Int = 0, val comentario: String? = null)

data class SimularRequest(
    val perfis: List<String> = emptyList(),
    val metodo: String = "get",
    val caminho: String = "",
    val rascunho: RegrasDocumento? = null,
)
