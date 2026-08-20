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
import br.andrew.sap.model.cobranca.CobrancaHistoricoLinha
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaRegistroPatch
import br.andrew.sap.model.cobranca.CobrancaRemocaoLog
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.batch.BatchIdOnly
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
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
    val logService: CobrancaLogService,
    val batchService: BatchService,
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
            U_UsuarioId = auth.id,
            U_Status = req.status,
            U_Acao = req.acao,
            U_Situacao = req.situacao,
            U_Ocorrencia = req.ocorrencia,
            U_Observacao = req.observacao,
            U_DataPromessa = req.dataPromessa,
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

    fun historico(auth: User, tipo: String, docEntry: Int, instlmntId: Int): List<CobrancaHistoricoLinha> {
        validarEscopo(auth, tipo, docEntry, instlmntId)
        val code = CobrancaRegistro.code(tipo, docEntry, instlmntId)
        val registro = buscarPorCode(code) ?: return emptyList()
        return paraTela(registro.historico, auth)
    }

    private fun paraTela(historico: List<CobrancaHistorico>, auth: User): List<CobrancaHistoricoLinha> =
        historico.sortedByDescending { it.LineId }
            .map { CobrancaHistoricoLinha.de(it, ehAutor(it, auth)) }

    /**
     * Autoria pelo U_UsuarioId (User.id: SalesPersonCode ou EmployeeID), nao pelo nome: dois
     * cobradores homonimos apagariam a linha um do outro, e quem fosse renomeado no SAP perderia
     * acesso as proprias linhas pra sempre.
     *
     * Linha gravada antes do campo existir nao tem id nenhum. Cair pro nome nesse caso e o unico
     * jeito de o historico que ja esta no SAP continuar removivel por quem o escreveu - o buraco
     * do homonimo segue valendo so pra essas linhas antigas, e vai secando conforme entra acao
     * nova. Se preferir travar essas linhas de vez, e trocar o else por false.
     */
    private fun ehAutor(linha: CobrancaHistorico, auth: User): Boolean =
        if (!linha.U_UsuarioId.isNullOrBlank())
            linha.U_UsuarioId == auth.id
        else
            linha.U_Usuario == auth._name

    /**
     * Remocao restrita a quem registrou: o historico e a prova do que foi combinado com o
     * cliente, entao ninguem apaga a linha de outro cobrador - nem quem tem acesso total.
     * Deletar a linha errada aqui nao tem desfazer, o SAP nao versiona a UDT.
     */
    // beforeInvocation: se a remocao aplica o cabecalho e estoura na conferencia, o metodo sai por
    // excecao - sem isso o dashboard continuaria servindo o numero de antes.
    @CacheEvict("cobranca-dashboard", allEntries = true, beforeInvocation = true)
    fun removerHistorico(auth: User, tipo: String, docEntry: Int, instlmntId: Int, lineId: Int): List<CobrancaHistoricoLinha> {
        validarTipo(tipo)
        validarEscopo(auth, tipo, docEntry, instlmntId)
        val code = CobrancaRegistro.code(tipo, docEntry, instlmntId)

        synchronized(locksPorTitulo.computeIfAbsent(code) { Any() }) {
            val registro = buscarPorCode(code)
                ?: throw CobrancaException("Não existe histórico de cobrança para $tipo $docEntry/$instlmntId")
            val linha = registro.historico.firstOrNull { it.LineId == lineId }
                ?: throw CobrancaException("Essa ação já não está mais no histórico de cobrança")

            if (!ehAutor(linha, auth))
                throw ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Só ${linha.U_Usuario} pode remover essa ação do histórico",
                )

            val restante = registro.historico.filter { it.LineId != lineId }
            val auditoria = CobrancaRemocaoLog.de(code, linha, auth._name, auth.id)

            /*
             * Auditoria e remocao no MESMO changeset: o Service Layer trata o changeset como uma
             * transacao, entao ou as duas acontecem ou nenhuma. Em chamadas separadas existia a
             * janela em que a auditoria gravava "linha removida" e a remocao falhava depois,
             * deixando registro afirmando remocao que nao houve (e retentativa duplicando isso).
             * Mesma propriedade que RegiaoService.substituir ja usa.
             *
             * Sem nenhuma linha restante o registro deixa de ser acompanhamento: mante-lo vazio
             * faria o titulo continuar contando como trabalhado no dashboard (que olha a presenca
             * do registro em @COB_TITULO, nao o historico) e seguir aparecendo nos filtros de
             * "rastreado" com o cabecalho de uma acao que nao existe mais. Apagar devolve o titulo
             * pra "1 - NAO INICIADO", que e o estado real.
             *
             * O header B1S-ReplaceCollectionsOnPatch e obrigatorio no PATCH: sem ele o Service
             * Layer faz merge da colecao e devolve 200 com a linha ainda la.
             */
            val codigoDaAuditoria = requireNotNull(auditoria.Code) { "Auditoria de remoção sem Code" }
            val lote = BatchList().add(BatchMethod.POST, auditoria, logService)

            /*
             * Os dois ramos conferem o resultado relendo o registro. Nao e redundancia: o
             * BatchService so acusa erro quando consegue interpretar a resposta do SAP - resposta
             * que o parser nao reconhece vira lista vazia de respostas, e "nenhum erro" passa por
             * sucesso. Alem disso o Service Layer as vezes responde 200 e ignora a remocao.
             */
            if (restante.isEmpty()) {
                lote.add(BatchMethod.DELETE, BatchIdOnly("'$code'"), this)
                enviarLote(lote, code, lineId)
                if (buscarPorCode(code) != null) {
                    apagarAuditoriaNaoAplicada(codigoDaAuditoria)
                    throw CobrancaException(
                        "O SAP não apagou o registro de cobrança $code. O histórico está intacto - " +
                            "tente de novo ou avise o suporte.",
                    )
                }
                return emptyList()
            }

            lote.add(
                BatchMethod.PATCH,
                CobrancaRegistroPatch(code, payloadSemALinhaRemovida(restante, linha)),
                this,
                mapOf("B1S-ReplaceCollectionsOnPatch" to "true"),
            )
            enviarLote(lote, code, lineId)

            val relido = buscarPorCode(code)
                ?: throw CobrancaException("Falha ao reler o registro de cobrança $code")

            if (relido.historico.any { it.LineId == lineId }) {
                apagarAuditoriaNaoAplicada(codigoDaAuditoria)
                throw CobrancaException(
                    "O SAP não removeu a ação do histórico de $code (linha $lineId continua lá). " +
                        "O histórico está intacto, mas o cabeçalho do título pode ter ficado " +
                        "desatualizado - refaça a remoção.",
                )
            }

            return paraTela(relido.historico, auth)
        }
    }

    /**
     * O changeset e transacional: se a remocao nao acontece, o POST da auditoria tambem e desfeito
     * pelo SAP. Tipar a falha aqui evita que texto cru do Service Layer ("400 - Property ... is
     * invalid") chegue na tela como erro generico, e mantem a mensagem dizendo o que importa pro
     * cobrador: nada foi apagado.
     */
    private fun enviarLote(lote: BatchList, code: String, lineId: Int) {
        try {
            batchService.run(lote)
        } catch (e: Exception) {
            throw CobrancaException(
                "Não foi possível remover a ação $lineId do histórico de $code: ${e.message}. " +
                    "Nada foi apagado.",
            )
        }
    }

    /**
     * Auditoria afirmando remocao que nao houve e pior que nao ter auditoria, entao ela e apagada.
     * Se nem isso der, o log e o unico caminho pra alguem achar o registro e apagar a mao - por
     * isso aqui tem log, diferente do resto do fluxo, cuja trilha e a propria UDT.
     */
    private fun apagarAuditoriaNaoAplicada(codigoDaAuditoria: String) {
        try {
            logService.delete("'$codigoDaAuditoria'")
        } catch (e: Exception) {
            logger.error(
                "Falha ao apagar a auditoria {} de uma remocao que o SAP nao aplicou. O registro " +
                    "afirma remocao que nao aconteceu e precisa ser apagado a mao em @COB_TITULO_LOG.",
                codigoDaAuditoria, e,
            )
        }
    }

    /**
     * Mandar o historico sem a linha removida NAO basta: por padrao o Service Layer faz merge da
     * colecao filha e a linha omitida continua no SAP, com 200 na resposta. Quem apaga e o header
     * B1S-ReplaceCollectionsOnPatch (updateReplacingCollections), e a releitura confere.
     *
     * O cabecalho tem que ser refeito, senao a tela continua mostrando status/acao de uma linha
     * apagada. Ele e o ACUMULADO das acoes, nao uma copia da ultima linha: registrarAcao so
     * escreve no cabecalho o campo que o cobrador mexeu, entao cada campo vale ate alguem
     * informar outro valor. Por isso cada um volta pro ultimo valor informado entre as linhas
     * que sobraram - copiar a linha mais recente zeraria campo que uma acao anterior sustenta
     * (acao de hoje so com observacao apagaria o status escolhido semana passada).
     *
     * Campo de texto sem nenhum valor no historico vai como "" (e assim que a UDT guarda
     * ausencia, ver cobranca-cobradores.sql). Data e diferente: "" nao e data valida pro Service
     * Layer, o vazio de campo db_Date e null.
     */
    private fun payloadSemALinhaRemovida(
        restante: List<CobrancaHistorico>,
        removida: CobrancaHistorico,
    ): Map<String, Any?> {
        val emOrdem = restante.sortedBy { it.LineId ?: 0 }
        val maisRecente = emOrdem.last()
        val payload = mutableMapOf<String, Any?>(
            "COB_TITULO_LCollection" to restante,
            "U_Status" to ultimoInformado(emOrdem) { it.U_Status },
            "U_Acao" to ultimoInformado(emOrdem) { it.U_Acao },
            "U_Situacao" to ultimoInformado(emOrdem) { it.U_Situacao },
            "U_Ocorrencia" to ultimoInformado(emOrdem) { it.U_Ocorrencia },
            "U_Observacao" to ultimoInformado(emOrdem) { it.U_Observacao },
            // Quem cobrou por ultimo e quando: esses dois sao da linha mais recente mesmo.
            "U_Cobrador" to maisRecente.U_Cobrador,
            "U_DataAcao" to maisRecente.U_Data,
        )
        recompoeAPromessa(payload, emOrdem, removida)
        return payload
    }

    /**
     * So mexe na promessa do cabecalho quando a linha removida era a que prometeu. Linha gravada
     * antes do U_DataPromessa existir na UDT tem o campo nulo mesmo tendo prometido de verdade -
     * recompor sempre apagaria a promessa legitima do cabecalho ao remover qualquer linha antiga.
     *
     * Quando mexe, vale a ultima promessa que sobrou; se nenhuma sobrou, o campo e limpo com null
     * (o SAP nao aceita "" em campo de data), senao o titulo seguiria contando como promessa
     * vencida no dashboard por causa de uma acao que nao existe mais.
     */
    private fun recompoeAPromessa(
        payload: MutableMap<String, Any?>,
        emOrdem: List<CobrancaHistorico>,
        removida: CobrancaHistorico,
    ) {
        if (removida.U_DataPromessa.isNullOrBlank())
            return
        payload["U_DataPromessa"] = emOrdem.lastOrNull { !it.U_DataPromessa.isNullOrBlank() }?.U_DataPromessa
    }

    private fun ultimoInformado(emOrdem: List<CobrancaHistorico>, campo: (CobrancaHistorico) -> String?): String =
        emOrdem.lastOrNull { !campo(it).isNullOrBlank() }?.let(campo) ?: ""

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
