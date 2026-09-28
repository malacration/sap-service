package br.andrew.sap.services.cobranca

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class CobrancaTitulosSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/cobranca/cobranca-titulos.sql")
    )

    @Test
    fun `valor com acento ainda estreita a consulta no SAP, pelo prefixo ASCII`() {
        // Sem isto, status="8 - EM NEGOCIACAO" (que soAscii recusa por causa do C-cedilha e do
        // A-til) some do SQL e sobra so o filtro em Kotlin - o laco de buscarAte varre a base
        // de 20 em 20 ate juntar 20 linhas aprovadas, e o drill-down do dashboard trava.
        // O LIKE do prefixo devolve quase o conjunto exato ja do lado do SAP.
        listOf("statusPrefixo", "cobradorPrefixo", "situacaoPrefixo").forEach { parametro ->
            assertTrue(sql.contains("LIKE :$parametro"), "titulos-sql perdeu o filtro $parametro")
            assertTrue(sql.contains(":${parametro}IsFilter"), "$parametro precisa ser opcional")
        }
    }

    @Test
    fun `nao reintroduz a lista fixa de filiais do titulos-sql`() {
        assertFalse(
            sql.contains("BPLId\" in ("),
            "filial deve ser filtro opcional (x = :filial OR x < :filialIsFilter), nao uma lista fixa de BPLId"
        )
    }

    @Test
    fun `mantem visivel o titulo que ja esta em acompanhamento mesmo apos a baixa`() {
        assertTrue(
            sql.contains("OR C.\"Code\" IS NOT NULL"),
            "sem essa clausula a parcela some da lista assim que e paga, perdendo o historico de cobranca"
        )
    }

    @Test
    fun `escopo deliberado - titulo quitado que nunca precisou de cobranca fica de fora da consulta`() {
        // Decisao consciente (nao e bug): manter P."Status" = 'O' como parte do primeiro braco
        // do OR significa que um titulo ja fechado no SAP e que NUNCA teve @COB_TITULO (C.Code
        // IS NULL) nao passa por nenhum dos dois lados do OR - nunca chega no Kotlin. Por isso o
        // filtro "Situacao SAP = Pago/Todos" so mostra titulo que ja foi acompanhado; nao e um
        // extrato geral de notas fiscais quitadas. Se um dia isso precisar mudar, e uma decisao
        // de escopo (custo de volume/performance ao abrir a consulta pra todo o historico do
        // SAP), nao um ajuste trivial de filtro.
        assertTrue(sql.contains("P.\"Status\" = 'O'"))
    }

    @Test
    fun `le o acompanhamento da UDT, nunca dos UDFs do INV6`() {
        assertFalse(sql.contains("P.\"U_StatusCobranca\""))
        assertFalse(sql.contains("P.\"U_AgenteCobrador\""))
        assertTrue(sql.contains("LEFT JOIN \"@COB_TITULO\" C"))
    }

    @Test
    fun `usa o Tipo NF ao juntar com a UDT, pra nao colidir com adiantamento de mesmo DocEntry`() {
        assertTrue(sql.contains("C.\"U_Tipo\" = 'NF'"))
    }

    @Test
    fun `expoe o status oficial da parcela para SituacaoSap nao depender do saldo calculado`() {
        // Saldo (InsTotal - PaidToDate) pode dar negativo por rateio/adiantamento vinculado
        // mesmo com a parcela ainda aberta no SAP - SituacaoSap tem que vir do Status real.
        assertTrue(sql.contains("P.\"Status\" AS \"StatusParcela\""))
    }

    @Test
    fun `filtros opcionais de filial, vendedor e cliente seguem o idioma x-ou-xIsFilter`() {
        assertTrue(sql.contains(":filialIsFilter"))
        assertTrue(sql.contains(":vendedorIsFilter"))
        assertTrue(sql.contains(":clienteIsFilter"))
    }

    @Test
    fun `situacao e intervalo de vencimento sao filtrados no SQL, nao em Kotlin`() {
        // Filtro resolvido so em Kotlin e aplicado DEPOIS de a linha vir do SAP, e o laco de
        // paginacao de CobrancaConsultaService busca pagina nova ate juntar linha aprovada
        // suficiente - filtro seletivo fora do SQL faz o backend varrer a base de 20 em 20.
        assertTrue(sql.contains(":statusParcelaIsFilter"))
        assertTrue(sql.contains(":vencimentoDe"))
        assertTrue(sql.contains(":vencimentoAte"))
    }

    @Test
    fun `mes de lancamento e filtrado no DocDate pelo SQL, sem funcao de data`() {
        // O intervalo do mes e calculado em Kotlin (CobrancaConsultaService) e chega pronto: o
        // parser do SQLQueries nao tem precedente pra YEAR()/MONTH()/TO_VARCHAR nessas views.
        assertTrue(sql.contains("NS.\"DocDate\" >= :lancamentoDe"))
        assertTrue(sql.contains("NS.\"DocDate\" <= :lancamentoAte"))
        assertFalse(sql.contains("MONTH("))
        assertFalse(sql.contains("YEAR("))
    }

    @Test
    fun `filtro de campo da UDT usa coluna nao-nula como escape, nunca a propria coluna nula`() {
        // C."U_Status" vem de LEFT JOIN: e nulo pra titulo nunca acompanhado. Se o "desligado"
        // fosse testado nele (C."U_Status" < :statusIsFilter), NULL < valor seria desconhecido
        // e TODO titulo nunca acompanhado sumiria da tela. O escape tem que ser uma coluna que
        // nunca e nula - aqui o DocEntry da propria fatura.
        assertTrue(sql.contains("C.\"U_Status\"    = :status   OR NS.\"DocEntry\" < :statusIsFilter"))
        assertTrue(sql.contains("C.\"U_Cobrador\"  = :cobrador OR NS.\"DocEntry\" < :cobradorIsFilter"))
        assertTrue(sql.contains("C.\"U_Situacao\"  = :situacao OR NS.\"DocEntry\" < :situacaoIsFilter"))
        assertFalse(
            sql.contains("C.\"U_Status\" < :statusIsFilter"),
            "escape em coluna de LEFT JOIN faria o titulo nunca acompanhado desaparecer"
        )
    }

    @Test
    fun `filtros do drill-down do dashboard tambem escapam em coluna nao-nula`() {
        // "Sem nenhuma acao" e presenca/ausencia de registro na UDT, e promessa vencida le uma
        // coluna do LEFT JOIN - os dois sao nulos pro titulo nunca acompanhado. Se o escape
        // fosse na propria coluna, ligar QUALQUER outro filtro faria esses titulos sumirem.
        assertTrue(sql.contains("C.\"Code\" IS NULL OR NS.\"DocEntry\" < :semAcompanhamentoIsFilter"))
        assertTrue(sql.contains("NS.\"DocEntry\" < :comAcompanhamentoIsFilter"))
        assertTrue(sql.contains("C.\"U_DataPromessa\" <= :promessaVencidaAte OR NS.\"DocEntry\" < :promessaVencidaIsFilter"))
        assertFalse(
            sql.contains("C.\"U_DataPromessa\" < :promessaVencidaIsFilter"),
            "escape em coluna de LEFT JOIN faria o titulo sem promessa desaparecer"
        )
    }

    @Test
    fun `comAcompanhamento exige acao ANTES de algum recebimento no periodo - o mesmo criterio do card, nao so ter historico`() {
        // "Alguma acao, a qualquer momento" deixava passar titulo cobrado SO DEPOIS de ja ter
        // recebido - esse titulo nunca compos o card (que exige @COB_TITULO_L.U_Data <= data do
        // pagamento). Sem essa linha, o drill-down do Recuperado mostrava linha que nao pertence
        // ao card.
        assertTrue(sql.contains("INNER JOIN \"@COB_TITULO\" CX ON CX.\"U_Tipo\" = 'NF' AND CX.\"U_DocEntry\" = PGX.\"DocEntry\" AND CX.\"U_InstlmntID\" = PGX.\"InstId\""))
        assertTrue(sql.contains("INNER JOIN \"@COB_TITULO_L\" HX ON HX.\"Code\" = CX.\"Code\" AND HX.\"U_Data\" <= PRX.\"DocDate\""))
        assertTrue(sql.contains("WHERE PGX.\"DocEntry\" = NS.\"DocEntry\" AND PGX.\"InstId\" = P.\"InstlmntID\" AND PGX.\"InvType\" = 13"))
    }

    @Test
    fun `toggle de a vista compara DocDate com DueDate, desligado por padrao`() {
        // A vista = lancado e vencido no mesmo dia. NS.DocDate/DueDate nunca sao nulos (vem de
        // INNER JOIN), entao o escape podia ser em qualquer um dos dois - segue o padrao do
        // arquivo usando NS."DocEntry".
        assertTrue(sql.contains("NS.\"DocDate\" <> P.\"DueDate\" OR NS.\"DocEntry\" < :ocultarAvistaIsFilter"))
    }

    @Test
    fun `filtro de data de pagamento usa PR DocDate com escape em coluna nao-nula`() {
        // PR vem de LEFT JOIN (pode ser nulo pra titulo sem pagamento) - mesmo motivo de
        // semAcompanhamento/promessaVencidaAte: escapar na propria coluna faria todo titulo
        // sem pagamento sumir quando o filtro estivesse desligado.
        assertTrue(sql.contains("PR.\"DocDate\" >= :dataPagamentoDe OR NS.\"DocEntry\" < :dataPagamentoDeIsFilter"))
        assertTrue(sql.contains("PR.\"DocDate\" <= :dataPagamentoAte OR NS.\"DocEntry\" < :dataPagamentoAteIsFilter"))
    }

    @Test
    fun `traz o telefone do cliente pra tela de cobranca sem poder derrubar linha`() {
        // O join com OCRD e so pra exibir contato - se fosse INNER, titulo cujo parceiro nao
        // casa (codigo migrado, cliente inativado) sairia da lista de cobranca.
        assertTrue(sql.contains("LEFT JOIN OCRD CL ON CL.\"CardCode\" = NS.\"CardCode\""))
        assertTrue(sql.contains("CL.\"Phone1\" AS \"Telefone\""))
        assertTrue(sql.contains("CL.\"Cellular\" AS \"Celular\""))
    }

    @Test
    fun `nao usa funcoes que o parser do SQLQueries do SAP B1 nao reconhece nesse contexto`() {
        // Saldo, DiasAtraso e SituacaoSap sao calculados em Kotlin (CobrancaTituloSap.toDto);
        // IFNULL/DAYS_BETWEEN/CASE WHEN aqui ja quebraram o provisionamento em producao
        // ("mismatched input '.' expecting FROM") por nao terem precedente nas views existentes.
        assertFalse(sql.contains("IFNULL"))
        assertFalse(sql.contains("DAYS_BETWEEN"))
        assertFalse(sql.contains("CASE WHEN"))
        assertFalse(sql.contains("CURRENT_DATE"))
    }

    @Test
    fun `traz data, valor e observacao do ultimo recebimento sem poder derrubar o titulo`() {
        // LEFT JOIN (nao INNER): titulo sem nenhum recebimento tem que continuar na lista,
        // so com essas 3 colunas em branco - mesmo motivo do LEFT JOIN com OCRD.
        assertTrue(sql.contains("LEFT JOIN RCT2 PG"))
        assertTrue(sql.contains("LEFT JOIN ORCT PR"))
        assertTrue(sql.contains("PR.\"DocDate\" AS \"DataPagamento\""))
        assertTrue(sql.contains("PG.\"SumApplied\" AS \"ValorPago\""))
        assertTrue(sql.contains("PR.\"Comments\" AS \"ObservacaoPagamento\""))
    }

    @Test
    fun `liga o recebimento a parcela pelo mesmo join de parcelas-pagas e recuperado`() {
        // RCT2."DocNum" e o DocEntry do ORCT (nao o numero do documento) - o mesmo cuidado
        // documentado em CobrancaDashboardSqlTest para o join do card Recuperado.
        assertTrue(sql.contains("PG.\"DocEntry\" = NS.\"DocEntry\" AND PG.\"InstId\" = P.\"InstlmntID\" AND PG.\"InvType\" = 13"))
        assertTrue(sql.contains("PR.\"DocEntry\" = PG.\"DocNum\""))
    }

    @Test
    fun `recebimento cancelado nao vaza ValorPago nem duplica a parcela`() {
        // Bug reportado: o filtro de cancelado so estava no LEFT JOIN ORCT (PR), nao no LEFT
        // JOIN RCT2 (PG). PG."SumApplied" e selecionado direto de PG, entao um recebimento
        // cancelado (PG preenchido, PR nulo) vazava o valor cancelado como ValorPago; e se a
        // parcela tambem tivesse um recebimento valido, a parcela duplicava (uma linha pro
        // cancelado escapando por "PR IS NULL", outra pro valido). O filtro de cancelado tem
        // que estar no proprio JOIN de PG, senao PG casa com o recebimento cancelado.
        assertTrue(
            sql.contains(
                "LEFT JOIN RCT2 PG\n" +
                    "         ON PG.\"DocEntry\" = NS.\"DocEntry\" AND PG.\"InstId\" = P.\"InstlmntID\" AND PG.\"InvType\" = 13\n" +
                    "         AND EXISTS (\n" +
                    "             SELECT 1 FROM ORCT PRX WHERE PRX.\"DocEntry\" = PG.\"DocNum\" AND (PRX.\"Canceled\" = 'N' OR PRX.\"Canceled\" IS NULL)\n" +
                    "         )"
            )
        )
    }

    @Test
    fun `parcela com mais de um recebimento (pagamento parcial) traz so o mais recente, sem duplicar a linha`() {
        // Uma parcela pode ter varios RCT2 (recebimentos parciais); sem esse desempate por
        // NOT EXISTS a linha do titulo duplicaria uma vez por recebimento na lista.
        assertTrue(
            sql.contains(
                "AND (\n" +
                    "        PG.\"DocEntry\" IS NULL\n" +
                    "        OR NOT EXISTS ("
            )
        )
        assertTrue(sql.contains("PG2.\"DocEntry\" = PG.\"DocEntry\" AND PG2.\"InstId\" = PG.\"InstId\" AND PG2.\"InvType\" = 13"))
        assertTrue(sql.contains("PR2.\"DocDate\" > PR.\"DocDate\""))
        assertTrue(sql.contains("PG2.\"DocNum\" > PG.\"DocNum\""))
    }

    @Test
    fun `o desempate do ultimo pagamento respeita a janela de dataPagamento`() {
        // Sem isto a parcela paga DUAS vezes (uma dentro do periodo, outra depois) sumia inteira
        // do recorte: a linha do pagamento de dentro perdia o desempate pro pagamento posterior,
        // e a do posterior caia no filtro de data - subtracao silenciosa contra o card. Com o
        // filtro desligado os sentinelas (1900-01-01 / 9999-12-31) deixam a subquery identica.
        assertTrue(sql.contains("AND PR2.\"DocDate\" >= :dataPagamentoDe"))
        assertTrue(sql.contains("AND PR2.\"DocDate\" <= :dataPagamentoAte"))
    }

    @Test
    fun `nao usa RCT2 LineNum - coluna nao existe nesse schema do SAP B1`() {
        // Provisionamento real ja quebrou com "Column 'LineNum' from table 'RCT2' not exist."
        // (SAP B1 Service Layer, erro 703). O desempate usa so DocDate + DocNum do pagamento.
        assertFalse(sql.contains("LineNum"))
    }
}

class CobrancaTitulosAdiantamentoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/cobranca/cobranca-titulos-adiantamento.sql")
    )

    @Test
    fun `usa o Tipo AD ao juntar com a UDT, pra nao colidir com fatura de mesmo DocEntry`() {
        // ODPI (adiantamento) e OINV (fatura) tem contadores de DocEntry independentes no
        // SAP (ObjType 203 x 13) - sem o Tipo na chave, os dois colidiriam em @COB_TITULO.
        assertTrue(sql.contains("C.\"U_Tipo\" = 'AD'"))
    }

    @Test
    fun `expoe o status oficial da parcela para SituacaoSap nao depender do saldo calculado`() {
        assertTrue(sql.contains("P.\"Status\" AS \"StatusParcela\""))
    }

    @Test
    fun `liga o adiantamento ao contrato de venda futura pelo U_venda_futura`() {
        assertTrue(sql.contains("\"@AR_CONTRATO_FUTURO\""))
        assertTrue(sql.contains("T0.\"U_venda_futura\""))
    }

    @Test
    fun `mantem visivel o adiantamento que ja esta em acompanhamento mesmo apos a baixa`() {
        assertTrue(sql.contains("OR C.\"Code\" IS NOT NULL"))
    }

    @Test
    fun `filtros opcionais seguem o mesmo idioma x-ou-xIsFilter da query de faturas`() {
        assertTrue(sql.contains(":filialIsFilter"))
        assertTrue(sql.contains(":vendedorIsFilter"))
        assertTrue(sql.contains(":clienteIsFilter"))
    }

    @Test
    fun `situacao e intervalo de vencimento tambem sao filtrados no SQL aqui`() {
        // As duas queries recebem a MESMA lista de parametros em CobrancaConsultaService -
        // se uma view deixar de aceitar um parametro, a consulta quebra pro tipo dela.
        assertTrue(sql.contains(":statusParcelaIsFilter"))
        assertTrue(sql.contains(":vencimentoDe"))
        assertTrue(sql.contains(":vencimentoAte"))
    }

    @Test
    fun `mes de lancamento existe aqui tambem, senao a consulta quebra pro adiantamento`() {
        // As duas views recebem a MESMA lista de parametros - parametro que sobra numa delas
        // derruba a consulta inteira daquele tipo de titulo.
        assertTrue(sql.contains("T0.\"DocDate\" >= :lancamentoDe"))
        assertTrue(sql.contains("T0.\"DocDate\" <= :lancamentoAte"))
    }

    @Test
    fun `filtro de campo da UDT tambem usa coluna nao-nula como escape aqui`() {
        assertTrue(sql.contains("C.\"U_Status\"    = :status   OR T0.\"DocEntry\" < :statusIsFilter"))
        assertTrue(sql.contains("C.\"U_Cobrador\"  = :cobrador OR T0.\"DocEntry\" < :cobradorIsFilter"))
        assertTrue(sql.contains("C.\"U_Situacao\"  = :situacao OR T0.\"DocEntry\" < :situacaoIsFilter"))
    }

    @Test
    fun `os filtros do drill-down existem aqui tambem, senao a consulta quebra pro adiantamento`() {
        // As duas views recebem a MESMA lista de parametros em CobrancaConsultaService.
        assertTrue(sql.contains("C.\"Code\" IS NULL OR T0.\"DocEntry\" < :semAcompanhamentoIsFilter"))
        assertTrue(sql.contains("T0.\"DocEntry\" < :comAcompanhamentoIsFilter"))
        assertTrue(sql.contains("C.\"U_DataPromessa\" <= :promessaVencidaAte OR T0.\"DocEntry\" < :promessaVencidaIsFilter"))
    }

    @Test
    fun `comAcompanhamento do adiantamento tambem exige acao ANTES do recebimento, com InvType 203 e U_Tipo AD`() {
        assertTrue(sql.contains("INNER JOIN \"@COB_TITULO\" CX ON CX.\"U_Tipo\" = 'AD' AND CX.\"U_DocEntry\" = PGX.\"DocEntry\" AND CX.\"U_InstlmntID\" = PGX.\"InstId\""))
        assertTrue(sql.contains("INNER JOIN \"@COB_TITULO_L\" HX ON HX.\"Code\" = CX.\"Code\" AND HX.\"U_Data\" <= PRX.\"DocDate\""))
        assertTrue(sql.contains("WHERE PGX.\"DocEntry\" = T0.\"DocEntry\" AND PGX.\"InstId\" = P.\"InstlmntID\" AND PGX.\"InvType\" = 203"))
    }

    @Test
    fun `toggle de a vista tambem existe aqui, senao a consulta quebra pro adiantamento`() {
        assertTrue(sql.contains("T0.\"DocDate\" <> P.\"DueDate\" OR T0.\"DocEntry\" < :ocultarAvistaIsFilter"))
    }

    @Test
    fun `filtro de data de pagamento tambem existe aqui, senao a consulta quebra pro adiantamento`() {
        assertTrue(sql.contains("PR.\"DocDate\" >= :dataPagamentoDe OR T0.\"DocEntry\" < :dataPagamentoDeIsFilter"))
        assertTrue(sql.contains("PR.\"DocDate\" <= :dataPagamentoAte OR T0.\"DocEntry\" < :dataPagamentoAteIsFilter"))
    }

    @Test
    fun `traz o telefone do cliente aqui tambem, senao a coluna fica vazia so pro adiantamento`() {
        assertTrue(sql.contains("LEFT JOIN OCRD CL ON CL.\"CardCode\" = T0.\"CardCode\""))
        assertTrue(sql.contains("CL.\"Phone1\" AS \"Telefone\""))
        assertTrue(sql.contains("CL.\"Cellular\" AS \"Celular\""))
    }

    @Test
    fun `nao usa UNION, COALESCE ou CAST - sem precedente no parser do SQLQueries do SAP B1`() {
        assertFalse(sql.contains("UNION"))
        assertFalse(sql.contains("COALESCE"))
        assertFalse(sql.contains("CAST("))
        assertFalse(sql.contains("IFNULL"))
    }

    @Test
    fun `traz data, valor e observacao do ultimo recebimento do adiantamento sem poder derrubar a linha`() {
        assertTrue(sql.contains("LEFT JOIN RCT2 PG"))
        assertTrue(sql.contains("LEFT JOIN ORCT PR"))
        assertTrue(sql.contains("PR.\"DocDate\" AS \"DataPagamento\""))
        assertTrue(sql.contains("PG.\"SumApplied\" AS \"ValorPago\""))
        assertTrue(sql.contains("PR.\"Comments\" AS \"ObservacaoPagamento\""))
    }

    @Test
    fun `usa o InvType 203 ao ligar o recebimento, pra nao colidir com o recebimento de fatura`() {
        assertTrue(sql.contains("PG.\"DocEntry\" = T0.\"DocEntry\" AND PG.\"InstId\" = P.\"InstlmntID\" AND PG.\"InvType\" = 203"))
        assertTrue(sql.contains("PR.\"DocEntry\" = PG.\"DocNum\""))
    }

    @Test
    fun `recebimento cancelado do adiantamento tambem nao vaza ValorPago nem duplica a parcela`() {
        assertTrue(
            sql.contains(
                "LEFT JOIN RCT2 PG\n" +
                    "         ON PG.\"DocEntry\" = T0.\"DocEntry\" AND PG.\"InstId\" = P.\"InstlmntID\" AND PG.\"InvType\" = 203\n" +
                    "         AND EXISTS (\n" +
                    "             SELECT 1 FROM ORCT PRX WHERE PRX.\"DocEntry\" = PG.\"DocNum\" AND (PRX.\"Canceled\" = 'N' OR PRX.\"Canceled\" IS NULL)\n" +
                    "         )"
            )
        )
    }

    @Test
    fun `adiantamento com mais de um recebimento tambem traz so o mais recente, sem duplicar a linha`() {
        assertTrue(
            sql.contains(
                "AND (\n" +
                    "        PG.\"DocEntry\" IS NULL\n" +
                    "        OR NOT EXISTS ("
            )
        )
        assertTrue(sql.contains("PG2.\"DocEntry\" = PG.\"DocEntry\" AND PG2.\"InstId\" = PG.\"InstId\" AND PG2.\"InvType\" = 203"))
        assertTrue(sql.contains("PR2.\"DocDate\" > PR.\"DocDate\""))
        assertTrue(sql.contains("PG2.\"DocNum\" > PG.\"DocNum\""))
    }

    @Test
    fun `o desempate do adiantamento tambem respeita a janela de dataPagamento`() {
        assertTrue(sql.contains("AND PR2.\"DocDate\" >= :dataPagamentoDe"))
        assertTrue(sql.contains("AND PR2.\"DocDate\" <= :dataPagamentoAte"))
    }

    @Test
    fun `nao usa RCT2 LineNum aqui tambem - coluna nao existe nesse schema do SAP B1`() {
        assertFalse(sql.contains("LineNum"))
    }
}

class TitulosEmailSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/titulos.sql"))

    @Test
    fun `email semanal passa a ler o status e o cobrador da UDT de cobranca`() {
        assertTrue(sql.contains("C.\"U_Status\" AS \"U_StatusCobranca\""))
        assertTrue(sql.contains("C.\"U_Cobrador\" AS \"U_AgenteCobrador\""))
        assertFalse(
            sql.contains("P.\"U_StatusCobranca\""),
            "nao deve mais ler o UDF antigo do INV6"
        )
        assertFalse(
            sql.contains("P.\"U_AgenteCobrador\""),
            "nao deve mais ler o UDF antigo do INV6"
        )
    }

    @Test
    fun `usa o Tipo NF ao juntar com a UDT, pra nao colidir com adiantamento de mesmo DocEntry`() {
        assertTrue(sql.contains("C.\"U_Tipo\" = 'NF'"))
    }
}

class CobrancaCobradoresSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/cobranca/cobranca-cobradores.sql")
    )

    @Test
    fun `lista os cobradores da UDT, nao das linhas que a tela carregou`() {
        assertTrue(sql.contains("DISTINCT"))
        assertTrue(sql.contains("\"@COB_TITULO\""))
    }

    @Test
    fun `descarta cobrador vazio - a UDT guarda string vazia, nao so null`() {
        assertTrue(sql.contains("IS NOT NULL"))
        assertTrue(sql.contains("<> ''"))
    }

    @Test
    fun `nao usa funcao sem precedente no parser do SQLQueries`() {
        assertFalse(sql.contains("COALESCE"))
        assertFalse(sql.contains("IFNULL"))
        assertFalse(sql.contains("CAST("))
    }
}

