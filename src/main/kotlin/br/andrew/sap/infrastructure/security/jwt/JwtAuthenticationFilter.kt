package br.andrew.sap.infrastructure.security.jwt

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Base64


class JwtAuthenticationFilter(private val jwtHandler: JwtHandler, private val disable : Boolean = false) : OncePerRequestFilter() {

    val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    private val mapper = ObjectMapper()

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if(SecurityContextHolder.getContext().authentication != null){
            // ja autenticado por um filtro anterior (ex.: Keycloak)
            filterChain.doFilter(request,response)
            return
        }
        val compactJws = request.getHeader("Authorization")
        // Este filtro so trata tokens internos (HMAC/HS256). Tokens RS256
        // (Keycloak) sao do KeycloakAuthenticationFilter; se chegaram aqui sem
        // terem sido tratados (ex.: Keycloak desabilitado ou issuer divergente),
        // apenas ignora em vez de tentar validar com a chave HMAC (o que gerava
        // UnsupportedJwtException no log).
        if(compactJws != null
            && !request.requestURL.contains("/otp/login")
            && !request.requestURL.contains("/logar")
            && isInternalToken(compactJws)){
            try {
                SecurityContextHolder.getContext().authentication = jwtHandler.getUser(compactJws)
            }catch (e :Exception) {
                log.error("Erro no filtro",e)
            }
        }
        // Modo bypass: garante um usuario admin-fake quando nada autenticou
        // (inclusive quando ha um token nao-interno, ex.: Keycloak, no header).
        if(disable && SecurityContextHolder.getContext().authentication == null){
            SecurityContextHolder.getContext().authentication = User("-1","Nenhum vendedor",
            UserOriginEnum.SalePerson,"","","",listOf(),listOf("admin","pix_admin"))
        }
        filterChain.doFilter(request,response)
    }

    /** Verdadeiro se o JWT usa algoritmo HMAC (HS*), ou seja, e um token interno. */
    private fun isInternalToken(token: String): Boolean {
        return try {
            val header = token.removePrefix("Bearer ").trim().split(".").firstOrNull() ?: return false
            val json = String(Base64.getUrlDecoder().decode(header))
            val alg = mapper.readTree(json).get("alg")?.asText() ?: return false
            alg.uppercase().startsWith("HS")
        } catch (e: Exception) {
            false
        }
    }
}