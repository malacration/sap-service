package br.andrew.sap.rovema.model


class SapEnvrioment(val host: String,
                    val user: String,
                    val password: String,
                    val companyDB: String) {

    fun getLogin() : Login{
        return Login(user,password,companyDB)
    }

}