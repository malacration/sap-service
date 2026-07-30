package br.andrew.sap.mock

import br.andrew.sap.model.sistema.Login
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.sistema.Session
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.cadastro.BusinessPartnersService
import org.mockito.Mockito
import org.mockito.kotlin.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.net.URI

class Mocks {

    companion object{

        fun getSapEnvrioment() : SapEnvrioment{
            return SapEnvrioment("","","","")
        }

        fun getBusinessPartnersService(): BusinessPartnersService {
            return Mockito.mock(BusinessPartnersService::class.java)

        }

        fun getAuthService(): AuthService {
            val env = getSapEnvrioment()
            val rest = Mockito.mock(RestTemplate::class.java)
            whenever(
                rest.postForEntity(
                    any<URI>(),
                    any<Login>(),
                    eq(Session::class.java)
                )
            ).thenReturn(ResponseEntity.ok(Session("mock-session", "", 30)))
            return AuthService(env, rest)
        }
    }
}
