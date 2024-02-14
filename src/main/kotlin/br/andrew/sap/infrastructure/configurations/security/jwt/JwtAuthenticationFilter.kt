package br.andrew.sap.infrastructure.configurations.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.filter.OncePerRequestFilter


class JwtAuthenticationFilter(private val jwtHandler: JwtHandler) : OncePerRequestFilter() {

    val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if(!request.requestURL.contains("/otp/login") && request.getHeader("Authorization") != null){
            try {
                val compactJws = request.getHeader("Authorization")
                SecurityContextHolder.getContext().authentication = jwtHandler.getUser(compactJws)
            }catch (e :Exception) {
                log.error("Erro no filtro",e)
            }
        }
        filterChain.doFilter(request,response)
    }
}