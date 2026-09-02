package br.andrew.sap.services.cadastro

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.stock.ItemsService
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Locale

/**
 * Passa para maiusculo o nome dos cadastros de venda: produto (prefixo do filtro da calculadora),
 * localidade e cliente.
 *
 * So o NOME. Codigo (ItemCode/Code/CardCode) fica intacto de proposito: e chave, e renomear
 * quebraria a referencia em todo documento ja gravado.
 *
 * So caixa alta, nada de remover acento ou pontuacao - "Jose D'Avila" vira "JOSE D'AVILA", nao
 * "JOSE DAVILA". A normalizacao agressiva existe no LocalidadeService.normalizaNome e vale para
 * cadastro NOVO; aplicar aquilo retroativamente mudaria nome que ja esta em uso.
 */
@Service
class NormalizacaoCadastroService(
    val sqlQueriesService: SqlQueriesService,
    val itemsService: ItemsService,
    val localidadeService: LocalidadeService,
    val businessPartnersService: BusinessPartnersService,
    @Value("\${normalizacao.item.prefixo:PAC}") val prefixoItem: String) {

    val logger = LoggerFactory.getLogger(NormalizacaoCadastroService::class.java)
    private val mapper = ObjectMapper()

    companion object {
        const val ITEM = "item"
        const val LOCALIDADE = "localidade"
        const val CLIENTE = "cliente"
        //OITM, @RO_LOCAIS e OCRD num UNION ALL, ja filtrando "nome <> UPPER(nome)"
        const val VIEW_DIVERGENTES = "normalizacao-cadastro-divergente.sql"
    }

    /** Lista o que seria alterado, sem gravar nada. */
    fun previa(): List<CadastroParaNormalizar> {
        return buscarDivergencias()
    }

    /**
     * Aplica cadastro a cadastro. Falha em um NAO interrompe os outros: sao milhares de registros
     * independentes, e abortar tudo por causa de um deixaria a base pela metade sem relatorio.
     */
    fun aplicar(): List<CadastroParaNormalizar> {
        return buscarDivergencias().onEach { alvo ->
            try {
                //Aspas explicitas: a chave dos tres cadastros e texto, mas o EntitiesService.update
                //so poe aspas quando o id NAO parece numero. Localidade com codigo "230" saia como
                //Locais(230) e o Service Layer respondia "203 - Error in query syntax", porque o
                //Code do UDO e alfanumerico. Id ja entre aspas o update repassa intacto.
                servicoDe(alvo.tipo).update(patch(alvo), "'${alvo.codigo}'")
                alvo.aplicado = true
            } catch (e: Exception) {
                alvo.aplicado = false
                alvo.erro = e.message
                logger.error("erro ao normalizar ${alvo.tipo} ${alvo.codigo}", e)
            }
        }
    }

    /**
     * A comparacao acontece no banco: a view devolve SO os cadastros com minuscula, em vez de o
     * Service Layer paginar dezenas de milhares de produtos, localidades e clientes para o
     * middleware comparar um a um. Uma consulta no lugar de centenas de paginas.
     *
     * O banco e so o FILTRO - quem calcula o valor novo continua sendo o Kotlin, em
     * [divergencia]. UPPER() do HANA e uppercase() do Java podem discordar em caso de borda
     * (o 'B' alemao vira "SS" no Java), e gravar um valor que o proprio codigo nao produziria
     * seria pior do que deixar a linha passar. Por isso o resultado da view ainda e reconferido
     * aqui: linha que o Kotlin considera ja maiuscula e descartada.
     */
    private fun buscarDivergencias(): List<CadastroParaNormalizar> {
        return sqlQueriesService
            .getAll<CadastroDivergente>(VIEW_DIVERGENTES, listOf(Parameter("prefixo", "'$prefixoItem%'")))
            .mapNotNull { divergencia(it.tipo ?: return@mapNotNull null, it.codigo, it.nome) }
    }

    private fun divergencia(tipo: String, codigo: String?, nome: String?): CadastroParaNormalizar? {
        if(codigo.isNullOrBlank() || nome.isNullOrBlank())
            return null
        val maiusculo = nome.uppercase(Locale.ROOT)
        if(nome == maiusculo)
            return null
        return CadastroParaNormalizar(tipo, codigo, nome, maiusculo)
    }

    private fun servicoDe(tipo: String): EntitiesService<*> {
        return when(tipo) {
            ITEM -> itemsService
            LOCALIDADE -> localidadeService
            CLIENTE -> businessPartnersService
            else -> throw Exception("Tipo de cadastro desconhecido: $tipo")
        }
    }

    /**
     * Corpo minimo, so o campo do nome. Mandar a entidade inteira de volta num PATCH e o que faz
     * o Service Layer responder "Internal error (-5002)" quando alguma propriedade nao e aceita
     * na escrita. O JSON e montado pelo ObjectMapper, nao concatenado: nome com aspas ou barra
     * quebraria o payload.
     */
    private fun patch(alvo: CadastroParaNormalizar): String {
        val campo = when(alvo.tipo) {
            ITEM -> "ItemName"
            LOCALIDADE -> "Name"
            CLIENTE -> "CardName"
            else -> throw Exception("Tipo de cadastro desconhecido: ${alvo.tipo}")
        }
        return mapper.writeValueAsString(mapOf(campo to alvo.novo))
    }
}

data class CadastroParaNormalizar(
    val tipo: String,
    val codigo: String,
    val atual: String,
    val novo: String,
    var aplicado: Boolean? = null,
    var erro: String? = null)

/** Uma linha da view: os tres cadastros chegam no mesmo formato. */
@JsonIgnoreProperties(ignoreUnknown = true)
class CadastroDivergente(val tipo: String? = null, val codigo: String? = null, val nome: String? = null)
