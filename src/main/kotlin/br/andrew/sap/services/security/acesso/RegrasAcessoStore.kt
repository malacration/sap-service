package br.andrew.sap.services.security.acesso

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.acesso.AcessoRegrasVersao
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Versoes das regras de acesso no SAP (UDO ACESSO_REGRAS). So existe com `fields.acesso=true`.
 *
 * As consultas usam `$select` (o EntitiesService.get nao tem): o cache confere a versao a cada
 * minuto e a tela lista o historico, e nenhum dos dois precisa baixar o memo com o documento.
 */
@Service
@ConditionalOnProperty(value = ["fields.acesso"], havingValue = "true", matchIfMissing = false)
class RegrasAcessoStore(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
    EntitiesService<AcessoRegrasVersao>(env, restTemplate, authService), RegrasAcessoRepositorio {

    override fun path(): String = "/b1s/v1/ACESSO_REGRAS"

    private val metadados = "Code,Name,U_Usuario,U_UsuarioId,U_Data,U_Hora,U_Origem,U_Comentario,U_Resumo"

    override fun ultimaVersao(): Int? =
        consultar("\$select=Code&\$orderby=Code desc&\$top=1", 1)
            .tryGetValues<AcessoRegrasVersao>().firstOrNull()?.numeroDaVersao()

    override fun buscar(versao: Int): AcessoRegrasVersao? =
        consultar("\$filter=Code eq '${AcessoRegrasVersao.codigo(versao)}'&\$top=1", 1)
            .tryGetValues<AcessoRegrasVersao>().firstOrNull()

    override fun listar(limite: Int): List<AcessoRegrasVersao> =
        consultar("\$select=$metadados&\$orderby=Code desc&\$top=$limite", limite)
            .tryGetValues<AcessoRegrasVersao>()

    override fun gravar(versao: AcessoRegrasVersao) {
        val code = versao.Code ?: throw IllegalArgumentException("Code da versao e obrigatorio")
        try {
            salvarComRetentativa(versao)
        } catch (e: Exception) {
            // Nao da para confiar no texto do erro do SAP (idioma, 5xx, timeout com a linha gravada).
            // Pergunta ao SAP de quem e a linha: se for desta tentativa a gravacao deu certo.
            val dono = try { idEscritaDe(code) } catch (_: Exception) { throw e }
            decidirAposFalhaNaGravacao(e, versao, dono)
        }
    }

    /** null = a linha nao existe; "" = existe sem identificador (gravada por outra origem). */
    private fun idEscritaDe(code: String): String? =
        consultar("\$select=Code,U_IdEscrita&\$filter=Code eq '$code'&\$top=1", 1)
            .tryGetValues<AcessoRegrasVersao>().firstOrNull()?.let { it.U_IdEscrita ?: "" }

    private fun consultar(query: String, tamanhoDaPagina: Int): OData =
        exchangeWithValidSession(OData::class.java) { session ->
            RequestEntity
                .get(env.host + path() + "?" + query)
                .header("cookie", session.cookieHeader())
                .header("Prefer", "odata.maxpagesize=$tamanhoDaPagina")
                .build()
        }.body ?: OData()

    // A tabela recem-criada as vezes ainda esta propagando quando o seed roda e o SAP responde
    // "is invalid". Reenvia com backoff (mesmo padrao do TravaRegraService).
    private fun salvarComRetentativa(versao: AcessoRegrasVersao) {
        val tentativas = 5
        val esperaBaseMs = 500L
        repeat(tentativas) { tentativa ->
            try {
                save(versao)
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

/**
 * O POST da versao falhou; `dono` e o U_IdEscrita da linha que o SAP tem hoje nesse Code.
 *  - null: a linha nao existe, o erro foi real - relanca.
 *  - igual ao nosso: a gravacao deu certo e so a resposta se perdeu - segue.
 *  - qualquer outro (inclusive vazio): outra gravacao ficou com o Code - conflito.
 */
internal fun decidirAposFalhaNaGravacao(erro: Exception, versao: AcessoRegrasVersao, dono: String?) {
    when {
        dono == null -> throw erro
        dono == versao.U_IdEscrita -> return
        else -> throw RegrasConflitoException(
            "A versao ${versao.numeroDaVersao()} das regras ja foi gravada por outra pessoa. Recarregue a tela."
        )
    }
}
