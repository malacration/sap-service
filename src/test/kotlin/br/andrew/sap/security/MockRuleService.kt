package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService

class MockRuleService : RuleService {

    override fun get(role : RolesEnum): List<Rule> {
        return if(role == RolesEnum.admin)
            listOf(Rule("/**","*"))
        else if(role == RolesEnum.vendedor)
            listOf(
                Rule("/clientes/*","get"),
            )
        else if(role == RolesEnum.vendedor_admin)
            listOf(
                Rule("/clientes/*", listOf("get","post")),
                Rule("/vendedor/**","*")
            )
        else
            listOf()
    }

    fun get(roles: List<RolesEnum>) : List<Rule>{
        return roles.flatMap { get(it) }
    }
}