package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.odata.Parameter
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.cobranca.CobrancaAdiantamentoSap
import br.andrew.sap.model.cobranca.CobrancaRegistro
import br.andrew.sap.model.cobranca.CobrancaTitulo
import br.andrew.sap.model.cobranca.CobrancaTituloSap
import br.andrew.sap.services.abstracts.SqlQueriesService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class CobrancaConsultaService(val sqlQueriesService: SqlQueriesService) {

    private val formatoSap = DateTimeFormatter.BASIC_ISO_DATE // yyyyMMdd, igual ao que o SAP devolve em DueDate

    companion object {
        // Mesmo sentinela que o resto do projeto usa pra "filtro de texto desligado" (ver
        // clienteIsFilter aqui e em contratos-vendafutura.sql): '~' e alto o suficiente na
        // ordenacao pra que "coluna < '~'" seja verdadeiro pra qualquer valor real.
        private const val SEM_FILTRO = "~"
    }

    // INV6/DPI6."Status" e 'O' (aberta) ou 'C' (fechada) - e a mesma fonte que alimenta o
    // SituacaoSap em CobrancaTituloSap.toDto, por isso o de-para fica aqui e nao no controller.
    private fun statusParcelaDe(situacaoSap: String?): String = when (situacaoSap) {
        "ABERTO" -> "O"
        "PAGO" -> "C"
        else -> SEM_FILTRO
    }

    fun listar(
        auth: User,
        filial: Int? = null,
        vendedor: Int? = null,
        cliente: String? = null,
        data: LocalDate = LocalDate.now(),
        diasAtrasoMin: Int? = null,
        status: String? = null,
        cobrador: String? = null,
        situacao: String? = null,
        situacaoSap: String? = null,
        vencimentoDe: LocalDate? = null,
        vencimentoAte: LocalDate? = null,
        // Vem do drill-down do dashboard: "sem nenhuma acao" = nunca teve registro em
        // @COB_TITULO; promessaVencidaAte = tinha data prometida ate essa data e nao pagou.
        semAcompanhamento: Boolean? = null,
        promessaVencidaAte: LocalDate? = null,
        tipo: String? = null, // "NF", "AD" ou null (todos)
        pagina: Int = 0,
        tamanhoPagina: Int = 20,
    ): List<CobrancaTitulo> {
        val vendedorEfetivo = if (auth.superVendedor() == Int.MAX_VALUE) vendedor else auth.getIdInt()

        // Todo filtro que da pra resolver no SQL TEM que ser resolvido no SQL. O laco de
        // paginacao abaixo so para quando junta linhas aprovadas suficientes, e o que sobra
        // pro filtro local (passaNosFiltrosLocais) e descartado DEPOIS de ja ter vindo do SAP -
        // ou seja, cada linha descartada localmente custa uma ida e volta HTTP a mais. Filtro
        // seletivo resolvido so localmente fazia o backend varrer a inadimplencia inteira de 20
        // em 20 pra montar uma pagina.
        // DiasAtraso = hoje - DueDate, entao "atraso >= N" e o mesmo que "DueDate <= hoje - N":
        // aproveita o :data que ja existe na view em vez de criar parametro novo.
        val dataEfetiva = data.minusDays((diasAtrasoMin ?: 0).coerceAtLeast(0).toLong())
        val statusParcela = statusParcelaDe(situacaoSap)

        val parametros = listOf(
            Parameter("data", dataEfetiva.toString()),
            Parameter("filial", filial ?: Int.MAX_VALUE),
            Parameter("filialIsFilter", if (filial == null) Int.MAX_VALUE else -1),
            Parameter("vendedor", vendedorEfetivo ?: Int.MAX_VALUE),
            Parameter("vendedorIsFilter", if (vendedorEfetivo == null) Int.MAX_VALUE else -1),
            Parameter("cliente", cliente ?: SEM_FILTRO),
            Parameter("clienteIsFilter", if (cliente == null) SEM_FILTRO else ""),
            Parameter("statusParcela", statusParcela),
            Parameter("statusParcelaIsFilter", if (statusParcela == SEM_FILTRO) SEM_FILTRO else ""),
            // Data nao usa o idioma x/xIsFilter (feito pra igualdade) - com sentinela larga a
            // comparacao de intervalo ja fica sempre verdadeira quando o filtro esta desligado.
            Parameter("vencimentoDe", vencimentoDe?.toString() ?: "1900-01-01"),
            Parameter("vencimentoAte", vencimentoAte?.toString() ?: "9999-12-31"),
            // Status/Cobrador/Situacao vem do LEFT JOIN da UDT, entao sao NULOS pra titulo que
            // nunca foi acompanhado - e o idioma normal (coluna < :xIsFilter) nao serve pra
            // desligar o filtro num campo nulo, porque NULL < '~' e desconhecido e a linha
            // sumiria. Por isso o "escape" quando o filtro esta desligado e testado numa coluna
            // que NUNCA e nula (o DocEntry do proprio documento): DocEntry < Int.MAX_VALUE e
            // verdadeiro pra toda linha, e a condicao inteira passa junto com os nulos. Com o
            // filtro ligado vira DocEntry < -1 (falso), sobrando so a igualdade - e nulo nao e
            // igual a nada, o que exclui os nao-acompanhados, que e exatamente o desejado.
            Parameter("status", status ?: SEM_FILTRO),
            Parameter("statusIsFilter", if (status == null) Int.MAX_VALUE else -1),
            Parameter("cobrador", cobrador ?: SEM_FILTRO),
            Parameter("cobradorIsFilter", if (cobrador == null) Int.MAX_VALUE else -1),
            Parameter("situacao", situacao ?: SEM_FILTRO),
            Parameter("situacaoIsFilter", if (situacao == null) Int.MAX_VALUE else -1),
            // Os dois filtros que o drill-down do dashboard usa. Diferente dos de cima, nao tem
            // valor pra comparar - sao presenca/ausencia -, entao quem liga e desliga e so o
            // IsFilter, no mesmo escape em coluna nao-nula.
            Parameter("semAcompanhamentoIsFilter", if (semAcompanhamento == true) -1 else Int.MAX_VALUE),
            // Quando o filtro esta desligado a data nao importa (o IsFilter larga a condicao
            // inteira), mas o parametro tem que existir - a view sempre pede os dois.
            Parameter("promessaVencidaAte", (promessaVencidaAte ?: data).toString()),
            Parameter("promessaVencidaIsFilter", if (promessaVencidaAte == null) Int.MAX_VALUE else -1),
        )

        // Adiantamentos (ODPI/DPI6): volume normalmente bem menor que o de faturas, entao
        // busca tudo que casa com o filtro de uma vez. Se isso um dia crescer a ponto de
        // ficar lento, aplicar aqui o mesmo "buscar so o suficiente" usado para as faturas.
        val adiantamentos = if (tipo == CobrancaRegistro.TIPO_NOTA_FISCAL) emptyList() else
            sqlQueriesService.getAll<CobrancaAdiantamentoSap>("cobranca-titulos-adiantamento.sql", parametros)
                .map { it.toDto() }
                .filter { passaNosFiltrosLocais(it, diasAtrasoMin, status, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte) }

        // O SAP ja pagina a resposta do SQLQueries sozinho (nextLink). Sem filial/vendedor/
        // cliente escolhidos isso e a empresa inteira - buscar tudo de uma vez (getAll) fazia
        // dezenas de idas e voltas pro SAP remoto antes de responder, e a tela travava.
        // Em vez disso, so busca paginas novas do SAP ate acumular linhas suficientes (depois
        // dos filtros locais) pra cobrir a pagina pedida. Isso sempre basta mesmo somando os
        // adiantamentos depois: as N faturas mais antigas buscadas aqui + TODOS os
        // adiantamentos, ordenados e cortados na pagina certa, nunca deixam faltar fatura -
        // no pior caso alguma sobra pra pagina seguinte, que e recalculada do zero.
        val faturas = mutableListOf<CobrancaTitulo>()
        if (tipo != CobrancaRegistro.TIPO_ADIANTAMENTO) {
            val alvo = (pagina + 1) * tamanhoPagina
            var paginaSap = sqlQueriesService.execute("cobranca-titulos.sql", parametros)

            while (paginaSap != null) {
                faturas.addAll(
                    paginaSap.tryGetValues<CobrancaTituloSap>()
                        .map { it.toDto() }
                        .filter { passaNosFiltrosLocais(it, diasAtrasoMin, status, cobrador, situacao, situacaoSap, vencimentoDe, vencimentoAte) }
                )
                if (faturas.size >= alvo || !paginaSap.hasNext())
                    break
                paginaSap = sqlQueriesService.nextLink(paginaSap.nextLink())
            }
        }

        val combinado = (faturas + adiantamentos).sortedWith(compareBy({ it.DueDate }, { it.DocNum }))
        val inicio = (pagina * tamanhoPagina).coerceAtMost(combinado.size)
        val fim = (inicio + tamanhoPagina).coerceAtMost(combinado.size)
        return combinado.subList(inicio, fim)
    }

    // Todos esses filtros ja sao aplicados no SQL (ver a lista de Parameter acima) - isso aqui
    // e rede de seguranca, nao a regra principal: se a view no SAP estiver defasada em relacao
    // ao .sql do repositorio, o resultado continua correto, so mais lento. Nao adicione filtro
    // NOVO so aqui: filtro que existe apenas em Kotlin custa uma ida e volta ao SAP por linha
    // descartada, porque o laco de paginacao busca pagina nova ate juntar linha aprovada.
    //
    // semAcompanhamento e promessaVencidaAte NAO entram aqui de proposito. "Nunca teve
    // acompanhamento" e a ausencia do registro em @COB_TITULO (C."Code" IS NULL), e o DTO nao
    // carrega o Code - da pra chutar por "todos os U_* nulos", mas titulo que tem registro so
    // com observacao preenchida seria classificado como sem acompanhamento e a rede DESCARTARIA
    // linha que o SQL devolveu certo. Rede de seguranca imprecisa e pior que rede nenhuma.
    private fun passaNosFiltrosLocais(
        titulo: CobrancaTitulo,
        diasAtrasoMin: Int?,
        status: String?,
        cobrador: String?,
        situacao: String?,
        situacaoSap: String?,
        vencimentoDe: LocalDate?,
        vencimentoAte: LocalDate?,
    ): Boolean {
        return (diasAtrasoMin == null || titulo.DiasAtraso >= diasAtrasoMin) &&
            (status == null || titulo.U_Status == status) &&
            (cobrador == null || titulo.U_Cobrador == cobrador) &&
            (situacao == null || titulo.U_Situacao == situacao) &&
            (situacaoSap == null || titulo.SituacaoSap == situacaoSap) &&
            (vencimentoDe == null || titulo.DueDate >= vencimentoDe.format(formatoSap)) &&
            (vencimentoAte == null || titulo.DueDate <= vencimentoAte.format(formatoSap))
    }
}
