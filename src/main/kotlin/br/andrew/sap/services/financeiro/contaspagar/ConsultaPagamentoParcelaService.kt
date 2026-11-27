package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.financeiro.contaspagar.CodigoConsultaPagamento.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Confere no SAP se a parcela de uma nota fiscal de entrada (contas a pagar) foi paga.
 *
 * Ordem: coerencia da chave de acesso -> filial (CNPJ do tomador) -> fornecedor (CNPJ do
 * prestador) -> nota (chave, ou numero + modelo + serie; nao cancelada) -> parcela -> moeda ->
 * valor -> pagamentos. A primeira etapa que falha define o codigo.
 *
 * Localizacao da nota (OPCH, sempre na filial e num fornecedor do CNPJ, CANCELED = 'N'):
 *  - Tipo (NFE/CTE/NFSE) = o da chave de acesso ou, sem chave, `tipoNota`. Com tipo, so valem notas
 *    do(s) modelo(s) configurado(s) em `contas-pagar.modelos.*` (OPCH.Model = ONFM.AbsEntry), o que
 *    evita colisao CTe x NFS-e do mesmo transportador. Sem tipo nem chave: sem filtro de modelo.
 *  - Chave de acesso (OPCH.U_ChaveAcesso, campo do TaxPlus) quando o pedido traz: e o 1o criterio.
 *    Antes de ir ao SAP, a chave tem de ser coerente com o pedido (numero, serie da chave de 44,
 *    tipo e CNPJ do emitente); senao DADOS_NOTA_DIVERGENTES. Achou pela chave: a nota tem de ter
 *    o mesmo numero (Serial) e modelo, senao DADOS_NOTA_DIVERGENTES; duas notas -> AMBIGUO. Nao
 *    achou (nota ainda sem chave no SAP): segue pelo numero, descartando notas com OUTRA chave.
 *  - Numero: OPCH.Serial (INTEGER). NFS-e com mais de 9 digitos significativos e lancada com os 9
 *    ultimos (o prefixo de ano cai): procura pelos 9 ultimos e tambem pelo numero inteiro se couber
 *    em Int. Nota casada so pelos 9 ultimos precisa de chave NFS-e (50) no SAP com o numero
 *    completo do pedido; sem chave no SAP nao da para confirmar -> AMBIGUO (o mesmo final pode
 *    repetir entre anos).
 *  - Serie NFe/CTe/sem tipo: a do pedido ou, sem ela, a da chave de 44. A nota precisa ter a MESMA
 *    serie (sem espacos, sem caixa, sem zeros a esquerda: "001" == "1", "000" == "0"). Serie "0"
 *    nao casa com nota sem serie.
 *  - Serie NFS-e (do RPS/DPS, ate 5 caracteres; SeriesStr tem 3 e fica vazia em ~72% das NFS-e):
 *    nunca filtra; so desempata, e so contra SeriesStr preenchida (igual sem zeros a esquerda, ou
 *    igual aos 3 primeiros caracteres da serie do pedido).
 *
 * Moeda: o SoftExpert nao informa moeda e InsTotal/PaidToDate/SumApplied sao da moeda local. Nota
 * em outra moeda (OPCH.DocCur diferente de OADM.MainCurncy) ou com moeda desconhecida vira
 * MOEDA_NAO_SUPORTADA antes de comparar qualquer valor.
 *
 * Base LIQUIDA (o SoftExpert manda o valor liquido da parcela):
 *  - Retencao na fonte de categoria Fatura ja sai do DocTotal e das parcelas: PCH6.InsTotal ja e
 *    liquido dela.
 *  - Retencao de categoria Pagamento (PCH5.Category = 'P') fica dentro do InsTotal e e descontada no
 *    pagamento. Liquido = InsTotal - PCH6.WTSum da parcela, usado so quando a soma de WTSum de
 *    todas as parcelas bate com o total de PCH5 categoria P; senao RETENCAO_NAO_SUPORTADA.
 *  - Pago ao fornecedor = soma de (VPM2.SumApplied - VPM2.WtAppld) dos pagamentos efetuados (OVPM
 *    nao cancelado) desta parcela: SumApplied inclui a retencao descontada no pagamento.
 *
 * Regra de quitacao (tolerancia [tolerancia], padrao R$ 0,01, maximo R$ 0,10, so para arredondamento):
 *  - VALOR_DIVERGENTE: InsTotal ou liquido ausente, zero ou negativo, ou |liquido - valor
 *    informado| > tolerancia. Checado antes do pagamento, porque confirmar uma parcela de valor
 *    diferente seria confirmar outra obrigacao.
 *  - "Cobre o liquido": valor >= liquido - tolerancia, mas nunca abaixo do liquido quando ele e
 *    menor ou igual a tolerancia. Como o liquido e > 0, zero nunca cobre.
 *  - PAGO: o pago ao fornecedor cobre o liquido E a baixa no SAP (PaidToDate) tambem cobre. Pagar a
 *    mais (juros, multa) continua PAGO: nao ha teto. Existir um pagamento, sozinho, nao basta.
 *  - BAIXADA_SEM_PAGAMENTO: PaidToDate cobre, mas os pagamentos nao (devolucao, reconciliacao,
 *    desconto, lancamento manual). Vai para conferencia manual.
 *  - PAGO_PARCIAL: algum valor baixado ou pago, abaixo do liquido.
 *  - EM_ABERTO: nada baixado nem pago.
 *
 * Agendamento (PaymentOrdered / assistente de pagamento ainda nao executado) nao gera OVPM e,
 * portanto, nao conta como pago.
 */
