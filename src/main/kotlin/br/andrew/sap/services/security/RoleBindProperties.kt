package br.andrew.sap.services.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "role")
class RoleBindProperties{
    lateinit var bind : List<RoleBind>
}

class RoleBind{
    lateinit var username : String
    lateinit var roles : List<String>
}