package br.andrew.sap.services.logistica
import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.batch.BatchIdOnly
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService

@Service
class RegiaoService(val batchService : BatchService,
                    env : SapEnvrioment,
                    restTemplate: RestTemplate,
                    authService: AuthService)
    : EntitiesService<Regiao>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/Regiao"
    }

    /**
     * Paginacao em memoria (sobre getTodas) em vez de $top/$skip no service
     * layer: os filtros de "ativa"/"filial" precisam ser combinados com "and"
     * junto de um filtro de busca que ja usa "or" (Code/NomeRegiao), e o
     * Filter/Predicate atual so suporta um unico conector pra todos os
     * predicados - misturar os dois num $filter so daria uma condicao errada.
     * Como regiao e cadastro de baixo volume (poucas dezenas), paginar em
     * memoria e seguro.
     */
    fun getPage(search : String?, ativa : Boolean?, filial : Int?, page : Pageable) : Page<Regiao> {
        val todas = getTodas(search)
            .filter { ativa == null || it.ativa == ativa }
            .filter { filial == null || it.U_Filial == filial }
        val inicio = (page.pageNumber * page.pageSize).coerceIn(0, todas.size)
        val fim = (inicio + page.pageSize).coerceIn(inicio, todas.size)
        return PageImpl(todas.subList(inicio, fim), page, todas.size.toLong())
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
        regiao.Code = code
        if(regiao.Name.isNullOrBlank())
            regiao.Name = regiao.U_NomeRegiao
        regiao.U_CustoTransporte = validaCustoTransporte(regiao.U_CustoTransporte)
        //toda regiao nova comeca desativada - so passa a valer quando o
        //usuario ativa (ver ativar())
        regiao.U_Ativa = "0"
        save(regiao)
        return getRegiao(code)
    }

    /**
     * Edicao do cadastro da regiao: nome, coordenador e o proprio codigo.
     * As localidades, faixas, filial e situacao (ativa/inativa) nao sao
     * tocadas aqui - cada uma tem sua propria operacao.
     */
    fun atualizarCadastro(code : String, dados : Regiao) : Regiao {
        val nome = dados.U_NomeRegiao?.trim()
        if(nome.isNullOrBlank())
            throw Exception("O nome da regiao e obrigatorio")
        //parte do objeto completo lido do SAP (com linhas/faixas ja carregadas):
        //essas colecoes sao sempre serializadas (mesmo vazias), entao um patch
        //montado so com os campos vindos do front apagaria o que ja existe
        val regiao = getRegiao(code)
        regiao.U_NomeRegiao = nome
        val name = dados.Name?.trim()
        regiao.Name = if(name.isNullOrBlank()) (regiao.Name ?: nome) else name
        regiao.U_CodCordenador = dados.U_CodCordenador?.trim()
        regiao.U_CustoTransporte = validaCustoTransporte(dados.U_CustoTransporte)
        val novoCode = dados.Code?.trim()
        if(!novoCode.isNullOrBlank() && novoCode != code)
            return trocarCodigo(regiao, novoCode)
        update(regiao, "'$code'")
        return getRegiao(code)
    }

    /**
     * O Code e a chave do UDO no service layer - nao existe PATCH que troque
     * o codigo de uma regiao. Entao "renomear o codigo" e apagar a regiao e
     * recria-la com o novo Code, levando junto tudo que ela tinha (nome,
     * coordenador, filial, situacao, localidades e faixas de preco).
     *
     * DELETE antes do POST, num unico changeset batch: dentro do changeset as
     * operacoes rodam na mesma transacao, entao (a) o novo registro so e
     * inserido depois que o antigo saiu - se o SAP validar Name duplicado, a
     * copia nao esbarra nela - e (b) se qualquer uma das duas falhar, nenhuma
     * e aplicada, evitando perder a regiao no meio do caminho.
     *
     * Nenhum documento guarda o codigo da regiao (o frete e calculado achando
     * a regiao ativa da filial + localidade, ver DocumentForAngular), entao a
     * troca nao deixa referencia orfa.
     */
    private fun trocarCodigo(regiao : Regiao, novoCode : String) : Regiao {
        val codeAntigo = regiao.Code ?: throw Exception("Regiao sem codigo")
        if(existe(novoCode))
            throw Exception("Ja existe uma regiao com o codigo $novoCode")
        regiao.Code = novoCode
        //as linhas filhas carregam o Code do pai - sem reescrever, o service
        //layer recusaria a copia (ou gravaria linha apontando pro codigo velho)
        regiao.linhas.forEach { it.Code = novoCode }
        regiao.faixas.forEach { it.Code = novoCode }
        batchService.run(BatchList()
            .add(BatchMethod.DELETE, BatchIdOnly("'$codeAntigo'"), this)
            .add(BatchMethod.POST, regiao, this))
        return getRegiao(novoCode)
    }

    /**
     * Custo de transporte da fabrica ate a unidade da regiao, por unidade
     * vendida. Regiao cadastrada antes do campo existir vem nula - vale 0, e
     * nao null, pra o valor ir explicito no PATCH e poder ser zerado.
     */
    private fun validaCustoTransporte(valor : Double?) : Double {
        val custo = valor ?: 0.0
        if(custo < 0)
            throw Exception("O custo de transporte nao pode ser negativo")
        return custo
    }

    fun existe(code : String) : Boolean {
        return get(Filter("Code", code, Condicao.EQUAL))
            .tryGetValues<Regiao>()
            .isNotEmpty()
    }

    /**
     * Varias regioes podem compartilhar a mesma filial (ex.: regiao normal e
     * uma promocional) - a exclusividade e so entre regioes ATIVAS da mesma
     * filial, garantida em ativar().
     */
    fun atualizaFilial(code : String, filial : Int?) : Regiao {
        //envia o objeto completo (com linhas/faixas ja carregadas) - agora que
        //essas colecoes sao sempre serializadas (mesmo vazias), um patch parcial
        //apagaria as linhas/faixas existentes
        val regiao = getRegiao(code)
        //0 limpa o vinculo, o service layer nao aceita null em campo numerico de usuario
        regiao.U_Filial = filial ?: 0
        update(regiao, "'$code'")
        return getRegiao(code)
    }

    /**
     * So uma regiao pode estar ativa por filial ao mesmo tempo - ativar uma
     * regiao quando ja existe outra ativa na mesma filial e um erro (o
     * usuario precisa decidir explicitamente qual troca, ver substituir()).
     * Regioes sem filial vinculada nao disputam essa exclusividade entre si.
     */
    fun ativar(code : String) : Regiao {
        val regiao = getRegiao(code)
        val filial = regiao.U_Filial
        if(filial != null && filial != 0){
            val outraAtiva = getAll(Regiao::class.java)
                .firstOrNull { it.Code != code && it.U_Filial == filial && it.ativa }
            if(outraAtiva != null)
                throw Exception("A regiao ${outraAtiva.Code} ja esta ativa para essa filial - use Substituir Regiao para trocar")
        }
        regiao.U_Ativa = "1"
        update(regiao, "'$code'")
        return getRegiao(code)
    }

    fun desativar(code : String) : Regiao {
        val regiao = getRegiao(code)
        regiao.U_Ativa = "0"
        update(regiao, "'$code'")
        return getRegiao(code)
    }

    /**
     * Troca explicita da regiao ativa de uma filial: desativa `code` (que
     * precisa estar ativa) e ativa `novoCode` (que precisa estar inativa e
     * vinculada a mesma filial). As duas operacoes vao num unico changeset
     * batch - se uma falhar no SAP, a outra tambem nao e aplicada, evitando
     * ficar sem nenhuma regiao ativa (ou com duas) por causa de uma falha
     * no meio do caminho.
     */
    fun substituir(code : String, novoCode : String) : Regiao {
        if(code == novoCode)
            throw Exception("A regiao a substituir nao pode ser a mesma que sera ativada")
        val atual = getRegiao(code)
        if(!atual.ativa)
            throw Exception("A regiao $code nao esta ativa")
        val filial = atual.U_Filial
        if(filial == null || filial == 0)
            throw Exception("A regiao $code nao tem filial vinculada")
        val nova = getRegiao(novoCode)
        if(nova.ativa)
            throw Exception("A regiao $novoCode ja esta ativa")
        if(nova.U_Filial != filial)
            throw Exception("A regiao $novoCode nao esta vinculada a mesma filial que $code")

        atual.U_Ativa = "0"
        nova.U_Ativa = "1"
        batchService.run(BatchList()
            .add(BatchMethod.PATCH, atual, this)
            .add(BatchMethod.PATCH, nova, this))
        return getRegiao(novoCode)
    }

    /**
     * Uma localidade pode pertencer a varias regioes (ex.: regioes de vendedores
     * ou linhas de entrega diferentes cobrindo a mesma localidade). A distancia
     * e obrigatoria no vinculo (usada pelo calculo de frete) e e especifica de
     * cada regiao - a mesma localidade pode ter distancias diferentes em
     * regioes diferentes.
     */
    fun addLocalidade(code : String, codLocal : String, distanciaKm : Double) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).addLocalidade(codLocal, distanciaKm))
    }

    fun removeLocalidade(code : String, codLocal : String) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).removeLocalidade(codLocal))
    }

    fun atualizaDistancia(code : String, codLocal : String, distanciaKm : Double) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).atualizaDistancia(codLocal, distanciaKm))
    }

    fun getRegioesByLocalidade(codLocal : String) : List<Regiao> {
        return getAll(Regiao::class.java)
            .filter { it.temLocalidade(codLocal) }
    }

    /**
     * Envia o objeto Regiao completo (linhas/faixas ja carregadas do GET) com o
     * header "B1S-ReplaceCollectionsOnPatch: true", que instrui o Service Layer a
     * SUBSTITUIR a colecao filha pelo array enviado em vez de fazer merge com o
     * que ja existe - sem ele, remover uma linha so por omissao no array nao
     * era suficiente pra removê-la de fato. Usado tanto pras linhas (localidades)
     * quanto pras faixas (preco).
     */
    private fun salvarComColecoesSubstituidas(regiao : Regiao) : Regiao {
        exchangeWithValidSession(OData::class.java) { session ->
            RequestEntity
                .patch(env.host + this.path() + "('${regiao.Code}')")
                .header("cookie", session.cookieHeader())
                .header("B1S-ReplaceCollectionsOnPatch", "true")
                .body(regiao)
        }
        return getRegiao(regiao.Code!!)
    }

    fun addFaixa(code : String, qtdeMinima : Int, valorKm : Double) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).addFaixa(qtdeMinima, valorKm))
    }

    fun atualizaFaixa(code : String, lineId : Int, qtdeMinima : Int, valorKm : Double) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).atualizaFaixa(lineId, qtdeMinima, valorKm))
    }

    fun removeFaixa(code : String, lineId : Int) : Regiao {
        return salvarComColecoesSubstituidas(getRegiao(code).removeFaixa(lineId))
    }
}
