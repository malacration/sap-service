package br.andrew.sap.infrastructure.configurations.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

class JwtFilter() : OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest,
                                  response: HttpServletResponse,
                                  filterChain: FilterChain) {
        if(!request.requestURL.contains("/otp/login") && request.getHeader("Authorization") != null){
            SecurityContextHolder.getContext().authentication = User("A","andrew", listOf())
        }
        filterChain.doFilter(request,response)
    }
}