package br.andrew.sap.services.documents

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.tax.SalesTaxCode
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

    fun aplicaDesonerado(order : Document): Document {
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
        aplicaDesoneradoFrete(order)
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
    fun aplicaDesoneradoFrete(order : Document) {
        val fretes = order.documentAdditionalExpenses
            .filter { it.expenseCode == AdditionalExpenses.CODIGO_FRETE }
        if(fretes.isEmpty())
            return

        val comNegociado = fretes.filter { it.temFreteNegociado() }
        if(comNegociado.isEmpty()) {
            logger.debug("Documento {} tem frete sem U_frete_negociado - nada a majorar", order.docNum)
            return
        }

        val taxCodePorLinha = taxCodeDespesaService.porLinha(
            order.docObjectCode, order.docEntry, AdditionalExpenses.CODIGO_FRETE)

        comNegociado.forEach { despesa ->
            val taxCode = despesa.TaxCode?.takeIf { it.isNotBlank() }
                ?: taxCodePorLinha[despesa.LineNum]
            if(taxCode.isNullOrBlank()) {
                logger.warn("Frete do documento {} (LineNum {}) ficou sem codigo de imposto - " +
                    "nao foi majorado e o liquido vai sair abaixo do negociado",
                    order.docNum, despesa.LineNum)
                return@forEach
            }

            val desonerados = taxCodeService.getById("'${taxCode}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines?.filter { impostos.ids.contains(it.STAType) } ?: listOf()
            if(desonerados.isEmpty()) {
                logger.debug("Codigo de imposto {} do frete do documento {} nao tem imposto " +
                    "desonerado - LineTotal fica como esta", taxCode, order.docNum)
                return@forEach
            }

            desonerados.forEach { tax ->
                val taxParam = taxAuthoritiesService.get(tax).tryGetValue<SalesTaxAuthorities>()
                if(taxParam.u_Outros <= 0) {
                    logger.debug("Autoridade {}/{} do frete do documento {} tem U_Outros zerado - " +
                        "sem majoracao por essa linha de imposto",
                        tax.STACode, tax.STAType, order.docNum)
                    return@forEach
                }
                despesa.LineTotal = PrecoUnitarioComDesoneracao()
                    .calculaPreco(BigDecimal(despesa.U_frete_negociado!!.toString()), taxParam)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()
            }
        }
    }
}