package br.andrew.sap.services.security

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import br.andrew.sap.model.Version
import br.andrew.sap.services.security.interfaces.RuleService as iRuleService
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RuleService(
    val roles : RoleRules,
    val resourceLoader: ResourceLoader) : iRuleService {

    private val yamlMapper = YAMLMapper()

    override fun get(role: RolesEnum): List<Rule> {
        val roleRules = yamlMapper.readValue(getRulesResource().inputStream, RoleRules::class.java)
        return roleRules.roles[role.toString()] ?: emptyList()
    }

    private fun getRulesResource(): Resource {
        return resourceLoader.getResource("classpath:rules.yml")
    }
}

@Configuration
@PropertySource("classpath:/rules.yml", ignoreResourceNotFound = false)
class RoleRules(val roles: Map<String, List<Rule>>)