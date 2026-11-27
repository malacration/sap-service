package br.andrew.sap.services.financeiro.contaspagar

import br.andrew.sap.services.cadastro.BussinessPlaceService
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.stereotype.Service

/**
 * Leituras do SAP usadas na conferencia de pagamento de nota de entrada. Somente leitura.
 *
 * Interface separada para que a regra de negocio ([ConsultaPagamentoParcelaService]) seja
 * testada sem Service Layer nem sap-odbc.
 */
interface ContasPagarSapGateway {
    /** BPLIds cuja `FederalTaxID` (OBPL.TaxIdNum), sem mascara, e igual a [cnpj] (14 digitos). */
    fun filiaisPorCnpj(cnpj: String): List<Int>

    /** Todas as filiais (BusinessPlaces) com CNPJ de 14 digitos, ordenadas por BPLId. */
    fun filiais(): List<FilialSap> = emptyList()

    /** CardCodes de fornecedores (CardType 'S') com CRD7.TaxId0 igual a [cnpj] (comparado so por digitos). */
    fun fornecedoresPorCnpj(cnpj: String): List<String>

    /** Notas de entrada nao canceladas com `Serial` = [numero] nas filiais/fornecedores dados. */
    fun notasEntrada(numero: Int, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap>

    /** Notas de entrada nao canceladas com `U_ChaveAcesso` = [chave] nas filiais/fornecedores dados. */
    fun notasPorChave(chave: String, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap>

    /** Parcelas (PCH6) da nota, com a moeda do documento e a moeda local da empresa. */
    fun parcelas(docEntry: Int): List<ParcelaNotaEntradaSap>

    /** Aplicacoes de pagamentos efetuados nao cancelados (VPM2 + OVPM) na parcela [parcela] da nota. */
    fun pagamentos(docEntry: Int, parcela: Int): List<PagamentoNotaEntradaSap>
}

/**
 * Implementacao. Nada aqui escreve no SAP, nem na subida da aplicacao.
 *
 * - Filial: entidade `BusinessPlaces` do Service Layer (poucas linhas, comparadas em Kotlin pelo
 *   CNPJ sem mascara, o que dispensa adivinhar como o CNPJ foi digitado no cadastro).
 * - Todo o resto vem do sap-odbc (gateway SQL somente leitura, [OdbcClient], `odbc.*`), com bind
 *   parameter: o CNPJ do fornecedor fica na sub-colecao `BPFiscalTaxIDCollection` (sem filtro por
 *   filho no Service Layer); a nota precisa de OPCH.Model e OPCH.U_ChaveAcesso (campo do TaxPlus),
 *   colunas conferidas direto no HANA; `DocumentInstallments` nao expoe `PaidToDate`/`Status` da
 *   parcela e `VendorPayments` nao filtra pela nota aplicada em `PaymentInvoices`. Nao se usa view
 *   `SQLQueries` porque ela precisa ser criada no SAP (o ProvisioningConfiguration grava `views/`
 *   como consulta global na subida e sobrescreve outra de mesmo nome).
 * Os .sql ficam em `resources/odbc/contas-pagar-*.sql`.
 */
@Service
class ContasPagarSapGatewayImpl(
    private val bussinessPlaceService: BussinessPlaceService,
    private val odbc: OdbcClient,
) : ContasPagarSapGateway {

    private val sqlFornecedor by lazy { SqlResource.carregar(SQL_FORNECEDOR) }
    private val sqlNotasNumero by lazy { SqlResource.carregar(SQL_NOTAS_NUMERO) }
    private val sqlNotasChave by lazy { SqlResource.carregar(SQL_NOTAS_CHAVE) }
    private val sqlParcelas by lazy { SqlResource.carregar(SQL_PARCELAS) }
    private val sqlPagamentos by lazy { SqlResource.carregar(SQL_PAGAMENTOS) }

    override fun filiais(): List<FilialSap> =
        bussinessPlaceService.getAllBusinessPlaces().mapNotNull { f ->
            val id = f.BPLID ?: return@mapNotNull null
            val cnpj = f.FederalTaxID?.filter(Char::isDigit)?.takeIf { it.length == 14 } ?: return@mapNotNull null
            FilialSap(id, f.BPLName?.trim().orEmpty(), cnpj, ativa = f.Disabled != "tYES")
        }.sortedBy { it.bplId }

    override fun filiaisPorCnpj(cnpj: String): List<Int> =
        bussinessPlaceService.getAllBusinessPlaces()
            .filter { it.FederalTaxID?.filter(Char::isDigit) == cnpj }
            .mapNotNull { it.BPLID }
            .distinct()

    override fun fornecedoresPorCnpj(cnpj: String): List<String> {
        require(cnpj.length == 14 && cnpj.all(Char::isDigit)) { "CNPJ deve ter 14 digitos" }
        return odbc.consultar(sqlFornecedor, parametrosFornecedor(cnpj), MAX_LINHAS)
            .rows.mapNotNull { it["CardCode"]?.toString()?.takeIf(String::isNotBlank) }.distinct()
    }

    // Uma consulta por fornecedor (quase sempre um so CardCode por CNPJ): o bind e escalar.
    // Resultado truncado (mais de MAX_NOTAS linhas) vira OdbcException: nunca se decide com parte.
    override fun notasEntrada(numero: Int, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap> {
        require(filiais.isNotEmpty() && fornecedores.isNotEmpty()) { "Filial e fornecedor sao obrigatorios" }
        return fornecedores.distinct().flatMap { cardCode ->
            odbc.consultar(sqlNotasNumero, parametrosNotasNumero(numero, cardCode), MAX_NOTAS).rows.map(NotaEntradaSap::from)
        }.filter { it.filial in filiais && it.cardCode in fornecedores }
    }

    override fun notasPorChave(chave: String, filiais: List<Int>, fornecedores: List<String>): List<NotaEntradaSap> {
        require(filiais.isNotEmpty() && fornecedores.isNotEmpty()) { "Filial e fornecedor sao obrigatorios" }
        require(chave.isNotBlank() && chave.all(Char::isLetterOrDigit)) { "Chave de acesso invalida" }
        return odbc.consultar(sqlNotasChave, parametrosNotasChave(chave), MAX_NOTAS).rows.map(NotaEntradaSap::from)
            .filter { it.filial in filiais && it.cardCode in fornecedores }
    }

    // Resultado truncado vira OdbcException no OdbcClient: nunca se confere com parte das linhas.
    override fun parcelas(docEntry: Int): List<ParcelaNotaEntradaSap> =
        odbc.consultar(sqlParcelas, parametrosParcelas(docEntry), MAX_LINHAS).rows.map(ParcelaNotaEntradaSap::from)

    override fun pagamentos(docEntry: Int, parcela: Int): List<PagamentoNotaEntradaSap> =
        odbc.consultar(sqlPagamentos, parametrosPagamentos(docEntry, parcela), MAX_LINHAS)
            .rows.map(PagamentoNotaEntradaSap::from)

    companion object {
        const val SQL_FORNECEDOR = "odbc/contas-pagar-fornecedor.sql"
        const val SQL_NOTAS_NUMERO = "odbc/contas-pagar-notas-numero.sql"
        const val SQL_NOTAS_CHAVE = "odbc/contas-pagar-notas-chave.sql"
        const val SQL_PARCELAS = "odbc/contas-pagar-parcelas.sql"
        const val SQL_PAGAMENTOS = "odbc/contas-pagar-pagamentos.sql"
        const val MAX_NOTAS = 50
        const val MAX_LINHAS = 1000

        // Parametros de cada .sql: o sap-odbc recusa (400) parametro faltando ou sobrando.
        fun parametrosFornecedor(cnpj: String): Map<String, Any?> = mapOf("cnpj" to cnpj)
        fun parametrosNotasNumero(numero: Int, cardCode: String): Map<String, Any?> =
            mapOf("numero" to numero, "cardCode" to cardCode)
        fun parametrosNotasChave(chave: String): Map<String, Any?> = mapOf("chave" to chave)
        fun parametrosParcelas(docEntry: Int): Map<String, Any?> = mapOf("docEntry" to docEntry)
        fun parametrosPagamentos(docEntry: Int, parcela: Int): Map<String, Any?> =
            mapOf("docEntry" to docEntry, "parcela" to parcela)
    }
}

/** Filial do SAP exposta ao Nota Agil (`GET /contas-pagar/filiais`). */
data class FilialSap(val bplId: Int, val nome: String, val cnpj: String, val ativa: Boolean)
