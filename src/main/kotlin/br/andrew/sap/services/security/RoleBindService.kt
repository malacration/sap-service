package br.andrew.sap.services.security

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import org.springframework.stereotype.Service

@Service
class RoleBindService(val roleBind : RoleBindProperties) {

    fun get(user : User) : List<String>{
        return if(user.origin == UserOriginEnum.SalePerson)
            roleBind.bind
                .firstOrNull{ it.username == user.id}
                ?.roles
                ?.map { it } ?: listOf()
        else if(user.origin == UserOriginEnum.EmployeesInfo)
            return roleBind.bind
                .firstOrNull{ it.username == user.emailAddress}
                ?.roles
                ?.map {it} ?: listOf()
        else
            listOf()
    }
}