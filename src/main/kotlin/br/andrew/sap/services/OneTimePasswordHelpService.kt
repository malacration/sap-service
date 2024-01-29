package br.andrew.sap.services

import java.util.Random
import java.util.function.Supplier

class OneTimePasswordHelpService {

    private val LENGTH = 6

    fun createRandomOneTimePassword(): Supplier<Int> {
        return Supplier<Int> {
            val random = Random()
            val oneTimePassword = StringBuilder()
            for (i in 0 until LENGTH) {
                val randomNumber: Int = random.nextInt(10)
                oneTimePassword.append(randomNumber)
            }
            oneTimePassword.toString().trim { it <= ' ' }.toInt()
        }
    }
}