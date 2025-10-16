package br.andrew.sap.model.authentication

import br.andrew.sap.infrastructure.odata.OData

interface UserSourceService {

    fun getByUserName(username: String): User
    fun changePassword(user : User,hashedPassword : String) : OData?
}