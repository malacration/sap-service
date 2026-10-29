package br.andrew.sap.controllers.authentication

import br.andrew.sap.infrastructure.security.RoleBasedAuthorizationFilter
import br.andrew.sap.infrastructure.security.RulePathMatcher
import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.interfaces.RuleService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * "Quais destas URLs eu posso chamar?" - o menu do front pergunta por aqui quais telas mostrar.
 *
 * Responde com as MESMAS regras e o MESMO casamento do filtro de autorizacao (o bean RuleService e o mesmo: arquivo
 * ou cadastro no SAP, conforme acesso.regras.fonte), so para os perfis do proprio usuario. Nao depende de
 * fields.acesso: o menu de todo usuario usa isto. Todo usuario autenticado pode chamar
 * (RoleBasedAuthorizationFilter.LIBERADAS_A_AUTENTICADOS). So leitura, sem SAP: as regras estao em memoria.
 */
@RestController
@RequestMapping("acesso")
class MinhasPermissoesController(
    private val ruleService: RuleService,
    @Value("\${server.servlet.context-path:}") context: String,
) {
    private val matcher = RulePathMatcher(context)

    /**
     * O front pergunta pelo caminho da API ("/carregamento"); o filtro recebe a requestURI, que vem com o
     * context-path na frente. A consulta e feita sobre a URI que a chamada real teria, para dar o mesmo resultado
     * inclusive quando o caminho perguntado ja comeca com algo igual ao context-path. URL completa
     * ("https://host/v7/x") ja traz a URI inteira e nao ganha o prefixo de novo.
     */
    private val prefixoDoContexto = context.trim().trim('/').let { if (it.isEmpty()) "" else "/$it" }

    @PostMapping("minhas-permissoes")
    fun minhasPermissoes(auth: Authentication, @RequestBody consultas: List<PermissaoConsulta?>): List<PermissaoResposta> {
        val usuario = auth as? User ?: throw PermissaoConsultaInvalida("Usuário não identificado.")
        val validas = validarConsultas(consultas, MAXIMO_DE_CONSULTAS)
        // As liberadas a todo autenticado entram para a resposta bater com o filtro em qualquer caminho perguntado.
        val regras = RoleBasedAuthorizationFilter.LIBERADAS_A_AUTENTICADOS + usuario.roles.flatMap { ruleService.get(it) }
        return validas.map { c ->
            val metodo = c.metodo.trim().lowercase()
            val limpo = RulePathMatcher.limparCaminho(c.caminho)
            val uri = if (RulePathMatcher.ehUrlCompleta(c.caminho)) limpo else prefixoDoContexto + limpo
            PermissaoResposta(c.metodo, c.caminho, matcher.autoriza(regras, metodo, uri) != null)
        }
    }

    // Erros com status proprio: o handler global transforma qualquer excecao em 500 e manda mensagem ao Telegram.

    @ExceptionHandler(PermissaoConsultaInvalida::class)
    fun onInvalida(e: PermissaoConsultaInvalida) = erro(e.message)

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun onIlegivel(e: HttpMessageNotReadableException) = erro("Corpo inválido: envie uma lista de {metodo, caminho}.")

    private fun erro(mensagem: String?): ResponseEntity<Map<String, Any?>> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("erro" to "consulta_invalida", "mensagem" to mensagem))

    companion object {
        const val MAXIMO_DE_CONSULTAS = 100
        const val TAMANHO_MAXIMO_CAMINHO = 300
        private val METODOS = setOf("get", "post", "put", "patch", "delete")

        /** Tambem usada pelo simular-lote da tela de Regras de Acesso: as duas recebem a mesma lista de consultas. */
        fun validarConsultas(consultas: List<PermissaoConsulta?>, maximo: Int): List<PermissaoConsulta> {
            if (consultas.size > maximo)
                throw PermissaoConsultaInvalida("No máximo $maximo consultas por chamada.")
            return consultas.map { c ->
                if (c == null)
                    throw PermissaoConsultaInvalida("Consulta vazia na lista: envie {metodo, caminho}.")
                if (c.metodo.trim().lowercase() !in METODOS)
                    throw PermissaoConsultaInvalida("Método inválido: '${c.metodo.take(20)}' (use get, post, put, patch ou delete).")
                if (c.caminho.isBlank() || c.caminho.length > TAMANHO_MAXIMO_CAMINHO)
                    throw PermissaoConsultaInvalida("Caminho vazio ou com mais de $TAMANHO_MAXIMO_CAMINHO caracteres.")
                c
            }
        }
    }
}

data class PermissaoConsulta(val metodo: String = "", val caminho: String = "")

data class PermissaoResposta(val metodo: String, val caminho: String, val permitido: Boolean)

class PermissaoConsultaInvalida(mensagem: String) : RuntimeException(mensagem)
