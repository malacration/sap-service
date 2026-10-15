package br.andrew.sap.services.security.acesso

import br.andrew.sap.services.security.Rule
import br.andrew.sap.services.security.interfaces.RuleService

/** Entrega ao filtro de autorizacao as regras que estao no SAP, lidas do cache em memoria. */
class SapRuleService(private val cache: RegrasAcessoCache) : RuleService {

    override fun get(role: String): List<Rule> = cache.atual().regras[role] ?: emptyList()
}
