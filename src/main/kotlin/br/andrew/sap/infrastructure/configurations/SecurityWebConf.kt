package br.andrew.sap.infrastructure.configurations

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

//
//@Configuration
//class SecurityWebConf(@Value("\${spring.security.disable:false}") val disable: Boolean) {
//
//    @Bean
//    fun authorizationRequestRepository(http : HttpSecurity): DefaultSecurityFilterChain {
//        return if(disable) {
//            http.cors{
//                it.disable()
//            }.anonymous{
//                it.disable()
//            }.authorizeHttpRequests {
//                it.anyRequest().permitAll()
//            }.build()
//        }
//        else{
//            http.cors{
//                it.disable()
//            }.authorizeHttpRequests {
//                it.anyRequest().permitAll()
//            }.build()
//        }
//    }
//
//}