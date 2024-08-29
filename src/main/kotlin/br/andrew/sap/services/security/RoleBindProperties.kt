package br.andrew.sap.services.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "role")
class RoleBindProperties{
    var bind : List<RoleBind> = listOf()
}

class RoleBind{
    lateinit var username : String
    lateinit var roles : List<String>
}