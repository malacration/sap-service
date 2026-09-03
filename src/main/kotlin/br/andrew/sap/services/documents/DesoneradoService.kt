package br.andrew.sap.services.documents

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.tax.SalesTaxCode
import br.andrew.sap.model.sap.tax.TaxCodeDespesa
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import br.andrew.sap.services.tax.TaxCodeDespesaService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        val impostos : ImpostosDesonerados,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService,
                        val taxCodeDespesaService: TaxCodeDespesaService){

    val logger: Logger = LoggerFactory.getLogger(DesoneradoService::class.java)

    /**
     * [rascunho] tem que vir `true` quando o Document veio de `Drafts`. O rascunho carrega
     * `docObjectCode` do documento de destino (oOrders), mas o `docEntry` dele e da sequencia
     * do ODRF - mandar isso para o ramo RDR13 da view leria a despesa de um PEDIDO REAL que
     * por acaso tenha o mesmo numero, e majoraria o frete pelo imposto de outro documento.
     */
    fun aplicaDesonerado(order : Document, rascunho : Boolean = false): Document {
        order.productsByTax().forEach{
            taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines?.filter { impostos.ids.contains(it.STAType) }
                ?.forEach{ tax ->
                    var taxParam = taxAuthoritiesService.get(tax)
                        .tryGetValue<SalesTaxAuthorities>()
                    it.value.forEach { p ->
                        //Capturado ANTES de mexer no UnitPrice: sem preco negociado o alvo e o
                        //proprio UnitPrice, que a linha de baixo sobrescreve.
                        val precoAlvo = p.precoAlvo()

                        p.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p, taxParam).toString()
                        if(impostos.tipoImpostoFuturo.contains(tax.STAType))
                            p.valorDesonerado = taxParam.valorImposto(p)

                        //Ler "U_preco_negociado" cru aqui dava totalEsperado 0 em pedido com o
                        //campo zerado: o "resto" absorvia a linha inteira e virava desconto de
                        //100%, que o SAP recusa com "(7) Desconto nao permitido" (DocNum 65506).
                        val totalEsperado = precoAlvo
                            .multiply(BigDecimal(p.Quantity))
                            .setScale(2,RoundingMode.HALF_UP)

                        val desconto = BigDecimal("1")
                            .minus(BigDecimal(p.DiscountPercent?: 0.0).divide(BigDecimal("100")))

                        val totalObtido = BigDecimal(p.UnitPrice)
                            .multiply(desconto)
                            .multiply(BigDecimal(p.Quantity))
                            .minus(taxParam.valorImposto(p))
                            .setScale(2,RoundingMode.HALF_UP)
                        p.resto = p.resto.plus(totalObtido.minus(totalEsperado))
                    }
                }
        }
        aplicaDesoneradoFrete(order, rascunho)
        order.aplicaDescontoDesonerado()
        order.u_pedido_update = "0"
        return order
    }

    /**
     * Mesma rotina do preco unitario, agora na despesa adicional de frete: quando a despesa tem
     * ICMS desonerado, o LineTotal e majorado a partir do frete negociado
     * (PrecoUnitarioComDesoneracao.calculaPreco) para que o liquido volte ao valor combinado com
     * o cliente - exatamente o que UnitPrice faz com U_preco_negociado nas linhas.
     *
     * Sem a majoracao o contrato nao fecha: com frete negociado de 450 e ICMS desonerado de 19%,
     * o SAP abate 85,50 e o cliente paga 364,50, enquanto o `valorConferencia()` e a view
     * `frete-faturado-contrato.sql` contabilizam os 450 negociados. Majorado para 555,56, o SAP
     * abate 105,56 e o liquido volta a ser 450. A majoracao nao cobra a mais - ela cancela um
     * abatimento que o SAP faz de qualquer jeito.
     *
     * O codigo de imposto NAO vem do Service Layer (`despesa.TaxCode` e sempre nulo, ver
     * [TaxCodeDespesaService]), entao a busca cai na view. O campo do Service Layer continua
     * sendo tentado primeiro: custa zero e blinda caso a SAP passe a preencher.
     *
     * Despesa sem U_frete_negociado (vazio ou zero) e documento anterior a essa funcionalidade:
     * fica intacta, nenhum recalculo e feito.
     */
    fun aplicaDesoneradoFrete(order : Document, rascunho : Boolean = false) {
        val comNegociado = order.documentAdditionalExpenses
            .filter { it.expenseCode == AdditionalExpenses.CODIGO_FRETE && it.temFreteNegociado() }
        if(comNegociado.isEmpty())
            return

        //A view cobre QUT13/RDR13/INV13/RIN13. Rascunho guarda a despesa em DRF13, que ela nao
        //le - e consultar pelo docObjectCode dele (oOrders) acertaria um PEDIDO REAL de mesmo
        //numero, porque sequencia de DocEntry e por tabela. Melhor nao majorar do que majorar
        //pelo imposto de outro documento.
        val rateios = if(rascunho) {
            logger.warn("Documento {} e rascunho: a despesa dele vive em DRF13, que a view {} " +
                "nao cobre - o frete nao sera majorado", order.docNum, TaxCodeDespesaService.VIEW)
            listOf()
        } else taxCodeDespesaService.rateiosDoFrete(
            order.docObjectCode, order.docEntry, AdditionalExpenses.CODIGO_FRETE)

        val aliquotas = mutableMapOf<String, BigDecimal>()
        comNegociado.forEach { despesa ->
            val taxa = aliquotaDoFrete(order, despesa, rateios, aliquotas) ?: return@forEach
            if(taxa.signum() <= 0) {
                logger.debug("Frete do documento {} nao tem imposto desonerado - LineTotal fica " +
                    "como esta", order.docNum)
                return@forEach
            }
            despesa.LineTotal = PrecoUnitarioComDesoneracao()
                .calculaPrecoComTaxa(BigDecimal(despesa.U_frete_negociado!!.toString()), taxa)
                .setScale(2, RoundingMode.HALF_UP)
                .toDouble()
        }
    }

    /**
     * A alíquota desonerada que incide sobre a despesa, ou nulo quando nao da para saber.
     *
     * Com um codigo de imposto so - o caso de quase todo documento - e a alíquota dele. Quando
     * os rateios tem codigos diferentes (uma varredura em producao achou 9 documentos assim),
     * e a media PONDERADA pelo valor de cada rateio: majorar tudo pela alíquota de um dos
     * codigos deixaria o liquido acima ou abaixo do negociado.
     *
     * As duas situacoes usam a mesma conta - com um codigo so, a media ponderada e a propria
     * alíquota dele -, entao nao ha caminho especial para o caso raro.
     */
    private fun aliquotaDoFrete(order : Document,
                                despesa : AdditionalExpenses,
                                rateios : List<TaxCodeDespesa>,
                                cache : MutableMap<String, BigDecimal>) : BigDecimal? {
        //O TaxCode do Service Layer e sempre nulo hoje, mas continua sendo tentado primeiro:
        //custa zero e blinda caso a SAP passe a preencher.
        despesa.TaxCode?.takeIf { it.isNotBlank() }?.also {
            return aliquotaDesonerada(it, cache)
        }

        if(rateios.isEmpty()) {
            logger.warn("Frete do documento {} ficou sem codigo de imposto - nao foi majorado e " +
                "o liquido vai sair abaixo do negociado", order.docNum)
            return null
        }

        val pesoTotal = rateios.fold(BigDecimal.ZERO) { acc, r ->
            acc.plus(BigDecimal((r.LineTotal ?: 0.0).toString()))
        }
        //Rateio sem valor nao serve de peso: cai para media simples entre os codigos distintos.
        if(pesoTotal.signum() <= 0) {
            val codigos = rateios.mapNotNull { it.TaxCode }.distinct()
            logger.warn("Os rateios do frete do documento {} nao tem valor para ponderar - " +
                "usando a media simples de {}", order.docNum, codigos)
            return codigos
                .fold(BigDecimal.ZERO) { acc, codigo -> acc.plus(aliquotaDesonerada(codigo, cache)) }
                .divide(BigDecimal(codigos.size), 6, RoundingMode.HALF_UP)
        }

        return rateios.fold(BigDecimal.ZERO) { acc, r ->
            val peso = BigDecimal((r.LineTotal ?: 0.0).toString())
            acc.plus(aliquotaDesonerada(r.TaxCode!!, cache).multiply(peso))
        }.divide(pesoTotal, 6, RoundingMode.HALF_UP)
    }

    /**
     * Soma das alíquotas desoneradas de um codigo de imposto.
     *
     * Soma em vez de escolher: o `5109-003` tem duas linhas que passam pelo filtro - a STAType
     * 10, com `U_Outros` 0 e portanto alíquota 0, e a 25, com `U_Outros` 100. Somar da o
     * resultado certo e dispensa a ordem em que o SAP devolve as linhas. A versao anterior
     * sobrescrevia o valor a cada linha e so funcionava porque a 25 vinha por ultimo.
     */
    private fun aliquotaDesonerada(taxCode : String, cache : MutableMap<String, BigDecimal>) : BigDecimal {
        return cache.getOrPut(taxCode) {
            (taxCodeService.getById("'${taxCode}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines?.filter { impostos.ids.contains(it.STAType) } ?: listOf())
                .fold(BigDecimal.ZERO) { acc, tax ->
                    acc.plus(taxAuthoritiesService.get(tax)
                        .tryGetValue<SalesTaxAuthorities>().rateBaseOutro())
                }
        }
    }
}