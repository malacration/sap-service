package br.andrew.sap.services.documents

import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.impostos.ImpostosDesonerados
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.tax.SalesTaxCode
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        val impostos : ImpostosDesonerados,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService){

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
     * Despesa sem U_frete_negociado (vazio ou zero) e documento anterior a essa funcionalidade:
     * fica intacta, nenhum recalculo e feito.
     */
    fun aplicaDesoneradoFrete(order : Document) {
        order.documentAdditionalExpenses
            .filter { it.expenseCode == AdditionalExpenses.CODIGO_FRETE && it.temFreteNegociado() }
            .forEach { despesa ->
                val taxCode = despesa.TaxCode
                if(taxCode.isNullOrBlank())
                    return@forEach
                taxCodeService.getById("'${taxCode}'").tryGetValue<SalesTaxCode>()
                    .salesTaxCodes_Lines?.filter { impostos.ids.contains(it.STAType) }
                    ?.forEach { tax ->
                        val taxParam = taxAuthoritiesService.get(tax).tryGetValue<SalesTaxAuthorities>()
                        despesa.LineTotal = PrecoUnitarioComDesoneracao()
                            .calculaPreco(BigDecimal(despesa.U_frete_negociado!!.toString()), taxParam)
                            .setScale(2, RoundingMode.HALF_UP)
                            .toDouble()
                    }
            }
    }
}