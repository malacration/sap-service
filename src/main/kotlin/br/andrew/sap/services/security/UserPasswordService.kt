package br.andrew.sap.services.security

import br.andrew.sap.model.SapUser
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserPassword
import org.springframework.stereotype.Service

@Service
class UserPasswordService {

    fun login(userPassword : UserPassword) : User{
        return User("1","Windson", listOf())
    }
}