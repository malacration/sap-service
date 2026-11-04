package br.andrew.sap.services.logistica
import br.andrew.sap.infrastructure.odata.*
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.model.logistica.Carregamento
import br.andrew.sap.model.logistica.PedidoUpdate
import br.andrew.sap.model.logistica.PedidoUpdateLine
import br.andrew.sap.model.sap.comercial.DocEntry
import br.andrew.sap.model.sap.sistema.SequenceCode
import br.andrew.sap.model.sap.documents.OrderSales
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.abstracts.SqlQueriesService
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchMethod
import br.andrew.sap.services.batch.BatchService
import br.andrew.sap.services.documents.OrdersService
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import br.andrew.sap.services.security.AuthService
import br.andrew.sap.model.logistica.CarregamentoLinha
import br.andrew.sap.model.logistica.EntregaFaturaReserva
import br.andrew.sap.model.logistica.LinhaFaturaReserva
import br.andrew.sap.model.logistica.RetiradaFatura
import br.andrew.sap.model.producao.BatchStock
import br.andrew.sap.model.sap.documents.Delivery
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.SqlResource
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import br.andrew.sap.model.sap.documents.DocumentTypes
import br.andrew.sap.services.fiscal.TaxNfeService
import java.io.ByteArrayOutputStream
import java.text.Normalizer
import java.math.BigDecimal
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.math.RoundingMode

