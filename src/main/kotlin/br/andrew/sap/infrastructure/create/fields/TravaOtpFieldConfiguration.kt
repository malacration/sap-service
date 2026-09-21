package br.andrew.sap.infrastructure.create.fields

import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import br.andrew.sap.services.structs.UserFieldsMDService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

/**
 * Campo onde o usuario digita o codigo de liberacao (OTP) para destravar a
 * TransactionNotification. Criado no header de documento de marketing: em SAP B1
 * o UDF de header e compartilhado entre todos os documentos de marketing, entao
 * criar em OINV faz o U_otp_liberacao aparecer em cotacao/pedido/entrega/nota etc.
 *
 * A trava le esse campo e passa para a funcao HANA fnValidaOtpBypass; o codigo e
 * gerado pelo /trava/otp (TravaOtpController).
 */
@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields"], havingValue = "true", matchIfMissing = true)
class TravaOtpFieldConfiguration(val userFieldsMDService: UserFieldsMDService) {

    init {
        userFieldsMDService.findOrCreate(
            FieldMd("otp_liberacao", "Código de liberação", "OINV", DbType.db_Alpha).also {
                it.size = 8
            }
        )
    }
}
