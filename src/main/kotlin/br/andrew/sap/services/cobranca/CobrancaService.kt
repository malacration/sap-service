package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAcaoLoteItem
import br.andrew.sap.model.cobranca.CobrancaAcaoRequest
import br.andrew.sap.model.cobranca.CobrancaAcaoResultado
import br.andrew.sap.model.cobranca.CobrancaException
import br.andrew.sap.model.cobranca.CobrancaHistorico
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

@Service
class CobrancaService(
    env: SapEnvrioment,
    restTemplate: RestTemplate,
    authService: AuthService,
    val consultaService: CobrancaConsultaService,
) : EntitiesService<CobrancaRegistro>(env, restTemplate, authService) {

    private val logger = LoggerFactory.getLogger(CobrancaService::class.java)

    // Lock em memoria - so protege dentro do mesmo processo. Nao cobre a janela breve de
    // deploy com 2 instancias simultaneas, aceita como risco residual (ver PR).
    private val locksPorTitulo = ConcurrentHashMap<String, Any>()

    override fun path(): String = "/b1s/v1/COB_TITULO"

    @CacheEvict("cobranca-dashboard", allEntries = true)
    fun registrarAcao(tipo: String, docEntry: Int, instlmntId: Int, req: CobrancaAcaoRequest, auth: User): CobrancaRegistro {
        validarTipo(tipo)
        validar(req)
        val code = CobrancaRegistro.code(tipo, docEntry, instlmntId)
        val agora = LocalDate.now().toString()
        val cobrador = auth._name

        val novaLinha = CobrancaHistorico(
            U_Data = agora,
            U_Hora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            U_Usuario = cobrador,
            U_Cobrador = cobrador,
            U_Status = req.status,
            U_Acao = req.acao,
            U_Situacao = req.situacao,
            U_Ocorrencia = req.ocorrencia,
            U_Observacao = req.observacao,
        )

        synchronized(locksPorTitulo.computeIfAbsent(code) { Any() }) {
            val existente = buscarPorCode(code)

            if (existente == null) {
                val titulo = consultaService.buscarTituloParaEscopo(tipo, docEntry, instlmntId)
                    ?: throw CobrancaException("Parcela não encontrada no SAP: $tipo $docEntry/$instlmntId")

                val registro = CobrancaRegistro(
                    Code = code,
                    U_Tipo = tipo,
                    U_DocEntry = docEntry,
                    U_InstlmntID = instlmntId,
                    U_CardCode = titulo.CardCode,
                    U_Status = req.status,
                    U_Acao = req.acao,
                    U_Situacao = req.situacao,
                    U_Ocorrencia = req.ocorrencia,
                    U_Observacao = req.observacao,
                    U_Cobrador = cobrador,
                    U_DataAcao = agora,
                    U_DataPromessa = req.dataPromessa,
                    historico = mutableListOf(novaLinha),
                )
                val criado = save(registro).tryGetValue<CobrancaRegistro>()
                alertarSeODocEntryNaoPersistiu(criado, docEntry)
                return criado
            }

            existente.historico.add(novaLinha)

            val payload = mutableMapOf<String, Any?>(
                "U_Cobrador" to cobrador,
                "U_DataAcao" to agora,
                "COB_TITULO_LCollection" to existente.historico,
            )
            req.status?.let { payload["U_Status"] = it }
            req.acao?.let { payload["U_Acao"] = it }
            req.situacao?.let { payload["U_Situacao"] = it }
            req.ocorrencia?.let { payload["U_Ocorrencia"] = it }
            req.observacao?.let { payload["U_Observacao"] = it }
            (req.dataPromessa ?: existente.U_DataPromessa)?.let { payload["U_DataPromessa"] = it }
            repoeCamposDeRegistroLegado(existente, payload, tipo, docEntry, instlmntId)

            update(payload, code)
            return buscarPorCode(code) ?: throw CobrancaException("Falha ao reler o registro de cobrança $code")
        }
    }

    @CacheEvict("cobranca-dashboard", allEntries = true)
    fun registrarAcaoEmLote(itens: List<CobrancaAcaoLoteItem>, auth: User): List<CobrancaAcaoResultado> {
        return itens.map { item ->
            try {
                registrarAcao(item.tipo, item.docEntry, item.instlmntId, item.toAcaoRequest(), auth)
                CobrancaAcaoResultado(item.tipo, item.docEntry, item.instlmntId, success = true)
            } catch (e: Exception) {
                CobrancaAcaoResultado(item.tipo, item.docEntry, item.instlmntId, success = false, error = e.message)
            }
        }
    }

    fun historico(auth: User, tipo: String, docEntry: Int, instlmntId: Int): List<CobrancaHistorico> {
        validarEscopo(auth, tipo, docEntry, instlmntId)
        val code = CobrancaRegistro.code(tipo, docEntry, instlmntId)
        val registro = buscarPorCode(code) ?: return emptyList()
        return registro.historico.sortedByDescending { it.LineId }
    }

    private fun validarEscopo(auth: User, tipo: String, docEntry: Int, instlmntId: Int) {
        if (CobrancaEscopo.temAcessoTotal(auth))
            return
        val titulo = consultaService.buscarTituloParaEscopo(tipo, docEntry, instlmntId)
        if (titulo == null || titulo.SlpCode != auth.getIdInt())
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Este título não pertence ao seu escopo de vendedor")
    }

    /**
     * Registro criado antes das correcoes pode ter U_DocEntry nulo (a coluna era SMALLINT e o
     * DocEntry da NF nao cabia) ou U_CardCode nulo (nunca era escrito). O caminho de atualizacao
     * so mandava os campos que o cobrador mexeu, entao esses registros ficavam presos: cada nova
     * acao engordava o historico e o titulo continuava sem casar com a view - que junta por
     * U_DocEntry - e sumido da tela pra sempre.
     *
     * Repoe na primeira acao seguinte, em vez de exigir UPDATE manual no banco por registro.
     * A consulta extra ao SAP so acontece quando falta algum dos dois, o que e raro.
     */
    private fun repoeCamposDeRegistroLegado(
        existente: CobrancaRegistro,
        payload: MutableMap<String, Any?>,
        tipo: String,
        docEntry: Int,
        instlmntId: Int,
    ) {
        if (existente.U_DocEntry != null && existente.U_CardCode != null)
            return

        if (existente.U_DocEntry == null) {
            payload["U_DocEntry"] = docEntry
            logger.warn("Repondo U_DocEntry={} no registro de cobranca {}", docEntry, existente.Code)
        }
        if (existente.U_CardCode == null)
            consultaService.buscarTituloParaEscopo(tipo, docEntry, instlmntId)?.CardCode
                ?.let { payload["U_CardCode"] = it }
    }

    /**
     * O UDF U_DocEntry nasceu SMALLINT no HANA (teto 32767) enquanto o DocEntry de OINV nesta
     * base ja passa de 150 mil. O Service Layer nao reclama: aceita o POST, grava todo o resto
     * e devolve 200 com o campo nulo. Como a view de titulos junta a UDT justamente por
     * U_DocEntry, a linha fica orfa e a tela mostra a parcela como "1 - NAO INICIADO" mesmo com
     * o historico gravado - foi assim que o bug passou semanas invisivel.
     *
     * Nao derruba a acao do cobrador de proposito: o registro em si esta correto e a culpa e
     * nossa, entao o certo e deixar rastro em vez de punir quem so registrou a cobranca.
     */
    private fun alertarSeODocEntryNaoPersistiu(criado: CobrancaRegistro, docEntry: Int) {
        if (criado.U_DocEntry == null)
            logger.error(
                "Cobranca {} criada com U_DocEntry nulo (enviamos {}). A parcela nao vai aparecer " +
                    "acompanhada na tela de cobranca enquanto a coluna U_DocEntry de @COB_TITULO " +
                    "nao for alargada para INTEGER no HANA.",
                criado.Code, docEntry,
            )
    }

    private fun buscarPorCode(code: String): CobrancaRegistro? {
        return get(Filter(Predicate("Code", code, Condicao.EQUAL))).tryGetValues<CobrancaRegistro>().firstOrNull()
    }

    private fun validar(req: CobrancaAcaoRequest) {
        if (req.observacao.isNullOrBlank() && req.ocorrencia.isNullOrBlank())
            throw CobrancaException("Informe uma observação ou selecione uma ocorrência do que foi feito.")
    }

    private fun validarTipo(tipo: String) {
        if (tipo != CobrancaRegistro.TIPO_NOTA_FISCAL && tipo != CobrancaRegistro.TIPO_ADIANTAMENTO)
            throw CobrancaException("Tipo de título inválido: $tipo")
    }
}