@Service
class CarregamentoService(val batchService: BatchService, val pedidoVendaService : OrdersService, val sqlQueriesService : SqlQueriesService, val odbc: OdbcClient, val taxNfeService: TaxNfeService, env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService) :
    EntitiesService<Carregamento>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/ORD_CARREGAMENTO"
    }

    /** Quantidade em ordens Abertas de todos os itens, numa consulta (odbc/estoque-em-carregamento.sql). */
    fun estoqueEmCarregamentoPorItem(): Map<String, Double> =
        odbc.consultar(SqlResource.carregar("odbc/estoque-em-carregamento.sql"), emptyMap(), 10000).rows
            .associate { it["ItemCode"].toString() to decimal(it["Quantidade"]).toDouble() }

    fun docEntryPedido(idOrdemCarregamento: Int): List<DocEntry> {
        val parameters = listOf(
            Parameter("U_ORD_CARREGAMENTO", idOrdemCarregamento.toString())
        )
        return sqlQueriesService
            .getAll<DocEntry>("docentry-ordem-carregamento.sql", parameters)
    }

    fun cancelarPedidos(idOrdem: Int, docNums: List<Int>) {
        docNums.forEach { docNum ->
            val parameters = listOf(Parameter("DocNum", docNum.toString()))
            sqlQueriesService.execute("cancelPedido.sql", parameters)
        }
    }


    fun getTotaisPedidoseQuantidade(ordemCarregamento: Int): List<Carregamento> {
        val parameters = listOf(
            Parameter("ordCarregamento", ordemCarregamento) // igual ao do SQL
        )
        val pedidos = sqlQueriesService.execute("pedidoeestoque.sql", parameters)?.tryGetValues<Carregamento>() ?: emptyList()
        return pedidos + sqlQueriesService.getAll<Map<String, Any?>>("pedidoeestoque-faturas.sql", parameters)
            .map { retirada<Carregamento>(it, decimal(it["Retirada"])) }
    }

    /** Linhas de fatura de reserva da ordem, no mesmo formato de ord-carregamento.sql. */
    fun faturasDaOrdem(idOrdem: String): List<OrderSales> {
        val linhas = sqlQueriesService.getAll<Map<String, Any?>>("ord-carregamento-faturas.sql", listOf(Parameter("U_ORD_CARREGAMENTO", idOrdem.toInt())))
        val saldos = linhas.map { it["DocEntry"].toString().toInt() }.distinct()
            .flatMap { comSaldo(sqlQueriesService.getAll("fatura-reserva-saldo.sql", listOf(Parameter("docEntry", it), Parameter("ordem", idOrdem.toInt())))) }
            .associate { (linha, saldo) -> chaveLinha(linha) to saldo }
        return comFrete(linhas.map { linha ->
            retirada<OrderSales>(linha, decimal(linha["Retirada"])).also { it.SaldoFatura = saldos[chaveLinha(linha)] }
        })
    }

    /**
     * Faturas de reserva com saldo a entregar, nos filtros da busca de pedidos. Fatura sem
     * localidade aparece em qualquer localidade: nota antiga que ninguem vai preencher.
     * A pagina sai em CarregamentoController.searchFaturas.
     */
    fun searchFaturasReserva(dataInicial: String, dataFinal: String, filial: Int, localidade: String, vendedor: Int?): List<OrderSales> =
        comSaldo(sqlQueriesService.getAll("fatura-reserva-search.sql", listOf(
            Parameter("startDate", dataInicial),
            Parameter("finalDate", dataFinal),
            Parameter("filial", filial),
            Parameter("localidade", localidade),
            Parameter("vendedor", vendedor ?: -2),
            Parameter("vendedorDisable", if (vendedor == null) -2 else Int.MAX_VALUE),
        )))
            .filter { (_, saldo) -> saldo.signum() > 0 }
            .map { (linha, saldo) -> retirada<OrderSales>(linha, saldo).also { it.SaldoFatura = saldo } }
            .let { comFrete(it) }

    /**
     * Frete de cada retirada de NF de venda futura: frete da NF x quantidade / quantidade total da
     * NF, a mesma conta da entrega (EntregaFaturaReserva). Uma consulta para todas as NFs da lista.
     * Cada linha respeita o teto da entrega (o que resta do frete da NF: frete - DrawnTotal). A soma de
     * varias linhas da mesma NF pode passar do teto na tela: quem fecha a conta e a entrega, que depende de
     * quais linhas forem retiradas - e a tela nao sabe isso ao listar.
     */
    private fun comFrete(linhas: List<OrderSales>): List<OrderSales> {
        val docEntries = linhas.mapNotNull { it.DocEntry?.toIntOrNull() }.distinct()
        if (docEntries.isEmpty()) return linhas
        val fretes = odbc.consultar(SqlResource.carregar("odbc/fatura-reserva-frete.sql"), mapOf("docEntries" to docEntries), 10000)
            .rows.associate { it["DocEntry"].toString().toInt() to Triple(decimal(it["Frete"]), decimal(it["Quantidade"]), decimal(it["FreteBaixado"])) }
        linhas.groupBy { it.DocEntry?.toIntOrNull() }.forEach { (docEntry, daNF) ->
            val (frete, total, baixado) = fretes[docEntry] ?: return@forEach
            if (total.signum() <= 0) return@forEach
            //A tela recalcula ao mudar a retirada: manda o unitario sem arredondar e o que resta do frete da NF.
            daNF.forEach { linha ->
                linha.DistribSum = freteExibido(frete, baixado, decimal(linha.Quantity), total).toDouble()
                linha.FreteUnitario = frete.divide(total, 10, java.math.RoundingMode.HALF_UP).toDouble()
                linha.FreteRestante = (frete - baixado).max(BigDecimal.ZERO).toDouble()
            }
        }
        return linhas
    }

    /*
     * O SQLQueries do Service Layer nao aceita conta nem CASE no SELECT ("Invalid SQL syntax"),
     * entao as views de fatura de reserva trazem uma linha por retirada com os valores crus
     * (OpenQty, Retirada, OrdemAberta, PesoLinha/QtdFatura da linha inteira) e a conta sai aqui.
     */

    private inline fun <reified T : Any> retirada(linha: Map<String, Any?>, quantidade: BigDecimal): T =
        OData(LinkedHashMap(linha + mapOf("Quantity" to quantidade, "Weight1" to peso(linha, quantidade),
            "Name" to localidade(linha)))).tryGetValue<T>()

    /**
     * Grava as retiradas de fatura de reserva da ordem (substitui a colecao inteira). O saldo
     * de cada linha e o OpenQty da fatura - ja desconta entrega e devolucao - menos o que
     * outras ordens Abertas reservaram; ordem Fechada ja virou entrega e saiu do OpenQty.
     * shortcut: duas ordens salvas no mesmo instante podem reservar o mesmo saldo; a geracao
     * da entrega confere o OpenQty de novo, entao o pior caso e a nota recusada.
     */
    fun salvarRetiradas(idOrdem: Int, retiradas: List<RetiradaFatura>) {
        val linhas = retiradas
            .groupBy { it.docEntry to it.lineNum }
            .map { (chave, itens) -> Triple(chave.first, chave.second, itens.sumOf { BigDecimal(it.quantidade.toString()) }) }
            .groupBy { it.first }
            .flatMap { (docEntry, daFatura) ->
                val saldos = comSaldo(sqlQueriesService.getAll("fatura-reserva-saldo.sql", listOf(
                    Parameter("docEntry", docEntry), Parameter("ordem", idOrdem))))
                daFatura.map { (_, lineNum, quantidade) ->
                    val (saldo, disponivel) = saldos.find { (it.first["BaseLine"] as Number).toInt() == lineNum }
                        ?: throw Exception("NF de venda futura DocEntry $docEntry linha $lineNum nao e NF de venda futura valida")
                    if (quantidade.signum() <= 0)
                        throw Exception("Quantidade invalida na NF de venda futura DocEntry $docEntry linha $lineNum")
                    if (quantidade > disponivel)
                        throw Exception("NF de venda futura DocEntry $docEntry linha $lineNum (${saldo["ItemCode"]}): retirada de " +
                            "${quantidade.toPlainString()} maior que o saldo disponivel ${disponivel.toPlainString()}")
                    CarregamentoLinha(
                        U_docEntryFatura = docEntry,
                        U_baseLine = lineNum,
                        U_qtdRetirada = quantidade.toDouble(),
                        U_itemCode = saldo["ItemCode"]?.toString(),
                        U_cardCode = saldo["CardCode"]?.toString(),
                    )
                }
            }
        updateReplacingCollections(mapOf("ORD_CRG_LINHACollection" to linhas), idOrdem.toString())
    }

    /** Uma entrega (ODLN) por fatura de reserva da ordem. Regras em [EntregaFaturaReserva]. */
    fun entregasDaOrdem(carregamento: Carregamento, lotes: (Int, Product) -> List<BatchStock>): List<Delivery> {
        val mapper = jacksonObjectMapper()
        return carregamento.retiradasDeFatura()
            .groupBy { it.U_docEntryFatura ?: throw Exception("Retirada sem NF de venda futura na ordem ${carregamento.DocEntry}") }
            .map { (docEntryFatura, retiradas) ->
                val fatura = odbc.consultar(SqlResource.carregar("odbc/fatura-reserva-entrega.sql"),
                    mapOf("fatura" to docEntryFatura), 1000).rows
                    .map { mapper.convertValue(it, LinhaFaturaReserva::class.java) }
                val bplId = fatura.firstOrNull()?.BPLId?.toString() ?: ""
                val seqCode = procuraSequenceCode(bplId).firstOrNull()?.SeqCode
                    ?: throw Exception("SeqCode não encontrado para a filial $bplId")
                EntregaFaturaReserva.montar(docEntryFatura, fatura, retiradas, seqCode) { lotes(docEntryFatura, it) }.also {
                    it.comments = observacaoComOrdem(carregamento.DocEntry!!, fatura.first().Observacao,
                        fatura.first().NumeroNF?.let { n -> "Based On A/R Invoices $n." })
                }
            }
    }

    /**
     * Pedidos da busca do carregamento pelo sap-odbc (odbc/carregamento-pedidos.sql), paginados por
     * pedido. Pelo odbc porque o SQLQueries nao aceita IFNULL(...) LIKE: sem filtro de localidade ou
     * cidade, pedido sem localidade/cidade tambem aparece. Devolve a pagina e se ha mais.
     */
    fun searchPedidos(dataInicial: String, dataFinal: String, filial: Int, localidade: String, vendedor: Int?,
                      cidade: String?, pagina: Int, porPagina: Int): Pair<List<OrderSales>, Boolean> {
        val rows = odbc.consultar(SqlResource.carregar("odbc/carregamento-pedidos.sql"), mapOf(
            "startDate" to dataInicial,
            "finalDate" to dataFinal,
            "localidade" to localidade,
            "cidade" to "%${semAcento(cidade).uppercase()}%",
            "filial" to filial,
            "vendedor" to (vendedor ?: -2),
            "vendedorDisable" to (if (vendedor == null) -2 else Int.MAX_VALUE),
            "inicio" to pagina * porPagina,
            "fim" to (pagina + 1) * porPagina,
        ), 10000).rows
        val total = rows.firstOrNull()?.get("TotalDocumentos")?.toString()?.toIntOrNull() ?: 0
        return rows.map { OData(LinkedHashMap(it)).tryGetValue<OrderSales>() } to (total > (pagina + 1) * porPagina)
    }

    /**
     * Historico da ordem (odbc/carregamento-historico.sql). Cada salvamento grava duas versoes no log
     * (cabecalho e depois as retiradas), entao versoes seguidas com a mesma acao, usuario e cabecalho
     * viram uma so, com o estado da ultima.
     */
    fun historico(idOrdem: Int): List<Map<String, Any?>> =
        agruparHistorico(odbc.consultar(SqlResource.carregar("odbc/carregamento-historico.sql"), mapOf("ordem" to idOrdem), 1000).rows)

    /** Notas e entregas da ordem com o status da NF-e na TaxPlus (odbc/carregamento-notas-status.sql). */
    fun statusNotas(idOrdem: Int): List<Map<String, Any?>> =
        odbc.consultar(SqlResource.carregar("odbc/carregamento-notas-status.sql"), mapOf("ordem" to idOrdem), 1000).rows

    /**
     * Zip com o DANFE de cada nota/entrega da ordem. Sem chave (nao foi para a SEFAZ) ou falha na
     * TaxPlus vai para erros.txt sem derrubar o resto.
     * shortcut: monta em memoria; uma ordem tem poucas notas. Se passar de dezenas, usar arquivo
     * temporario como NotaFiscalLoteService.zip.
     */
    fun danfes(idOrdem: Int): ByteArray {
        val saida = ByteArrayOutputStream()
        ZipOutputStream(saida).use { zip ->
            val erros = mutableListOf<String>()
            statusNotas(idOrdem).forEach { nota ->
                val docType = (nota["DocType"] as Number).toInt()
                val nome = "${if (docType == DocumentTypes.oDeliveryNotes.value) "entrega" else "nota"}-${nota["Serial"] ?: nota["DocNum"]}"
                if (nota["KeyNfe"]?.toString().isNullOrBlank()) {
                    erros += "$nome: NF-e ainda nao foi para a SEFAZ"
                    return@forEach
                }
                try {
                    val pdf = taxNfeService.pdf(taxNfeService.consultar((nota["DocEntry"] as Number).toInt(), docType))
                    zip.putNextEntry(ZipEntry("$nome.pdf"))
                    zip.write(pdf)
                    zip.closeEntry()
                } catch (t: Throwable) {
                    erros += "$nome: DANFE nao baixado (${t.message})"
                }
            }
            if (erros.isNotEmpty()) {
                zip.putNextEntry(ZipEntry("erros.txt"))
                zip.write(erros.joinToString("\n").toByteArray())
                zip.closeEntry()
            }
        }
        return saida.toByteArray()
    }

    fun procuraSequenceCode(BPLId: String): List<SequenceCode> {
        val parameters = listOf(Parameter("BPLId", BPLId))

        return sqlQueriesService
            .execute("procura-sequence-code.sql", parameters)
            ?.tryGetValues<SequenceCode>()
            ?: emptyList()
    }

    fun cancelar(id: String, usuario: String): Carregamento {
        val docEntry = id.toInt()

        val pedidosVinculados = docEntryPedido(docEntry)

        val tripleRemover = pedidosVinculados.map { ref ->
            val order = pedidoVendaService.getById(ref.DocEntry.toString()).tryGetValue<OrderSales>()

            val pedidosUpdate = PedidoUpdate(
                order.DocEntry.toString(),
                order.DocumentLines.map { PedidoUpdateLine(it.DocEntry!!, it.LineNum!!, null) },
                order.docNum
            )
            Triple(BatchMethod.PATCH, pedidosUpdate, pedidoVendaService)
        }

        if (tripleRemover.isNotEmpty()) {
            batchService.run(BatchList().addAll(tripleRemover))
        }

        val carregamento = getById(id).tryGetValue<Carregamento>()
        carregamento.U_Status = "Cancelado"
        carregamento.U_usuario = usuario
        carregamento.U_acao = "Cancelada"
        //Sem LineId o PATCH acrescentaria as linhas de novo. As retiradas ficam como historico:
        //ordem Cancelada nao conta no saldo da fatura.
        carregamento.ORD_CRG_LINHACollection = null

        update(carregamento, id)

        return carregamento
    }


    fun preencherTotais(page: Page<Carregamento>): Page<Carregamento> {
        val ordens = page.content

        val ids = ordens.mapNotNull { it.DocEntry }
        if (ids.isEmpty()) {
            return page
        }

        val parameters = mutableListOf<Parameter>()
        val totalParams = 20
        val lastId = ids.last()

        for (i in 0 until totalParams) {
            val idParaBusca = if (i < ids.size) ids[i] else lastId
            parameters.add(Parameter("p${i + 1}", idParaBusca.toString()))
        }

        //Pedidos e faturas de reserva da mesma ordem vem em linhas separadas: soma as duas.
        val pedidos: List<Map<String, Any?>> = sqlQueriesService.execute("peso-ordem-carregamento.sql", parameters)
            ?.tryGetValues<Map<String, Any?>>() ?: emptyList()
        val faturas = sqlQueriesService.getAll<Map<String, Any?>>("peso-ordem-carregamento-faturas.sql", parameters)
            .groupBy { it["DocEntry"] }
            .map { (docEntry, retiradas) -> mapOf(
                "DocEntry" to docEntry,
                "Weight1" to retiradas.sumOf { peso(it, decimal(it["Retirada"])) },
                "docEntryQuantity" to retiradas.map { it["Fatura"] }.distinct().size) }
        val porOrdem = (pedidos + faturas).groupBy { it["DocEntry"].toString().toIntOrNull() }

        ordens.forEach { ordem ->
            val dadosExtras = porOrdem[ordem.DocEntry].orEmpty()
            ordem.Weight1 = dadosExtras.sumOf { (it["Weight1"] as? Number)?.toDouble() ?: 0.0 }
            ordem.docEntryQuantity = dadosExtras.sumOf { (it["docEntryQuantity"] as? Number)?.toInt() ?: 0 }
        }

        return page
    }

    companion object {
        //Data e hora (minuto) entram na chave: dois salvamentos iguais em minutos diferentes nao se juntam.
        private val CAMPOS_HISTORICO = listOf("Status", "Usuario", "Acao", "Placa", "Motorista", "Transportadora",
            "Capacidade", "UpdateDate", "UpdateTime")

        const val EM_ANDAMENTO = " em andamento"

        internal fun agruparHistorico(versoes: List<Map<String, Any?>>): List<Map<String, Any?>> {
            val linhas = versoes.fold(mutableListOf<Map<String, Any?>>()) { acc, v ->
                val anterior = acc.lastOrNull()
                if (anterior != null && CAMPOS_HISTORICO.all { anterior[it]?.toString() == v[it]?.toString() }) acc[acc.lastIndex] = v
                else acc.add(v)
                acc
            }
            //"Criacao/Alteracao em andamento" so some quando a linha seguinte e o fim da MESMA operacao (sucesso ou
            //recusa). Se a operacao caiu no meio, ou nao conseguiu registrar o fim, a tentativa continua no
            //historico - mesmo que outra acao venha depois - e nao diz que deu certo.
            return linhas.filterIndexed { i, v ->
                val acao = v["Acao"]?.toString().orEmpty()
                if (!acao.endsWith(EM_ANDAMENTO)) return@filterIndexed true
                val seguinte = linhas.getOrNull(i + 1)?.get("Acao")?.toString().orEmpty()
                val terminou = if (acao.startsWith("Criação")) seguinte == "Criada" || seguinte.startsWith("Falhou")
                    else seguinte == "Alterada" || seguinte.startsWith("Alteração recusada")
                !terminou
            }
        }

        /** Frete proporcional da retirada, limitado ao que resta do frete da NF (frete - baixado), como a entrega. */
        internal fun freteExibido(frete: BigDecimal, baixado: BigDecimal, quantidade: BigDecimal, total: BigDecimal): BigDecimal =
            freteProporcional(frete, quantidade, total).min((frete - baixado).max(BigDecimal.ZERO))

        internal fun freteProporcional(frete: BigDecimal, quantidade: BigDecimal, quantidadeNF: BigDecimal): BigDecimal =
            frete.multiply(quantidade).divide(quantidadeNF, 2, java.math.RoundingMode.HALF_UP)

        /**
         * Observacao da nota/entrega gerada pela ordem: "Ordem de carregamento N." na frente do texto
         * do documento de origem (instrucao de entrega), que o SAP copiaria sozinho se o campo fosse
         * vazio, + a referencia ao documento de origem ("Based On ..."), que o SAP so poe com o campo
         * vazio. Comments tem 254 caracteres: se nao couber, corta o texto do meio, nunca a ordem nem a
         * referencia.
         */
        fun observacaoComOrdem(ordem: Int, original: String?, baseadoEm: String? = null): String {
            val ordemTexto = "Ordem de carregamento $ordem."
            val espaco = 254 - ordemTexto.length - (baseadoEm?.let { it.length + 1 } ?: 0) - 1
            val meio = original?.trim()?.takeIf { it.isNotEmpty() }?.take(maxOf(espaco, 0))
            return listOfNotNull(ordemTexto, meio, baseadoEm).joinToString(" ").take(254)
        }

        internal fun chaveLinha(linha: Map<String, Any?>) = linha["DocEntry"].toString() to linha["BaseLine"].toString()

        internal fun decimal(valor: Any?) = BigDecimal(valor?.toString() ?: "0")

        /** Saldo de cada linha de fatura: OpenQty menos o que ordens Abertas ja reservaram. */
        internal fun comSaldo(linhas: List<Map<String, Any?>>): List<Pair<Map<String, Any?>, BigDecimal>> =
            linhas.groupBy { chaveLinha(it) }.values.map { daLinha ->
                val reservado = daLinha.filter { it["OrdemAberta"] != null }.sumOf { decimal(it["Retirada"]) }
                daLinha.first() to decimal(daLinha.first()["OpenQty"]).minus(reservado)
            }

        /** NF de venda futura antiga vem sem localidade: mostra a cidade/UF do endereco de entrega. */
        internal fun localidade(linha: Map<String, Any?>): String? =
            linha["Name"]?.toString()?.takeIf { it.isNotBlank() }
                ?: listOf(linha["CityS"], linha["StateS"]).mapNotNull { it?.toString()?.takeIf(String::isNotBlank) }
                    .joinToString(" - ").takeIf { it.isNotEmpty() }

        /** Filtro de cidade da busca: contem, sem acento e sem caixa. Sem filtro passa tudo. */
        internal fun cidadeConfere(cidade: String?, filtro: String?): Boolean =
            filtro.isNullOrBlank() || semAcento(cidade).contains(semAcento(filtro))

        internal fun semAcento(texto: String?) =
            Normalizer.normalize(texto ?: "", Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()

        /** Peso da retirada: a view traz o peso (PesoLinha) e a quantidade (QtdFatura) da linha inteira. */
        internal fun peso(linha: Map<String, Any?>, quantidade: BigDecimal): BigDecimal =
            decimal(linha["PesoLinha"]).multiply(quantidade).divide(decimal(linha["QtdFatura"]), 4, RoundingMode.HALF_UP)
    }
}
