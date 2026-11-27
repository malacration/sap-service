package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.integracao.AttachmentService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Leitura de notas fiscais de entrada (OPCH) para o Nota Agil: listagem por filial e periodo,
 * busca pelos DocEntry escolhidos e download do PDF anexado a nota. Nada e gravado no SAP.
 *
 * - Filial: `BusinessPlaces` do Service Layer, pelo CNPJ sem mascara (mesma regra da consulta de
 *   pagamento, [ContasPagarSapGateway.filiaisPorCnpj]).
 * - Notas e anexos: sap-odbc (`resources/odbc/contas-pagar-notas-entrada-*.sql`), com bind.
 * - Arquivo: Service Layer `Attachments2(AbsEntry)/$value?filename='nome.ext'` ([AttachmentService]).
 *
 * Periodo limitado a [MAX_DIAS] dias e [MAX_NOTAS] notas: resultado truncado vira
 * `OdbcException("resultado_truncado")`, nunca uma lista parcial.
 */
@Service
class NotasEntradaService(
    private val gateway: ContasPagarSapGateway,
    private val odbc: OdbcClient,
    private val attachments: AttachmentService,
    @Value("\${contas-pagar.modelos.nfe:39}") modelosNfe: String = "39",
    @Value("\${contas-pagar.modelos.cte:45}") modelosCte: String = "45",
    @Value("\${contas-pagar.modelos.nfse:46,60}") modelosNfse: String = "46,60",
) {
    private val sqlPeriodo by lazy { SqlResource.carregar(SQL_PERIODO) }
    private val sqlDocEntries by lazy { SqlResource.carregar(SQL_DOCENTRIES) }
    private val sqlAnexos by lazy { SqlResource.carregar(SQL_ANEXOS) }

    private val modelos: Map<String, TipoNotaEntrada> = buildMap {
        ConsultaPagamentoParcelaService.listaModelos("nfe", modelosNfe).forEach { put(it, TipoNotaEntrada.NFE) }
        ConsultaPagamentoParcelaService.listaModelos("cte", modelosCte).forEach { put(it, TipoNotaEntrada.CTE) }
        ConsultaPagamentoParcelaService.listaModelos("nfse", modelosNfse).forEach { put(it, TipoNotaEntrada.NFSE) }
    }

    fun filiais(): List<FilialSap> = gateway.filiais()

    fun listar(request: ListagemNotasEntradaRequest): ListagemNotasEntradaResponse {
        val cnpjFilial = ConsultaPagamentoParcelaService.cnpj(request.cnpjFilial)
            ?: throw ConsultaInvalidaException("cnpjFilial deve ter 14 digitos")
        // Ausente ou em branco = sem filtro. Informado sem digitos validos (0-9) e invalido, nunca "todos".
        val fornecedor = request.cpfCnpjFornecedor?.takeIf { it.isNotBlank() }?.filter(::digitoAscii)?.also {
            if (it.length != 11 && it.length != 14) throw ConsultaInvalidaException("cpfCnpjFornecedor deve ter 11 ou 14 digitos")
        }
        // So digitos 0-9: toLongOrNull aceitaria sinal ("-1" e o "todos" do SQL) e digito Unicode.
        val numero = request.numeroNota?.trim()?.takeIf { it.isNotEmpty() }?.let {
            it.takeIf { n -> n.all(::digitoAscii) }?.trimStart('0')?.ifEmpty { "0" }?.toLongOrNull()
                ?: throw ConsultaInvalidaException("numeroNota deve ser numerico")
        }
        val docEntries = request.docEntries?.distinct()
        if (docEntries != null && (docEntries.isEmpty() || docEntries.size > MAX_DOCENTRIES || docEntries.any { it <= 0 }))
            throw ConsultaInvalidaException("docEntries deve ter de 1 a $MAX_DOCENTRIES DocEntry positivos")

        // Valida tudo antes de ir ao SAP.
        val periodo = if (docEntries == null) periodo(request.dataInicial, request.dataFinal) else null

        val filiais = gateway.filiaisPorCnpj(cnpjFilial)
        if (filiais.isEmpty()) throw NotaEntradaNaoEncontradaException("Nenhuma filial do SAP com o CNPJ informado")

        val linhas = if (periodo == null) {
            odbc.consultar(sqlDocEntries, parametrosDocEntries(docEntries!!), MAX_NOTAS).rows
        } else {
            // Fornecedor e numero no SQL: o limite de MAX_NOTAS vale para o resultado ja filtrado.
            odbc.consultar(sqlPeriodo, parametrosPeriodo(filiais, periodo.first, periodo.second, fornecedor, numero), MAX_NOTAS).rows
        }
        val notas = linhas.mapNotNull { NotaEntradaListagem.from(it, cnpjFilial, ::tipo) }
            // Por DocEntry a filial nao vai no SQL: confere aqui, para nao importar nota de outra filial.
            .filter { it.filial in filiais }
            // Reaplicados (o SQL so restringe; a regra exata de CNPJ/CPF e a do modelo).
            .filter { fornecedor == null || it.cpfCnpjFornecedor == fornecedor }
            .filter { numero == null || it.numero == numero }
        return ListagemNotasEntradaResponse(notas)
    }

    /** PDF anexado a nota [docEntry] da filial [cnpjFilial]; o de maior linha quando houver varios. */
    fun anexoPdf(docEntry: Int, cnpjFilial: String?): Pair<String, ByteArray> {
        val nota = listar(ListagemNotasEntradaRequest(cnpjFilial = cnpjFilial, docEntries = listOf(docEntry))).notas.firstOrNull()
            ?: throw NotaEntradaNaoEncontradaException("Nota de entrada nao encontrada na filial")
        if (nota.anexosPdf == 0) throw NotaEntradaNaoEncontradaException("Nota de entrada sem PDF anexado")
        val anexo = odbc.consultar(sqlAnexos, parametrosAnexos(docEntry), MAX_LINHAS_ANEXO).rows
            .mapNotNull { linha ->
                val abs = linha["AbsEntry"]?.toString()?.toBigDecimalOrNull()?.toInt() ?: return@mapNotNull null
                val nome = linha["FileName"]?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
                AnexoNotaEntrada(abs, nome, linha["FileExt"]?.toString()?.trim() ?: "pdf")
            }.firstOrNull() ?: throw NotaEntradaNaoEncontradaException("Nota de entrada sem PDF anexado")
        val (tipoConteudo, bytes) = try {
            attachments.downloadArquivo(anexo.absEntry, anexo.arquivo)
        } catch (e: org.springframework.web.client.RestClientResponseException) {
            // Ex.: "Fail to get the LINUX mount point for folder" (pasta de anexos do SAP nao montada
            // no servidor do Service Layer). O corpo do SL fica so no log do interceptor.
            throw AnexoIndisponivelException("Service Layer nao entregou o anexo (HTTP ${e.statusCode.value()})")
        }
        if (bytes.isEmpty()) throw NotaEntradaNaoEncontradaException("Anexo PDF vazio no SAP")
        return anexo.arquivo to bytes.also {
            // O Service Layer devolve JSON de erro com 200 em alguns cenarios: so aceita PDF de verdade.
            if (!tipoConteudo.contains("pdf", ignoreCase = true) && !(it.size > 4 && String(it, 0, 4) == "%PDF"))
                throw NotaEntradaNaoEncontradaException("Anexo da nota nao e um PDF valido")
        }
    }

    private fun tipo(modelo: String?): TipoNotaEntrada? = modelo?.trim()?.let(modelos::get)

    private fun periodo(dataInicial: String?, dataFinal: String?): Pair<LocalDate, LocalDate> {
        fun data(valor: String?, campo: String): LocalDate = try {
            LocalDate.parse(valor?.trim() ?: throw ConsultaInvalidaException("$campo e obrigatoria (yyyy-MM-dd)"))
        } catch (e: DateTimeParseException) {
            throw ConsultaInvalidaException("$campo invalida (yyyy-MM-dd)")
        }
        val inicio = data(dataInicial, "dataInicial")
        val fim = data(dataFinal, "dataFinal")
        if (fim.isBefore(inicio)) throw ConsultaInvalidaException("dataFinal anterior a dataInicial")
        if (ChronoUnit.DAYS.between(inicio, fim) >= MAX_DIAS)
            throw ConsultaInvalidaException("Periodo maximo de $MAX_DIAS dias")
        return inicio to fim
    }

    companion object {
        const val SQL_PERIODO = "odbc/contas-pagar-notas-entrada-periodo.sql"
        const val SQL_DOCENTRIES = "odbc/contas-pagar-notas-entrada-docentries.sql"
        const val SQL_ANEXOS = "odbc/contas-pagar-notas-entrada-anexos.sql"
        const val MAX_DIAS = 31L
        const val MAX_NOTAS = 2000
        const val MAX_DOCENTRIES = 100
        const val MAX_LINHAS_ANEXO = 50

        // Parametros de cada .sql: o sap-odbc recusa (400) parametro faltando ou sobrando.
        /**
         * [fornecedor] so digitos (vazio = todos) e o padrao LIKE com os digitos em ordem
         * (`%1%2%...%`, ver o .sql); [numero] = Serial (-1 = todos).
         */
        fun parametrosPeriodo(filiais: List<Int>, inicio: LocalDate, fim: LocalDate,
                              fornecedor: String? = null, numero: Long? = null): Map<String, Any?> {
            val digitos = fornecedor?.filter(::digitoAscii).orEmpty()
            return mapOf("filiais" to filiais, "dataInicial" to inicio.toString(), "dataFinal" to fim.toString(),
                "fornecedor" to digitos,
                "fornecedorPadrao" to if (digitos.isEmpty()) "" else digitos.toList().joinToString("%", "%", "%"),
                "numero" to (numero ?: -1L))
        }
        fun parametrosDocEntries(docEntries: List<Int>): Map<String, Any?> = mapOf("docEntries" to docEntries)
        fun parametrosAnexos(docEntry: Int): Map<String, Any?> = mapOf("docEntry" to docEntry)
    }
}
