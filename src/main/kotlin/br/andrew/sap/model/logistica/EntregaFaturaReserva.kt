package br.andrew.sap.model.logistica

import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.Delivery
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.documents.base.AdditionalExpenses
import br.andrew.sap.model.sap.documents.base.Product
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.math.BigDecimal
import java.math.RoundingMode

/** Linha da fatura de reserva como vem de odbc/fatura-reserva-entrega.sql. */
@JsonIgnoreProperties(ignoreUnknown = true)
class LinhaFaturaReserva(
    val CardCode: String,
    val BPLId: Int,
    val SlpCode: Int?,
    val ChaveAcesso: String?,
    val LineNum: Int,
    val ItemCode: String,
    val Quantity: BigDecimal,
    val OpenQty: BigDecimal,
    val LineTotal: BigDecimal,
    val CFOPCode: String?,
    val WhsCode: String,
    val Desonerado: BigDecimal,
    val FreteLinha: Int? = null,
    val FreteCodigo: Int? = null,
    val Frete: BigDecimal? = null,
    val FreteBaixado: BigDecimal? = null,
    val QtdDespesas: Int? = null,
    val FreteRegra: String? = null,
    val FreteRegra2: String? = null,
    val Observacao: String? = null,
    val NumeroNF: Int? = null,
    val DescontoNF: BigDecimal? = null,
)

/**
 * Monta a entrega (ODLN, BaseType 13) de uma retirada parcial de fatura de reserva. Cada regra
 * aqui e uma trava da TransactionNotification, conferida no HOMOLOG2 em 2026-10-27:
 * - Usage 17 e CFOP de remessa: o Service Layer copia o 5922/6922 da fatura ("CFOP Errado!");
 * - U_ChaveAcesso da fatura ("Venda Futura precisa referenciar a chave de acesso"), que e de
 *   cabecalho - por isso uma entrega por fatura;
 * - DiscountPercent = ICMS desonerado da retirada / (total da retirada + frete). Sem ele o Service Layer
 *   copia o percentual de cabecalho da fatura, que nao bate com o desonerado da entrega
 *   ("desconto divergente do valor do impoto desonerado"). TotalDiscount e ignorado quando o
 *   percentual vem da base, entao tem que ser o percentual.
 * Frete: a despesa da NF (codigo 5 "Frete Simples Fatura" na venda futura, nao o 1 do pedido) vai
 * na entrega proporcional a quantidade entregue sobre a quantidade total da NF, vinculada a despesa
 * da NF (BaseDocType 13) como nas entregas feitas no SAP: o vinculo baixa o frete da NF (DrawnTotal),
 * fecha a entrega e calcula PIS/COFINS/CBS/IBS do frete.
 * Com o frete vinculado o Service Layer copia o desconto INTEIRO da NF (DiscSum) para a entrega e
 * aplica o percentual enviado assim: desconto = DiscSum da NF / (1 - percentual/100) (medido em
 * rascunhos no HOMOLOG2, 2026-10-29). Por isso o percentual enviado e o que leva esse desconto ao
 * desonerado da entrega - negativo numa retirada parcial; a regra (7) da TN recusa se nao bater.
 */
object EntregaFaturaReserva {

    const val USAGE_VENDA_FUTURA = 17
    private val CFOP_ENTREGA = mapOf("5922" to "5116", "6922" to "6116")

