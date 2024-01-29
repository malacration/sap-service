package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.infrastructure.configurations.security.filter.User
import br.andrew.sap.model.partner.BusinessPartner
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.stereotype.Component

@Component("authz")
@EnableMethodSecurity
class MethodSecurityConfig{

    init {
        println("Ola mundo")
    }

    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val usuario = operations.authentication
        return false;
    }

    fun acessoCliente(operations: MethodSecurityExpressionOperations): Boolean {
        val usuario = operations.authentication as User
        return if(operations.returnObject is BusinessPartner){
            (operations.returnObject as BusinessPartner).getCpfCnpj().equals(usuario.id)
        }else {
            false
        }
    }
}