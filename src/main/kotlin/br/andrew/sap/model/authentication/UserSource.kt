package br.andrew.sap.model.authentication

import com.fasterxml.jackson.annotation.JsonIgnore

interface UserSource {

    @JsonIgnore
    fun getOrigin() : UserOriginEnum

    @JsonIgnore
    fun getId() : String

    @JsonIgnore
    fun getUserName() : String

    @JsonIgnore
    fun getFullName() : String

    @JsonIgnore
    fun getPassword() : String?

    @JsonIgnore
    fun getEmailAdress() : String?{
        return getUserName()
    }

    @JsonIgnore
    fun getBussinesPlace() : List<Int>

    @JsonIgnore
    fun getUser(): User {
        return User(getId(),getFullName(),getOrigin(),getUserName(),getEmailAdress(),getPassword(),getBussinesPlace())
    }
}