package br.andrew.sap.infrastructure.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies.UPPER_CAMEL_CASE
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager
import org.apache.hc.client5.http.socket.ConnectionSocketFactory
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.core5.http.HttpStatus
import org.apache.hc.core5.http.config.Registry
import org.apache.hc.core5.http.config.RegistryBuilder
import org.apache.hc.core5.ssl.SSLContexts
import org.apache.hc.core5.ssl.TrustStrategy
import org.apache.hc.core5.util.TimeValue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext


@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(): RestTemplate? {
        val acceptingTrustStrategy = TrustStrategy { cert: Array<X509Certificate?>?, authType: String? -> true }
        val sslContext: SSLContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build()
        val sslsf = SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE)
        val socketFactoryRegistry: Registry<ConnectionSocketFactory> = RegistryBuilder.create < ConnectionSocketFactory ? > ()
                .register("https", sslsf)
                .register("http", PlainConnectionSocketFactory())
                .build()
        val connectionManager = PoolingHttpClientConnectionManager(socketFactoryRegistry)
        //sem isso, uma conexao do pool que morreu ociosa (ex.: tunel SSH derrubando
        //silenciosamente) e reusada as cegas. GET e retentado automaticamente pelo
        //HttpClient5 quando isso da NoHttpResponseException, mas PATCH/POST nao -
        //a excecao estoura direto pro chamador. Validar apos ociosidade elimina isso.
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(1))
        val httpClient = HttpClients.custom().setConnectionManager(connectionManager).build()

        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.httpClient = httpClient
        return RestTemplate(requestFactory).also {
            it.interceptors.add(LogRequisicaoComFalhaInterceptor())
        }
    }

}