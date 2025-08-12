package br.andrew.sap.services.security

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.security.roles.RolesEnum
import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.authentication.UserPassword
import br.andrew.sap.model.authentication.UserSourceService
import br.andrew.sap.services.EmployeesInfoService
import br.andrew.sap.services.MailService
import br.andrew.sap.services.MyMailMessage
import br.andrew.sap.services.SalesPersonsService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserPasswordService(
    val service : SalesPersonsService,
    val roleBindService: RoleBindService,
    val mailService : MailService,
    val employerService : EmployeesInfoService
) {

    val logger = LoggerFactory.getLogger(UserPasswordService::class.java)

    fun login(request : UserPassword) : User{
        logger.info("Tentando fazer login para ${request.username}")
        val storageUser : User = try {
             service.getByUserName(request.username)
        }catch (e : Exception){
            try {
                employerService.getByUserName(request.username)
            }catch (t : Exception){
                t.addSuppressed(e)
                logger.error("Usuario nao encontrado",t)
                throw UsernameNotFoundException("Usuario nao encontrado")
            }
        }
        val passwordEncoder = BCryptPasswordEncoder()
        if(storageUser.password == "" || storageUser.password == null){
            setTemporalPassword(storageUser)
            throw PasswordEmptyException()
        }
        if(!passwordEncoder.matches(request.password, storageUser.password))
            throw BadCredentialsException("Senha incorreta")
        roleBindService.get(storageUser).also { storageUser.roles = it }
        return storageUser
    }

    fun setTemporalPassword(user: User) {
        val password = createRandomPassword()
        changePassword(user,password)
        val body  = "$password"
        if(!user.emailAddress.isNullOrEmpty())
            mailService.sendEmail(MyMailMessage(user.emailAddress,"Senha temporaria - Portal de vendas", body))
    }

    fun changePassword(user: User, rawNewPassword : String): OData? {
        val passwordEncoder = BCryptPasswordEncoder()
        val hashedPassword = passwordEncoder.encode(rawNewPassword)
        val isChangePassword = user.password == rawNewPassword

        val service : UserSourceService = if(user.origin == UserOriginEnum.SalePerson){
            this.service
        }
        else if(user.origin == UserOriginEnum.EmployeesInfo){
            this.employerService
        }else
            throw Exception("Nao foi detecado um service para ${user.origin}")
        return service.changePassword(user,hashedPassword).also {
            if(isChangePassword && !user.emailAddress.isNullOrEmpty()) {
                val body = "Sua senha foi modificada. Caso não tenha realizado esta ação, entre em contato com o administrador do sistema."
                mailService.sendEmail(MyMailMessage(user.emailAddress, "Senha modificada - Portal de vendas", body))
            }
        }
    }

    fun createRandomPassword(): String {
        val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val specialCharacters = "!@#\$%&*-"
        val allCharacters = upperCaseLetters + lowerCaseLetters + digits + specialCharacters
        val password = StringBuilder()
        password.append(upperCaseLetters.random())
        password.append(lowerCaseLetters.random())
        password.append(digits.random())
        password.append(specialCharacters.random())
        repeat(10 - 4) {
            password.append(allCharacters.random())
        }
        return password.toList().shuffled().joinToString("")
    }
}