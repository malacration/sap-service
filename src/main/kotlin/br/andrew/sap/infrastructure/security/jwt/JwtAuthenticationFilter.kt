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
        // Modo bypass: o token e ignorado por completo e toda requisicao roda como o mesmo
        // usuario admin-fake. Sem isso, quem passasse pelo /otp/login (que em modo disable
        // aceita qualquer CPF/CNPJ sem validar OTP) viraria business_partner, perdendo o
        // vinculo de vendedor_admin - e com isso as filiais em /branch e os produtos em
        // produto-tabela.sql, alem de estourar em User.getIdInt() com um CPF/CNPJ.
        if(disable){
            SecurityContextHolder.getContext().authentication = usuarioBypass()
            filterChain.doFilter(request,response)
            return
        }
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
        filterChain.doFilter(request,response)
    }

    /**
     * Usuario do modo bypass. Entra como vendedor_admin porque o SlpCode e -1, que nao existe em
     * OSLP nem em @LIBERAPARA: sem esse vinculo o User.superVendedor() vale -1, a busca de
     * produtos (produto-tabela.sql) volta vazia e /branch nao lista filial nenhuma. Com o vinculo
     * o superVendedor vale Int.MAX_VALUE e libera as duas coisas, do mesmo jeito que ja faz com
     * parceiros e contratos.
     *
     * bussinesPlace fica vazio de proposito - quem lista filial e o /branch (BranchController),
     * que devolve todas para super vendedor. O /me so espelha esse campo, nao restringe nada.
     */
    private fun usuarioBypass() : User {
        return User("-1","Nenhum vendedor", UserOriginEnum.SalePerson,"","","",
            listOf(), listOf("admin","pix_admin","vendedor_admin"))
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