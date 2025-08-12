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

class RoleBasedAuthorizationFilter(val service : RuleService) : OncePerRequestFilter() {

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

    fun isAuthorized(path: String, method : String, authentication: Authentication): Boolean {
        if(authentication !is User)
            throw Exception("Nao foi possivel fazer parse da interface para User")
        var urlPermitidas : List<Rule> = authentication.roles.flatMap { service.get(it) }
        return urlPermitidas.any { rule ->
            matchPath(rule.url, path)
                    &&
                    (rule.actions.any { it.equals(method, ignoreCase = true) } || rule.actions.contains("*"))
        }
    }

    private fun matchPath(pattern: String, path: String): Boolean {
        val adjustedPattern = if (!pattern.startsWith("/")) "/$pattern" else pattern
        val adjustedPath = if (!path.startsWith("/")) "/$path" else path

        if (adjustedPattern.endsWith("/**")) {
            return adjustedPath.startsWith(adjustedPattern.removeSuffix("/**"))
        }

        val regexPattern = adjustedPattern
            .replace("**", "__DOUBLE_WILDCARD__")
            .replace("*", "[^/]*")
            .replace("__DOUBLE_WILDCARD__", ".*")
            .replace("/$", "(/.*)?")
        return adjustedPath.matches(Regex(regexPattern))
    }
}