class CobrancaTituloVendedorSqlTest {

    @Test
    fun `view da fatura traz o CardCode, senao registrarAcao nao tem de onde gravar U_CardCode`() {
        val sql = Files.readString(Path.of("src/main/resources/views/cobranca/cobranca-titulo-vendedor.sql"))
        assertTrue(sql.contains("\"CardCode\""))
    }

    @Test
    fun `view do adiantamento tambem traz o CardCode, pelo mesmo motivo`() {
        val sql = Files.readString(Path.of("src/main/resources/views/cobranca/cobranca-titulo-vendedor-adiantamento.sql"))
        assertTrue(sql.contains("\"CardCode\""))
    }
}

/**
 * View auxiliar do ValorRecebidoNoPeriodo: soma TODOS os recebimentos da parcela na janela, nao
 * so o mais recente (ValorPago). Ficou de fora de cobranca-titulos.sql porque o SAP recusa
 * subquery escalar com sum() na lista de SELECT (erro 701, "Invalid SQL syntax") - o parser
 * aceita subquery como predicado (WHERE/ON, ja usado em cobranca-titulos.sql), nao como valor
 * de coluna. Por isso virou linha a linha aqui, agregada em Kotlin (CobrancaConsultaService).
 */
class CobrancaRecebimentosPeriodoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/cobranca/cobranca-recebimentos-periodo.sql")
    )

    @Test
    fun `traz cada recebimento em linha propria, sem agregar aqui`() {
        assertTrue(sql.contains("PG.\"DocEntry\""))
        assertTrue(sql.contains("PG.\"InstId\""))
        assertTrue(sql.contains("PG.\"SumApplied\""))
        assertFalse(sql.contains("SELECT sum("), "a soma e feita em Kotlin, nao nesta view")
        assertFalse(sql.contains("GROUP BY"))
    }

    @Test
    fun `so fatura - InvType 13 - e recebimento nao cancelado`() {
        assertTrue(sql.contains("PG.\"InvType\" = 13"))
        assertTrue(sql.contains("PR.\"Canceled\" = 'N' OR PR.\"Canceled\" IS NULL"))
    }

    @Test
    fun `recorta pela mesma janela de data de pagamento do resto do modulo`() {
        assertTrue(sql.contains("PR.\"DocDate\" >= :dataPagamentoDe"))
        assertTrue(sql.contains("PR.\"DocDate\" <= :dataPagamentoAte"))
    }

    @Test
    fun `com o filtro ligado, so soma recebimento com acao registrada ANTES dele - igual o card`() {
        // Reproduz o mesmo criterio de cobranca-recuperado.sql (INNER JOIN @COB_TITULO_L com
        // H.U_Data <= data do pagamento), so que como EXISTS: nao precisamos do NOT EXISTS de
        // desempate do card (que escolhe UMA linha de H pra evitar duplicar a transacao no
        // GROUP BY) porque aqui so importa SE existe alguma acao qualificada, nao qual.
        assertTrue(sql.contains("LEFT JOIN \"@COB_TITULO\" C"))
        assertTrue(sql.contains("C.\"U_Tipo\" = 'NF' AND C.\"U_DocEntry\" = PG.\"DocEntry\" AND C.\"U_InstlmntID\" = PG.\"InstId\""))
        assertTrue(sql.contains("PG.\"DocEntry\" < :acaoAntesPagamentoIsFilter"))
        assertTrue(sql.contains("EXISTS (\n                SELECT 1 FROM \"@COB_TITULO_L\" H WHERE H.\"Code\" = C.\"Code\" AND H.\"U_Data\" <= PR.\"DocDate\""))
    }

    @Test
    fun `restringe por filial, vendedor e cliente - senao a varredura come pagina com dado fora do filtro`() {
        // Bug reportado: sem isso, qualquer recebimento da empresa inteira consumia o teto de
        // paginas do CobrancaConsultaService, mesmo pertencendo a filial/cliente fora do filtro
        // atual da tela.
        assertTrue(sql.contains("INNER JOIN OINV NS ON NS.\"DocEntry\" = PG.\"DocEntry\""))
        assertTrue(sql.contains("NS.\"BPLId\"    = :filial   OR NS.\"BPLId\"    < :filialIsFilter"))
        assertTrue(sql.contains("NS.\"SlpCode\"  = :vendedor OR NS.\"SlpCode\"  < :vendedorIsFilter"))
        assertTrue(sql.contains("NS.\"CardCode\" = :cliente  OR NS.\"CardCode\" < :clienteIsFilter"))
    }
}

