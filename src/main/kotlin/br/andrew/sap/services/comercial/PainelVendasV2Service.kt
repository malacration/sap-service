package br.andrew.sap.services.comercial

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.comercial.painel.*
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Painel de vendas v2: agrega no banco, via sap-odbc.
 *
 * O painel v1 (PainelVendasService) soma em memoria porque o SQLQueries do
 * Service Layer recusa YEAR()/MONTH() em GROUP BY. Isso nao escala e nao permite
 * filtrar por filial, grupo ou regiao - dai esta versao.
 *
 * Responsabilidade central desta classe: **calcular o escopo efetivo** (filiais e
 * vendedor) a partir do usuario, e nunca confiar no que o front pediu.
 */
@Service
class PainelVendasV2Service(
    private val odbc: OdbcClient,
    /**
     * Allowlist de filiais do AMBIENTE (`painel-vendas.filiais=1,2,5`).
     *
     * Limite que vem do deploy, nao do usuario: nem admin passa dele. Serve para
     * um ambiente so enxergar as filiais que lhe dizem respeito, independente do
     * que o cadastro ou o Keycloak liberarem.
     *
     * Vazio = sem restricao adicional. Isso e seguro porque e configuracao de
     * infraestrutura, nao permissao de usuario - a permissao por usuario
     * continua valendo por cima.
     */
    @Value("\${painel-vendas.filiais:}") private val filiaisDoAmbiente: List<Int> = listOf(),
) {

    private val log = LoggerFactory.getLogger(PainelVendasV2Service::class.java)

    private val sqlKpis by lazy { carregar("odbc/painel-vendas-kpis.sql") }
    private val sqlEvolucao by lazy { carregar("odbc/painel-vendas-evolucao.sql") }

    fun kpis(auth: User, filtro: PainelFiltro): PainelKpis {
        val escopo = escopoEfetivo(auth, filtro)
        val linha = odbc.consultar(sqlKpis, parametros(sqlKpis, filtro, escopo), MAX_ROWS_KPI)
            .rows.firstOrNull()
            ?: throw IllegalStateException("A consulta de KPIs nao devolveu nenhuma linha.")

        val qtdFaturas = int(exigir(linha, "QTD_FATURAS"))
        val bruto = decimal(exigir(linha, "FATURAMENTO_BRUTO"))
        val clientesAtivos = int(exigir(linha, "CLIENTES_ATIVOS"))
        val valorProdutos = decimal(exigir(linha, "VALOR_PRODUTOS"))
        val qtdItens = decimal(exigir(linha, "QTD_ITENS"))

        return PainelKpis(
            faturamentoBruto = bruto,
            devolucoes = decimal(exigir(linha, "DEVOLUCOES")),
            faturamentoLiquido = decimal(exigir(linha, "FATURAMENTO_LIQUIDO")),
            qtdFaturas = qtdFaturas,
            qtdDevolucoes = int(exigir(linha, "QTD_DEVOLUCOES")),
            clientesAtivos = clientesAtivos,
            valorProdutos = valorProdutos,
            qtdItens = qtdItens,
            filiaisComVenda = int(exigir(linha, "FILIAIS_COM_VENDA")),
            // Zero faturas acontece de verdade: periodo vazio, ou venda e
            // cancelamento se anulando dentro do mesmo recorte.
            ticketMedio = if (qtdFaturas > 0)
                bruto.divide(BigDecimal(qtdFaturas), 2, RoundingMode.HALF_UP) else null,
            ticketMedioPorCliente = if (clientesAtivos > 0)
                bruto.divide(BigDecimal(clientesAtivos), 2, RoundingMode.HALF_UP) else null,
            // Quantidade pode ser negativa quando as devolucoes superam as vendas
            // no recorte; dividir por isso produziria preco negativo. Nesse caso,
            // vazio - o indicador nao significa nada ali.
            precoMedio = if (qtdItens > BigDecimal.ZERO)
                valorProdutos.divide(qtdItens, 2, RoundingMode.HALF_UP) else null,
            // No nivel do painel o recorte SEMPRE mistura unidades de medida
            // (kg, unidade, caixa). So seria confiavel dentro de um unico produto
            // ou grupo homogeneo - o que o ranking por produto vai permitir.
            precoMedioConfiavel = false,
            escopo = escopo.paraDto(),
        )
    }

    fun evolucao(auth: User, filtro: PainelFiltro): List<PontoEvolucao> {
        val escopo = escopoEfetivo(auth, filtro)
        val sql = sqlEvolucao.replace("__TRUNC__", fragmentoGranularidade(filtro.granularidade))
        // Duas series na MESMA consulta: duplicar a chamada dobraria a varredura
        // sobre OINV/ORIN so por causa do comparativo.
        val rows = odbc.consultar(sql, parametros(sql, filtro, escopo), MAX_ROWS_SERIE * 2).rows

        val atual = linkedMapOf<String, PontoEvolucao>()
        val anterior = mutableMapOf<String, BigDecimal>()

        rows.forEach { linha ->
            val periodo = exigir(linha, "PERIODO")?.toString() ?: return@forEach
            val valor = decimal(exigir(linha, "FATURAMENTO"))
            when (exigir(linha, "SERIE")?.toString()) {
                "ATUAL" -> atual[periodo] = PontoEvolucao(
                    periodo = periodo,
                    faturamento = valor,
                    qtdFaturas = int(exigir(linha, "QTD_FATURAS")),
                    anoAnterior = null,
                )
                // O SQL desloca a data antes de agrupar. Somar um ano ao rotulo
                // da semana trocaria a segunda-feira por outro dia da semana.
                "ANTERIOR" -> anterior[periodo] = valor
                else -> throw IllegalStateException("Serie desconhecida na consulta do painel.")
            }
        }

        // A serie anterior pode ter periodo que a atual nao tem (ex.: mes sem
        // venda este ano). Esses pontos entram com faturamento zero, senao a
        // linha do ano passado apareceria cortada sem explicacao.
        (anterior.keys - atual.keys).forEach { periodo ->
            atual[periodo] = PontoEvolucao(periodo, BigDecimal.ZERO, 0, null)
        }

        return atual.values
            .map { it.copy(anoAnterior = anterior[it.periodo]) }
            .sortedBy { it.periodo }
    }

    // ---------------------------------------------------------------- escopo

    private class Escopo(
        val filiais: List<Int>,
        val vendedor: Int,
        val superVendedor: Int,
        /** Admin sem filtro explicito: consulta todas as filiais, sem lista. */
        val todasFiliais: Boolean,
    ) {
        /** true quando a consulta deve restringir a um vendedor especifico. */
        fun filtraVendedor() = superVendedor != Int.MAX_VALUE || vendedor > 0

        fun paraDto() = EscopoAplicado(
            filiais = filiais,
            todasFiliais = todasFiliais,
            todosVendedores = !filtraVendedor(),
            vendedor = if (filtraVendedor()) vendedor else null,
        )
    }

    /**
     * Escopo efetivo = intersecao entre o que o usuario PEDIU e o que ele PODE ver.
     *
     * Duas regras que nao podem ser relaxadas:
     *  - lista de filiais vazia no token NEGA tudo. Interpretar vazio como "todas"
     *    faria um usuario mal provisionado enxergar a empresa inteira.
     *  - o escopo de vendedor do painel v1 e preservado: vendedor comum so ve o
     *    proprio SlpCode, mesmo que peca outro no filtro.
     */
    private fun escopoEfetivo(auth: User, filtro: PainelFiltro): Escopo {
        val superVendedor = auth.superVendedor()

        // ADMIN ve tudo: todas as filiais e todos os vendedores, sem depender de
        // cadastro de filial (HEM10/@RO_FILIAL_LINHA) nem de papel `filial-N` no
        // Keycloak. Continua podendo filtrar - o filtro so deixa de ser limite e
        // passa a ser escolha.
        val doAmbiente = filiaisDoAmbiente.filter { it > 0 }

        if (auth.isAdmin()) {
            // Admin ve tudo, EXCETO o que o ambiente nao expoe.
            val pedidas = filtro.filiais.distinct()
                .let { if (doAmbiente.isEmpty()) it else it.filter { f -> f in doAmbiente } }
            if (filtro.filiais.isNotEmpty() && pedidas.isEmpty()) {
                throw PainelAcessoException("Nenhuma das filiais selecionadas esta disponivel neste ambiente.")
            }
            val efetivas = pedidas.ifEmpty { doAmbiente }
            return Escopo(
                // Placeholder quando nao ha filtro nem allowlist: a lista nunca vai
                // vazia ao SQL, porque `IN ()` e erro de sintaxe. O flag todasFiliais
                // e que desliga a restricao.
                filiais = efetivas.ifEmpty { listOf(FILIAL_PLACEHOLDER) },
                vendedor = filtro.slpCode ?: 0,
                superVendedor = superVendedor,
                todasFiliais = efetivas.isEmpty(),
            )
        }

        val permitidas = auth.bussinesPlace
            .let { if (doAmbiente.isEmpty()) it else it.filter { f -> f in doAmbiente } }
        if (permitidas.isEmpty()) {
            throw PainelAcessoException(
                if (doAmbiente.isNotEmpty())
                    "Seu usuario nao tem filial liberada entre as disponiveis neste ambiente."
                else
                    "Seu usuario nao tem filial liberada para o painel de vendas. " +
                        "Peca a liberacao ao administrador.",
            )
        }
        val pedidas = filtro.filiais.filter { it in permitidas }
        val filiais = if (filtro.filiais.isEmpty()) permitidas else pedidas
        if (filiais.isEmpty()) {
            throw PainelAcessoException("Nenhuma das filiais selecionadas esta liberada para voce.")
        }

        // Nao-admin mantem a regra do painel v1 (PainelVendasService.resolverVendedor):
        // vendedor comum so enxerga o proprio SlpCode.
        val vendedor = if (superVendedor == Int.MAX_VALUE)
            filtro.slpCode ?: if (auth.origin == UserOriginEnum.SalePerson) auth.getIdInt() else 0
        else
            auth.getIdInt()

        return Escopo(filiais, vendedor, superVendedor, todasFiliais = false)
    }

    /**
     * Monta os parametros e devolve SOMENTE os que o SQL realmente usa.
     *
     * O sap-odbc recusa parametro sobrando (regra do ReadOnlySqlValidator), entao
     * um mapa fixo quebra assim que duas consultas passam a ter conjuntos
     * diferentes de placeholders - foi exatamente o que aconteceu quando a
     * evolucao ganhou o comparativo de ano anterior e os KPIs, que nao usam essas
     * datas, comecaram a falhar com 400.
     *
     * Derivar do proprio SQL elimina a classe do problema: acrescentar um
     * placeholder novo passa a bastar.
     */
    private fun parametros(sql: String, filtro: PainelFiltro, escopo: Escopo): Map<String, Any?> {
        validarPeriodo(filtro)
        val usados = PLACEHOLDER.findAll(sql).map { it.groupValues[1] }.toSet()
        return disponiveis(filtro, escopo).filterKeys { it in usados }
    }

    private fun disponiveis(filtro: PainelFiltro, escopo: Escopo): Map<String, Any?> {
        return mapOf(
            "dataInicio" to filtro.dataInicio.toString(),
            "dataFim" to filtro.dataFim.toString(),
            // Mesmo recorte, um ano atras - comparativo de sazonalidade.
            "dataInicioAnterior" to filtro.dataInicio.minusYears(1).toString(),
            "dataFimAnterior" to filtro.dataFim.minusYears(1).toString(),
            // Lista viaja como colecao: o NamedParameterJdbcTemplate do sap-odbc
            // expande em IN (...). Confirmado por BindListaTest la.
            "filiais" to escopo.filiais,
            "todasFiliais" to if (escopo.todasFiliais) 1 else 0,
            "vendedor" to escopo.vendedor,
            // Flag explicita em vez de depender de `SlpCode < Int.MAX_VALUE`:
            // aquela comparacao e verdadeira para qualquer linha, entao o filtro de
            // vendedor pedido por um admin era silenciosamente ignorado.
            "todosVendedores" to if (escopo.filtraVendedor()) 0 else 1,
        )
    }

    /**
     * O periodo precisa de teto: sem ele, uma consulta de dez anos varre INV1
     * inteira e estoura o timeout de 30s do gateway. Recusar com mensagem clara
     * e melhor do que entregar timeout.
     */
    private fun validarPeriodo(filtro: PainelFiltro) {
        if (filtro.dataFim.isBefore(filtro.dataInicio)) {
            throw PainelAcessoException("A data final nao pode ser anterior a inicial.")
        }
        val dias = ChronoUnit.DAYS.between(filtro.dataInicio, filtro.dataFim)
        val maximo = when (filtro.granularidade) {
            Granularidade.DIA -> 92L
            Granularidade.SEMANA -> 366L
            Granularidade.MES -> 1096L
        }
        if (dias > maximo) {
            throw PainelAcessoException(
                "O periodo de ${dias} dias excede o maximo de ${maximo} para esta granularidade. " +
                    "Reduza o intervalo ou use uma granularidade maior.",
            )
        }
    }

    /**
     * Fragmento de SQL por granularidade - whitelist fechada.
     *
     * Nao existe bind parameter para nome de funcao ou coluna, entao o `when`
     * exaustivo sobre o enum e o que impede injecao. Nada aqui e montado com
     * texto vindo do front.
     *
     * SEMANA devolve a data de INICIO da semana: agrupar por YEAR()+WEEK()
     * quebraria a semana que atravessa a virada do ano em duas.
     */
    private fun fragmentoGranularidade(g: Granularidade): String = when (g) {
        Granularidade.DIA -> """TO_VARCHAR(MOV.DATA_DOC, 'YYYY-MM-DD')"""
        Granularidade.SEMANA -> """TO_VARCHAR(ADD_DAYS(MOV.DATA_DOC, -1 * (WEEKDAY(MOV.DATA_DOC))), 'YYYY-MM-DD')"""
        Granularidade.MES -> """TO_VARCHAR(MOV.DATA_DOC, 'YYYY-MM')"""
    }

    /**
     * Carrega o SQL e REMOVE os comentarios.
     *
     * O ReadOnlySqlValidator do sap-odbc recusa `--` e blocos de comentario (eles
     * servem para esconder trecho da consulta de quem audita). Os arquivos .sql
     * daqui sao comentados de proposito, para serem legiveis por quem mantem; os
     * comentarios so nao podem viajar ate o gateway.
     */
    private fun carregar(caminho: String): String {
        val bruto = ClassPathResource(caminho).inputStream.bufferedReader().use { it.readText() }
        return bruto.lineSequence()
            .map { linha -> linha.substringBefore("--").trimEnd() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
            .removeSuffix(";")
    }

    /**
     * Garante que a coluna existe antes de converter.
     *
     * Devolver zero para coluna ausente produziria um KPI plausivel e falso - o
     * pior modo de falha num painel gerencial, porque ninguem desconfia de um
     * numero bem formatado. Se o contrato da consulta mudar, falha alto.
     */
    private fun exigir(linha: Map<String, Any?>, coluna: String): Any? {
        if (!linha.containsKey(coluna)) {
            throw IllegalStateException(
                "A consulta do painel nao devolveu a coluna '$coluna'. " +
                    "Colunas recebidas: ${linha.keys.sorted()}",
            )
        }
        return linha[coluna]
    }

    /**
     * NULL vira zero (agregacao sobre periodo vazio devolve NULL legitimamente),
     * mas texto nao numerico e erro - nao zero.
     *
     * O sap-odbc serializa BigDecimal como STRING (toPlainString); ler como Double
     * corromperia centavos, entao a conversao passa sempre por String.
     */
    private fun decimal(valor: Any?): BigDecimal = when (valor) {
        null -> BigDecimal.ZERO
        is BigDecimal -> valor
        else -> valor.toString().trim().toBigDecimalOrNull()
            ?: throw IllegalStateException("Valor nao numerico vindo da consulta: '$valor'")
    }

    private fun int(valor: Any?): Int = when (valor) {
        null -> 0
        is Number -> valor.toInt()
        else -> valor.toString().trim().toBigDecimalOrNull()?.toInt()
            ?: throw IllegalStateException("Valor nao numerico vindo da consulta: '$valor'")
    }

    private companion object {
        /** `:nome` no SQL, ignorando o segundo `:` de um cast `a::int`. */
        val PLACEHOLDER = Regex("(?<!:):([A-Za-z][A-Za-z0-9_]*)")

        /** Nunca casa com filial real; so ocupa o `IN (...)` quando o filtro esta desligado. */
        const val FILIAL_PLACEHOLDER = -1
        const val MAX_ROWS_KPI = 10
        // Teto da serie: 1096 dias na granularidade DIA e o pior caso.
        const val MAX_ROWS_SERIE = 1200
    }
}

class PainelAcessoException(override val message: String) : RuntimeException(message)
