package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.CessionariaLote
import br.andrew.sap.services.integracao.PdfService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Locale

/**
 * "Comunicado de Cessao de Titulo": um PDF por NF do lote, com as parcelas em aberto dela (ou so as
 * escolhidas na tela) numa tabela, avisando o cliente que o titulo foi cedido a uma cessionaria
 * (UDT @NFL_CESSIONARIA). Parcelas e filial vem do sap-odbc, em grupos de 100 NF. O papel timbrado
 * (logos e marca) e fixo, do classpath (png em templates/termo-cessao); o logo da tela nao e usado.
 */
@Service
class TermoCessaoPdfService(val odbc: OdbcClient, val templateEngine: TemplateEngine) {

    private val sqlCessionarias by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-cessionarias.sql") }
    private val sqlParcelas by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-termo-cessao.sql") }
    private val pdf = PdfService()

    // parcelas = as que entraram no PDF.
    class TermoPdf(val docEntry: Int, val pdf: Result<ByteArray>, val parcelas: List<Int> = listOf())

    // Publica: o Thymeleaf le as propriedades por reflexao.
    class ParcelaPdf(val titulo: String, val valor: String, val vencimento: String)

    fun cessionarias(): List<CessionariaLote> = odbc.consultar(sqlCessionarias, mapOf(), 1000).rows.map {
        CessionariaLote(it["Codigo"].toString().trim(), it["Nome"]?.toString()?.trim() ?: "", it["Telefone"]?.toString()?.trim(),
            it["NomePdf"]?.toString()?.trim())
    }

    // Texto de erro controlado (Exception pura): vai para a tela.
    fun cessionaria(codigo: String?): CessionariaLote {
        if (codigo.isNullOrBlank())
            throw Exception("Escolha a cessionaria do comunicado de cessao.")
        return cessionarias().firstOrNull { it.codigo == codigo.trim() }
            ?: throw Exception("Cessionaria invalida ou inativa. Escolha uma da lista.")
    }

    /**
     * Um PDF por NF, com todas as parcelas em aberto dela; entrega cada um a [aoGerar] assim que
     * sai (quem chama grava e solta os bytes). Cada grupo de 100 NF e consultado sozinho: se a
     * consulta de um falhar, [aoFalharGrupo] recebe as NF dele e os demais seguem (a consulta e a
     * leitura das linhas do grupo ficam juntas). [expirou] e checado antes de cada consulta e de
     * cada PDF: devolvendo um erro, as NF nao processadas vao para [aoFalharGrupo] com ele e nada
     * mais roda. Devolve os DocEntry que tinham parcela em aberto. [selecao] (DocEntry -> parcelas
     * escolhidas) restringe as parcelas das NF que aparecem nela; as outras vao com todas as em aberto.
     */
    fun gerar(docEntries: List<Int>, cessionaria: CessionariaLote, logo: String? = null,
              selecao: Map<Int, Set<Int>> = mapOf(),
              expirou: () -> Throwable? = { null },
              aoFalharGrupo: (List<Int>, Throwable) -> Unit = { _, _ -> }, aoGerar: (TermoPdf) -> Unit): Set<Int> {
        val comParcela = mutableSetOf<Int>()
        val grupos = docEntries.distinct().chunked(100)
        for ((i, parte) in grupos.withIndex()) {
            expirou()?.let { prazo ->
                aoFalharGrupo(grupos.drop(i).flatten(), prazo)
                return comParcela
            }
            // A query vem ordenada por DocEntry e parcela; groupBy mantem a ordem.
            val porNota = try {
                odbc.consultar(sqlParcelas, mapOf("docEntries" to parte), 10_000).rows.map {
                    Triple(it["DocEntry"].toString().toBigDecimal().toInt(), it["Parcela"].toString().toBigDecimal().toInt(), it)
                }.filter { (docEntry, parcela) ->
                    selecao[docEntry]?.contains(parcela) ?: true
                }.groupBy({ it.first }, { it.third }).toList()
            } catch (t: Throwable) {
                aoFalharGrupo(parte, t)
                continue
            }
            for ((j, nota) in porNota.withIndex()) {
                expirou()?.let { prazo ->
                    aoFalharGrupo(porNota.drop(j).map { it.first } + grupos.drop(i + 1).flatten(), prazo)
                    return comParcela
                }
                comParcela.add(nota.first)
                aoGerar(TermoPdf(nota.first, runCatching { pdf.htmlToPdf(html(nota.second, cessionaria, logo)) },
                    nota.second.map { it["Parcela"].toString().toBigDecimal().toInt() }))
            }
        }
        return comParcela
    }

