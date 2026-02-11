package br.andrew.sap.infrastructure.configurations.uzzipay

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.uzzipay.ContaUzziPayPix
import br.andrew.sap.model.uzzipay.builder.RequestPixDueDateSemContaBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration


@Configuration
@ConfigurationProperties(prefix = "uzzipay", ignoreInvalidFields = true, ignoreUnknownFields = true)
class UzziPayEnvrioment {

    fun getContaByCnpj(cnpj: String): ContaUzziPayPix {
        return contas.firstOrNull() { it.cnpj == cnpj } ?: throw Exception("Conta não da uzzipay encontrada")
    }

    fun getContaByCnpj(bp : BussinessPlace): ContaUzziPayPix {
        return (
                contas.firstOrNull() { it.cnpj == bp.cnpjSemMascara() || it.cnpj == bp.FederalTaxID }
                ?: throw Exception("Conta não da uzzipay encontrada"))
            .also {
                it.bussinessPlace = bp
            }
    }

    lateinit var host : String
    lateinit var consultaHost : String
    var authUrl: String? = null
    var authHost: String? = null
    var authPath: String? = null
    var authGrantType: String? = null
    var authUsername: String? = null
    var authPassword: String? = null
    var authClientId: String? = null
    var authClientSecret: String? = null
    var authScope: String? = null
    var contas : List<ContaUzziPayPix> = listOf()
        set(value) {
            field = value
            if(field.isNotEmpty())
                RequestPixDueDateSemContaBuilder.setUzziPayEnvrioment(this)
        }

    fun buildAuthUrl(): String {
        authUrl?.takeIf { it.isNotBlank() }?.let { return it }
        val base = (authHost ?: host).trimEnd('/')
        val path = authPath?.takeIf { it.isNotBlank() } ?: "/auth/token"
        return "$base$path"
    }
}
