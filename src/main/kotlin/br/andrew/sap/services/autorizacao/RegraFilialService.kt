package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sistema.RegraFilial
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
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
) : EntitiesService<RegraFilial>(env, restTemplate, authService) {

    override fun path() = "/b1s/v1/regrafilial"

    fun getTodos(): List<RegraFilial> {
        return getAll(RegraFilial::class.java)
    }

    /**
     * A regra vale nessa filial?
     *
     * Motivo sem nenhuma linha cadastrada vale em TODA filial. Esse default existe para o
     * cadastro entrar sem mudar o comportamento atual: hoje toda regra do motor vale em
     * qualquer filial, e o contrario (nada cadastrado = regra desligada em todo lugar)
     * desativaria em silencio, no deploy, controles financeiros que ja estao valendo.
     */
    fun ativaPara(motivo: String, filial: String?): Boolean {
        val doMotivo = getTodos().filter { it.U_motivo == motivo }
        if (doMotivo.isEmpty())
            return true
        return doMotivo.any { it.U_filial == filial }
    }

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

    fun remover(id: String) {
        delete("'$id'")
    }
}
