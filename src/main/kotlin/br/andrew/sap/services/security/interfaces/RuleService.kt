package br.andrew.sap.services.security.interfaces

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import br.andrew.sap.services.security.Rule

interface RuleService {

    fun get(role : RolesEnum): List<Rule>
}

//class Rule(url : String, actions : List<String>)