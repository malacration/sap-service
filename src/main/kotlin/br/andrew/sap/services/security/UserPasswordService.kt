package br.andrew.sap.services.security

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.SalePerson
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserPassword
import br.andrew.sap.services.MailService
import br.andrew.sap.services.MyMailMessage
import br.andrew.sap.services.SalesPersonsService
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserPasswordService(
    val service : SalesPersonsService,
    val mailService : MailService
) {

    val logger = LoggerFactory.getLogger(UserPasswordService::class.java)

    fun login(request : UserPassword) : User{
        logger.info("Tentando fazer login para ${request.username}")
        val storageSalePerson = try {
             service.getById(request.username).tryGetValue<SalePerson>()
        }catch (e : Exception){
            throw UsernameNotFoundException("Usuario nao encontrado")
        }
        val passwordEncoder = BCryptPasswordEncoder()
        if(storageSalePerson.u_password == "" || storageSalePerson.u_password == null){
            setTemporalPassword(storageSalePerson)
            throw PasswordEmptyException()
        }
        if(!passwordEncoder.matches(request.password, storageSalePerson.u_password))
            throw BadCredentialsException("Senha incorreta")
        val permission = listOf(SimpleGrantedAuthority("vendedor"))
        return User(storageSalePerson.SalesEmployeeCode.toString(),storageSalePerson.SalesEmployeeName,permission)
    }

    fun setTemporalPassword(salePerson: SalePerson) {
        val password = createRandomPassword()
        changePassword(salePerson,password)
        val body  = "$password"
        mailService.sendEmail(MyMailMessage("andrewc3po@gmail.com","Senha temporaria - Portal de vendas", body))
    }

    fun changePassword(salePerson: SalePerson, password : String): OData? {
        val passwordEncoder = BCryptPasswordEncoder()
        val hashedPassword = passwordEncoder.encode(password)
        val isChangePassword = salePerson.u_password == "" || salePerson.u_password == null
        salePerson.u_password = hashedPassword
        return service.update(salePerson,salePerson.SalesEmployeeCode.toString()).also {
            if(isChangePassword) {
                val body = "Sua senha foi modificada. Caso não tenha realizado esta ação, entre em contato com o administrador do sistema."
                mailService.sendEmail(MyMailMessage("andrewc3po@gmail.com", "Senha modificada - Portal de vendas", body))
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