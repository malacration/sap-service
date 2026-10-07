package br.andrew.sap.services.journal.importacao

import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Le o CSV de lancamentos no mesmo layout do /journal/save (JournalEntriesService.readCsv):
 *
 *     filial;(ignorada);data;conta debito;conta credito;valor;historico;grupo economico;centro de custo;ref1;ref2;ref3
 *
 * Diferente do readCsv, nunca lanca excecao por causa do conteudo: cada problema vira erro da
 * linha, para a previa mostrar o arquivo inteiro. E corrige o que o readCsv deixava passar calado:
 * data invalida que rola para outro dia (31/02 -> 03/03), ano com dois digitos, valor com milhar,
 * valor zero ou negativo, ';' dentro do historico e arquivo sem cabecalho perdendo a 1a linha.
 */
object LancamentoCsvParser {

    /**
     * OJDT.Memo aceita 254 caracteres nesta versao do SAP: o maior Memo gravado no HOMOLOG2 tem 254, e
     * o curl antigo importou historicos de 60 (lancamento 1499715). Um limite menor aqui recusaria
     * arquivo que sempre entrou.
     */
    const val TAMANHO_HISTORICO = 254
    private const val COLUNAS_MINIMAS = 9
    private const val COLUNAS_MAXIMAS = 12
    private const val ASPAS_SEM_FECHAR = "Aspas sem fechamento na linha. O texto entre aspas não pode ter quebra de linha."
    private const val ASPAS_MALFORMADAS = "Aspas malformadas: depois de fechar as aspas, o campo tem que terminar no ';'."

