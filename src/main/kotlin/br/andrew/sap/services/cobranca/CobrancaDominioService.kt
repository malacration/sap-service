package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.cobranca.CobrancaDominio
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class CobrancaDominioService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<CobrancaDominio>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/COB_DOMINIO"

    @Cacheable("cobranca-dominio")
    fun listar(tipo: String? = null): List<CobrancaDominio> {
        val filtro = Filter(Predicate("U_Ativo", "Y", Condicao.EQUAL))
        if (tipo != null)
            filtro.add(Predicate("U_Tipo", tipo, Condicao.EQUAL))
        return getAll(CobrancaDominio::class.java, filtro)
            .sortedWith(compareBy({ it.U_Tipo }, { it.U_Ordem ?: 0 }))
    }

    @CacheEvict("cobranca-dominio", allEntries = true)
    fun salvar(dominio: CobrancaDominio): CobrancaDominio {
        val tipo = dominio.U_Tipo ?: throw CobrancaException("Tipo é obrigatório")
        val codigo = dominio.U_Codigo ?: throw CobrancaException("Código é obrigatório")
        dominio.Code = CobrancaDominio.code(tipo, codigo)

        val existente = get(Filter(Predicate("Code", dominio.Code!!, Condicao.EQUAL)))
            .tryGetValues<CobrancaDominio>().firstOrNull()

        return if (existente == null)
            save(dominio).tryGetValue()
        else {
            update(dominio, dominio.Code!!)
            get(Filter(Predicate("Code", dominio.Code!!, Condicao.EQUAL))).tryGetValues<CobrancaDominio>().first()
        }
    }

    fun findOrCreate(tipo: String, codigo: String, descricao: String, ordem: Int) {
        val code = CobrancaDominio.code(tipo, codigo)
        val existente = get(Filter(Predicate("Code", code, Condicao.EQUAL))).tryGetValues<CobrancaDominio>()
        if (existente.isEmpty())
            salvarComRetentativa(CobrancaDominio(code, tipo, codigo, descricao, ordem, "Y"))
    }

    // Logo apos o boot criar os UDFs de @COB_DOMINIO, o Service Layer pode demorar
    // alguns segundos pra reconhecer o campo novo e recusa o primeiro POST com
    // "Property '...' is invalid" (armadilha documentada no CLAUDE.md do backend).
    // Como o seeder grava 54 linhas em sequencia logo no boot, isso e reproduzivel;
    // tenta de novo com espera crescente antes de derrubar a aplicacao.
    private fun salvarComRetentativa(dominio: CobrancaDominio) {
        val tentativas = 5
        val esperaBaseMs = 500L
        repeat(tentativas) { tentativa ->
            try {
                save(dominio)
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
