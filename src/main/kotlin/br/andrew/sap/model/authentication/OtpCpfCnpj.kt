package br.andrew.sap.model.authentication


class OtpCpfCnpj(val cpfCnpj : String, val otp : Int){
    fun getUser() : User {
        return User(cpfCnpj,"Cliente", listOf()).also { it.otp = otp }
    }
}