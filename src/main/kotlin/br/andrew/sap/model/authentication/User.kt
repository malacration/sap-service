package br.andrew.sap.model.authentication

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class User(val id : String,
           val _name : String,
           val origin : UserOriginEnum,
           val userName : String,
           val emailAddress : String? = null,
           val _password : String? = null,
           var roles : List<RolesEnum> = listOf()) : UserDetails, Authentication {

    private var authenticated = true
    var otp : Int? = null

    fun getIdInt() : Int{
        return id.toIntOrNull() ?: throw Exception("O ID do seu login contem algum erro ${id}")
    }
    override fun getName(): String? {
        return _name
    }

    override fun getAuthorities(): List<out GrantedAuthority> {
        return roles.map { it.getGrantedAuthority() }
    }

    override fun getCredentials(): Any {
        return this
    }

    override fun getDetails(): Any {
        return this
    }

    override fun getPrincipal(): Any {
        return id
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

    fun superVendedor(): Int {
        return if(roles.contains(RolesEnum.super_vendedor) || roles.contains(RolesEnum.admin))
            Int.MAX_VALUE
        else
            -1
    }

    @JsonIgnore
    override fun getPassword(): String? {
        return _password
    }

    override fun getUsername(): String {
        return id
    }

    override fun isAccountNonExpired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAccountNonLocked(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCredentialsNonExpired(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isEnabled(): Boolean {
        return true
    }
}