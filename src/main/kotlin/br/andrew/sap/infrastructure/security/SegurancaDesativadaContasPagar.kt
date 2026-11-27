package br.andrew.sap.infrastructure.security

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Chave de teste LOCAL/HOMOLOG para o Nota Agil chamar a consulta de contas a pagar sem a service
 * account do Keycloak: `sap-rovema.seguranca.desativada=true` ou a variavel `SAP_ROVEMA_SECURITY_DISABLED=true`.
 * Padrao desligado; nunca deve ir para properties versionado. O nome e especifico da aplicacao de
 * proposito: um `SEGURANCA_DESATIVADA` generico (relaxed binding de `seguranca.desativada`) NAO liga a chave.
 *
 * Alcance: SOMENTE `POST /contas-pagar/pagamento-parcela/consulta` (ver [SegurancaDesativadaContasPagarFilter]).
 * O resto da API segue exigindo autenticacao e as regras do rules.yml. Nao confundir com
 * `spring.security.disable`, que abre a API inteira.
 *
 * Com profile de producao ([PERFIS_PRODUCAO]) ativo ou padrao (`spring.profiles.default`) a aplicacao NAO sobe.
 */
@Component
class SegurancaDesativadaContasPagar(
    @Value(PROPRIEDADE) val ativa: Boolean,
    environment: Environment,
) {
    init {
        if (ativa) {
            val producao = perfisProducao(environment)
            check(producao.isEmpty()) {
                "sap-rovema.seguranca.desativada / SAP_ROVEMA_SECURITY_DISABLED=true nao e permitido com o profile " +
                    "$producao: a chave e somente para teste local/homologacao. Remova a variavel."
            }
            log.warn("************************************************************************************")
            log.warn("* SEGURANCA DESATIVADA (SAP_ROVEMA_SECURITY_DISABLED) para POST {} *", CAMINHO)
            log.warn("* requisicao sem token passa como '{}'. SOMENTE teste local/homolog.  *", PRINCIPAL)
            log.warn("************************************************************************************")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(SegurancaDesativadaContasPagar::class.java)

        /**
         * Propriedade ou, na falta dela, a variavel de ambiente; padrao desligado. Nomes especificos do
         * sap-rovema: nada generico como `seguranca.desativada` / `SEGURANCA_DESATIVADA`.
         */
        const val PROPRIEDADE = "\${sap-rovema.seguranca.desativada:\${SAP_ROVEMA_SECURITY_DISABLED:false}}"

        /** Unica rota liberada (sem context-path), so com POST. */
        const val CAMINHO = "/contas-pagar/pagamento-parcela/consulta"

        const val PRINCIPAL = "auth-disabled-local"

        /** Perfil do rules.yml que autoriza [CAMINHO] (o mesmo da service account do Nota Agil). */
        const val ROLE = "integracao_contas_pagar"

        /** Profiles (sem diferenciar maiusculas) com os quais a chave impede a subida. */
        val PERFIS_PRODUCAO = setOf("prd", "prod", "production", "producao")

        /**
         * Profiles de producao entre os ativos E os padrao (`spring.profiles.default` /
         * `SPRING_PROFILES_DEFAULT`, que valem quando nenhum e ativado). Os padrao entram mesmo com
         * profile ativo: falha a mais, nunca a menos.
         */
        fun perfisProducao(environment: Environment): List<String> =
            (environment.activeProfiles + environment.defaultProfiles)
                .filter { it.lowercase() in PERFIS_PRODUCAO }.distinct()

        /** Usuario sintetico: so a role da integracao, sem filial nem vinculo de vendedor. */
        fun usuario(): User = User(PRINCIPAL, PRINCIPAL, UserOriginEnum.EmployeesInfo, PRINCIPAL, null, null,
            listOf(), listOf(ROLE))
    }
}

/**
 * So entra na cadeia com [SegurancaDesativadaContasPagar.ativa]. Para `POST` exatamente em
 * [SegurancaDesativadaContasPagar.CAMINHO], sem `Authorization` e sem autenticacao, coloca o usuario
 * sintetico com a role `integracao_contas_pagar`; o RoleBasedAuthorizationFilter continua aplicando as
 * regras normalmente. Qualquer outra requisicao passa intocada.
 */
class SegurancaDesativadaContasPagarFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (request.method == "POST"
            && request.requestURI.removePrefix(request.contextPath) == SegurancaDesativadaContasPagar.CAMINHO
            && request.getHeader(HttpHeaders.AUTHORIZATION) == null
            && SecurityContextHolder.getContext().authentication == null) {
            SecurityContextHolder.getContext().authentication =
                SegurancaDesativadaContasPagar.usuario()
            // Somente metodo e caminho fixo; nada do corpo.
            log.warn("SEGURANCA DESATIVADA: POST {} aceita sem autenticacao como '{}'",
                SegurancaDesativadaContasPagar.CAMINHO, SegurancaDesativadaContasPagar.PRINCIPAL)
        }
        filterChain.doFilter(request, response)
    }

    private companion object {
        val log = LoggerFactory.getLogger(SegurancaDesativadaContasPagarFilter::class.java)
    }
}
