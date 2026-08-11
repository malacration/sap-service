package br.andrew.sap.services.cobranca

import br.andrew.sap.model.authentication.User

object CobrancaEscopo {

    fun temAcessoTotal(auth: User): Boolean {
        return auth.superVendedor() == Int.MAX_VALUE || auth.roles.contains("cobranca")
    }

    fun vendedorEfetivo(auth: User, vendedorSolicitado: Int?): Int? {
        return if (temAcessoTotal(auth)) vendedorSolicitado else auth.getIdInt()
    }
}
