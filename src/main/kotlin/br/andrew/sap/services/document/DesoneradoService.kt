package br.andrew.sap.services.document

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.impostos.PrecoUnitarioComDesoneracao
import br.andrew.sap.model.sap.tax.SalesTaxAuthorities
import br.andrew.sap.model.sap.tax.SalesTaxCode
import br.andrew.sap.services.tax.SalesTaxAuthoritiesService
import br.andrew.sap.services.tax.SalesTaxCodeService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class DesoneradoService(val taxCodeService: SalesTaxCodeService,
                        @Value("\${imposto.icms.desonerado:0}") val tipoImposto : List<Int>,
                        @Value("\${imposto.icms.desonerado.futuro:0}")val tipoImpostoFuturo : List<Int>,
                        val taxAuthoritiesService: SalesTaxAuthoritiesService){

    val allImpotos = mutableListOf<Int>().also {
        it.addAll(tipoImposto)
        it.addAll(tipoImpostoFuturo)
    }

    fun aplicaDesonerado(order : Document): Document {
        order.productsByTax().forEach{
            taxCodeService.getById("'${it.key}'").tryGetValue<SalesTaxCode>()
                .salesTaxCodes_Lines?.filter { allImpotos.contains(it.STAType) }
                ?.forEach{ tax ->
                    var taxParam = taxAuthoritiesService.get(tax)
                        .tryGetValue<SalesTaxAuthorities>()
                    it.value.forEach { p ->
                        p.UnitPrice = PrecoUnitarioComDesoneracao().calculaPreco(p, taxParam).toString()
                        if(tipoImpostoFuturo.contains(tax.STAType))
                            p.valorDesonerado = taxParam.valorImposto(p)

                        val totalEsperado = BigDecimal(p.U_preco_negociado ?: throw Exception("Informe o preco negociado"))
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
        order.aplicaDescontoDesonerado()
        order.u_pedido_update = "0"
        return order
    }
}