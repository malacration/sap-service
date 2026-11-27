package br.andrew.sap.services.fiscal

import br.andrew.sap.model.fiscal.ChaveDocumentoLote
import br.andrew.sap.model.fiscal.CriterioImportacaoNumeroNota
import br.andrew.sap.model.fiscal.ImportacaoInvalidaException
import br.andrew.sap.model.fiscal.ImportacaoLoteResultado
import br.andrew.sap.model.fiscal.LinhaImportacaoLote
import br.andrew.sap.model.fiscal.ModoImportacaoLote
import br.andrew.sap.model.fiscal.SituacaoLinhaImportacao
import br.andrew.sap.model.fiscal.TipoDocumentoLote
import br.andrew.sap.services.odbc.OdbcClient
import br.andrew.sap.services.odbc.OdbcException
import br.andrew.sap.services.odbc.SqlResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.text.Normalizer

/**
 * Monta o lote a partir da planilha de titulos do banco (a "Consulta de titulos" do BB, exportada
 * em .xlsx): cabecalho "Nosso numero | Nome pagador | Nro beneficiario | Valor | Vencimento |
 * Situacao", com um bloco de totais acima dele.
 *
 * O nosso numero e a chave: a BankPlus grava o boleto com ele (sem o digito verificador) e com o
 * documento e a parcela de origem (odbc/nota-fiscal-lote-boletos.sql). O "Nro beneficiario" so
 * confere: NF vem "numero parcela" ("005715 2"), adiantamento vem o DocEntry.
 */
