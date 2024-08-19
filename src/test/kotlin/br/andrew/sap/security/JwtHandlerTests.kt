package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.security.jwt.JwtSecretBean
import br.andrew.sap.infrastructure.security.roles.RolesEnum
import br.andrew.sap.model.authentication.User
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority

class JwtHandlerTests {

    val service = JwtHandler(JwtSecretBean("windson-jose-pedro-gabriel-gadelha"))

    @Test
    fun testaTokenInicial(){
        val authorities = listOf(RolesEnum.admin)
        val userInput = User("id-windson","windson",authorities)
        val tokenOutput = service.getToken(userInput)
        val userOutput = service.getUser(tokenOutput.token)

        Assertions.assertEquals(userInput.id, userOutput.id)
        Assertions.assertEquals(userInput.name, userOutput.name)
        Assertions.assertEquals(userInput.authorities.size, userOutput.authorities.size)
    }
}