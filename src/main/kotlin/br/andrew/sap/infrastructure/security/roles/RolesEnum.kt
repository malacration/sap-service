package br.andrew.sap.infrastructure.security.roles

import org.springframework.security.core.authority.SimpleGrantedAuthority


@Deprecated("Melhor usar Strings em vez de manter hardcode")
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