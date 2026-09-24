import br.andrew.sap.model.sap.comercial.DebOrCredt
import br.andrew.sap.model.sap.comercial.ReconciliationListRows
import br.andrew.sap.model.sap.comercial.ReconciliationRow
import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
class JournalEntry(val journalEntryLines : List<JournalEntryLines>, val memo : String) : ReconciliationListRows {

    var taxDate : String? = null
    var ReferenceDate : String? = null
    var JdtNum : Int? = null
    var OriginalJournal : String? = null
    var Original : Int? = null
    var U_Atualizar_Observacao : Int? = null
    var U_Atualizar_Centro_de_Custo : Int? = null
    var TransactionCode : String? = null


    var Reference : String? = null
    var Reference2 : String? = null
    var Reference3 : String? = null


    @JsonIgnore
    fun getFilial() : Int? {
        return this.journalEntryLines.map { it.getBPLID() }.firstOrNull()
    }

    fun costingCodes(costingCode: String, costingCode2: String) {
        journalEntryLines.forEach{
            it.costingCode = costingCode
            it.costingCode2 = costingCode2
        }
    }

    //o SAP devolve dimensao nao preenchida ora como null ora como string vazia; so "!= null"
    //faria o rateio vazio ser copiado para o lancamento e, por causa do JsonInclude NON_EMPTY,
    //nem seria enviado no PATCH - o lancamento seria marcado como processado sem rateio nenhum
    private fun DocumentLines.temRateio() = !CostingCode.isNullOrBlank() && !CostingCode2.isNullOrBlank()

    fun costingCodes(document: Document) {
        val line : DocumentLines = document.DocumentLines.firstOrNull { it.temRateio() }
            ?: throw Exception("Esse documento nao possuiu nenhuma linha de produto")
        costingCodes(line.CostingCode!!,line.CostingCode2!!)
    }

    //Todo fazer teste de unidade para esse cara
    @JsonIgnore
    fun hasContaResultado(): Boolean {
        return journalEntryLines.any { it.isContaResultado() }
    }

    //cada dimensao e preenchida em separado: linha com grupo economico valido e centro de custo
    //vazio mantem o grupo que ja estava la
    @JsonIgnore
    fun preencheCentroCustoDasContas(contas: List<String>, filial: Int, costingCode: String, costingCode2: String): Boolean {
        val alvo = contas.map { it.trim() }
        var alterou = false
        journalEntryLines
            .filter { it.AccountCode.trim() in alvo && it.getBPLID() == filial }
            .forEach {
                if (it.costingCode.isNullOrBlank()) {
                    it.costingCode = costingCode
                    alterou = true
                }
                if (it.costingCode2.isNullOrBlank()) {
                    it.costingCode2 = costingCode2
                    alterou = true
                }
            }
        return alterou
    }

    /**
     * O Service Layer recusa qualquer update no lancamento quando alguma linha aponta para uma
     * regra de reparticao inativa ("-5002 Inactive distribution rule: X"), mesmo que o PATCH nao
     * mexa naquela linha. Cada dimensao e avaliada em separado para preservar a que continua
     * valida.
     */
    @JsonIgnore
    fun substituiRateioInativo(
        inativosGrupo: Set<String>,
        inativosCentro: Set<String>,
        padraoPorFilial: Map<String, DistribuicaoCustoByBranch>
    ): List<String> {
        //devolve o que foi trocado, e nao so um sim/nao: trocar dimensao de lancamento contabil
        //precisa aparecer no log com o valor antigo e o novo
        val trocas = mutableListOf<String>()
        journalEntryLines.forEach { linha ->
            val padrao = padraoPorFilial[linha.getBPLID().toString()] ?: return@forEach
            if (linha.costingCode?.trim() in inativosGrupo) {
                trocas += "linha ${linha.Line_ID} grupo ${linha.costingCode}->${padrao.grupoEconomico}"
                linha.costingCode = padrao.grupoEconomico
            }
            if (linha.costingCode2?.trim() in inativosCentro) {
                trocas += "linha ${linha.Line_ID} centro ${linha.costingCode2}->${padrao.centroCusto}"
                linha.costingCode2 = padrao.centroCusto
            }
        }
        return trocas
    }

    constructor(filial : Int,
                accDebit : String,
                accCredit : String,
                value : Double,
                memo : String) : this(
        listOf(
            JournalEntryLines(accDebit,value,0.0,filial),
            JournalEntryLines(accCredit,0.0,value,filial)
        ),
        memo
    )

    @JsonIgnore
    override fun getReconciliationRows(debOrCredt: DebOrCredt): List<ReconciliationRow> {
        return if(debOrCredt == DebOrCredt.Credt)
            this.journalEntryLines.filter { it.Credit > 0 }.map {
                it.JdtNum = this.JdtNum;
                it
            }
        else
            this.journalEntryLines.filter { it.debit > 0 }.map {
                it.JdtNum = this.JdtNum;
                it
            }
    }

    override fun toString(): String {
        return "JournalEntry(JdtNum=$JdtNum, memo='$memo')"
    }

}
