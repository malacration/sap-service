package br.andrew.sap.services.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest

/**
 * Gera/valida o codigo de liberacao (OTP) das travas da TransactionNotification.
 *
 * A formula precisa ser IDENTICA a da funcao HANA fnValidaOtpBypass
 * (repo sap-sql-sovis, src/functions/fnValidaOtpBypass.sql):
 *
 *   janela = floor(epochSegundosUTC / windowSeconds)
 *   input  = secret | janela | UPPER(TRIM(regra))
 *   codigo = SHA-256(input em UTF-8) -> HEX maiusculo -> primeiros `length` caracteres
 *
 * Mantenha secret/regra em ASCII: e o que garante TO_BINARY(NVARCHAR) no HANA
 * bater byte a byte com o UTF-8 daqui.
 *
 * O segredo mora so aqui (application.properties). Trocou o segredo? Troque tambem
 * na funcao HANA, senao os codigos param de bater.
 */
@Service
class TravaOtpService(
    @Value("\${trava.otp.secret}") private val secret: String,
    @Value("\${trava.otp.window-seconds:120}") private val windowSeconds: Long,
    @Value("\${trava.otp.length:6}") private val length: Int,
) {

    init {
        // Sem default no @Value: se trava.otp.secret nao estiver configurado, o Spring
        // ja falha no boot. Aqui barramos tambem o valor vazio/em branco. Esses codigos
        // burlam regras da TransactionNotification, entao nao pode haver segredo implicito.
        require(secret.isNotBlank()) {
            "trava.otp.secret nao configurado - defina um segredo forte (igual ao da funcao HANA fnValidaOtpBypass)"
        }
    }

    fun janelaAtual(epochSeconds: Long = System.currentTimeMillis() / 1000): Long =
        Math.floorDiv(epochSeconds, windowSeconds)

    fun segundosParaProximaJanela(epochSeconds: Long = System.currentTimeMillis() / 1000): Long =
        windowSeconds - Math.floorMod(epochSeconds, windowSeconds)

    fun gerar(regra: String, janela: Long = janelaAtual()): String {
        val regraNorm = regra.trim().uppercase()
        val input = "$secret|$janela|$regraNorm"
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        val hex = digest.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
        return hex.substring(0, length)
    }

    /** Aceita a janela atual e a anterior, igual a funcao HANA. */
    fun validar(regra: String, codigo: String?): Boolean {
        if (codigo.isNullOrBlank()) return false
        val alvo = codigo.trim().uppercase()
        val atual = janelaAtual()
        return alvo == gerar(regra, atual) || alvo == gerar(regra, atual - 1)
    }
}
