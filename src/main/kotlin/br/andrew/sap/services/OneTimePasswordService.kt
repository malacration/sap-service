package br.andrew.sap.services

import br.andrew.sap.infrastructure.configurations.security.otp.User
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CachePut
import org.springframework.stereotype.Service
import java.util.Date


@Service
class OneTimePasswordService(private val cacheManager : CacheManager,
                             private  @Value("\${otp.ttl:300}") val expiryIntervalSeconds : Int) {
    private  val oneTimePasswordHelpService: OneTimePasswordHelpService = OneTimePasswordHelpService()


    @CachePut(cacheNames = [cacheName])
    fun getOneTimePassword(key : String) : OneTimePassword {
        val oneTimePassword = oneTimePasswordHelpService.createRandomOneTimePassword().get()
        val expires = Date(System.currentTimeMillis() + (expiryIntervalSeconds*1000) )
        return OneTimePassword(oneTimePassword,expires)

    }

    fun checkPassword(key : String, password : Int): Boolean {
        val recoveryPassword : Any? = cacheManager.getCache(cacheName)?.get(key)?.get()
        return if(recoveryPassword is OneTimePassword)
            recoveryPassword.passwrod == password
        else
            false
    }

    fun checkPassword(user: User): Boolean {
        return if(user.otp != null)
            checkPassword(user.id,user.otp!!)
        else
            false
    }

    companion object{
        const val cacheName = "onteTimePassword"
    }
}


class OneTimePassword(val passwrod : Int, expires : Date){

}