    //"uuuu" exige 4 digitos no ano e STRICT recusa 31/02 em vez de rolar para marco
    private val formatoData = DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)
    private val milharSemCentavos = Regex("""\d{1,3}(\.\d{3})+""")
    //com virgula: inteiro sem separador, ou com ponto a cada 3 digitos. "1.23,45" nao e 123,45
    private val brasileiro = Regex("""(\d{1,3}(\.\d{3})+|\d+),\d+""")
    private val numero = Regex("""\d+(\.\d+)?""")
    private val quebraDeLinha = Regex("\r\n|\n|\r")

    fun ler(bytes: ByteArray): LeituraCsv {
        val (texto, codificacao) = decodificar(bytes)
        return ler(texto, codificacao)
    }

    /**
     * O Excel em portugues salva CSV em Windows-1252, e quem gera pelo sistema costuma mandar UTF-8.
     * UTF-8 estrito primeiro: um byte acentuado de Windows-1252 nunca forma UTF-8 valido, entao a
     * tentativa falha em vez de trocar o acento por lixo.
     */
    fun decodificar(bytes: ByteArray): Pair<String, String> {
        val temBom = bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        val conteudo = if (temBom) bytes.copyOfRange(3, bytes.size) else bytes
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(conteudo))
                .toString() to "UTF-8"
        } catch (e: CharacterCodingException) {
            String(conteudo, Charset.forName("windows-1252")) to "Windows-1252"
        }
    }

    fun ler(texto: String, codificacao: String): LeituraCsv {
        val linhasArquivo = texto.split(quebraDeLinha)
        if (linhasArquivo.all { it.isBlank() })
            return LeituraCsv(codificacao, emptyList(), listOf("O arquivo está vazio."))

        val erros = mutableListOf<String>()
        if (pareceLancamento(separar(linhasArquivo.first()).valores))
            erros += "A primeira linha do arquivo é sempre o cabeçalho e não é importada, mas ela começa com " +
                "uma filial, como um lançamento. Inclua a linha de cabeçalho no arquivo."

        val linhas = linhasArquivo.withIndex()
            .drop(1)
            .map { (indice, conteudo) -> indice + 1 to separar(conteudo) }
            //o Excel exporta as linhas vazias do fim da planilha como ";;;;;;;;". Linha com problema de
            //aspas fica mesmo vazia: uma aspa solta e resto de campo partido, nao linha em branco
            .filter { (_, campos) -> campos.problema != null || campos.valores.any { it.isNotBlank() } }
            .map { (numero, campos) -> linha(numero, campos.valores.map { it.trim() }, campos.problema) }

        if (linhas.isEmpty() && erros.isEmpty())
            erros += "O arquivo não tem nenhum lançamento além do cabeçalho."
        return LeituraCsv(codificacao, linhas, erros)
    }

    class Campos(val valores: List<String>, val problema: String?)

    /**
     * Separa por ';' respeitando aspas, que e como o Excel exporta um campo que contem ';'. Aspas que
     * nao fecham na linha, ou texto depois da aspa de fechamento, viram problema da linha em vez de
     * serem engolidos: um historico cortado errado iria para o SAP sem ninguem ver.
     */
    fun separar(linha: String): Campos {
        val campos = mutableListOf<String>()
        val atual = StringBuilder()
        var entreAspas = false
        var aposAspas = false
        var problema: String? = null
        var i = 0
        while (i < linha.length) {
            val c = linha[i]
            when {
                entreAspas && c == '"' && linha.getOrNull(i + 1) == '"' -> { atual.append('"'); i++ }
                entreAspas && c == '"' -> { entreAspas = false; aposAspas = true }
                entreAspas -> atual.append(c)
                c == ';' -> { campos += atual.toString(); atual.clear(); aposAspas = false }
                aposAspas -> if (!c.isWhitespace()) problema = problema ?: ASPAS_MALFORMADAS
                c == '"' && atual.isBlank() -> { atual.clear(); entreAspas = true }
                else -> atual.append(c)
            }
            i++
        }
        if (entreAspas) problema = ASPAS_SEM_FECHAR
        campos += atual.toString()
        return Campos(campos, problema)
    }

    fun valor(bruto: String): Pair<BigDecimal?, String?> {
        val texto = bruto.replace("R$", "").replace(" ", "").replace(" ", "")
        if (texto.isEmpty()) return null to "Valor não informado."
        if (texto.startsWith("-"))
            return null to "Valor negativo: '$bruto'. As contas de débito e crédito já definem o sentido."
        val normalizado = when {
            texto.contains(',') && !brasileiro.matches(texto) -> return null to "Valor inválido: '$bruto'."
            texto.contains(',') -> texto.replace(".", "").replace(',', '.')
            milharSemCentavos.matches(texto) ->
                return null to "Valor ambíguo: '$bruto'. Use vírgula nos centavos (ex.: 1.234,00)."
            else -> texto
        }
        if (!numero.matches(normalizado)) return null to "Valor inválido: '$bruto'."
        val valor = BigDecimal(normalizado)
        if (valor.signum() == 0) return null to "O valor tem que ser maior que zero."
        if (valor.stripTrailingZeros().scale() > 2) return null to "Valor com mais de 2 casas decimais: '$bruto'."
        return valor.setScale(2) to null
    }

    fun data(texto: String): LocalDate? =
        try {
            LocalDate.parse(texto, formatoData)
        } catch (e: DateTimeParseException) {
            null
        }

    //cabecalho nunca comeca com numero; exigir tambem data valida deixaria passar calado um arquivo
    //sem cabecalho cuja primeira linha tem data errada
    private fun pareceLancamento(campos: List<String>): Boolean = campos[0].trim().toIntOrNull() != null

    private fun linha(numero: Int, campos: List<String>, problemaAspas: String?): LinhaLancamento {
        fun campo(i: Int) = campos.getOrElse(i) { "" }
        val (valor, erroValor) = valor(campo(5))
        val linha = LinhaLancamento(
            linha = numero,
            filial = campo(0).toIntOrNull()?.takeIf { it > 0 },
            dataLancamento = data(campo(2)),
            contaDebito = campo(3),
            contaCredito = campo(4),
            valor = valor,
            historico = campo(6),
            grupoEconomico = campo(7).ifBlank { null },
            centroCusto = campo(8).ifBlank { null },
            referencia = campo(9).ifBlank { null },
            referencia2 = campo(10).ifBlank { null },
            referencia3 = campo(11).ifBlank { null },
        )

        problemaAspas?.let {
            linha.erros += it
            return linha
        }
        //com coluna faltando, os campos seguintes estao todos deslocados: um erro so diz mais
        //do que "valor invalido", "historico vazio", ... um por coluna
        if (campos.size < COLUNAS_MINIMAS) {
            linha.erros += "A linha tem ${campos.size} colunas; são pelo menos $COLUNAS_MINIMAS " +
                "(da filial até o centro de custo)."
            return linha
        }
        if (campos.drop(COLUNAS_MAXIMAS).any { it.isNotBlank() })
            linha.erros += "A linha tem ${campos.size} colunas; são no máximo $COLUNAS_MAXIMAS. Se o histórico " +
                "ou a referência tem ';', coloque o texto entre aspas."

        if (linha.filial == null)
            linha.erros += if (campo(0).isBlank()) "Filial não informada." else "Filial inválida: '${campo(0)}'."
        if (linha.dataLancamento == null)
            linha.erros += if (campo(2).isBlank()) "Data não informada."
            else "Data inválida: '${campo(2)}'. Use dd/mm/aaaa."
        if (linha.contaDebito.isBlank()) linha.erros += "Conta de débito não informada."
        if (linha.contaCredito.isBlank()) linha.erros += "Conta de crédito não informada."
        if (linha.contaDebito.isNotBlank() && linha.contaDebito == linha.contaCredito)
            linha.erros += "A conta de débito e a de crédito são a mesma (${linha.contaDebito})."
        erroValor?.let { linha.erros += it }
        if (linha.historico.isBlank()) linha.erros += "Histórico não informado."
        else if (linha.historico.length > TAMANHO_HISTORICO)
            linha.erros += "Histórico com ${linha.historico.length} caracteres; o SAP aceita até $TAMANHO_HISTORICO."
        return linha
    }
}
