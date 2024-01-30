package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.infrastructure.configurations.security.otp.OneTimePasswordAuthenticationProvider
import br.andrew.sap.infrastructure.configurations.security.jwt.JwtAuthenticationFilter
import br.andrew.sap.infrastructure.configurations.security.jwt.JwtHandler
import br.andrew.sap.infrastructure.configurations.security.jwt.JwtSecretBean
import br.andrew.sap.infrastructure.configurations.security.otp.OneTimePasswordAuthenticationFilter
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
    jwtSecretBean : JwtSecretBean,
    private val otpService : OneTimePasswordService)  {

    private val jwtHandler = JwtHandler(jwtSecretBean)
    private val userService = UserJwtService()

    @Bean
    fun authorizationRequestRepository(http : HttpSecurity): DefaultSecurityFilterChain {
        val authManager : AuthenticationManager = ProviderManager(OneTimePasswordAuthenticationProvider())
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
                .authenticationProvider(OneTimePasswordAuthenticationProvider())
                .addFilterBefore(OneTimePasswordAuthenticationFilter(authManager,jwtHandler,otpService),UsernamePasswordAuthenticationFilter::class.java)
                .addFilterBefore(JwtAuthenticationFilter(jwtHandler),OneTimePasswordAuthenticationFilter::class.java)
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