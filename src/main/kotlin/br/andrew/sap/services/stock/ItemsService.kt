package br.andrew.sap.services.stock

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.calculadora.LastPrice
import br.andrew.sap.model.estoque.Item
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.ProdutoSelecao
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.concurrent.TimeUnit


@Service
open class ItemsService(
    env : SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val sqlQueriesService: SqlQueriesService,
    val cacheManager : CacheManager
) : EntitiesService<Item>(env, restTemplate,authService) {

    override fun path(): String {
        return "/b1s/v1/Items"
    }

    fun getPriceBase(itemCode: Product, priceListId : Int): Double {
        return getPriceBase(itemCode.id, priceListId)
    }

    fun getPriceBase(itemCode: String, priceListId : Int): Double {
        return getById("'$itemCode'")
            .tryGetValue<Item>()
            .itemPrices.filter { it.PriceList == priceListId }
            .firstOrNull()?.Price ?: throw Exception("Price[$priceListId] not found; ItemCode[$itemCode]")
    }

    fun fullSearchText(keyWord : String, idVendedor : Int, branchId : Int): NextLink<Product> {
        val parameters = listOf(
            Parameter("search","'%${keyWord.uppercase()}%'"),
            Parameter("zero",0),
            Parameter("yes","'Y'"),
            Parameter("vendedor",idVendedor),
            Parameter("branchId",branchId)
        )
        if(keyWord.contains("SQLQueries('produto-tabela.sql')"))
            return sqlQueriesService.nextLink(keyWord)!!.tryGetNextValues<Product>()
        return sqlQueriesService.execute("produto-tabela.sql", parameters)!!.tryGetNextValues<Product>()
    }

    fun produtosComEstrutura(prefix : String): List<ProdutoSelecao> {
        val parameters = listOf(
            Parameter("search","'$prefix%'"),
        )
        return sqlQueriesService.getAll<ProdutoSelecao>("calculadora-produtos.sql", parameters)
    }

    companion object{
        var produtos : MutableSet<Produto> = mutableSetOf()
    }

    fun getAllCached(itemCodes : List<String>): List<Produto> {
        val produtosParaProcurar = itemCodes.filter { !produtos.map { it.ItemCode }.contains(it) }
        val itensCacheado = produtos.filter { itemCodes.contains(it.ItemCode) }
        val itensNovos = if(produtosParaProcurar.isEmpty())
                listOf()
            else
                getAll(Produto::class.java,Filter("ItemCode", produtosParaProcurar, Condicao.IN))
        produtos.addAll(itensNovos)
        return itensNovos+itensCacheado.toList()
    }


    fun getLastPrice(itens : String, deposito : String): List<LastPrice> {
        val parameters = listOf(
            Parameter("item",itens),
            Parameter("deposito",deposito),
        )
        return sqlQueriesService.execute("item-last-prices.sql", parameters)!!.tryGetValues<LastPrice>()
    }
}


