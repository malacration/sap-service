package br.andrew.sap.infrastructure.configurations.security.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter


class JwtAuthenticationFilter(val jwtHandler: JwtHandler) : OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if(!request.requestURL.contains("/otp/login") && request.getHeader("Authorization") != null){
            try {
                val compactJws = request.getHeader("Authorization")
                SecurityContextHolder.getContext().authentication = jwtHandler.getUser(compactJws)
            }catch (e :Exception) {
                e.printStackTrace()
            }
        }
        filterChain.doFilter(request,response)
    }
}