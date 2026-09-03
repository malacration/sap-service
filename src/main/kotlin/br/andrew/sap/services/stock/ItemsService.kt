package br.andrew.sap.services.stock

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.calculadora.LastPrice
import br.andrew.sap.model.estoque.Item
import br.andrew.sap.model.calculadora.Produto
import br.andrew.sap.model.calculadora.ProdutoSelecao
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.Cacheable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

/**
 * A view fica no SAP, nao na aplicacao, e o nome do arquivo e o sqlCode dela - ou seja, ela e
 * GLOBAL e compartilhada por todas as APIs que apontam pra mesma company. Quando a v7.1
 * acrescentou o :superVendedor em produto-tabela.sql, o boot reescreveu a view no SAP e a versao
 * anterior da API, que continua chamando a mesma consulta sem esse parametro, parou de buscar
 * produto. Por isso a consulta nova mora num arquivo proprio: produto-tabela.sql ficou congelada
 * como contrato da API antiga e quem evolui e a v2. Ver src/main/resources/views/README.md.
 */
private const val VIEW_PRODUTO_TABELA = "produto-tabela-v2.sql"

//prefixo sem o sufixo de versao de proposito: casa com o nextLink da v2 e tambem com um emitido
//pela API antiga (produto-tabela.sql), que continua valendo no SAP - sem isso a URL da proxima
//pagina seria tratada como termo de busca
private const val PREFIXO_NEXTLINK_PRODUTO_TABELA = "SQLQueries('produto-tabela"

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

    /**
     * Busca de produtos para venda. A tabela de preco disponivel e limitada pelo @LIBERAPARA do
     * vendedor (por filial ou nominalmente), exceto para super vendedor - admin/vendedor_admin,
     * onde User.superVendedor() vale Int.MAX_VALUE e produto-tabela-v2.sql libera todas as tabelas
     * pelo "PriceList" < :superVendedor. Para vendedor comum superVendedor e -1 e a comparacao
     * nunca e verdadeira, mantendo a restricao. Mesmo idioma de contratos-vendafutura.sql e
     * parceiro-full-search-text.sql.
     *
     * Importa para o modo spring.security.disable=true: o usuario falso tem SlpCode -1, que nao
     * existe em OSLP nem em @LIBERAPARA - sem o vinculo de vendedor_admin a busca volta vazia.
     */
    fun fullSearchText(keyWord : String, idVendedor : Int, branchId : Int, superVendedor : Int = -1): NextLink<Product> {
        val parameters = listOf(
            Parameter("search","'%${keyWord.uppercase()}%'"),
            Parameter("zero",0),
            Parameter("yes","'Y'"),
            Parameter("vendedor",idVendedor),
            Parameter("branchId",branchId),
            Parameter("superVendedor",superVendedor)
        )
        if(keyWord.contains(PREFIXO_NEXTLINK_PRODUTO_TABELA))
            return sqlQueriesService.nextLink(keyWord)!!.tryGetNextValues<Product>()
        return sqlQueriesService.execute(VIEW_PRODUTO_TABELA, parameters)!!.tryGetNextValues<Product>()
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


    fun fullItemSearch(keyWord: String): NextLink<Product>{
        val parameters = listOf(
            Parameter("item","'%${keyWord.uppercase()}%'"),
            Parameter("zero",0),
        )
        if(keyWord.contains("SQLQueries('search-product.sql')"))
            return sqlQueriesService.nextLink(keyWord)!!.tryGetNextValues<Product>()
        return sqlQueriesService.execute("search-product.sql", parameters)!!.tryGetNextValues<Product>()

    }
}