@Service
class ConsultaPagamentoParcelaService(
    private val sap: ContasPagarSapGateway,
    @Value("\${contas-pagar.tolerancia-valor:0.01}") private val tolerancia: BigDecimal,
    /** OPCH.Model (ONFM.AbsEntry) de NFe modelo 55, separados por virgula. */
    @Value("\${contas-pagar.modelos.nfe:39}") modelosNfe: String = "39",
    /** OPCH.Model de CTe modelo 57. */
    @Value("\${contas-pagar.modelos.cte:45}") modelosCte: String = "45",
    /** OPCH.Model de NFS-e (46 NFS-e, 60 NFSe-Via no SBOGRUPOROVEMA). */
    @Value("\${contas-pagar.modelos.nfse:46,60}") modelosNfse: String = "46,60",
) {
    private val modelos: Map<TipoNotaEntrada, Set<String>> = mapOf(
        TipoNotaEntrada.NFE to listaModelos("nfe", modelosNfe),
        TipoNotaEntrada.CTE to listaModelos("cte", modelosCte),
        TipoNotaEntrada.NFSE to listaModelos("nfse", modelosNfse),
    )

    init {
        require(tolerancia >= BigDecimal.ZERO && tolerancia <= TOLERANCIA_MAXIMA) {
            "contas-pagar.tolerancia-valor deve estar entre 0 e $TOLERANCIA_MAXIMA (so arredondamento)"
        }
        val todos = modelos.values.flatten()
        require(todos.size == todos.toSet().size) { "contas-pagar.modelos.*: o mesmo modelo em mais de um tipo" }
    }

    fun consultar(request: ConsultaPagamentoParcelaRequest): ConsultaPagamentoParcelaResponse {
        val cnpjFilial = cnpj(request.cnpjFilial) ?: throw ConsultaInvalidaException("cnpjFilial deve ter 14 digitos")
        val cnpjFornecedor = cnpj(request.cnpjFornecedor)
            ?: throw ConsultaInvalidaException("cnpjFornecedor deve ter 14 digitos")
        val valorEsperado = request.valorParcela?.takeIf { it > BigDecimal.ZERO }
            ?: throw ConsultaInvalidaException("valorParcela deve ser positivo")
        val numeroTexto = request.numeroNota?.trim().orEmpty()
        if (numeroTexto.isEmpty()) throw ConsultaInvalidaException("numeroNota e obrigatorio")

        val chave = when (val leitura = ChaveAcessoNota.ler(request.chaveAcesso)) {
            ChaveAcessoNota.Leitura.Ausente -> null
            ChaveAcessoNota.Leitura.Invalida -> return resposta(DADOS_NOTA_DIVERGENTES,
                "Chave de acesso em formato invalido (esperado 44 ou 50 digitos, ou id de DPS)")
            is ChaveAcessoNota.Leitura.Valida -> leitura.chave
        }
        val tipoPedido = TipoNotaEntrada.de(request.tipoNota)
        if (chave != null) {
            divergenciaChave(chave, tipoPedido, cnpjFornecedor, numeroTexto, request.serieNota)
                ?.let { return resposta(DADOS_NOTA_DIVERGENTES, it) }
        }
        val tipo = chave?.tipo ?: tipoPedido

        val filiais = sap.filiaisPorCnpj(cnpjFilial)
        if (filiais.isEmpty()) return resposta(FILIAL_NAO_ENCONTRADA, "Nenhuma filial do SAP com o CNPJ do tomador")
        val fornecedores = sap.fornecedoresPorCnpj(cnpjFornecedor)
        if (fornecedores.isEmpty()) {
            return resposta(FORNECEDOR_NAO_ENCONTRADO, "Nenhum fornecedor do SAP com o CNPJ do prestador")
        }

        val (nota, criterio) = when (val busca = localizar(chave, tipo, numeroTexto, request.serieNota, filiais, fornecedores)) {
            is Busca.Falha -> return resposta(busca.codigo, busca.mensagem)
            is Busca.Achou -> busca.nota to busca.criterio
        }
        val parcelas = sap.parcelas(nota.docEntry!!).filter { it.instlmntId != null }
        val notaResumo = NotaEntradaResumo(nota.docEntry, nota.docNum, nota.sequenceSerial, nota.seriesString,
            data(nota.docDate), nota.cardCode, nota.filial, parcelas.firstOrNull()?.moedaDocumento,
            nota.modelo, criterio)
        val parcela = selecionaParcela(request.numeroParcela, parcelas)
            ?: return resposta(PARCELA_NAO_ENCONTRADA, mensagemParcelaNaoEncontrada(request.numeroParcela, parcelas.size),
                nota = notaResumo)

        val moeda = parcela.moedaDocumento
        if (moeda == null || parcela.moedaLocal == null || moeda != parcela.moedaLocal) {
            return resposta(MOEDA_NAO_SUPORTADA,
                if (moeda == null || parcela.moedaLocal == null) "Moeda da nota de entrada desconhecida no SAP"
                else "Nota de entrada em moeda estrangeira ($moeda); a conferencia so suporta a moeda local",
                nota = notaResumo.copy(moeda = moeda))
        }

        val valor = parcela.insTotal?.takeIf { it > BigDecimal.ZERO }
            ?: return resposta(VALOR_DIVERGENTE, "Parcela sem valor total positivo no SAP", nota = notaResumo)
        val retencao = retencaoPagamentoParcela(parcela, parcelas)
            ?: return resposta(RETENCAO_NAO_SUPORTADA,
                "Retencao no pagamento da nota nao fecha por parcela no SAP; confira o valor liquido manualmente",
                nota = notaResumo)
        val liquido = valor - retencao
        if (liquido <= BigDecimal.ZERO) {
            return resposta(VALOR_DIVERGENTE, "Parcela sem valor liquido positivo no SAP", nota = notaResumo)
        }
        // Linha sem SumApplied ou com WtAppld ilegivel/negativo nao conta como pagamento (falha fechada).
        val pagamentos = sap.pagamentos(nota.docEntry, parcela.instlmntId!!)
            .filter { it.instId == parcela.instlmntId && it.sumApplied != null && it.wtAppld != null && it.wtAppld >= BigDecimal.ZERO }
            .sortedWith(compareBy({ it.docDate }, { it.docEntry }))
        val baixado = parcela.paidToDate ?: BigDecimal.ZERO
        val pago = pagamentos.fold(BigDecimal.ZERO) { soma, it -> soma + (it.sumApplied!! - it.wtAppld!!) }
        val parcelaResumo = ParcelaResumo(
            numero = parcela.instlmntId,
            vencimento = data(parcela.dueDate),
            valor = valor.dinheiro(),
            valorBaixado = baixado.dinheiro(),
            saldo = (liquido - pago).max(BigDecimal.ZERO).dinheiro(),
            status = parcela.status,
            valorPagamentos = pago.dinheiro(),
            retencaoPagamento = retencao.dinheiro(),
            valorLiquido = liquido.dinheiro(),
        )
        val pagamentosResumo = pagamentos.map {
            PagamentoResumo(it.docEntry, it.docNum, data(it.docDate), it.sumApplied!!.dinheiro(), it.wtAppld!!.dinheiro())
        }

        val (codigo, mensagem) = classifica(valorEsperado, liquido, baixado, pago)
        return ConsultaPagamentoParcelaResponse(codigo, mensagem, notaResumo, parcelaResumo, pagamentosResumo)
    }

    private sealed interface Busca {
        data class Achou(val nota: NotaEntradaSap, val criterio: CriterioNota) : Busca
        data class Falha(val codigo: CodigoConsultaPagamento, val mensagem: String) : Busca
    }

    /**
     * Chave x pedido, sem ir ao SAP. Devolve a divergencia (mensagem sem a chave nem o CNPJ) ou null.
     * NFS-e: o numero do pedido pode vir completo ou so com os 9 ultimos digitos.
     */
    internal fun divergenciaChave(
        chave: ChaveAcessoNota, tipoPedido: TipoNotaEntrada?, cnpjFornecedor: String, numeroTexto: String, serieTexto: String?,
    ): String? {
        if (tipoPedido != null && chave.tipo != null && tipoPedido != chave.tipo) {
            return "Chave de acesso e de ${chave.tipo}, mas o pedido informa $tipoPedido"
        }
        if (chave.emitente != cnpjFornecedor) return "CNPJ do emitente da chave de acesso diverge do CNPJ do fornecedor"
        if (chave.numero != null) {
            val numero = numeroTexto.takeIf { it.all(Char::isDigit) }?.toBigInteger()
                ?: return "Numero da nota invalido para conferir com a chave de acesso"
            val nfse = chave.tipo == TipoNotaEntrada.NFSE
            val confere = numero == chave.numero ||
                (nfse && (numero < NOVE_DIGITOS || chave.numero < NOVE_DIGITOS) && numero.mod(NOVE_DIGITOS) == chave.numero.mod(NOVE_DIGITOS))
            if (!confere) return "Numero da nota diverge do numero embutido na chave de acesso"
        }
        val serie = normalizaSerie(serieTexto)
        if (chave.serie != null && serie != null && serie != normalizaSerie(chave.serie)) {
            return "Serie da nota diverge da serie embutida na chave de acesso"
        }
        return null
    }

    private fun localizar(
        chave: ChaveAcessoNota?, tipo: TipoNotaEntrada?, numeroTexto: String, serieTexto: String?,
        filiais: List<Int>, fornecedores: List<String>,
    ): Busca {
        // NFS-e com chave: o numero completo e o da chave (ja conferido com o do pedido em
        // divergenciaChave). Pedido com o numero curto (so os 9 ultimos) nao escapa da confirmacao
        // do numero completo exigida para casamento pelos 9 ultimos digitos.
        val numeroChave = chave?.numero?.takeIf { chave.tipo == TipoNotaEntrada.NFSE }?.toString()
        val numero = numeroNota(numeroChave ?: numeroTexto, tipo)
        val modelosTipo = tipo?.let { modelos.getValue(it) }
        if (chave != null) {
            val porChave = sap.notasPorChave(chave.valor, filiais, fornecedores).validas()
            if (porChave.size > 1) {
                return Busca.Falha(AMBIGUO, "${porChave.size} notas de entrada com a mesma chave de acesso; confira manualmente")
            }
            porChave.singleOrNull()?.let { nota ->
                if (nota.sequenceSerial == null || nota.sequenceSerial !in numero.seriais) {
                    return Busca.Falha(DADOS_NOTA_DIVERGENTES, "Chave de acesso encontrada no SAP em nota de outro numero")
                }
                if (modelosTipo != null && nota.modelo?.trim() !in modelosTipo) {
                    return Busca.Falha(DADOS_NOTA_DIVERGENTES, "Chave de acesso encontrada no SAP em nota de outro modelo")
                }
                return Busca.Achou(nota, CriterioNota.CHAVE_ACESSO)
            }
        }
        if (numero.seriais.isEmpty()) {
            return Busca.Falha(NOTA_NAO_ENCONTRADA,
                "Numero da nota nao e um inteiro positivo dentro da faixa do campo Serial do SAP")
        }

        // So pelos 9 ultimos: nao e o numero inteiro do pedido.
        fun truncada(n: NotaEntradaSap) = numero.truncado != null && n.sequenceSerial == numero.truncado &&
            n.sequenceSerial.toBigInteger() != numero.completo
        // Chave NFS-e da propria nota no SAP: null = sem chave legivel (nao confirma nem descarta).
        fun numeroChaveSap(n: NotaEntradaSap): BigInteger? =
            (ChaveAcessoNota.ler(n.chaveAcesso) as? ChaveAcessoNota.Leitura.Valida)?.chave
                ?.takeIf { it.tipo == TipoNotaEntrada.NFSE }?.numero

        var notas = numero.seriais.flatMap { sap.notasEntrada(it, filiais, fornecedores) }.validas()
        if (modelosTipo != null) notas = notas.filter { it.modelo?.trim() in modelosTipo }
        // A nota do pedido tem chave: nota com OUTRA chave no SAP e outro documento.
        if (chave != null) notas = notas.filter { it.chaveAcesso.isNullOrBlank() || it.chaveAcesso.trim().uppercase() == chave.valor }
        // Casada pelos 9 ultimos com chave NFS-e de outro numero completo (outro ano): outro documento.
        notas = notas.filter { !truncada(it) || numeroChaveSap(it).let { n -> n == null || n == numero.completo } }

        val serie: String?
        if (tipo == TipoNotaEntrada.NFSE) {
            serie = null
            val seriePedido = serieTexto?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
            if (notas.size > 1 && seriePedido != null) {
                notas.filter { serieNfseConfere(it.seriesString, seriePedido) }.takeIf { it.isNotEmpty() }?.let { notas = it }
            }
        } else {
            serie = normalizaSerie(serieTexto) ?: chave?.serie?.let(::normalizaSerie)
            if (serie != null) notas = notas.filter { normalizaSerie(it.seriesString) == serie }
        }

        val nota = when (notas.size) {
            0 -> return Busca.Falha(NOTA_NAO_ENCONTRADA,
                if (serie == null) "Nota de entrada nao encontrada para filial, fornecedor e numero"
                else "Nota de entrada nao encontrada para filial, fornecedor, numero e serie")
            1 -> notas.single()
            else -> return Busca.Falha(AMBIGUO,
                "${notas.size} notas de entrada atendem aos criterios; informe a serie ou confira manualmente")
        }
        if (!truncada(nota)) return Busca.Achou(nota, CriterioNota.NUMERO)
        if (numeroChaveSap(nota) == null) {
            return Busca.Falha(AMBIGUO, "Nota de servico casada so pelos 9 ultimos digitos do numero, sem chave de acesso " +
                "no SAP para confirmar o numero completo; confira manualmente")
        }
        return Busca.Achou(nota, CriterioNota.NUMERO_TRUNCADO)
    }

    private fun List<NotaEntradaSap>.validas() =
        filter { it.docEntry != null && it.cancelado?.trim() == "N" }.distinctBy { it.docEntry }

    /**
     * @param liquido valor liquido da parcela no SAP (InsTotal - retencao no pagamento).
     * @param pago soma do pago ao fornecedor (SumApplied - WtAppld) nesta parcela.
     */
    internal fun classifica(
        esperado: BigDecimal,
        liquido: BigDecimal?,
        baixado: BigDecimal,
        pago: BigDecimal,
    ): Pair<CodigoConsultaPagamento, String> {
        if (liquido == null || liquido <= BigDecimal.ZERO) {
            return VALOR_DIVERGENTE to "Parcela sem valor liquido positivo no SAP"
        }
        // Sempre > 0: a tolerancia nunca deixa um valor zero "cobrir" a parcela.
        val minimo = if (liquido > tolerancia) liquido - tolerancia else liquido
        return when {
            (liquido - esperado).abs() > tolerancia ->
                VALOR_DIVERGENTE to "Valor liquido da parcela no SAP diverge do valor informado"
            // Sem teto: juros/multa pagos junto deixam o pago acima do liquido e continua PAGO.
            baixado >= minimo && pago >= minimo ->
                PAGO to "Parcela quitada por pagamento efetuado no SAP"
            baixado >= minimo ->
                BAIXADA_SEM_PAGAMENTO to "Parcela baixada no SAP sem pagamento efetuado que cubra o valor liquido"
            baixado > BigDecimal.ZERO || pago > BigDecimal.ZERO ->
                PAGO_PARCIAL to "Parcela com pagamento parcial no SAP"
            else -> EM_ABERTO to "Parcela em aberto no SAP"
        }
    }

    /**
     * Retencao de categoria Pagamento que cabe a [parcela] (descontada no pagamento, fora do
     * liquido). Zero quando o documento nao tem retencao de categoria P (PCH6.WTSum e ignorado:
     * retencao de categoria Fatura ja esta fora do InsTotal). Com retencao P, usa PCH6.WTSum so se
     * todas as parcelas tem WTSum >= 0 e a soma bate com o total do documento (tolerancia por
     * parcela, de arredondamento). Qualquer outra coisa: null (RETENCAO_NAO_SUPORTADA).
     */
    internal fun retencaoPagamentoParcela(
        parcela: ParcelaNotaEntradaSap,
        parcelas: List<ParcelaNotaEntradaSap>,
    ): BigDecimal? {
        val documento = parcela.retencaoPagamentoDocumento ?: BigDecimal.ZERO
        if (documento < BigDecimal.ZERO) return null
        if (documento.signum() == 0) return BigDecimal.ZERO
        val porParcela = parcelas.map { it.wtSum ?: return null }
        if (porParcela.any { it < BigDecimal.ZERO }) return null
        val soma = porParcela.fold(BigDecimal.ZERO, BigDecimal::add)
        if ((soma - documento).abs() > tolerancia.multiply(BigDecimal(parcelas.size))) return null
        return parcela.wtSum
    }

    private fun selecionaParcela(numeroParcela: String?, parcelas: List<ParcelaNotaEntradaSap>): ParcelaNotaEntradaSap? {
        val texto = numeroParcela?.trim().orEmpty()
        if (texto.isEmpty()) return parcelas.singleOrNull()
        val numero = texto.takeIf { it.all(Char::isDigit) }?.toIntOrNull()?.takeIf { it > 0 } ?: return null
        return parcelas.singleOrNull { it.instlmntId == numero }
    }

    private fun mensagemParcelaNaoEncontrada(numeroParcela: String?, quantidade: Int) =
        if (numeroParcela.isNullOrBlank()) "A nota possui $quantidade parcelas; informe o numero da parcela"
        else "Parcela informada nao existe na nota de entrada"

    private fun resposta(codigo: CodigoConsultaPagamento, mensagem: String, nota: NotaEntradaResumo? = null) =
        ConsultaPagamentoParcelaResponse(codigo, mensagem, nota)

    companion object {
        fun cnpj(valor: String?): String? = valor?.filter(Char::isDigit)?.takeIf {
            it.length == 14 && valor.all { c -> c.isDigit() || c in ".-/ " }
        }

        private val NOVE_DIGITOS: BigInteger = BigInteger.TEN.pow(9)
        private val INT_MAX: BigInteger = BigInteger.valueOf(Int.MAX_VALUE.toLong())

        /**
         * Numero da nota -> valores de OPCH.Serial (INTEGER) a procurar.
         * @property completo numero sem zeros a esquerda; null se nao numerico, zero ou longo demais.
         * @property seriais Serial a procurar; vazio = nada a procurar.
         * @property truncado os 9 ultimos digitos (NFS-e com mais de 9 digitos), senao null.
         */
        data class NumeroNota(val completo: BigInteger?, val seriais: List<Int>, val truncado: Int?)

        /**
         * "000123" -> [123]. Nao numerico, zero ou longo demais -> nenhum Serial.
         * Acima de 9 digitos significativos: NFS-e procura pelos 9 ultimos (convencao do SAP para os
         * numeros longos de NFS-e, que nao cabem no Serial) e tambem pelo inteiro se couber em Int;
         * os demais tipos (e sem tipo) so pelo inteiro, se couber.
         */
        fun numeroNota(texto: String, tipo: TipoNotaEntrada? = null): NumeroNota {
            val digitos = texto.trim().takeIf { it.isNotEmpty() && it.all(Char::isDigit) }?.trimStart('0')
                ?.takeIf { it.isNotEmpty() && it.length <= 20 }
                ?: return NumeroNota(null, emptyList(), null)
            val completo = BigInteger(digitos)
            val inteiro = completo.takeIf { it <= INT_MAX }?.toInt()
            if (digitos.length <= 9 || tipo != TipoNotaEntrada.NFSE) return NumeroNota(completo, listOfNotNull(inteiro), null)
            val truncado = digitos.takeLast(9).toInt().takeIf { it > 0 }
            return NumeroNota(completo, listOfNotNull(truncado, inteiro).distinct(), truncado)
        }

        /** Desempate de NFS-e: SeriesStr preenchida, igual sem zeros a esquerda ou igual ao inicio (3) da serie do pedido. */
        fun serieNfseConfere(serieSap: String?, seriePedido: String): Boolean {
            val sap = serieSap?.trim()?.uppercase()?.takeIf { it.isNotEmpty() } ?: return false
            return normalizaSerie(sap) == normalizaSerie(seriePedido) || sap == seriePedido.trim().uppercase().take(3)
        }

        internal fun listaModelos(tipo: String, valor: String): Set<String> =
            valor.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet().also {
                require(it.isNotEmpty() && it.all { m -> m.all(Char::isDigit) }) {
                    "contas-pagar.modelos.$tipo deve listar OPCH.Model (ONFM.AbsEntry) numericos separados por virgula"
                }
            }

        val TOLERANCIA_MAXIMA = BigDecimal("0.10")

        /**
         * Serie comparada sem espacos, sem zeros a esquerda e sem caixa: "001" == "1", "000" == "0".
         * Ausente ou em branco -> null, que e diferente de "0": serie zero nao casa com nota sem serie.
         */
        fun normalizaSerie(serie: String?): String? = serie?.trim()?.uppercase()?.takeIf { it.isNotEmpty() }
            ?.let { it.trimStart('0').ifEmpty { "0" } }

        /** Datas do Service Layer chegam como `yyyy-MM-dd` ou `yyyy-MM-ddT00:00:00Z`. */
        fun data(valor: String?): String? = valor?.trim()?.let {
            if (it.length >= 10 && Regex("\\d{4}-\\d{2}-\\d{2}").matches(it.substring(0, 10))) it.substring(0, 10) else it
        }

        private fun BigDecimal.dinheiro(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
    }
}

/** Pedido malformado (CNPJ, valor ou numero ausentes). Vira 400 no controller. */
class ConsultaInvalidaException(message: String) : RuntimeException(message)
