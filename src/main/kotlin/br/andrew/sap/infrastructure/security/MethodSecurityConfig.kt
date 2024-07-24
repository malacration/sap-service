package br.andrew.sap.infrastructure.security

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sap.partner.BusinessPartner
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component("authz")
@EnableMethodSecurity
class MethodSecurityConfig(
    @Value("\${spring.security.disable:false}") val disable: Boolean,
){

    fun decide(operations: MethodSecurityExpressionOperations): Boolean {
        val usuario = operations.authentication
        return false;
    }

    fun acessoCliente(operations: MethodSecurityExpressionOperations): Boolean {
        if(disable)
            return disable
        val usuario = operations.authentication as User
        val retorno = getRetorno(operations)
        return if(retorno is BusinessPartner){
            acessoCliente(usuario,retorno)
        }else {
            true
        }
    }


    fun acessoCliente(user : Authentication?, bp : BusinessPartner): Boolean {
        if(disable || user == null)
            return disable
        val user = user as User
        return bp.getCpfCnpj().equals(user.id)
    }

    fun getRetorno(r : MethodSecurityExpressionOperations): Any? {
        val retorno = r.returnObject
        return if(retorno is ResponseEntity<*>)
            retorno.body
        else
            retorno
    }
}