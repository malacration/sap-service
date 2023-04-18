package br.andrew.sap.infrastructure

import br.andrew.sap.model.Version
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import java.time.LocalDateTime




@Configuration
class VersionBean {

    @Bean
    fun version(@Value("\${sap.service.layer.companyDB:companyDB}") companyDB : String,
                version : VersionEnvironment) : Version{
        return version.version.also {  it.company = companyDB }
    }
}

@Configuration
@PropertySource("classpath:/version.properties", ignoreResourceNotFound = true)
class VersionEnvironment {
    @Value("\${major:''}")
    private val major: String? = null

    @Value("\${minor:''}")
    private val minor: String? = null

    @Value("\${patch:#{0}}")
    private val patch: String? = null

    @Value("\${timestamp:#{''}}")
    var timestamp: String? = null
        get() {
            if (field!!.isEmpty()) field = LocalDateTime.now().toString()
            return field
        }

    @Value("\${commit_sha:#{'w1nd50n'}}")
    private val commitSha: String? = null
    val version: Version
        get() = Version(String.format("%s.%s.%s", major, minor, patch), timestamp!!, commitSha!!)
}
