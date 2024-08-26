package br.andrew.sap.model.authentication

import br.andrew.sap.infrastructure.security.roles.RolesEnum
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class User(val id : String,
           val roles : List<RolesEnum>) : UserDetails, Authentication {

    private var authenticated = true
    private var name : String? = null
    var otp : Int? = null

    constructor(id : String, name : String, authorities : List<RolesEnum>) : this(id,authorities){
        this.name = name
    }

    fun getIdInt() : Int{
        return id.toIntOrNull() ?: throw Exception("O ID do seu login contem algum erro ${id}")
    }
    override fun getName(): String? {
        return name
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

    override fun getPassword(): String {
        TODO("Not yet implemented")
    }

    override fun getUsername(): String {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }
}