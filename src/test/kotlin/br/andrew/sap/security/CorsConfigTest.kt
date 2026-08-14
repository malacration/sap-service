package br.andrew.sap.security

import br.andrew.sap.infrastructure.security.CorsConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CorsConfigTest {

    private fun checkOrigin(cors : CorsConfig, origin : String) =
        cors.getCorsConfig().checkOrigin(origin)

    @Test
    fun liberaOrigemVindaDoAmbiente(){
        val cors = CorsConfig(listOf("https://sustennutri-v7.artempestade.com.br"))
        Assertions.assertEquals(
            "https://sustennutri-v7.artempestade.com.br",
            checkOrigin(cors,"https://sustennutri-v7.artempestade.com.br"))
    }

    //o header Origin nunca traz path, entao os sufixos precisam ser ignorados
    @Test
    fun ignoraSufixoDePathNaConfiguracao(){
        listOf(
            "https://sustennutri-v7.artempestade.com.br/",
            "https://sustennutri-v7.artempestade.com.br/*",
            "https://sustennutri-v7.artempestade.com.br/**"
        ).forEach {
            val cors = CorsConfig(listOf(it))
            Assertions.assertEquals(
                "https://sustennutri-v7.artempestade.com.br",
                checkOrigin(cors,"https://sustennutri-v7.artempestade.com.br"),
                "origem configurada como '$it' deveria ser liberada")
        }
    }

    //docker-compose/k8s entregam as aspas dentro do valor da variavel
    @Test
    fun ignoraAspasVindasDaVariavelDeAmbiente(){
        val cors = CorsConfig(listOf("\"https://sustennutri-v7.artempestade.com.br/*\"","\"http://172.18.10.76:8090\""))
        Assertions.assertEquals(
            "https://sustennutri-v7.artempestade.com.br",
            checkOrigin(cors,"https://sustennutri-v7.artempestade.com.br"))
        Assertions.assertEquals(
            "http://172.18.10.76:8090",
            checkOrigin(cors,"http://172.18.10.76:8090"))
    }

    @Test
    fun mantemLocalhostLiberadoEmQualquerPorta(){
        val cors = CorsConfig(listOf("https://sustennutri-v7.artempestade.com.br"))
        Assertions.assertEquals("http://localhost:4200",checkOrigin(cors,"http://localhost:4200"))
        Assertions.assertEquals("http://localhost:8080",checkOrigin(cors,"http://localhost:8080"))
    }

    @Test
    fun bloqueiaOrigemNaoConfigurada(){
        val cors = CorsConfig(listOf("https://sustennutri-v7.artempestade.com.br"))
        Assertions.assertNull(checkOrigin(cors,"https://sustennutri-v7.artempestade.com.br.evil.com"))
        Assertions.assertNull(checkOrigin(cors,"http://sustennutri-v7.artempestade.com.br"))
    }

    //preflight do request real: GET com cache-control/pragma (nao safelisted)
    @Test
    fun liberaPreflightDoRequestDoNavegador(){
        val config = CorsConfig(listOf("https://sustennutri-v7.artempestade.com.br")).getCorsConfig()
        Assertions.assertNotNull(config.checkOrigin("https://sustennutri-v7.artempestade.com.br"))
        Assertions.assertNotNull(config.checkHttpMethod(org.springframework.http.HttpMethod.GET))
        Assertions.assertNotNull(config.checkHeaders(listOf("cache-control","pragma","content-type")))
    }
}
