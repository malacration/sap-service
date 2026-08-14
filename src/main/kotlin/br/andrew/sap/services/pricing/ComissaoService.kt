package br.andrew.sap.services.pricing


import br.andrew.sap.model.sistema.Comissao
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.price.PriceList
import br.andrew.sap.services.security.AuthService
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

    fun getTodas(): List<Comissao> {
        return getAll(Comissao::class.java)
    }

    fun criar(comissao: Comissao): Comissao {
        return save(comissao).tryGetValue()
    }

    fun atualizar(id: Int, comissao: Comissao): Comissao {
        update(comissao, "'$id'")
        return get(id)
    }
}