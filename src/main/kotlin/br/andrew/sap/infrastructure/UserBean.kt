package br.andrew.sap.infrastructure

import br.andrew.sap.model.sap.SapUser
import br.andrew.sap.services.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test && !aws")
class UserBean(val userService: UserService) {


    @Bean(name = arrayOf("user.current"))
    fun getCurrentUser() : SapUser {
        return userService.getCurretuser()
    }
}