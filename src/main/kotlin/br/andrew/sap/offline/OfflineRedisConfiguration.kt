package br.andrew.sap.offline

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@ConditionalOnProperty(prefix = "offline", name = ["enabled"], havingValue = "true")
class OfflineRedisConfiguration {

    @Bean
    fun offlineBinaryRedisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, ByteArray> {
        return RedisTemplate<String, ByteArray>().also {
            it.connectionFactory = connectionFactory
            it.keySerializer = StringRedisSerializer()
            it.valueSerializer = RedisSerializer.byteArray()
            it.afterPropertiesSet()
        }
    }
}
