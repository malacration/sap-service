package br.andrew.sap.infrastructure

import br.andrew.sap.model.User
import br.andrew.sap.services.UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserBean(val userService: UserService) {


    @Bean(name = arrayOf("user.current"))
    fun getCurrentUser() : User {
        return userService.getCurretuser()
    }
}