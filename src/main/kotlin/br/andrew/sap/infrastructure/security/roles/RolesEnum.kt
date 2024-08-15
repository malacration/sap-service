package br.andrew.sap.infrastructure.security.roles

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class RolesEnum {

    admin,
    vendedor,
    cliente,
    vendedor_admin;

    fun getGrantedAuthority(): SimpleGrantedAuthority {
        return SimpleGrantedAuthority(this.toString())
    }
}


//User ->