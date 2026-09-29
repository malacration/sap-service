package br.andrew.sap.services.comercial.sanitizacao

import java.math.BigDecimal

data class DocumentoSanitizacao(val docEntry: Int, val docNum: Int, val valor: BigDecimal)
data class ApropriacaoSanitizacao(
    val docEntry: Int, val docNum: Int, val transId: Int, val cancelada: Boolean,
    val valor: BigDecimal, val adiantamento: BigDecimal,
    val contrato: Int, val cliente: String, val filial: Int,
)
data class PernaSanitizacao(
    val linha: Int, val conta: String, val parceiro: String, val filial: Int,
    val debito: BigDecimal, val credito: BigDecimal, val saldo: BigDecimal,
    val reconciliacoes: List<Int>, val moedaEstrangeira: Boolean,
)
data class ParticipanteSanitizacao(
    val transId: Int, val linha: Int, val tipo: Int, val documento: Int, val valor: BigDecimal,
)
data class ReconciliacaoSanitizacao(
    val numero: Int, val tipo: Int, val data: String, val participantes: List<ParticipanteSanitizacao>,
) {
    /**
     * Só a reconciliação manual (ReconType 0, VFEC x nota) é cancelada explicitamente. A de
     * adiantamento (ReconType 16, IsSystem = Y) é do sistema: o SAP recusa cancelá-la e a
     * desfaz sozinho ao cancelar a nota de apropriação.
     */
    val cancelavel: Boolean get() = tipo == 0
}
data class ReclassificacaoSanitizacao(
    val transId: Int, val contrato: Int, val filial: Int, val cliente: String, val nomeCliente: String,
    val nota: DocumentoSanitizacao, val situacaoNota: String,
    val devolucoes: List<DocumentoSanitizacao>, val pernas: List<PernaSanitizacao>,
    val apropriacoes: List<ApropriacaoSanitizacao>, val reconciliacoes: List<ReconciliacaoSanitizacao>,
    val impedimentos: List<String>, val acoes: List<String>,
) {
    val podeAplicar: Boolean get() = impedimentos.isEmpty()
}
data class PreviaSanitizacao(val id: String, val data: String, val itens: List<ReclassificacaoSanitizacao>)
data class AplicarSanitizacao(val previaId: String, val transId: Int)
data class ResultadoSanitizacao(
    val transId: Int, val status: String, val mensagem: String,
    val estorno: Int? = null, val cancelamentos: List<Int> = emptyList(),
)

internal fun avaliarSanitizacao(
    pernas: List<PernaSanitizacao>, nota: DocumentoSanitizacao, cancelada: Boolean,
    devolucaoIntegral: Boolean, origemAmbigua: Boolean, estornosDevolucao: Int,
    apropriacoes: List<ApropriacaoSanitizacao>, contrato: Int, cliente: String, filial: Int,
    contaControle: String,
): List<String> = buildList {
    if (origemAmbigua) add("Vínculo ambíguo entre nota e reclassificação; conferir manualmente.")
    if (!cancelada && !devolucaoIntegral) add("Devolução parcial ou sem comprovação integral por linha; somente análise.")
    if (estornosDevolucao > 0) add("Já existe estorno de devolução (VFDV); conferir para evitar estorno em duplicidade.")
    if (pernas.size != 2 || pernas.count { it.debito.signum() > 0 && it.credito.signum() == 0 } != 1 ||
        pernas.count { it.credito.signum() > 0 && it.debito.signum() == 0 } != 1) {
        add("O lançamento não tem as duas pernas esperadas de débito e crédito.")
    }
    if (pernas.isNotEmpty() && pernas.all { it.reconciliacoes.isNotEmpty() })
        add("As duas pernas possuem reconciliação interna ativa; nenhuma correção prevista.")
    if (pernas.any { it.parceiro != cliente || it.filial != filial } ||
        pernas.filter { it.debito.signum() > 0 }.any { it.conta != contaControle })
        add("Contas, parceiro ou filial divergem da reclassificação de venda futura.")
    if (pernas.any { it.moedaEstrangeira }) add("Lançamento em moeda estrangeira; conferir manualmente.")
    if (nota.valor.signum() <= 0 || pernas.any { (it.debito + it.credito - nota.valor).abs() > BigDecimal("0.01") })
        add("Valor da reclassificação diverge da nota de origem.")
    if (apropriacoes.any { !it.cancelada && (it.contrato != contrato || it.cliente != cliente || it.filial != filial) })
        add("Apropriação com contrato, parceiro ou filial divergente; conferir o vínculo.")
}
