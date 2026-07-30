package br.andrew.sap.services.logistica
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class RegiaoService(env : SapEnvrioment,
                    restTemplate: RestTemplate,
                    authService: AuthService)
    : EntitiesService<Regiao>(env, restTemplate, authService) {

    //TESTE REGIAO2: apontado temporariamente pro clone "Regiao2" (criado certo,
    //com CanCreateDefaultForm/CanLog) pra diagnosticar o bug de remocao de
    //linha reaproveitando a tela em Angular ja existente. Reverter pra
    //"/b1s/v1/Regiao" quando terminar (ver Regiao2TesteConfiguration.kt).
    override fun path(): String {
        return "/b1s/v1/Regiao2"
    }

    /**
     * O GET da colecao de um UDO ja retorna as colecoes filhas (linhas e faixas)
     * junto de cada linha, entao uma unica chamada paginada e suficiente. A descricao
     * (Name) de cada localidade e resolvida pelo front, nao aqui.
     */
    fun getPage(search : String?, page : Pageable) : Page<Regiao> {
        val filter = if(search.isNullOrBlank()) Filter() else Filter(
            propertieImmutable = listOf(
                Predicate("Code", search, Condicao.CONTAINS),
                Predicate("U_NomeRegiao", search, Condicao.CONTAINS),
            ),
            defaultConector = "or")
        return get(filter, page).tryGetPageValues<Regiao>(page)
    }

    fun getRegiao(code : String) : Regiao {
        return getById("'$code'").tryGetValue()
    }

    /**
     * Todas as regioes (sem paginacao), usado pela geracao da tabela de frete de
     * todas as regioes de uma vez - o nome das localidades e resolvido pelo front.
     */
    fun getTodas(search : String?) : List<Regiao> {
        val filter = if(search.isNullOrBlank()) Filter() else Filter(
            propertieImmutable = listOf(
                Predicate("Code", search, Condicao.CONTAINS),
                Predicate("U_NomeRegiao", search, Condicao.CONTAINS),
            ),
            defaultConector = "or")
        return getAll(Regiao::class.java, filter)
    }

    fun criar(regiao : Regiao) : Regiao {
        val code = regiao.Code?.trim()
        if(code.isNullOrBlank())
            throw Exception("O codigo da regiao e obrigatorio")
        if(regiao.U_NomeRegiao.isNullOrBlank())
            throw Exception("O nome da regiao e obrigatorio")
        if(existe(code))
            throw Exception("Ja existe uma regiao com o codigo $code")
        if(regiao.U_Filial != null)
            validaFilialDisponivel(regiao.U_Filial!!, code)
        regiao.Code = code
        if(regiao.Name.isNullOrBlank())
            regiao.Name = regiao.U_NomeRegiao
        save(regiao)
        return getRegiao(code)
    }

    fun existe(code : String) : Boolean {
        return get(Filter("Code", code, Condicao.EQUAL))
            .tryGetValues<Regiao>()
            .isNotEmpty()
    }

    /**
     * Uma filial so pode estar vinculada a uma unica regiao por vez.
     */
    fun atualizaFilial(code : String, filial : Int?) : Regiao {
        if(filial != null)
            validaFilialDisponivel(filial, code)
        //envia o objeto completo (com linhas/faixas ja carregadas) - agora que
        //essas colecoes sao sempre serializadas (mesmo vazias), um patch parcial
        //apagaria as linhas/faixas existentes
        val regiao = getRegiao(code)
        //0 limpa o vinculo, o service layer nao aceita null em campo numerico de usuario
        regiao.U_Filial = filial ?: 0
        update(regiao, "'$code'")
        return getRegiao(code)
    }

    private fun validaFilialDisponivel(filial : Int, codeIgnorado : String) {
        val regiaoAtual = getRegiaoByFilial(filial)
        if(regiaoAtual != null && regiaoAtual.Code != codeIgnorado)
            throw Exception("A filial $filial ja esta vinculada a regiao ${regiaoAtual.Code} - ${regiaoAtual.U_NomeRegiao}")
    }

    fun getRegiaoByFilial(filial : Int) : Regiao? {
        return getAll(Regiao::class.java)
            .firstOrNull { it.U_Filial == filial }
    }

    /**
     * A localidade so pode pertencer a uma regiao, entao antes de vincular
     * verificamos se ela ja esta em uso em outra regiao.
     */
    fun addLocalidade(code : String, codLocal : String, distanciaKm : Double?) : Regiao {
        val regiaoAtual = getRegiaoByLocalidade(codLocal)
        if(regiaoAtual != null && regiaoAtual.Code != code)
            throw Exception("A localidade $codLocal ja pertence a regiao ${regiaoAtual.Code} - ${regiaoAtual.U_NomeRegiao}")
        return salvarLinhas(getRegiao(code).addLocalidade(codLocal, distanciaKm))
    }

    fun removeLocalidade(code : String, codLocal : String) : Regiao {
        return salvarLinhas(getRegiao(code).removeLocalidade(codLocal))
    }

    fun atualizaDistancia(code : String, codLocal : String, distanciaKm : Double) : Regiao {
        return salvarLinhas(getRegiao(code).atualizaDistancia(codLocal, distanciaKm))
    }

    fun getRegiaoByLocalidade(codLocal : String) : Regiao? {
        return getAll(Regiao::class.java)
            .firstOrNull { it.temLocalidade(codLocal) }
    }

    /**
     * O service layer substitui a colecao filha inteira quando ela vem no patch,
     * por isso enviamos sempre todas as linhas ja com os seus LineId.
     * TESTE: envia o objeto Regiao completo (nao so a colecao isolada), ja que
     * ele sempre vem carregado junto no GET - talvez o service layer precise
     * do contexto completo do pai pra processar a remocao de linha certo.
     */
    private fun salvarLinhas(regiao : Regiao) : Regiao {
        update(regiao, "'${regiao.Code}'")
        return getRegiao(regiao.Code!!)
    }

    fun addFaixa(code : String, qtdeMinima : Int, valorKm : Double) : Regiao {
        return salvarFaixas(getRegiao(code).addFaixa(qtdeMinima, valorKm))
    }

    fun atualizaFaixa(code : String, lineId : Int, qtdeMinima : Int, valorKm : Double) : Regiao {
        return salvarFaixas(getRegiao(code).atualizaFaixa(lineId, qtdeMinima, valorKm))
    }

    fun removeFaixa(code : String, lineId : Int) : Regiao {
        return salvarFaixas(getRegiao(code).removeFaixa(lineId))
    }

    private fun salvarFaixas(regiao : Regiao) : Regiao {
        update(regiao, "'${regiao.Code}'")
        return getRegiao(regiao.Code!!)
    }

    /**
     * TESTE REGIAO2 (diagnostico, remover junto com o resto): tenta remover a
     * linha via DELETE explicito por chave composta na colecao filha, em vez
     * do PATCH por omissao usado em salvarLinhas. No objeto "Regiao" original
     * isso apagava a regiao inteira ao inves de so a linha - testando aqui se
     * o mesmo acontece no "Regiao2", criado do jeito certo.
     */
    fun removeLocalidadeTesteExplicito(code : String, codLocal : String) : String {
        val linha = getRegiao(code).linhas.firstOrNull { it.U_Locais == codLocal }
            ?: throw Exception("Localidade $codLocal nao encontrada na regiao $code")
        exchangeWithValidSession(OData::class.java) { session ->
            RequestEntity
                .delete(env.host + this.path() + "('$code')/RO_REGIAO2_LINHASCollection(LineId=${linha.LineId})")
                .header("cookie", session.cookieHeader())
                .build()
        }
        return try {
            val depois = getRegiao(code)
            "Parent sobreviveu. Localidades restantes: ${depois.linhas.map { it.U_Locais }}"
        } catch (e : Exception) {
            "Parent SUMIU apos o delete explicito: ${e.message}"
        }
    }
}
