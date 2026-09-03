package br.andrew.sap.services.tax

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.model.sap.tax.TaxCodeDespesa
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * O codigo de imposto de uma despesa adicional, lido do banco porque o Service Layer nao
 * entrega esse campo por caminho nenhum.
 *
 * Levantamento que motivou isto (cotacao 176199, frete de R$ 450 com ICMS desonerado de 19%
 * que o SAP realmente abateu):
 *
 *     Service Layer, DocumentAdditionalExpenses:
 *       TaxCode = null   VatGroup = null   TaxPercent = null   TaxSum = 0
 *       DocExpenseTaxJurisdictions = []    DocumentLineAdditionalExpenses = []
 *     Banco, QUT13:
 *       TaxCode = '5109-003'
 *
 * Nao e projecao faltando: em 469 despesas com valor, varridas em 1.100 documentos, nenhuma
 * trouxe qualquer campo de imposto. O `$metadata` do proprio Service Layer nem declara
 * TaxCode no DocExpenseTaxJurisdiction. O campo existe so na familia `*13`.
 *
 * Herdar o codigo da linha de produto foi considerado e descartado: existem codigos fiscais
 * proprios de frete no cadastro (`5101-009 ICMS OUTROS FRETE`, `6108-003 ICMS FRETE`), e nos
 * dados as duas situacoes aparecem - na cotacao 156559 o frete acompanha a linha (19% nos
 * dois), na 169667 a linha e desonerada a 19,5% e o frete nao leva abatimento nenhum.
 */
@Service
class TaxCodeDespesaService(val sqlQueriesService: SqlQueriesService) {

    val logger: Logger = LoggerFactory.getLogger(TaxCodeDespesaService::class.java)

    companion object {
        const val VIEW = "tax-code-despesa.sql"
    }

    /**
     * Codigo de imposto de cada despesa do documento, indexado por `LineNum`.
     *
     * Uma consulta por documento em vez de uma por despesa: a view ja devolve todas as linhas
     * daquele `ExpnsCode`, e os schedules rodam a cada 15s sobre lotes de documentos.
     *
     * Falha de leitura nao derruba o calculo do documento inteiro - devolve vazio e quem chama
     * loga a despesa que ficou sem codigo. O preco de nao majorar um frete e conhecido; o de
     * abortar o desonerado das linhas de produto e maior.
     */
    fun porLinha(tipo: DocumentTypes?, docEntry: Int?, expnsCode: Int): Map<Int, String> {
        if (tipo == null || docEntry == null) {
            logger.warn("Sem tipo ou docEntry, nao da para buscar o codigo de imposto da despesa " +
                "(tipo=$tipo docEntry=$docEntry)")
            return mapOf()
        }
        return try {
            sqlQueriesService.getAll<TaxCodeDespesa>(VIEW, listOf(
                Parameter("docEntry", docEntry),
                Parameter("objType", tipo.value.toString()),
                Parameter("expnsCode", expnsCode)))
                .filter { it.LineNum != null && !it.TaxCode.isNullOrBlank() }
                .associate { it.LineNum!! to it.TaxCode!! }
        } catch (e: Exception) {
            logger.error("Falha ao ler o codigo de imposto das despesas do documento $docEntry " +
                "(${tipo.name}) na view $VIEW", e)
            mapOf()
        }
    }
}
