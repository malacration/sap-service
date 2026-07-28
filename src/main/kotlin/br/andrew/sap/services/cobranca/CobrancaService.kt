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
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class CobrancaService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<CobrancaRegistro>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/COB_TITULO"

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

        val existente = buscarPorCode(code)

        if (existente == null) {
            val registro = CobrancaRegistro(
                Code = code,
                U_Tipo = tipo,
                U_DocEntry = docEntry,
                U_InstlmntID = instlmntId,
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
            return save(registro).tryGetValue()
        }

        // Preserva o histórico existente (com seus LineId) e só acrescenta a linha nova.
        // PATCH de coleção filha de UDO substitui o que for enviado; reenviar as linhas
        // antigas junto é o que garante que elas não somem.
        existente.historico.add(novaLinha)

        // Campo omitido do payload = SAP mantém o valor atual ("não alterar" na tela).
        // Um campo presente com valor null seria interpretado como limpar o campo, o
        // que apagaria o que já estava registrado - por isso só entram os informados.
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

        update(payload, code)
        return buscarPorCode(code) ?: throw CobrancaException("Falha ao reler o registro de cobrança $code")
    }

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

    fun historico(tipo: String, docEntry: Int, instlmntId: Int): List<CobrancaHistorico> {
        val code = CobrancaRegistro.code(tipo, docEntry, instlmntId)
        val registro = buscarPorCode(code) ?: return emptyList()
        return registro.historico.sortedByDescending { it.LineId }
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
