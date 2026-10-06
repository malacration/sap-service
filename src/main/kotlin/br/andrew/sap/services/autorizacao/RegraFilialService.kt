package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sistema.RegraFilial
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.security.AuthService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Cadastro de onde cada regra vale (motivo -> filial), no mesmo espirito do AutorizadorService.
 *
 * NAO injeta RegraAutorizacaoService de proposito: e o RegraAutorizacaoService que depende
 * deste servico para filtrar as regras por filial, e injetar de volta fecharia um ciclo que
 * o Spring recusa no boot. Por isso a validacao de motivo recebe a lista pronta de quem
 * chama (o controller, que enxerga os dois).
 */
@Service
class RegraFilialService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    private val businessPlaceService: BussinessPlaceService,
) : EntitiesService<RegraFilial>(env, restTemplate, authService) {

    override fun path() = "/b1s/v1/regrafilial"

    fun getTodos(): List<RegraFilial> {
        return getAll(RegraFilial::class.java)
    }

    /**
     * Retrato do cadastro inteiro numa leitura so. Quem avalia varias regras deve pedir um
     * retrato e consultar todas contra ele: getTodos() e uma leitura paginada no Service Layer,
     * e chama-lo uma vez por regra multiplicava essa leitura pelo numero de regras em todo pedido.
     */
    fun cadastro(): CadastroRegraFilial = CadastroRegraFilial(getTodos())

    /**
     * @param motivosValidos motivos que o motor de regras realmente produz, vindos do
     *   controller. Motivo que nenhuma regra gera criaria uma restricao de filial para
     *   regra inexistente - inofensivo, mas engana quem le o cadastro achando que desligou algo.
     */
    fun criar(regraFilial: RegraFilial, motivosValidos: List<String>): RegraFilial {
        if (regraFilial.U_motivo.isBlank() || regraFilial.U_filial.isBlank())
            throw Exception("Motivo e filial sao obrigatorios")

        if (!motivosValidos.contains(regraFilial.U_motivo))
            throw Exception("Motivo '${regraFilial.U_motivo}' nao existe no motor de regras. " +
                "Motivos disponiveis: ${motivosValidos.joinToString(", ")}")

        regraFilial.U_filial = filialCadastrada(regraFilial.U_filial)

        val existentes = getTodos()
        if (existentes.any { it.U_motivo == regraFilial.U_motivo && it.U_filial == regraFilial.U_filial })
            throw Exception("A regra ${regraFilial.U_motivo} ja esta ativa na filial ${regraFilial.U_filial}")

        //mesma geracao de Code do AutorizadorService: @REGRAFILIAL e bott_MasterData e o
        //service layer recusa POST sem codigo, mesmo com ManageSeries ligado, quando nao ha
        //serie configurada no SAP do cliente
        regraFilial.Code = ((existentes.mapNotNull { it.Code?.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
        regraFilial.Name = regraFilial.Code
        return save(regraFilial).tryGetValue()
    }

    /**
     * Filial tem que existir no SAP, e e gravada no formato canonico do BPLID ("01" -> "1").
     *
     * Nao e so cosmetico: a PRIMEIRA linha de um motivo troca "vale em toda filial" por "vale
     * so nas filiais listadas" (CadastroRegraFilial.ativaPara). Um valor que nao casa com
     * nenhuma filial real - erro de digitacao, "01" em vez de "1", chamada direta a API -
     * desligaria a regra em TODAS as filiais em silencio, inclusive controles de credito.
     * Fonte: BusinessPlaces do SAP, nao o /branch do front (que pode vir filtrado por usuario).
     */
    private fun filialCadastrada(informada: String): String {
        val id = informada.trim().toIntOrNull()
        val filial = id?.let { bplid -> businessPlaceService.getAllBusinessPlaces().firstOrNull { it.BPLID == bplid } }
            ?: throw Exception("Filial '$informada' nao existe no SAP. Informe o codigo (BPLID) de uma filial cadastrada.")
        return filial.BPLID.toString()
    }

    fun remover(id: String) {
        delete("'$id'")
    }
}

class CadastroRegraFilial(private val linhas: List<RegraFilial>) {
    /**
     * A regra vale nessa filial?
     *
     * Motivo sem nenhuma linha cadastrada vale em TODA filial. Esse default existe para o
     * cadastro entrar sem mudar o comportamento atual: hoje toda regra do motor vale em
     * qualquer filial, e o contrario (nada cadastrado = regra desligada em todo lugar)
     * desativaria em silencio, no deploy, controles financeiros que ja estao valendo.
     */
    fun ativaPara(motivo: String, filial: String?): Boolean {
        val doMotivo = linhas.filter { it.U_motivo == motivo }
        if (doMotivo.isEmpty())
            return true
        return doMotivo.any { it.U_filial == filial }
    }
}
