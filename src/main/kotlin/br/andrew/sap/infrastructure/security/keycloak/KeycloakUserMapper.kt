package br.andrew.sap.infrastructure.security.keycloak

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.cadastro.EmployeesInfoService
import br.andrew.sap.services.security.interfaces.RuleService
import io.jsonwebtoken.Claims
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * Converte os claims de um token Keycloak no [User] usado pelo sistema.
 *
 * Regras (definidas pelo projeto):
 *  - As roles do app vem direto das roles do Keycloak (realm + client). Ex.:
 *    role `vendedor` no Keycloak casa com a role `vendedor` do rules.yml.
 *  - As filiais vem do cadastro do SAP, como no login interno (atribuicao de
 *    filial do colaborador ou filial do vendedor), somadas as roles no formato
 *    `filial-{id}-{nome}`: o `{id}` e o BPLId; `{nome}` e so para leitura.
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
    private val bussinessPlaceService: BussinessPlaceService,
) {

    private val log = LoggerFactory.getLogger(KeycloakUserMapper::class.java)
    private val filialPrefix = "filial-"

    fun toUser(claims: Claims): User {
        val todas = extractRoles(claims)
        // Service account de client de integracao: sempre caminho proprio, sem colaborador/vendedor/filial
        // do SAP. So o perfil de integracao, se tiver; sem ele, nenhum perfil (recusada).
        if (serviceAccountIntegracao(claims)) {
            if (todas.any { it.equals(ROLE_INTEGRACAO_CONTAS_PAGAR, ignoreCase = true) }) return usuarioIntegracao(claims)
            throw KeycloakUserProvisioningException(
                "Acesso via SSO incompleto para '${claims["preferred_username"]}': service account de integracao " +
                "sem o perfil $ROLE_INTEGRACAO_CONTAS_PAGAR. Procure o administrador do sistema."
            )
        }
        // Fora disso o perfil de integracao nunca vale; demais perfis inalterados.
        val roles = todas.filterNot { it.equals(ROLE_INTEGRACAO_CONTAS_PAGAR, ignoreCase = true) }
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
        // Falha na consulta nao e "sem vendedor": nesse caso as filiais do SAP
        // ficam de fora, senao um vendedor herdaria as filiais de colaborador.
        val vinculo = runCatching { employeesInfoService.getSalesPersonCodeByEmployeeId(employeeId!!) }
            .onFailure { log.warn("Falha ao buscar vendedor vinculado ao colaborador $employeeId", it) }
        val salesPersonCode = vinculo.getOrNull()
        val origin = if (salesPersonCode != null) UserOriginEnum.SalePerson else UserOriginEnum.EmployeesInfo
        val id = (salesPersonCode ?: employeeId).toString()
        val filiaisSap = if (vinculo.isSuccess) filiaisDoSap(employeeId!!, salesPersonCode) else listOf()

        return User(
            id = id,
            _name = name,
            origin = origin,
            userName = preferredUsername ?: email ?: claims.subject,
            emailAddress = email ?: preferredUsername,
            _password = null,
            bussinesPlace = (filiaisSap + businessPlaces).distinct(),
            roles = appRoles,
        )
    }

    /**
     * Filiais do cadastro do SAP, mesma fonte do login interno: vendedor usa a
     * filial do OSLP (@RO_FILIAL_LINHA), colaborador usa a atribuicao de filial
     * (HEM10). As roles `filial-*` do Keycloak continuam somando por cima.
     * Falha no SAP nao derruba o login: so deixa de somar as filiais de la.
     */
    private fun filiaisDoSap(employeeId: Int, salesPersonCode: Int?): List<Int> {
        return try {
            (if (salesPersonCode != null) bussinessPlaceService.getFilialBySalesPerson(salesPersonCode)
            else bussinessPlaceService.getFilialByEmployee(employeeId))
                .map { it.BPLId }
        } catch (e: Exception) {
            log.warn("Falha ao buscar filiais no SAP do colaborador $employeeId", e)
            listOf()
        }
    }

    /**
     * Token de service account (client_credentials) de um client em `keycloak.integracao-contas-pagar.client-ids`:
     * `azp` permitido, `preferred_username = service-account-<azp>` (username reservado pelo Keycloak ao usuario
     * da service account) e `client_id`/`clientId`, quando vier, igual ao azp.
     */
    private fun serviceAccountIntegracao(claims: Claims): Boolean {
        val azp = claims["azp"]?.toString()?.trim()
        if (azp.isNullOrEmpty() || azp !in properties.integracaoContasPagar.clientIds) return false
        val username = claims["preferred_username"]?.toString()?.trim() ?: return false
        if (!username.equals(SERVICE_ACCOUNT_PREFIXO + azp, ignoreCase = true)) return false
        return listOf("client_id", "clientId").all { claim -> claims[claim]?.let { it.toString().trim() == azp } ?: true }
    }

    /** Principal da service account de integracao: so o perfil de integracao, sem filial nem colaborador. */
    private fun usuarioIntegracao(claims: Claims): User {
        val azp = claims["azp"].toString().trim()
        val username = claims["preferred_username"].toString().trim()
        return User(
            id = azp,
            _name = username,
            origin = UserOriginEnum.Integracao,
            userName = username,
            emailAddress = null,
            _password = null,
            bussinesPlace = listOf(),
            roles = listOf(ROLE_INTEGRACAO_CONTAS_PAGAR),
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

    companion object {
        const val SERVICE_ACCOUNT_PREFIXO = "service-account-"
        /** Nome do perfil no rules.yml; fixo para a protecao nao se desencontrar do perfil. */
        const val ROLE_INTEGRACAO_CONTAS_PAGAR = "integracao_contas_pagar"
    }
}
