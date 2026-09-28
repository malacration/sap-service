package br.andrew.sap.services.comercial.sanitizacao

import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class SanitizacaoContratoConsulta(
    private val odbc: OdbcClient,
    @Value("\${venda-futura.sequencia_adiantamento:-1}") private val sequencia: Int,
    @Value("\${venda-futura.conta-controle:}") private val contaControle: String,
) {
    private val candidatos by lazy { sql("candidatos") }
    private val linhas by lazy { sql("linhas") }
    private val apropriacoes by lazy { sql("apropriacoes") }
    private val reconciliacoes by lazy { sql("reconciliacoes") }
    private val verificacao by lazy { sql("verificacao") }

    fun dataCorrente(): String = odbc.consultar("SELECT CURRENT_DATE AS \"Data\" FROM DUMMY", emptyMap(), 1)
        .rows.single().texto("Data").take(10)

    fun buscar(transId: Int = 0): List<ReclassificacaoSanitizacao> {
        check(sequencia > 0 && contaControle.isNotBlank()) { "Configure a sequência de apropriação e a conta de controle da venda futura." }
        val resultado = mutableListOf<ReclassificacaoSanitizacao>()
        var apos = 0
        do {
            val rows = consultar(candidatos, mapOf("apos" to apos, "transId" to transId))
            if (rows.isEmpty()) break
            val ids = rows.map { it.inteiro("TransId") }.distinct()
            val origens = rows.map { it.inteiro("DocEntry") }.distinct()
            val legs = consultar(linhas, mapOf("ids" to ids)).groupBy { it.inteiro("TransId") }
            val apps = consultar(apropriacoes, mapOf("origens" to origens, "sequencia" to sequencia))
            val transacoes = (ids + apps.filter { it.texto("CANCELED") == "N" }.map { it.inteiro("TransId") }).distinct()
            val recons = consultar(reconciliacoes, mapOf("ids" to transacoes)).groupBy { it.inteiro("ReconNum") }
                .map { (numero, membros) -> ReconciliacaoSanitizacao(numero, membros.first().inteiro("ReconType"),
                    membros.first().texto("ReconDate").take(10), membros.map {
                        ParticipanteSanitizacao(it.inteiro("TransId"), it.inteiro("TransRowId"), it.inteiro("SrcObjTyp"),
                            it.inteiro("SrcObjAbs"), it.decimal("ReconSum"))
                    }) }
            rows.groupBy { it.inteiro("TransId") }.forEach { (id, origemRows) ->
                val row = origemRows.first()
                val docEntry = row.inteiro("DocEntry")
                val appsItem = apps.filter { it.inteiro("U_TX_DocEntryRef") == docEntry }.map {
                    ApropriacaoSanitizacao(it.inteiro("DocEntry"), it.inteiro("DocNum"), it.inteiro("TransId"),
                        it.texto("CANCELED") == "Y", it.decimal("DocTotal"), it.decimal("DpmAmnt"),
                        it.inteiro("U_venda_futura"), it.texto("CardCode"), it.inteiro("BPLId"))
                }
                val idsItem = setOf(id) + appsItem.filter { !it.cancelada }.map { it.transId }
                val reconsItem = recons.filter { r -> r.participantes.any { it.transId in idsItem } }
                val legsItem = (legs[id] ?: emptyList()).map { l ->
                    PernaSanitizacao(l.inteiro("Line_ID"), l.texto("Account"), l.texto("ShortName"), l.inteiro("BPLId"),
                        l.decimal("Debit"), l.decimal("Credit"), l.decimal("BalDueDeb") - l.decimal("BalDueCred"),
                        reconsItem.filter { r -> r.participantes.any { it.transId == id && it.linha == l.inteiro("Line_ID") } }.map { it.numero },
                        listOf("FCDebit", "FCCredit", "BalFcDeb", "BalFcCred").any { l.decimal(it).signum() != 0 })
                }
                val nota = DocumentoSanitizacao(docEntry, row.inteiro("DocNum"), row.decimal("DocTotal"))
                val devolucoes = origemRows.filter { it["DevolucaoEntry"] != null }.distinctBy { it.inteiro("DevolucaoEntry") }
                    .map { DocumentoSanitizacao(it.inteiro("DevolucaoEntry"), it.inteiro("DevolucaoNum"), it.decimal("DevolucaoTotal")) }
                val contrato = row.inteiro("Contrato")
                val cliente = row.texto("CardCode")
                val filial = row.inteiro("BPLId")
                val cancelada = row.texto("CANCELED") == "Y"
                val integral = row.inteiro("DevolucaoIntegral") == 1
                val impedimentos = avaliarSanitizacao(legsItem, nota, cancelada, integral,
                    origemRows.map { it.inteiro("DocEntry") }.distinct().size != 1 || row.inteiro("ReclassificacoesOrigem") != 1,
                    row.inteiro("EstornosDevolucao"), appsItem, contrato, cliente, filial, contaControle).toMutableList()
                if (origemRows.any { it["DevolucaoAutomatica"]?.toString() == "1" })
                    impedimentos += "Devolução ainda pendente da rotina automática de estorno; aguarde o processamento e gere nova prévia."
                val acoes = if (impedimentos.isEmpty()) buildList {
                    reconsItem.filter { it.cancelavel }.forEach { add("Cancelar reconciliação interna ${it.numero}") }
                    appsItem.filter { !it.cancelada }.forEach { add("Cancelar nota de apropriação ${it.docNum} (DocEntry ${it.docEntry})") }
                    add("Estornar reclassificação $id")
                } else emptyList()
                resultado += ReclassificacaoSanitizacao(id, contrato, filial, cliente, row.texto("CardName"), nota,
                    if (cancelada) "Cancelada" else if (integral) "Devolvida integralmente" else "Devolução parcial / a conferir",
                    devolucoes, legsItem, appsItem, reconsItem, impedimentos, acoes)
            }
            apos = ids.max()
        } while (transId == 0 && ids.size == 200)
        return resultado
    }

    fun verificar(item: ReclassificacaoSanitizacao): ResultadoSanitizacao {
        val apps = item.apropriacoes.filter { !it.cancelada }.map { it.docEntry }
        // Confere todas, inclusive as de adiantamento não enviadas no lote: o SAP deve tê-las
        // desfeito ao cancelar a apropriação.
        val rows = consultar(verificacao, mapOf("transId" to item.transId, "apropriacoes" to apps.ifEmpty { listOf(-1) },
            "reconciliacoes" to item.reconciliacoes.map { it.numero }.ifEmpty { listOf(-1) }))
        val estornos = rows.filter { it.texto("Tipo") == "ESTORNO" }
        val cancelamentos = rows.filter { it.texto("Tipo") == "APROPRIACAO" }
        // O Cancel nativo lança o estorno e o documento de cancelamento na data do original.
        if (estornos.size != 1 || cancelamentos.size != apps.size || rows.any { it.texto("Tipo") == "PENDENCIA" } ||
            (estornos + cancelamentos).any { it.texto("Data").take(10) != it.texto("Original").take(10) })
            return ResultadoSanitizacao(item.transId, "CONFERIR", "SAP aceitou a transação, mas a conferência dos documentos ou das datas divergiu. Confira no SAP antes de repetir.")
        return ResultadoSanitizacao(item.transId, "APLICADO", "Reconciliações canceladas, apropriações canceladas e reclassificação estornada na data dos documentos originais.",
            estornos.single().inteiro("Id"), cancelamentos.map { it.inteiro("Id") })
    }

    private fun sql(nome: String) = SqlResource.carregar("odbc/sanitizacao-contratos-$nome.sql")
    private fun consultar(sql: String, params: Map<String, Any?>) = odbc.consultar(sql, params, 10000).rows
}

private fun Map<String, Any?>.texto(campo: String): String = requireNotNull(this[campo]) { "Campo $campo ausente na consulta." }.toString()
private fun Map<String, Any?>.inteiro(campo: String): Int = texto(campo).toBigDecimal().intValueExact()
private fun Map<String, Any?>.decimal(campo: String): BigDecimal = texto(campo).toBigDecimal().stripTrailingZeros()
