package br.andrew.sap.services.financeiro.retorno

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoAutomatico
import br.andrew.sap.model.financeiro.retorno.LancamentoRetornoLog
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/** Historico do Lancamento de Retorno (UDO LANC_RET_LOG). */
@Service
class LancamentoRetornoLogService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<LancamentoRetornoLog>(env, restTemplate, authService) {

    private val log = LoggerFactory.getLogger(LancamentoRetornoLogService::class.java)

    override fun path(): String = "/b1s/v1/LANC_RET_LOG"

    /**
     * Nunca lanca excecao: o historico nao pode desfazer nem bloquear um LCM que ja foi gravado.
     * Falha aqui vai so para o log da aplicacao.
     */
    fun registrar(
        origem: String,
        resultado: String,
        usuario: String,
        retorno: Int? = null,
        lcm: Int? = null,
        valor: BigDecimal? = null,
        mensagem: String? = null,
    ) {
        val agora = LocalDateTime.now()
        //instante legivel + sufixo aleatorio: duas instancias no mesmo milissegundo nao colidem
        val code = "${agora.format(CODE)}-${UUID.randomUUID().toString().take(6)}"
        try {
            save(LancamentoRetornoLog(
                Code = code, Name = code, U_Retorno = retorno, U_Origem = origem, U_Resultado = resultado,
                U_Lcm = lcm, U_Valor = valor?.toDouble(), U_Mensagem = mensagem?.take(254),
                U_Usuario = usuario.take(100), U_Data = agora.format(DATA),
            ))
        } catch (e: Exception) {
            log.error("Falha ao gravar o historico do Lancamento de Retorno ($origem $resultado retorno $retorno lcm $lcm)", e)
        }
    }

    /** Mais recentes primeiro. */
    fun ultimos(quantidade: Int = 200): List<LancamentoRetornoLog> =
        get(Filter(), OrderBy("Code", Order.DESC), Pageable.ofSize(quantidade)).tryGetValues()

    /** Ultima recusa do retorno, para a rotina nao repetir a mesma recusa a cada execucao. */
    fun ultimaRecusa(retorno: Int): LancamentoRetornoLog? =
        get(
            Filter(Predicate("U_Retorno", retorno, Condicao.EQUAL), Predicate("U_Resultado", "RECUSADO", Condicao.EQUAL)),
            OrderBy("Code", Order.DESC),
            Pageable.ofSize(1),
        ).tryGetValues<LancamentoRetornoLog>().firstOrNull()

    companion object {
        private val CODE = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        val DATA: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    }
}

/** Interruptor do lancamento automatico (UDO LANC_RET_CFG). */
@Service
class LancamentoRetornoAutomaticoService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val historico: LancamentoRetornoLogService,
) : EntitiesService<LancamentoRetornoAutomatico>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/LANC_RET_CFG"

    /** Sem a linha no SAP, desligado. */
    fun estado(): LancamentoRetornoAutomatico =
        get(Filter(Predicate("Code", CODIGO, Condicao.EQUAL))).tryGetValues<LancamentoRetornoAutomatico>().firstOrNull()
            ?: LancamentoRetornoAutomatico(CODIGO, CODIGO, "N")

    fun definir(ativo: Boolean, usuario: String): LancamentoRetornoAutomatico {
        val agora = LocalDateTime.now()
        val novo = LancamentoRetornoAutomatico(CODIGO, CODIGO, if (ativo) "Y" else "N", usuario.take(100),
            agora.format(LancamentoRetornoLogService.DATA))
        if (existe()) update(novo, CODIGO) else save(novo)
        historico.registrar("CONFIG", if (ativo) "LIGADO" else "DESLIGADO", usuario)
        return novo
    }

    private fun existe() = get(Filter(Predicate("Code", CODIGO, Condicao.EQUAL))).tryGetValues<LancamentoRetornoAutomatico>().isNotEmpty()

    companion object {
        const val CODIGO = "AUTOMATICO"
    }
}
