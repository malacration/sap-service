package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.infrastructure.configurations.security.otp.User
import br.andrew.sap.model.partner.BusinessPartner
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.stereotype.Component

@Component("authz")
@EnableMethodSecurity
class MethodSecurityConfig{

    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val usuario = operations.authentication
        return false;
    }

    fun acessoCliente(operations: MethodSecurityExpressionOperations): Boolean {
        val usuario = operations.authentication as User
        val retorno = getRetorno(operations)
        return if(retorno is BusinessPartner){
            retorno.getCpfCnpj().equals(usuario.id)
        }else {
            false
        }
    }

    fun getRetorno(r : MethodSecurityExpressionOperations): Any? {
        val retorno = r.returnObject
        return if(retorno is ResponseEntity<*>)
            retorno.body
        else
            retorno
    }
}