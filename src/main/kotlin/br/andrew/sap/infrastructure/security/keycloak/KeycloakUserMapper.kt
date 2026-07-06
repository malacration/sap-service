package br.andrew.sap.infrastructure.security.keycloak

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.EmployeesInfoService
import br.andrew.sap.services.security.RuleService
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Component

/**
 * Converte os claims de um token Keycloak no [User] usado pelo sistema.
 *
 * Regras (definidas pelo projeto):
 *  - As roles do app vem direto das roles do Keycloak (realm + client). Ex.:
 *    role `vendedor` no Keycloak casa com a role `vendedor` do rules.yml.
 *  - As filiais vem de roles no formato `filial-{id}-{nome}`: o `{id}` e o
 *    BPLId usado pelo sistema; `{nome}` e apenas para leitura/suporte.
 *  - Identidade: o EmployeeID (codigo de colaborador) vem do claim
 *    [KeycloakProperties.sapIdClaim] (ex.: `sap_code=10`). Por padrao o usuario
 *    opera como colaborador (EmployeesInfo, id = EmployeeID). Porem, se o
 *    cadastro desse colaborador no SAP tiver um vendedor vinculado
 *    (SalesPersonCode), ele passa a operar como esse vendedor (origin
 *    SalePerson e id = SalesPersonCode), de modo que as vendas usem o codigo de
 *    vendedor. O vinculo e buscado pelo EmployeeID e fica em cache.
 */
@Component
class KeycloakUserMapper(
    private val properties: KeycloakProperties,
    private val employeesInfoService: EmployeesInfoService,
    private val ruleService: RuleService,
) {

    private val filialPrefix = "filial-"

    fun toUser(claims: Claims): User {
        val roles = extractRoles(claims)
        val businessPlaces = roles
            .filter { it.startsWith(filialPrefix, ignoreCase = true) }
            .mapNotNull { parseFilialId(it) }
            .distinct()
        // So conta como permissao a role que de fato existe no rules.yml; isso
        // descarta as roles tecnicas do Keycloak (default-roles-*, offline_access,
        // uma_authorization) e detecta usuario sem nenhuma permissao real.
        val appRoles = roles
            .filterNot { it.startsWith(filialPrefix, ignoreCase = true) }
            .filter { ruleService.get(it).isNotEmpty() }

        val preferredUsername = claims["preferred_username"]?.toString()
        val email = claims["email"]?.toString()
        val name = claims["name"]?.toString() ?: preferredUsername ?: claims.subject

        // EmployeeID (codigo de colaborador) vem exclusivamente do claim sap_code.
        val employeeId = resolveEmployeeId(claims)

        // Falha cedo (na desserializacao) quando o usuario do SSO esta sem o
        // provisionamento minimo, para nao deixar o fluxo do Spring seguir.
        val faltando = mutableListOf<String>()
        if (employeeId == null)
            faltando.add("codigo de colaborador (claim '${properties.sapIdClaim}')")
        if (appRoles.isEmpty())
            faltando.add("nenhuma permissao (role) atribuida")
        if (faltando.isNotEmpty()) {
            val quem = preferredUsername ?: email ?: claims.subject
            throw KeycloakUserProvisioningException(
                "Acesso via SSO incompleto para '$quem': ${faltando.joinToString("; ")}. " +
                "Procure o administrador do sistema."
            )
        }

        // Se o colaborador tem um vendedor vinculado no SAP, opera como vendedor.
        val salesPersonCode = employeeId?.let { employeesInfoService.getSalesPersonCodeByEmployeeId(it) }
        val origin = if (salesPersonCode != null) UserOriginEnum.SalePerson else UserOriginEnum.EmployeesInfo
        val id = (salesPersonCode ?: employeeId).toString()

        return User(
            id = id,
            _name = name,
            origin = origin,
            userName = preferredUsername ?: email ?: claims.subject,
            emailAddress = email ?: preferredUsername,
            _password = null,
            bussinesPlace = businessPlaces,
            roles = appRoles,
        )
    }

    /** EmployeeID (codigo de colaborador) exclusivamente a partir do claim sap_code. */
    private fun resolveEmployeeId(claims: Claims): Int? {
        return claims[properties.sapIdClaim]?.toString()?.toIntOrNull()
    }

    /** `filial-3-matriz` -> 3 */
    private fun parseFilialId(role: String): Int? {
        return role.substring(filialPrefix.length)
            .substringBefore('-')
            .toIntOrNull()
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractRoles(claims: Claims): List<String> {
        val result = mutableListOf<String>()
        (claims["realm_access"] as? Map<String, Any?>)?.let { realmAccess ->
            (realmAccess["roles"] as? List<*>)?.forEach { it?.let { r -> result.add(r.toString()) } }
        }
        (claims["resource_access"] as? Map<String, Any?>)?.get(properties.clientId)?.let { client ->
            ((client as? Map<String, Any?>)?.get("roles") as? List<*>)
                ?.forEach { it?.let { r -> result.add(r.toString()) } }
        }
        return result.distinct()
    }
}
