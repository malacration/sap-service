package br.andrew.sap.services.fiscal

import br.andrew.sap.services.integracao.PdfService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * PDF do contrato de venda futura no layout "Controle de Pedido" - o mesmo que a tela de venda
 * futura monta no navegador (sap-front, venda-futura/gerar-pdf), aqui gerado no servidor para ir
 * no zip do lote. Cliente, data, produtos (soma dos itens), frete, total, itens, clausulas e o
 * CPF/CNPJ do cadastro do parceiro. Os dados vem do sap-odbc numa ida so para o lote inteiro.
 *
 * O logo vem da tela (o logo.png e do deploy do front, por cliente - ver LogoService no
 * sap-front), como data: URI; so imagem, com teto. Sem logo valido, o PDF sai sem logo.
 */
@Service
class ContratoVendaFuturaPdfService(val odbc: OdbcClient, val templateEngine: TemplateEngine) {

    private val sql by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-contratos.sql") }
    private val pdf = PdfService()

    class ContratoPdf(val docEntry: Int, val docNum: Int?, val conteudo: ByteArray, val cliente: String? = null)

    // Publica: o Thymeleaf le as propriedades por reflexao.
    class ItemPdf(val descricao: String, val quantidade: String, val preco: String, val total: String)

    /**
     * Um resultado por contrato pedido: o PDF, ou a falha (contrato que nao existe, erro ao gerar).
     * Falha num contrato nao derruba os outros.
     */
    fun gerar(contratos: List<Int>, logo: String? = null): Map<Int, Result<ContratoPdf>> {
        if (contratos.isEmpty())
            return mapOf()
        val linhas = contratos.distinct().chunked(500).flatMap { parte ->
            odbc.consultar(sql, mapOf("contratos" to parte), 10_000).rows
        }.groupBy { inteiro(it["DocEntry"]) }
        return contratos.distinct().associateWith { docEntry ->
            runCatching {
                val doContrato = linhas[docEntry] ?: throw Exception("contrato de venda futura $docEntry nao encontrado")
                ContratoPdf(docEntry, inteiro(doContrato.first()["DocNum"]), pdf.htmlToPdf(html(doContrato, logo)),
                    doContrato.first()["CardName"]?.toString())
            }
        }
    }

    fun html(linhas: List<Map<String, Any?>>, logo: String? = null): String {
        val cabecalho = linhas.first()
        val itens = linhas.filter { it["LineId"] != null }.map {
            val quantidade = decimal(it["Quantidade"])
            val preco = decimal(it["PrecoNegociado"])
            Triple(it["Descricao"]?.toString() ?: "", quantidade, preco)
        }
        val totalProdutos = itens.fold(BigDecimal.ZERO) { soma, (_, qtd, preco) -> soma + qtd * preco }
        val frete = decimal(cabecalho["ValorFrete"])
        val contexto = Context(LOCALE).apply {
            setVariable("logo", logoValido(logo))
            setVariable("numero", inteiro(cabecalho["DocNum"])?.let { "VF $it" } ?: "")
            setVariable("nome", cabecalho["CardName"]?.toString() ?: "")
            setVariable("data", cabecalho["DataCriacao"]?.toString()?.take(10)?.let {
                runCatching { LocalDate.parse(it).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrNull()
            } ?: "")
            setVariable("totalProdutos", moeda(totalProdutos))
            setVariable("frete", moeda(frete))
            setVariable("total", moeda(totalProdutos + frete))
            setVariable("cpfCnpj", cabecalho["CpfCnpj"]?.toString() ?: "")
            setVariable("itens", itens.map { (descricao, qtd, preco) ->
                ItemPdf(descricao, qtd.stripTrailingZeros().toPlainString(), moeda(preco), moeda(qtd * preco))
            })
        }
        return templateEngine.process("contrato-venda-futura", contexto)
    }

    private fun moeda(valor: BigDecimal): String =
        NumberFormat.getCurrencyInstance(LOCALE).format(valor.setScale(2, RoundingMode.HALF_UP))

    private fun decimal(valor: Any?): BigDecimal = valor?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO
    private fun inteiro(valor: Any?): Int? = valor?.toString()?.toBigDecimalOrNull()?.toInt()

    companion object {
        private val LOCALE = Locale.forLanguageTag("pt-BR")
        // ~512 KB de imagem em base64 (o mesmo teto do LogoService da tela).
        private const val TETO_LOGO = 700_000
        private val LOGO = Regex("^data:image/(png|jpeg|gif|webp);base64,[A-Za-z0-9+/]+=*$")

        // So data: URI de imagem: nada de URL (o iText buscaria na rede) nem outro tipo.
        fun logoValido(logo: String?): String? = logo?.trim()?.takeIf { it.length <= TETO_LOGO && LOGO.matches(it) }
    }
}
