package br.andrew.sap.infrastructure.security.keycloak

/**
 * Cria roles no Keycloak. Existe como interface para a gestao das regras de acesso ser testada
 * sem SSO e para a integracao poder ficar desligada (nenhuma implementacao registrada).
 */
interface KeycloakRolesGateway {

    /** true = a role foi criada agora; false = ja existia. Lanca [KeycloakAdminException] se nao deu. */
    fun garantirRole(nome: String): Boolean

    /**
     * Ja existe uma role de REALM com este nome? O login aceita roles de realm e de client com o mesmo nome,
     * entao quem ja tem a de realm passaria a receber o perfil. null = nao deu para verificar (sem permissao
     * de leitura de roles do realm, ou Keycloak fora do ar); nao e erro.
     */
    fun roleDeRealmExiste(nome: String): Boolean? = null
}

class KeycloakAdminException(mensagem: String, causa: Throwable? = null) : RuntimeException(mensagem, causa)