    // [linhas] = parcelas em aberto de uma NF (a filial, o cliente e o numero vem da primeira).
    fun html(linhas: List<Map<String, Any?>>, cessionaria: CessionariaLote, logo: String? = null): String {
        val linha = linhas.first()
        fun texto(campo: String) = linha[campo]?.toString()?.trim() ?: ""
        val filial = texto("BPLName")
        val cnpj = cnpjMatriz(texto("TaxIdNum"))
        val endereco = listOf(
            listOf(texto("Street"), texto("StreetNo")).filter { it.isNotEmpty() }.joinToString(", "),
            texto("Block"),
            listOf(texto("City"), texto("State")).filter { it.isNotEmpty() }.joinToString("/"),
            "CEP $CEP_FIXO",
        ).filter { it.isNotEmpty() }.joinToString(" - ")
        val numeroValor = NumberFormat.getNumberInstance(LOCALE).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
        val contexto = Context(LOCALE).apply {
            // Papel timbrado fixo do termo: o [logo] vindo da tela nao e usado aqui.
            setVariable("imagens", IMAGENS)
            setVariable("cliente", texto("CardName"))
            setVariable("filial", filial)
            setVariable("telefoneContato", TELEFONE_CONTATO)
            setVariable("cnpj", cnpj)
            setVariable("endereco", endereco)
            setVariable("serial", texto("Serial"))
            setVariable("parcelas", linhas.map {
                ParcelaPdf("${it["Serial"]?.toString()?.trim() ?: ""}-${it["Parcela"].toString().toBigDecimal().toInt()}",
                    numeroValor.format(it["Saldo"]?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO),
                    runCatching { LocalDate.parse(it["DueDate"].toString().trim().take(10)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) }.getOrDefault(""))
            })
            setVariable("cessionaria", cessionaria.nomePdf?.takeIf { it.isNotBlank() } ?: cessionaria.nome)
        }
        return templateEngine.process("termo-cessao", contexto)
    }

    companion object {
        // Contato e CEP fixos do termo para toda filial/cessionaria (decisao do usuario); o resto do endereco vem do SAP.
        const val TELEFONE_CONTATO = "(69) 99956-8185"
        const val CEP_FIXO = "76801-970"

        // O termo sempre imprime o CNPJ da matriz. shortcut: assume que a matriz e a /0001 da mesma raiz
        // (sem consulta); migrar para o MainBPL da OBPL se alguma empresa tiver matriz que nao seja /0001.
        fun cnpjMatriz(taxId: String): String {
            // Mesmo formato aceito no cadastro de cessionaria: so digitos e . / - espaco ("X12..." nao vira CNPJ).
            val d = if (NotaFiscalLoteCessionariaService.FORMATO_CNPJ.matches(taxId)) taxId.filter { it in '0'..'9' } else ""
            // Plain Exception = texto controlado que vai para o diagnostico da NF (a NF nao sai com CNPJ errado/da filial).
            if (!NotaFiscalLoteCessionariaService.cnpjValido(d))
                throw Exception("CNPJ da filial invalido no SAP: nao deu para montar o CNPJ da matriz")
            val base = d.substring(0, 8) + "0001"
            val c = base + NotaFiscalLoteCessionariaService.digitosVerificadores(base)
            return "${c.substring(0, 2)}.${c.substring(2, 5)}.${c.substring(5, 8)}/${c.substring(8, 12)}-${c.substring(12)}"
        }

        private val LOCALE = Locale.forLanguageTag("pt-BR")

        // Logos do papel timbrado (png em templates/termo-cessao) como data: URI, igual ao logo do contrato.
        private val IMAGENS: Map<String, String> by lazy {
            listOf("grupo-rovema", "sustennutri", "empresa-comprometida", "marca-sn").associateWith {
                "data:image/png;base64," + Base64.getEncoder().encodeToString(
                    ClassPathResource("templates/termo-cessao/$it.png").inputStream.use { s -> s.readBytes() })
            }
        }
    }
}