    fun montar(
        docEntryFatura: Int,
        fatura: List<LinhaFaturaReserva>,
        retiradas: List<CarregamentoLinha>,
        sequenceCode: Int,
        lotes: (Product) -> List<BatchStock>,
    ): Delivery {
        val cabecalho = fatura.firstOrNull()
            ?: throw Exception("NF de venda futura DocEntry $docEntryFatura nao encontrada, cancelada ou nao e de reserva")
        val chave = cabecalho.ChaveAcesso?.takeIf { it.isNotBlank() }
            ?: throw Exception("NF de venda futura DocEntry $docEntryFatura sem chave de acesso: a NF-e dela foi transmitida?")

        var total = BigDecimal.ZERO
        var desonerado = BigDecimal.ZERO
        val linhas = retiradas.map { retirada ->
            val linha = fatura.find { it.LineNum == retirada.U_baseLine }
                ?: throw Exception("Linha ${retirada.U_baseLine} nao existe na NF de venda futura DocEntry $docEntryFatura")
            val quantidade = BigDecimal(retirada.U_qtdRetirada?.toString() ?: "0").stripTrailingZeros()
            if (quantidade.signum() <= 0)
                throw Exception("Quantidade invalida na NF de venda futura DocEntry $docEntryFatura linha ${linha.LineNum}")
            if (quantidade > linha.OpenQty)
                throw Exception("NF de venda futura DocEntry $docEntryFatura linha ${linha.LineNum} (${linha.ItemCode}): retirada de " +
                    "${quantidade.toPlainString()} maior que o saldo a entregar ${linha.OpenQty.toPlainString()}")
            val cfop = CFOP_ENTREGA[linha.CFOPCode]
                ?: throw Exception("CFOP ${linha.CFOPCode} da NF de venda futura DocEntry $docEntryFatura nao tem CFOP de entrega futura mapeado")

            total += proporcional(linha.LineTotal, quantidade, linha.Quantity)
            desonerado += proporcional(linha.Desonerado, quantidade, linha.Quantity)

            //UnitPrice vazio nao vai no JSON (NON_EMPTY): preco e desconto de linha vem da fatura.
            Product(linha.ItemCode, quantidade.toPlainString(), "", USAGE_VENDA_FUTURA).also {
                it.BaseType = DocumentTypes.oInvoices.value
                it.BaseEntry = docEntryFatura
                it.BaseLine = linha.LineNum
                it.WarehouseCode = linha.WhsCode
                it.CFOPCode = cfop
                it.BatchNumbers = lotes(it).onEach { lote -> lote.toInvoice() }
            }
        }

        val descontoNF = cabecalho.DescontoNF ?: BigDecimal.ZERO
        //A formula do vinculo (desconto = DiscSum_NF / (1 - p/100)) so leva ao desonerado quando ambos sao
        //positivos, ou ambos zero. NF com desconto e entrega sem desonerado, ou desonerado sem desconto
        //na NF, nao fecham: o frete vai sem vinculo (entrega fica aberta e sem imposto no frete, mas o
        //desconto bate).
        val vincular = (descontoNF.signum() > 0) == (desonerado.signum() > 0)
        val despesa = frete(docEntryFatura, cabecalho, fatura, linhas, vincular)
        return Delivery(cabecalho.CardCode, null, linhas, cabecalho.BPLId.toString()).also {
            it.SequenceCode = sequenceCode
            it.U_ChaveAcesso = chave
            it.salesPersonCode = cabecalho.SlpCode ?: -1
            it.discountPercent = when {
                despesa?.BaseDocEntry != null -> percentualComFreteVinculado(descontoNF, desonerado)
                else -> {
                    //O percentual de cabecalho incide sobre itens + frete (entrega 28624 feita no SAP:
                    //10045,60 / (51515,90 + 4768) = 17,8481%).
                    val base = total + (despesa?.let { d -> BigDecimal(d.LineTotal.toString()) } ?: BigDecimal.ZERO)
                    if (base.signum() == 0) 0.0
                    else desonerado.divide(base, 8, RoundingMode.HALF_UP).multiply(BigDecimal(100)).toDouble()
                }
            }
            if (despesa != null) it.documentAdditionalExpenses = mutableListOf(despesa)
        }
    }

    /** Percentual que faz o Service Layer chegar a desconto = desonerado com o frete vinculado (ver acima). */
    internal fun percentualComFreteVinculado(descontoNF: BigDecimal, desonerado: BigDecimal): Double =
        if (descontoNF.signum() == 0 || desonerado.signum() == 0) 0.0
        else BigDecimal.ONE.subtract(descontoNF.divide(desonerado, 12, RoundingMode.HALF_UP))
            .multiply(BigDecimal(100)).setScale(6, RoundingMode.HALF_UP).toDouble()

    private fun frete(docEntryFatura: Int, cabecalho: LinhaFaturaReserva, fatura: List<LinhaFaturaReserva>,
                      linhas: List<Product>, vincular: Boolean): AdditionalExpenses? {
        //Nenhuma NF de venda futura do HOMOLOG2 tem mais de uma despesa; se aparecer, copiar so a primeira
        //vincularia/baixaria a despesa errada em silencio, entao recusa antes do batch.
        if ((cabecalho.QtdDespesas ?: 0) > 1)
            throw Exception("NF de venda futura DocEntry $docEntryFatura tem ${cabecalho.QtdDespesas} despesas adicionais; " +
                "a entrega com frete so trata NF com uma despesa. Gere esta entrega direto no SAP.")
        val frete = cabecalho.Frete?.takeIf { it.signum() > 0 } ?: return null
        val entregue = linhas.sumOf { BigDecimal(it.Quantity) }
        //Limitado ao que resta do frete da NF: entregas e devolucoes anteriores ja baixaram parte (NF
        //39012: entrega 510 + devolucao 510 = frete inteiro). Sem saldo, a entrega sai sem frete.
        val restante = frete - (cabecalho.FreteBaixado ?: BigDecimal.ZERO)
        val valor = proporcional(frete, entregue, fatura.sumOf { it.Quantity }).min(restante)
        if (valor.signum() <= 0) return null
        //TaxType e centro de custo como a nota do pedido manda a despesa dela.
        return AdditionalExpenses(cabecalho.FreteCodigo!!, valor.toDouble()).also {
            it.TaxType = "aext_NormalTax"
            it.DistributionRule = cabecalho.FreteRegra?.takeIf { r -> r.isNotBlank() }
            it.DistributionRule2 = cabecalho.FreteRegra2?.takeIf { r -> r.isNotBlank() }
            if (vincular) {
                it.BaseDocEntry = docEntryFatura
                it.BaseDocLine = cabecalho.FreteLinha
                it.BaseDocType = DocumentTypes.oInvoices.value
            }
        }
    }

    private fun proporcional(valor: BigDecimal, quantidade: BigDecimal, quantidadeLinha: BigDecimal) =
        valor.multiply(quantidade).divide(quantidadeLinha, 2, RoundingMode.HALF_UP)
}
