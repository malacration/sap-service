package br.andrew.sap.infrastructure.security

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class RoleBasedAuthorizationFilter(
    val service : RuleService,
    val context : String
) : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI
        val method = request.method
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if(authentication != null && authentication.isAuthenticated && !isAuthorized(path, method, authentication))
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Você não tem permissão para acessar este recurso.")
        else
            filterChain.doFilter(request, response)
    }

    private val matcher = RulePathMatcher(context)

    fun isAuthorized(path: String, method : String, authentication: Authentication): Boolean {
        if(authentication !is User)
            throw Exception("Nao foi possivel fazer parse da interface para User")
        if (matcher.autoriza(LIBERADAS_A_AUTENTICADOS, method, path) != null)
            return true
        val urlPermitidas : List<Rule> = authentication.roles.flatMap { service.get(it) }
        return matcher.autoriza(urlPermitidas, method, path) != null
    }

    companion object {
        /**
         * Liberadas a QUALQUER usuario autenticado, sem precisar estar nas regras de nenhum perfil: o menu de todo
         * usuario pergunta por elas quais telas pode ver. Caminho exato e metodo exato (sem curinga). Anonimo nao chega
         * aqui (o filtro so age com usuario autenticado; o Spring Security barra antes, com 403).
         */
        val LIBERADAS_A_AUTENTICADOS = listOf(Rule("/acesso/minhas-permissoes", "post"))
    }
}