@Service
class NotaFiscalLoteImportacaoService(
    val odbc: OdbcClient,
    val lote: NotaFiscalLoteService,
    @Value("\${nota-fiscal.lote.importacao.maximo-linhas:200}") val maximoLinhas: Int = 200,
    @Value("\${importacao.numero-nota.criterio:NUMERO_NOME}") val criterio: CriterioImportacaoNumeroNota = CriterioImportacaoNumeroNota.NUMERO_NOME,
) {

    private val sqlBoletos by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-boletos.sql") }

    private class LinhaPlanilha(val linha: Int, val nossoNumero: String, val beneficiario: String?, val pagador: String?,
                                val valor: BigDecimal?, val vencimento: String?, val situacaoBanco: String?)

    private class Boleto(val tipo: TipoDocumentoLote, val docEntry: Int, val parcela: Int?, val documento: String?,
                         val valor: BigDecimal?, val cancelado: Boolean)

    private class NotaPorNumero(val docEntry: Int, val serie: String?, val filial: String, val cardName: String?, val parcelas: Map<Int, ParcelaNota>)

    private class ParcelaNota(val valor: BigDecimal?, val vencimento: String?, val aberta: Boolean = true)

    // maxRows de cada ida ao sap-odbc nunca passa do teto dele (10.000, senao recusa). 100 numeros x ate 100
    // linhas (NF x parcelas) cabem; se o resultado voltar truncado, o grupo e dividido ate um numero so.
    private fun consultaNumeros(numeros: List<Int>): List<Map<String, Any?>> = try {
        odbc.consultar(sqlPorNumero, mapOf("numeros" to numeros), MAX_LINHAS_ODBC).rows
    } catch (e: OdbcException) {
        if (e.codigo != "resultado_truncado") throw e
        if (numeros.size == 1) throw OdbcException("resultado_truncado", "A NF ${numeros.single()} tem linhas demais para consultar.")
        val meio = numeros.size / 2
        consultaNumeros(numeros.subList(0, meio)) + consultaNumeros(numeros.subList(meio, numeros.size))
    }

    private val sqlPorNumero by lazy { SqlResource.carregar("odbc/nota-fiscal-lote-por-numero.sql") }

    /**
     * Modo NUMERO_NOTA: a chave e o numero da NF + parcela ("Seu Numero" / "Nro beneficiario"); o
     * nosso numero e ignorado. Procura entre TODAS as NF elegiveis com o numero, sem olhar o filtro da
     * tela (a NF achada entra no lote mesmo fora do filtro). Uma consulta por grupo de 500 numeros
     * (Serial IN (...)); o resto e feito em memoria. Mais de uma NF/parcela que bate = AMBIGUO.
     */
    fun importarPorNumeroNota(arquivo: ByteArray): ImportacaoLoteResultado {
        val linhas = linhasDoArquivo(arquivo, ModoImportacaoLote.NUMERO_NOTA)
        val chaves = linhas.map { chaveNota(it.beneficiario) }
        val numeros = chaves.mapNotNull { it?.first }.distinct()
        val candidatas = numeros.chunked(NUMEROS_POR_CONSULTA).flatMap(::consultaNumeros).groupBy { it["DocEntry"]?.toString()?.toBigDecimalOrNull()?.toInt() }
            .mapNotNull { (docEntry, rows) ->
                docEntry ?: return@mapNotNull null
                val r = rows.first()
                (r["Numero"]?.toString()?.toBigDecimalOrNull()?.toInt() ?: return@mapNotNull null) to NotaPorNumero(
                    docEntry, r["Serie"]?.toString()?.trim(),
                    "${r["BPLId"]} ${r["BPLName"] ?: ""}".trim(), r["CardName"]?.toString(),
                    rows.mapNotNull { p ->
                        p["Parcela"]?.toString()?.toBigDecimalOrNull()?.toInt()?.let {
                            it to ParcelaNota(p["ValorParcela"]?.toString()?.toBigDecimalOrNull(), p["VencimentoParcela"]?.toString()?.trim(),
                                aberta = ((p["ValorParcela"]?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO) -
                                    (p["PagoParcela"]?.toString()?.toBigDecimalOrNull() ?: BigDecimal.ZERO)) > LIMIAR_EM_ABERTO)
                        }
                    }.toMap())
            }.groupBy({ it.first }, { it.second })

        val resultado = linhas.mapIndexed { i, l ->
            val chave = chaves[i] ?: return@mapIndexed l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO,
                mensagem = if (INICIA_COM_NUMERO.containsMatchIn(l.beneficiario.orEmpty())) "linha ${l.linha}: parcela ilegivel"
                    else "linha ${l.linha}: numero da nota ilegivel")
            val (numero, parcela) = chave
            val todas = candidatas[numero].orEmpty()
            if (criterio == CriterioImportacaoNumeroNota.NUMERO_NOME) return@mapIndexed porNumeroNome(l, numero, parcela, todas)
            val comParcela = if (parcela == null) listOf() else todas.filter { parcela in it.parcelas }
            val pares = todas.flatMap { n -> n.parcelas.filter { (k, _) -> parcela == null || k == parcela }.map { n to it.key } }
            // 1) valor (+-tolerancia) e vencimento batem.
            val batem = pares.filter { (n, k) -> bate(l, n.parcelas.getValue(k)) }
            // 2) sem bater, o vencimento igual escolhe a parcela (o banco divide o valor igualmente, o SAP joga o arredondamento na ultima).
            val mesmoVenc = if (batem.isNotEmpty() || l.vencimento == null) listOf()
                else pares.filter { (n, k) -> n.parcelas.getValue(k).vencimento == l.vencimento }
            when {
                todas.isEmpty() -> l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO,
                    mensagem = "NF $numero nao encontrada (ou sem NF-e transmitida / cancelada)")
                batem.size == 1 -> l.resultado(SituacaoLinhaImportacao.ENCONTRADO, nota = batem.single().first, parcela = batem.single().second)
                // 3) varias: o nome do pagador so desempata (nunca rejeita nem escolhe sozinho).
                batem.size > 1 -> desempataPorNome(l, batem)?.let { (n, k) ->
                    l.resultado(SituacaoLinhaImportacao.ENCONTRADO, nota = n, parcela = k, mensagem = MSG_NOME)
                } ?: l.resultado(SituacaoLinhaImportacao.AMBIGUO,
                    mensagem = "NF $numero: mais de uma bate com valor e vencimento (${descricao(batem.map { it.first }.distinct())})")
                mesmoVenc.size == 1 -> mesmoVenc.single().let { (n, k) ->
                    l.resultado(SituacaoLinhaImportacao.DIVERGENTE, nota = n, parcela = k,
                        mensagem = (divergenciasNota(l, n.parcelas.getValue(k)) + listOfNotNull(MSG_VENCIMENTO.takeIf { pares.size > 1 })).joinToString("; "))
                }
                mesmoVenc.size > 1 -> desempataPorNome(l, mesmoVenc)?.let { (n, k) ->
                    l.resultado(SituacaoLinhaImportacao.DIVERGENTE, nota = n, parcela = k,
                        mensagem = (divergenciasNota(l, n.parcelas.getValue(k)) + MSG_VENCIMENTO + MSG_NOME).joinToString("; "))
                } ?: l.resultado(SituacaoLinhaImportacao.AMBIGUO,
                    mensagem = "NF $numero: mais de uma nota/parcela tem o vencimento da planilha e nenhuma bate no valor (${descricao(mesmoVenc.map { it.first }.distinct())})")
                parcela == null -> semParcela(l, numero, todas)
                comParcela.size > 1 -> desempata(l, numero, parcela, comParcela)
                comParcela.isEmpty() -> l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO,
                    mensagem = "NF $numero nao tem a parcela $parcela" + if (todas.size > 1) " (${descricao(todas)})" else "")
                else -> {
                    val nota = comParcela.single()
                    val avisos = divergenciasNota(l, nota.parcelas.getValue(parcela))
                    if (avisos.isEmpty()) l.resultado(SituacaoLinhaImportacao.ENCONTRADO, nota = nota, parcela = parcela)
                    else l.resultado(SituacaoLinhaImportacao.DIVERGENTE, nota = nota, parcela = parcela, mensagem = avisos.joinToString("; "))
                }
            }
        }
        val docEntries = resultado.filter { it.situacao == SituacaoLinhaImportacao.ENCONTRADO || it.situacao == SituacaoLinhaImportacao.DIVERGENTE }
            .mapNotNull { it.docEntry }.distinct()
        val documentos = lote.buscaDocumentos(docEntries, listOf()).distinctBy { it.chave }
        val porChave = documentos.associateBy { it.chave }
        val ordem = docEntries.map { ChaveDocumentoLote(TipoDocumentoLote.NF, it) }
        return ImportacaoLoteResultado(ordem.mapNotNull { porChave[it] }, resultado)
    }

    // Mais de uma nota com o numero e a parcela: so valor e vencimento desempatam (nunca o nome do cliente).
    private fun desempata(l: LinhaPlanilha, numero: Int, parcela: Int, notas: List<NotaPorNumero>): LinhaImportacaoLote {
        val base = "${notas.size} notas com a NF $numero parcela $parcela (${descricao(notas)})"
        if (l.valor == null || l.vencimento == null)
            return l.resultado(SituacaoLinhaImportacao.AMBIGUO,
                mensagem = "$base; ${listOfNotNull(if (l.valor == null) "valor" else null, if (l.vencimento == null) "vencimento" else null)
                    .joinToString(" e ")} da planilha ilegivel: nao deu para desempatar")
        val batem = notas.filter { n -> n.parcelas.getValue(parcela).let { it.valor != null && valorConfere(l.valor, it.valor) && it.vencimento == l.vencimento } }
        return when (batem.size) {
            1 -> l.resultado(SituacaoLinhaImportacao.ENCONTRADO, nota = batem.single(), parcela = parcela)
            0 -> l.resultado(SituacaoLinhaImportacao.AMBIGUO, mensagem = "$base; nenhuma bate com valor e vencimento")
            else -> l.resultado(SituacaoLinhaImportacao.AMBIGUO, mensagem = "$base; mais de uma bate com valor e vencimento (${descricao(batem)})")
        }
    }

    // Criterio NUMERO_NOME: numero da NF (+ parcela, se vier) e nome do parceiro parecido com o pagador.
    // Valor e vencimento nao sao criterio: se diferem da parcela, so entram na mensagem informativa.
    private fun porNumeroNome(l: LinhaPlanilha, numero: Int, parcela: Int?, todas: List<NotaPorNumero>): LinhaImportacaoLote {
        if (todas.isEmpty())
            return l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "NF $numero nao encontrada (ou sem NF-e transmitida / cancelada)")
        val candidatas = if (parcela == null) todas else todas.filter { parcela in it.parcelas }
        if (candidatas.isEmpty())
            return l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "NF $numero nao tem a parcela $parcela (${descricao(todas)})")
        if (PlanilhaXlsx.ilegivel(l.pagador))
            return l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "linha ${l.linha}: nome do parceiro ilegivel")
        if (l.pagador.isNullOrBlank())
            return l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "NF $numero: nome do pagador ausente na planilha")
        val parecidas = candidatas.filter { nomesParecidos(l.pagador, it.cardName) }
        return when {
            parecidas.isEmpty() -> l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO,
                mensagem = "NF $numero existe, mas o nome do parceiro nao confere (${descricao(candidatas)})")
            parecidas.size > 1 -> l.resultado(SituacaoLinhaImportacao.AMBIGUO,
                mensagem = "NF $numero: mais de uma nota com nome parecido (${descricao(parecidas)})")
            else -> {
                val nota = parecidas.single()
                // Parcela so a que o usuario informou; sem ela, nula = a NF inteira (todas as em aberto).
                if (parcela != null && !nota.parcelas.getValue(parcela).aberta)
                    return l.resultado(SituacaoLinhaImportacao.PAGA, nota = nota, parcela = parcela, mensagem = "NF $numero parcela $parcela ja paga")
                if (parcela == null && nota.parcelas.none { it.value.aberta })
                    return l.resultado(SituacaoLinhaImportacao.PAGA, nota = nota, parcela = null, mensagem = "NF $numero sem parcela em aberto")
                val avisos = if (parcela == null) listOf("parcela nao definida: valem todas as parcelas em aberto")
                    else divergenciasNota(l, nota.parcelas.getValue(parcela))
                l.resultado(SituacaoLinhaImportacao.ENCONTRADO, nota = nota, parcela = parcela,
                    // Casamento limpo (parcela definida e sem diferenca): sem mensagem; so aparece com diferenca ou "parcela nao definida".
                    mensagem = if (parcela != null && avisos.isEmpty()) null else (listOf("conferido por numero e nome") + avisos).joinToString("; "))
            }
        }
    }

    // Exatamente uma das candidatas com nome de cliente parecido com o pagador da planilha; senao nulo.
    private fun desempataPorNome(l: LinhaPlanilha, candidatas: List<Pair<NotaPorNumero, Int>>): Pair<NotaPorNumero, Int>? =
        candidatas.filter { (n, _) -> nomesParecidos(l.pagador, n.cardName) }.distinct().singleOrNull()

    private fun bate(l: LinhaPlanilha, p: ParcelaNota) =
        l.valor != null && l.vencimento != null && p.valor != null && valorConfere(l.valor, p.valor) && p.vencimento == l.vencimento

    // "Seu Numero" sem parcela: a parcela e a que bate com valor e vencimento (ja tentado acima). Sem
    // nenhuma que bata, uma nota com uma so parcela e comparada (diz o que difere); senao lista as parcelas.
    private fun semParcela(l: LinhaPlanilha, numero: Int, dentro: List<NotaPorNumero>): LinhaImportacaoLote {
        val unica = dentro.singleOrNull()
        if (unica != null && unica.parcelas.size == 1) {
            val (parcela, dados) = unica.parcelas.entries.single()
            return l.resultado(SituacaoLinhaImportacao.DIVERGENTE, nota = unica, parcela = parcela,
                mensagem = divergenciasNota(l, dados).joinToString("; "))
        }
        val motivo = if (l.valor == null || l.vencimento == null) "valor ou vencimento da planilha ilegivel: nao deu para escolher a parcela"
            else "nenhuma parcela bate com valor e vencimento da planilha"
        val parcelas = dentro.joinToString(" | ") { n ->
            "${descricao(listOf(n))}: " + n.parcelas.toSortedMap().entries.joinToString(", ") { (k, v) ->
                "parcela $k (${v.valor} em ${v.vencimento?.let(::br) ?: "?"})" }
        }
        return l.resultado(SituacaoLinhaImportacao.AMBIGUO,
            mensagem = "NF $numero" + (if (unica == null) " em mais de uma nota" else "") + ": $motivo; $parcelas")
    }

    private fun divergenciasNota(l: LinhaPlanilha, p: ParcelaNota): List<String> {
        val avisos = mutableListOf<String>()
        if (l.valor == null || p.valor == null) avisos.add("valor ilegivel: nao conferido")
        else if (!valorConfere(l.valor, p.valor)) avisos.add("valor na planilha ${l.valor}, na parcela ${p.valor}")
        if (l.vencimento == null || p.vencimento == null) avisos.add("vencimento ilegivel: nao conferido")
        else if (l.vencimento != p.vencimento) avisos.add("vencimento na planilha ${br(l.vencimento)}, na parcela ${br(p.vencimento)}")
        return avisos
    }

    private fun br(iso: String) = iso.split('-').reversed().joinToString("/")

    private fun descricao(notas: List<NotaPorNumero>) =
        notas.joinToString("; ") { "filial ${it.filial}${it.serie?.let { s -> " serie $s" } ?: ""}" }

    private fun LinhaPlanilha.resultado(situacao: SituacaoLinhaImportacao, nota: NotaPorNumero, parcela: Int?, mensagem: String? = null) =
        LinhaImportacaoLote(linha, nossoNumero, beneficiario, pagador, valor, vencimento, situacaoBanco,
            TipoDocumentoLote.NF, nota.docEntry, parcela, situacao, mensagem)

    private fun linhasDoArquivo(arquivo: ByteArray, modo: ModoImportacaoLote): List<LinhaPlanilha> {
        if (arquivo.isEmpty())
            throw ImportacaoInvalidaException("Arquivo vazio.")
        val linhas = linhasDaPlanilha(arquivo, modo)
        if (linhas.isEmpty())
            throw ImportacaoInvalidaException(if (modo == ModoImportacaoLote.NUMERO_NOTA)
                "Nao achei titulos na planilha: falta a coluna \"Seu numero\" (ou \"Nro beneficiario\") ou ela esta vazia."
            else "Nao achei titulos na planilha: falta a coluna \"Nosso numero\" ou ela esta vazia.")
        return linhas
    }

    fun importar(arquivo: ByteArray): ImportacaoLoteResultado {
        val linhas = linhasDoArquivo(arquivo, ModoImportacaoLote.NOSSO_NUMERO)

        val boletos = boletos(linhas.map { it.nossoNumero }.distinct())
        val achados = linhas.mapNotNull { boletos[it.nossoNumero] }
        val nf = achados.filter { it.tipo == TipoDocumentoLote.NF }.map { it.docEntry }.distinct()
        val ad = achados.filter { it.tipo == TipoDocumentoLote.AD }.map { it.docEntry }.distinct()
        val documentos = if (nf.isEmpty() && ad.isEmpty()) listOf()
            else lote.buscaDocumentos(nf, ad).distinctBy { it.chave }
        val naTela = documentos.map { it.chave }.toSet()

        val resultado = linhas.map { l ->
            val b = boletos[l.nossoNumero]
            when {
                b == null -> l.resultado(SituacaoLinhaImportacao.NAO_ENCONTRADO, mensagem = "nosso numero nao encontrado na BankPlus")
                ChaveDocumentoLote(b.tipo, b.docEntry) !in naTela -> l.resultado(SituacaoLinhaImportacao.FORA_DA_TELA, b,
                    "${if (b.tipo == TipoDocumentoLote.AD) "adiantamento" else "NF"} DocEntry ${b.docEntry} nao aparece na tela " +
                        "(sem NF-e transmitida, cancelamento ou adiantamento que nao e de venda futura)")
                else -> divergencias(l, b).let { avisos ->
                    if (avisos.isEmpty()) l.resultado(SituacaoLinhaImportacao.ENCONTRADO, b)
                    else l.resultado(SituacaoLinhaImportacao.DIVERGENTE, b, avisos.joinToString("; "))
                }
            }
        }
        // Na ordem da planilha.
        val ordem = resultado.mapNotNull { r -> r.tipo?.let { ChaveDocumentoLote(it, r.docEntry!!) } }.distinct()
        val porChave = documentos.associateBy { it.chave }
        return ImportacaoLoteResultado(ordem.mapNotNull { porChave[it] }, resultado)
    }

    private fun LinhaPlanilha.resultado(situacao: SituacaoLinhaImportacao, b: Boleto? = null, mensagem: String? = null) =
        LinhaImportacaoLote(linha, nossoNumero, beneficiario, pagador, valor, vencimento, situacaoBanco,
            b?.tipo, b?.docEntry, b?.parcela, situacao, mensagem)

    private fun divergencias(l: LinhaPlanilha, b: Boleto): List<String> {
        val avisos = mutableListOf<String>()
        if (b.cancelado)
            avisos.add("boleto cancelado na BankPlus")
        val partes = l.beneficiario?.trim()?.split(Regex("\\s+"))?.mapNotNull { it.toIntOrNull() }.orEmpty()
        // O que nao deu para ler nao foi conferido: nao pode sair como ENCONTRADO. NF precisa de
        // numero e parcela; adiantamento, do DocEntry.
        val esperadas = if (b.tipo == TipoDocumentoLote.NF) 2 else 1
        if (partes.size < esperadas)
            avisos.add("linha ${l.linha}: Nro beneficiario ilegivel: nao conferido")
        if (l.valor == null)
            avisos.add("valor da planilha ilegivel: nao conferido")
        when (b.tipo) {
            TipoDocumentoLote.NF -> {
                val numero = partes.getOrNull(0)
                val parcela = partes.getOrNull(1)
                if (numero != null && b.documento?.toIntOrNull() != null && numero != b.documento.toInt())
                    avisos.add("Nro beneficiario diz NF $numero, a BankPlus diz NF ${b.documento}")
                if (parcela != null && b.parcela != null && parcela != b.parcela)
                    avisos.add("Nro beneficiario diz parcela $parcela, a BankPlus diz parcela ${b.parcela}")
            }
            TipoDocumentoLote.AD -> {
                val docEntry = partes.getOrNull(0)
                if (docEntry != null && docEntry != b.docEntry)
                    avisos.add("Nro beneficiario diz adiantamento $docEntry, a BankPlus diz ${b.docEntry}")
            }
        }
        if (l.valor != null && b.valor != null && !valorConfere(l.valor, b.valor))
            avisos.add("valor na planilha ${l.valor}, na BankPlus ${b.valor}")
        return avisos
    }

    // Cabecalho pode se repetir (uma secao por carteira): cada um refaz o mapa de colunas.
    // O limite de titulos vale durante a leitura: o csv para no primeiro titulo acima dele, sem carregar o resto.
    private fun linhasDaPlanilha(arquivo: ByteArray, modo: ModoImportacaoLote): List<LinhaPlanilha> {
        val chaveNumeroNota = modo == ModoImportacaoLote.NUMERO_NOTA
        var colunas: Map<String, Int>? = null
        var primeiroCabecalho: Map<Int, String>? = null
        val linhas = mutableListOf<LinhaPlanilha>()
        fun registro(numero: Int, celulas: Map<Int, String>) {
            val nomes = celulas.mapValues { normaliza(it.value) }
            // Numero da nota: o primeiro cabecalho basta ter a chave; depois dele, so troca o mapa a linha com a
            // chave E outro titulo conhecido, ou a repeticao exata do primeiro ("NF" sozinho no meio e dado).
            val cabecalho = if (chaveNumeroNota) nomes.values.any { it in CHAVES_NUMERO_NOTA } &&
                    (colunas == null || nomes == primeiroCabecalho || nomes.values.count { it in TITULOS_CONHECIDOS } >= 2)
                else nomes.values.any { it == "nosso numero" }
            if (cabecalho) {
                if (primeiroCabecalho == null) primeiroCabecalho = nomes
                // Sinonimos de outros bancos (Sicoob): o primeiro de cada coluna manda.
                colunas = nomes.entries.reversed().associate { (coluna, nome) -> (SINONIMOS[nome] ?: nome) to coluna }
                return
            }
            val c = colunas ?: return
            val nosso = if (chaveNumeroNota) {
                if (celulas[c["nro beneficiario"]].isNullOrBlank()) return
                celulas[c["nosso numero"]].orEmpty()
            } else celulas[c["nosso numero"]]?.let(::nossoNumero) ?: return
            if (linhas.size >= maximoLinhas)
                throw ImportacaoInvalidaException("A planilha tem mais de $maximoLinhas titulos; o limite e $maximoLinhas.")
            linhas.add(LinhaPlanilha(
                linha = numero,
                nossoNumero = nosso,
                beneficiario = juntaNumeroParcela(celulas[c["nro beneficiario"]], celulas[c["parcela"]]),
                pagador = celulas[c["nome pagador"]],
                valor = celulas[c["valor"]]?.let(::valor),
                vencimento = celulas[c["vencimento"]]?.let(::data),
                situacaoBanco = celulas[c["situacao"]],
            ))
        }
        if (arquivo.size >= 2 && arquivo[0] == 'P'.code.toByte() && arquivo[1] == 'K'.code.toByte())
            PlanilhaXlsx.ler(arquivo).forEach { (n, c) -> registro(n, c) }
        else PlanilhaCsv.percorrer(arquivo, ::registro)
        return linhas
    }

    private fun boletos(nossosNumeros: List<String>): Map<String, Boleto> =
        nossosNumeros.chunked(500).flatMap { parte ->
            odbc.consultar(sqlBoletos, mapOf("nossosNumeros" to parte), parte.size * 5).rows
        }.mapNotNull { r ->
            val tipo = when (r["DocType"]?.toString()?.toBigDecimalOrNull()?.toInt()) {
                13 -> TipoDocumentoLote.NF
                203 -> TipoDocumentoLote.AD
                else -> return@mapNotNull null
            }
            val docEntry = r["DocEntry"]?.toString()?.toBigDecimalOrNull()?.toInt() ?: return@mapNotNull null
            r["NossoNumero"].toString() to Boleto(tipo, docEntry, r["Parcela"]?.toString()?.toBigDecimalOrNull()?.toInt(),
                r["Documento"]?.toString(), r["Valor"]?.toString()?.toBigDecimalOrNull(),
                r["Status"]?.toString()?.toBigDecimalOrNull()?.toInt() == STATUS_CANCELADO)
        }.groupBy({ it.first }, { it.second })
            // reemitido: fica o que nao esta cancelado
            .mapValues { (_, lista) -> lista.firstOrNull { !it.cancelado } ?: lista.first() }

    companion object {
        const val MSG_NOME = "desempatada pelo nome do pagador"
        const val MSG_VENCIMENTO = "escolhida pelo vencimento"
        private val SUFIXOS_EMPRESA = setOf("LTDA", "ME", "EPP", "EIRELI", "SA")
        private val ARTIGOS = setOf("DE", "DA", "DO", "DOS", "DAS", "E")

        // Sem acento, maiusculo, so letras/numeros, sem sufixo societario (LTDA, ME, EPP, EIRELI, SA, S A) no fim.
        fun normalizaNome(nome: String?): String {
            val tokens = Normalizer.normalize(nome.orEmpty(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
                .uppercase().replace(Regex("[^A-Z0-9]+"), " ").trim().split(' ').filter { it.isNotEmpty() }.toMutableList()
            while (tokens.isNotEmpty()) {
                when {
                    tokens.last() in SUFIXOS_EMPRESA -> tokens.removeLast()
                    tokens.size >= 2 && tokens.last() == "A" && tokens[tokens.size - 2] == "S" -> { tokens.removeLast(); tokens.removeLast() }
                    else -> break
                }
            }
            return tokens.joinToString(" ")
        }

        // Parecidos = um e prefixo do outro (o banco trunca o nome; minimo de 10 caracteres) ou todos os
        // termos do menor (sem DE/DA/DO/DOS/DAS/E) aparecem no maior, na mesma ordem.
        fun nomesParecidos(a: String?, b: String?): Boolean {
            val na = normalizaNome(a); val nb = normalizaNome(b)
            if (na.isEmpty() || nb.isEmpty()) return false
            // "#REF!" e afins nao sao nome: nunca viram "REF" para casar.
            if (PlanilhaXlsx.ilegivel(a) || PlanilhaXlsx.ilegivel(b)) return false
            if (na == nb) return true
            val (curto, longo) = if (na.length <= nb.length) na to nb else nb to na
            // Todo casamento parcial (prefixo ou subconjunto) exige 2+ termos significativos no menor: "COOPERATIVA" ou "AGRO" sozinhos sao fracos.
            val termos = curto.split(' ').filter { it !in ARTIGOS && it !in SUFIXOS_EMPRESA && it.length >= 3 }
            if (termos.size < 2) return false
            if (curto.length >= 10 && longo.startsWith(curto)) return true
            val resto = longo.split(' ').iterator()
            return termos.all { t -> resto.asSequence().any { it == t } }
        }

        const val NUMEROS_POR_CONSULTA = 100
        const val MAX_LINHAS_ODBC = 10_000

        // Diferenca de valor aceita entre a planilha e o SAP/BankPlus (R$, inclusive, em modulo).
        val TOLERANCIA_VALOR = BigDecimal("0.05")

        // Saldo (InsTotal - PaidToDate) acima disto = parcela em aberto; mesma conta do SQL do termo (nota-fiscal-lote-termo-cessao.sql).
        val LIMIAR_EM_ABERTO = BigDecimal("0.005")

        fun valorConfere(planilha: BigDecimal, sap: BigDecimal): Boolean = (planilha - sap).abs() <= TOLERANCIA_VALOR

        private val SINONIMOS = mapOf(
            "seu numero" to "nro beneficiario", "sacado" to "nome pagador", "valor (r$)" to "valor",
            // Modelo "numero + nome": Numero da nota | Parcela | Nome do parceiro | Valor | Vencimento
            "numero da nota" to "nro beneficiario", "nota" to "nro beneficiario", "nf" to "nro beneficiario", "numero nf" to "nro beneficiario",
            "nome do parceiro" to "nome pagador", "parceiro" to "nome pagador", "cliente" to "nome pagador",
        )
        // Cabecalhos que marcam a coluna da chave (numero da nota) no modo NUMERO_NOTA.
        private val CHAVES_NUMERO_NOTA = setOf("seu numero", "nro beneficiario", "numero da nota", "nota", "nf", "numero nf")

        private val TITULOS_CONHECIDOS = CHAVES_NUMERO_NOTA + SINONIMOS.keys +
            setOf("parcela", "nome pagador", "valor", "vencimento", "situacao", "nosso numero")

        // Numero inteiro digitado como "818.0" no Excel vira "818"; a coluna Parcela separada entra como "N P".
        private fun inteiroTexto(texto: String) = texto.trim().replace(Regex("^(\\d+)[.,]0+$"), "$1")

        fun juntaNumeroParcela(numero: String?, parcela: String?): String? {
            val n = numero?.let(::inteiroTexto)?.takeIf { it.isNotEmpty() } ?: return numero
            val p = parcela?.let(::inteiroTexto)?.takeIf { it.isNotEmpty() }
            // Parcela preenchida que nao e numero ("abc", "-"): marca como ilegivel para a linha nao virar "NF inteira".
            return if (p == null) n else if (p.all { it in '0'..'9' }) "$n $p" else "$n ?"
        }

        // Xlsx e um zip (comeca com "PK"); o resto e lido como csv.
        fun lerArquivo(arquivo: ByteArray): Map<Int, Map<Int, String>> =
            if (arquivo.size >= 2 && arquivo[0] == 'P'.code.toByte() && arquivo[1] == 'K'.code.toByte()) PlanilhaXlsx.ler(arquivo)
            else PlanilhaCsv.ler(arquivo)

        // "3956 1" / "0005647-1" / "0000830-02" / "0006515-002-02" -> (numero da NF, parcela); sem
        // parcela, parcela nula (a importacao escolhe por valor e vencimento). A forma
        // "numero-parcela-parcela" (Sicoob) so vale com os dois ultimos iguais.
        // So letras/espaco/ponto antes do numero ("NF 818", "Nº 818"); "#VALUE!" e outros simbolos nao viram numero.
        private const val PREFIXO_NOTA = "(?:(?i:nf-e)|[\\p{L}\\s.\u00BA\u00AA\u00B0])*"
        private val INICIA_COM_NUMERO = Regex("^$PREFIXO_NOTA\\d+")
        private val REFERENCIA_NOTA = Regex("^$PREFIXO_NOTA(\\d+)(?:[\\s/.-]+(\\d+))?(?:[\\s/.-]+(\\d+))?\\s*$")

        fun chaveNota(texto: String?): Pair<Int, Int?>? {
            // Referencia completa: numero, e opcionalmente separador+parcela (+ a mesma parcela repetida). Separador
            // pendurado ("818-"), letra ou simbolo depois do numero = ilegivel (nunca vira "NF inteira").
            val m = REFERENCIA_NOTA.matchEntire(texto.orEmpty()) ?: return null
            val partes = m.groupValues.drop(1).filter { it.isNotEmpty() }.map { it.toBigInteger() }
            if (partes.size == 3 && partes[1] != partes[2]) return null
            val numero = partes[0].takeIf { it.bitLength() < 31 }?.toInt()?.takeIf { it > 0 } ?: return null
            val parcela = if (partes.size == 1) null else partes[1].takeIf { it.bitLength() < 31 }?.toInt()?.takeIf { it > 0 } ?: return null
            return numero to parcela
        }

        // Status da BankPlus que os relatorios ja tratam como cancelado (ClienteXBoleto.sql: Status <> 5).
        const val STATUS_CANCELADO = 5

        // "35158540000021002-5" -> "35158540000021002": a BankPlus grava sem o digito verificador.
        fun nossoNumero(texto: String): String? =
            texto.substringBefore('-').filter { it.isDigit() }.takeIf { it.length >= 6 }

        // Celula de numero vem "16088.38"; texto digitado pode vir "16.088,38".
        fun valor(texto: String): BigDecimal? {
            val t = texto.replace("R$", "").trim()
            return (if (t.contains(',')) t.replace(".", "").replace(',', '.') else t).toBigDecimalOrNull()
        }

        // Celula de data vem como serial do Excel; texto, como dd/MM/yyyy.
        private val FORMATO_DATA = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(java.time.format.ResolverStyle.STRICT)

        // STRICT: 31/02/2026 nao existe e vira ilegivel (nao 03/03).
        // Serial do Excel so dentro do intervalo valido (1 = 1900-01-01 ... 2958465 = 9999-12-31); NaN/Infinity/1e20/negativo = ilegivel.
        fun data(texto: String): String? {
            val n = texto.toDoubleOrNull()
            if (n != null)
                return if (n.isFinite() && n >= 1.0 && n <= 2958465.0) PlanilhaXlsx.dataDoExcel(n).toString() else null
            return runCatching { java.time.LocalDate.parse(texto, FORMATO_DATA).toString() }.getOrNull()
        }

        fun normaliza(texto: String): String =
            Normalizer.normalize(texto, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase().replace(Regex("\\s+"), " ").trim()
    }
}
