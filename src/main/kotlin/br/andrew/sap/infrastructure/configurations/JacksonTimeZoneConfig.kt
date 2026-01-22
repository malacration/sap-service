package br.andrew.sap.infrastructure.configurations

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.util.*


@Configuration
class JacksonTimeZoneConfig {

    @Bean
    fun jacksonObjectMapperCustomization(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder ->
            jacksonObjectMapperBuilder.timeZone(
                TimeZone.getDefault()
            )
            jacksonObjectMapperBuilder.modulesToInstall(
                JavaTimeModule(),
                KotlinModule.Builder().build()
            )
            jacksonObjectMapperBuilder.featuresToEnable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
        }
    }
}
