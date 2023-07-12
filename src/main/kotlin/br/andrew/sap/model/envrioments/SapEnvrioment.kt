package br.andrew.sap.model.envrioments

import br.andrew.sap.model.Login


class SapEnvrioment(val host: String,
                    val user: String,
                    val password: String,
                    val companyDB: String) {

    fun getLogin() : Login {
        return Login(user,password,companyDB)
    }

}