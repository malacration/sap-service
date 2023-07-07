package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.SapEnvrioment
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class SapConfig {

    @Bean
    fun test(
            @Value("\${sap.service.layer.url:https://localhost:50000}") host : String,
            @Value("\${sap.service.layer.user:user}") user : String,
            @Value("\${sap.service.layer.password:password}") password : String,
            @Value("\${sap.service.layer.companyDB:companyDB}") companyDB : String,
    ): SapEnvrioment {
        return SapEnvrioment(host,user,password,companyDB)
    }

}