package br.andrew.sap.infrastructure

import br.andrew.sap.model.sap.SalePerson
import br.andrew.sap.services.From
import br.andrew.sap.services.EmailAdrres
import br.andrew.sap.services.MailService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration


@Configuration
class MailConfig(@Value("\${mail.list:[{ \"email\": \"windson@windson\", \"name\": \"windson\", \"type\": \"DEFAULT\"}]}") val mail : String,
                 @Value("\${mail.copy:windson@windson}") copyDefault : List<String>){


    init {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        val lista = mapper.readValue(mail, jacksonTypeRef<List<EmailAdrres>>())
        lista.firstOrNull()?.let { SalePerson.emailDefault = it.email }
        lista.find { it.type == From.COMERCIAL }?.let { SalePerson.emailDefault = it.email }
        From.mailList.addAll(lista)
    }
}