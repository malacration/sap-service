package br.andrew.sap.services.logistica
import br.andrew.sap.infrastructure.odata.NextLink
import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.model.sap.partner.CpfCnpj
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import java.text.Normalizer
import java.util.Locale

@Service
class LocalidadeService(val sqlQueriesService : SqlQueriesService, env : SapEnvrioment,
                        restTemplate: RestTemplate,
                        authService: AuthService)
    : EntitiesService<Localidade>(env, restTemplate,authService) {
    //UDO "Locais", que aponta pra @RO_LOCAIS (mesma tabela usada pela busca
    //full-text abaixo). NAO trocar pra AR_Localidade sem antes criar essa
    //tabela de verdade (ver RegiaoFreteConfiguration.kt como referencia do
    //padrao de migracao RO_* -> AR_* usado no Regiao) - AR_Localidade nunca
    //foi provisionada no SAP, so referenciada aqui por engano.
    override fun path(): String {
        return "/b1s/v1/Locais"
    }

    fun criar(localidade: Localidade): Localidade {
        val normalizada = normaliza(localidade)
        valida(normalizada)
        validaNomeDuplicado(normalizada)
        return save(normalizada).tryGetValue()
    }

    private fun valida(localidade: Localidade) {
        if(localidade.Code.isNullOrBlank())
            throw Exception("O codigo da localidade deve ser informado")
        if(localidade.Name.isNullOrBlank())
            throw Exception("O nome da localidade deve ser informado")
        if(!CODIGO_VALIDO.matches(localidade.Code))
            throw Exception("O codigo da localidade deve conter apenas letras e numeros, sem acentos ou caracteres especiais")
        if(!NOME_VALIDO.matches(localidade.Name))
            throw Exception("O nome da localidade deve conter apenas letras, numeros e espacos")
    }

    private fun validaNomeDuplicado(localidade: Localidade) {
        val existentes = get(Filter("Name", localidade.Name!!, Condicao.EQUAL))
            .tryGetValues<Localidade>()
        if(existentes.any { it.Code != localidade.Code })
            throw Exception("Ja existe uma localidade cadastrada com o nome ${localidade.Name}")
    }

    private fun normaliza(localidade: Localidade): Localidade {
        return Localidade(
            Code = normalizaCodigo(localidade.Code),
            Name = normalizaNome(localidade.Name),
        )
    }

    fun fullSearchTextFallBack(fullText: String, user: User): NextLink<Localidade> {
        if((fullText.startsWith("SQLQueries('localidade-search.sql')") || fullText.startsWith("SQLQueries('search-locality.sql')"))
            && fullText.contains("?"))
            return sqlQueriesService.nextLink(fullText)!!.tryGetNextValues()
        if(fullText.startsWith("SQLQueries('localidade-search.sql')") || fullText.startsWith("SQLQueries('search-locality.sql')"))
            return NextLink(listOf(), "")
        val busca = if(fullText.toDoubleOrNull() == null)
            normalizaBusca(fullText).replace("*", "%")
        else
            CpfCnpj(fullText).getWithMask()
        val parametros = listOf(
            Parameter("search","'%${busca}%'"),
        )
        return sqlQueriesService
            .execute("search-locality.sql", parametros)!!
            .tryGetNextValues()
    }

    private fun normalizaCodigo(value: String?): String {
        return removeAcentos(value.orEmpty())
            .uppercase(Locale.ROOT)
            .replace(Regex("[^A-Z0-9]"), "")
            .trim()
    }

    /**
     * Acento e preservado: "MANICORE" e "MANICORÉ" sao nomes diferentes e apagar o acento perde
     * informacao real. O Code continua sem acento - ele e a chave do UDO.
     *
     * Precisa andar junto com [normalizaBusca]: se um lado apagasse o acento e o outro nao, o
     * LIKE do HANA nunca casaria (ele e sensivel a acento tanto quanto a caixa).
     */
    private fun normalizaNome(value: String?): String {
        return value.orEmpty()
            .uppercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N} ]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /** Mesma regra de [normalizaNome], mais o curinga "*" que o usuario digita. */
    private fun normalizaBusca(value: String): String {
        return value
            .uppercase(Locale.ROOT)
            .replace(Regex("[^\\p{L}\\p{N} *]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun removeAcentos(value: String): String {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
    }

    companion object {
        private val CODIGO_VALIDO = Regex("^[A-Z0-9]+$")
        //acento permitido no nome; o codigo continua restrito por ser chave do UDO
        private val NOME_VALIDO = Regex("^[\\p{L}\\p{N} ]+$")
    }
}
