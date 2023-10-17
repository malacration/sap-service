package br.andrew.sap.infrastructure.configurations.security

import br.andrew.sap.services.my.UserJwtService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.RequestMatchers


@Configuration
class SecurityWebConf(@Value("\${spring.security.disable:false}") val disable: Boolean) {

    val filter : JwtAuthenticationFilter = JwtAuthenticationFilter()
    val userService = UserJwtService()

    @Bean
    fun authorizationRequestRepository(http : HttpSecurity): DefaultSecurityFilterChain {
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
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java)
                .csrf { it.disable() }
                .cors{ it.disable() }
                .authorizeHttpRequests { it
                    .requestMatchers(
                        "/state**",
                        "/city**/**",
                        "/business-partners/key/**"
                    ).permitAll()
                    .requestMatchers(HttpMethod.POST,"/business-partners/key/**")
                    .permitAll()
                    .anyRequest().authenticated()
                }
                .build()
        }
    }
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf{
            it.disable()
        }.authorizeHttpRequests{
            it.anyRequest().authenticated()
        }.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        }.authenticationProvider(authenticationProvider())
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter::class.java).build()
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