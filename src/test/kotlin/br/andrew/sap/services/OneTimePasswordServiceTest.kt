package br.andrew.sap.services

import br.andrew.sap.services.security.OneTimePasswordService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest


@SpringBootTest
class OneTimePasswordServiceTest {

    @Autowired
    var service : OneTimePasswordService? = null

    @Autowired
    @Value("\${otp.ttl:10}")
    var ttl : Int = 10

    @Test
    fun checkRecoveryTest(){
        val key = "cpf"
        val otp = service!!.getOneTimePassword(key)
        Assertions.assertTrue(otp.passwrod.toString().length>=5)
        Assertions.assertEquals(true,service!!.checkPassword(key,otp.passwrod))
    }

    @Test
    fun checkRecoveryFailTest(){
        val key = "cpf2"
        service!!.getOneTimePassword(key)
        Assertions.assertEquals(false,service!!.checkPassword(key,12354))
    }

    @Test
    fun checkRecoveryFailNoKeyTest(){
        val key = "nokey"
        Assertions.assertEquals(false,service!!.checkPassword(key,12354))
    }
}