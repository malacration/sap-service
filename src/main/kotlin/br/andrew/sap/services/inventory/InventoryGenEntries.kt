package br.andrew.sap.services.inventory

import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.estoque.EntradaSaidaEstoque
import br.andrew.sap.model.romaneio.RomaneioPesagem
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.structs.QuerysServices
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class InventoryGenEntries(restTemplate: RestTemplate,
                        env: SapEnvrioment,
                        val querysServices: QuerysServices,
                        authService : AuthService
) : EntitiesService<EntradaSaidaEstoque>(env,restTemplate, authService)  {

    override fun path(): String {
        return "/b1s/v1/InventoryGenEntries"
    }
}