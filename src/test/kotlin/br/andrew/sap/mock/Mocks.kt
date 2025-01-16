package br.andrew.sap.mock

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.Session
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.BusinessPartnersService
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.springframework.web.client.RestTemplate

class Mocks {

    companion object{

        fun getSapEnvrioment() : SapEnvrioment{
            return SapEnvrioment("","","","")
        }

        fun getBusinessPartnersService(): BusinessPartnersService {
            return Mockito.mock(BusinessPartnersService::class.java)

        }

        fun getAuthService(): AuthService {
            val auth = Mockito.mock(AuthService::class.java)
            Mockito.`when`(auth.getToken(any())).thenReturn(Session("","",0))
            return auth
        }
    }
}