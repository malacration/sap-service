package br.andrew.sap.security

import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService

class MockRuleService : RuleService {

    override fun get(role : String): List<Rule> {
        return if(role == "admin")
            listOf(Rule("/**","*"))
        else if(role == "vendedor")
            listOf(
                Rule("/clientes/*","get"),
            )
        else if(role == "vendedor_admin")
            listOf(
                Rule("/clientes/*", listOf("get","post")),
                Rule("/vendedor/**","*")
            )
        else
            listOf()
    }

    fun get(roles: List<String>) : List<Rule>{
        return roles.flatMap { get(it) }
    }
}