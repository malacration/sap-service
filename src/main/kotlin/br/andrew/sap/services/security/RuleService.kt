package br.andrew.sap.services.security

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import br.andrew.sap.services.security.interfaces.RuleService as iRuleService

@Service
class RuleService(val resourceLoader: ResourceLoader) : iRuleService {

    private val yamlMapper = YAMLMapper().registerKotlinModule()

    override fun get(role: String): List<Rule> {
        val roleRules = yamlMapper.readValue(getRulesResource().inputStream, RoleRules::class.java)
        return roleRules.roles[role] ?: emptyList()
    }

    private fun getRulesResource(): Resource {
        return resourceLoader.getResource("classpath:rules.yml")
    }
}

data class RoleRules @JsonCreator constructor(
    @JsonProperty("roles") val roles: Map<String, List<Rule>>
)