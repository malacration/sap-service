package br.andrew.sap.services.security

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import org.springframework.stereotype.Service

@Service
class RoleBindService(val roleBind : RoleBindProperties) {

    fun get(username : String) : List<RolesEnum>{
        return roleBind.bind
            .firstOrNull{ it.username == username}
            ?.roles
            ?.map { RolesEnum.valueOf(it) } ?: listOf()
    }
}