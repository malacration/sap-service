package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.infrastructure.configurations.security.authprovider.PhoneNumberAuthenticationProvider
import br.andrew.sap.infrastructure.configurations.security.filter.JwtFilter
import br.andrew.sap.infrastructure.configurations.security.filter.PhoneNumberAuthenticationFilter
import br.andrew.sap.services.OneTimePasswordService
import br.andrew.sap.services.my.UserJwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityWebConf(
    @Value("\${spring.security.disable:false}") val disable: Boolean,
    val otpService : OneTimePasswordService)  {

    val jwtFilter : JwtAuthenticationFilter = JwtAuthenticationFilter()

    val userService = UserJwtService()

    @Bean
    fun authorizationRequestRepository(http : HttpSecurity): DefaultSecurityFilterChain {
        val authManager : AuthenticationManager = ProviderManager(PhoneNumberAuthenticationProvider())
        return if(disable) {
            http.cors{
                it.disable()
            }.csrf {
                it.disable()
            }.authorizeHttpRequests {
                it.anyRequest().permitAll()
            }.build()
        }
        else{
            http.sessionManagement{it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
                .authenticationProvider(PhoneNumberAuthenticationProvider())
                .addFilterBefore(PhoneNumberAuthenticationFilter(authManager,otpService),UsernamePasswordAuthenticationFilter::class.java)
                .addFilterBefore(JwtFilter(),PhoneNumberAuthenticationFilter::class.java)
                .authorizeHttpRequests { it
                    .requestMatchers(
                        "/state**/**",
                        "/city**/**",
                        "/otp/cpf-cnpj/**",
                        "/business-partners/key/**",
                        "/business-partners/cpf-cnpj/contact/**",
                        "/invoice/cardcode/*/payment/**",
                        "/installment/*/paid",
                        "/invoice/*/parcela/**",
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST,"/business-partners/key/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.OPTIONS,"/**")
                    .permitAll()
                    .anyRequest().authenticated()
                }
                .csrf { it.disable() }
                .cors(CorsConfig().customizer)
                .build()
        }
    }

    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }

    fun authenticationProvider(): AuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userService)
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

}