class CobrancaRecebimentosPeriodoAdiantamentoSqlTest {

    private val sql = Files.readString(
        Path.of("src/main/resources/views/cobranca/cobranca-recebimentos-periodo-adiantamento.sql")
    )

    @Test
    fun `usa o InvType 203, pra nao colidir com recebimento de fatura`() {
        assertTrue(sql.contains("PG.\"InvType\" = 203"))
    }

    @Test
    fun `mesmo recorte de recebimento nao cancelado e janela de data`() {
        assertTrue(sql.contains("PR.\"Canceled\" = 'N' OR PR.\"Canceled\" IS NULL"))
        assertTrue(sql.contains("PR.\"DocDate\" >= :dataPagamentoDe"))
        assertTrue(sql.contains("PR.\"DocDate\" <= :dataPagamentoAte"))
    }

    @Test
    fun `mesmo filtro de acao antes do pagamento aqui, com U_Tipo AD`() {
        assertTrue(sql.contains("C.\"U_Tipo\" = 'AD' AND C.\"U_DocEntry\" = PG.\"DocEntry\" AND C.\"U_InstlmntID\" = PG.\"InstId\""))
        assertTrue(sql.contains("PG.\"DocEntry\" < :acaoAntesPagamentoIsFilter"))
    }

    @Test
    fun `restringe por filial, vendedor e cliente aqui tambem, via ODPI`() {
        assertTrue(sql.contains("INNER JOIN ODPI T0 ON T0.\"DocEntry\" = PG.\"DocEntry\""))
        assertTrue(sql.contains("T0.\"BPLId\"    = :filial   OR T0.\"BPLId\"    < :filialIsFilter"))
        assertTrue(sql.contains("T0.\"SlpCode\"  = :vendedor OR T0.\"SlpCode\"  < :vendedorIsFilter"))
        assertTrue(sql.contains("T0.\"CardCode\" = :cliente  OR T0.\"CardCode\" < :clienteIsFilter"))
    }
}
