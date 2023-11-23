package br.andrew.sap.infrastructure.configurations.softexpert

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
class SoftExpertEnvrioment(
    @Value("\${soft.expert.host:windson}") val host : String,
    @Value("\${soft.expert.user:windson}") val user : String,
    @Value("\${soft.expert.password:windson}") val password : String) {

}