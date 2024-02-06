package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.services.OneTimePasswordService
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit


@Configuration
class CacheConfig(val properties: CacheConfigurationProperties) {

    private val looger = LoggerFactory.getLogger(CacheConfig::class.java)

    @Bean
    fun cacheManager(): CacheManager {
        val manager = SimpleCacheManager()
        manager.setCaches(getCaches(ticker()))
        return manager
    }


    private fun getCaches(ticker: Ticker): List<CaffeineCache> {
        return properties.getCacheExpirations()
            .map { buildCache(it.key, ticker,it.value) }
    }

    private fun buildCache(name: String, ticker: Ticker, minutesToExpire: Long): CaffeineCache {
        return CaffeineCache(
            name, Caffeine.newBuilder()
                .expireAfterWrite(minutesToExpire, TimeUnit.SECONDS)
                .ticker(ticker)
                .build()
        )
    }

    @Bean
    fun ticker(): Ticker {
        return Ticker.systemTicker()
    }

}


@Configuration
class CacheConfigurationProperties(@Value("\${otp.ttl:300}") val otpTTL : Long) {
    private val minutos = 60L
    val timeoutSeconds: Long = 60 * minutos
    private val cacheExpirations: MutableMap<String, Long> = HashMap()

    //Cache in secondes
    init {
        cacheExpirations[OneTimePasswordService.cacheName] = otpTTL
        cacheExpirations["states"] = timeoutSeconds
        cacheExpirations["citys"] = timeoutSeconds
    }

    fun getCacheExpirations(): Map<String, Long> {
        return cacheExpirations
    }
}

