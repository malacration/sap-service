package br.andrew.sap.services.security

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.trava.TravaRegra
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Leitura/escrita do catálogo de regras de trava no SAP (UDO TRAVA_REGRA).
 * Espelha o padrão de CobrancaDominioService. Sem cache: a lista é pequena e
 * precisa refletir na hora quando uma regra é cadastrada no SAP.
 */
@Service
class TravaRegraService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<TravaRegra>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/TRAVA_REGRA"

    fun listar(todas: Boolean = false): List<TravaRegra> {
        val filtro = if (todas) Filter() else Filter(Predicate("U_Ativo", "Y", Condicao.EQUAL))
        return getAll(TravaRegra::class.java, filtro).sortedBy { it.U_Ordem ?: 0 }
    }

    fun salvar(regra: TravaRegra): TravaRegra {
        val codigo = regra.Code?.trim()?.uppercase()
            ?: throw Exception("Código da regra é obrigatório")
        regra.Code = codigo
        val existente = get(Filter(Predicate("Code", codigo, Condicao.EQUAL)))
            .tryGetValues<TravaRegra>().firstOrNull()
        return if (existente == null)
            save(regra).tryGetValue()
        else {
            update(regra, codigo)
            get(Filter(Predicate("Code", codigo, Condicao.EQUAL))).tryGetValues<TravaRegra>().first()
        }
    }

    fun deletar(codigo: String) {
        delete("'${codigo.trim().uppercase()}'")
    }

    /** Usado pelo seeder no boot. Idempotente. */
    fun findOrCreate(codigo: String, descricao: String, ordem: Int) {
        val code = codigo.trim().uppercase()
        val existente = get(Filter(Predicate("Code", code, Condicao.EQUAL))).tryGetValues<TravaRegra>()
        if (existente.isEmpty())
            salvarComRetentativa(TravaRegra(code, descricao, ordem, "Y"))
    }

    // A tabela recém-criada às vezes ainda está propagando quando o seeder roda;
    // o SAP responde "is invalid". Reenvia com backoff. (Mesmo padrão do CobrancaDominioService.)
    private fun salvarComRetentativa(regra: TravaRegra) {
        val tentativas = 5
        val esperaBaseMs = 500L
        repeat(tentativas) { tentativa ->
            try {
                save(regra)
                return
            } catch (e: Exception) {
                val ultimaTentativa = tentativa == tentativas - 1
                if (ultimaTentativa || e.message?.contains("is invalid") != true)
                    throw e
                Thread.sleep(esperaBaseMs * (tentativa + 1))
            }
        }
    }
}
