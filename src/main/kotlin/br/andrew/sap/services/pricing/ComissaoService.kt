package br.andrew.sap.services.pricing


import br.andrew.sap.model.Comissao
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.price.PriceList
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.cache.annotation.Caching
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ComissaoService(env : SapEnvrioment,
                      restTemplate: RestTemplate,
                      authService: AuthService,
                      private val priceList: PriceListsService)
    : EntitiesService<Comissao>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/comissao"
    }

    @Caching
    fun get(id : Int): Comissao {
        return getById("'$id'").tryGetValue();
    }

    fun getByIdTabela(idTabela: Int): Comissao {
        return get(priceList.getById(idTabela).tryGetValue<PriceList>().U_tipoComissao ?: throw Exception("Tabela sem tipo de comissão"))
    }
}