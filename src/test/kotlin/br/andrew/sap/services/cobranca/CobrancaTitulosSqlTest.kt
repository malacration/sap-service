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
    fun `nao usa funcoes que o parser do SQLQueries do SAP B1 nao reconhece nesse contexto`() {
        // Saldo, DiasAtraso e SituacaoSap sao calculados em Kotlin (CobrancaTituloSap.toDto);
        // IFNULL/DAYS_BETWEEN/CASE WHEN aqui ja quebraram o provisionamento em producao
        // ("mismatched input '.' expecting FROM") por nao terem precedente nas views existentes.
        assertFalse(sql.contains("IFNULL"))
        assertFalse(sql.contains("DAYS_BETWEEN"))
        assertFalse(sql.contains("CASE WHEN"))
        assertFalse(sql.contains("CURRENT_DATE"))
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
    fun `filtro de campo da UDT tambem usa coluna nao-nula como escape aqui`() {
        assertTrue(sql.contains("C.\"U_Status\"    = :status   OR T0.\"DocEntry\" < :statusIsFilter"))
        assertTrue(sql.contains("C.\"U_Cobrador\"  = :cobrador OR T0.\"DocEntry\" < :cobradorIsFilter"))
        assertTrue(sql.contains("C.\"U_Situacao\"  = :situacao OR T0.\"DocEntry\" < :situacaoIsFilter"))
    }

    @Test
    fun `nao usa UNION, COALESCE ou CAST - sem precedente no parser do SQLQueries do SAP B1`() {
        assertFalse(sql.contains("UNION"))
        assertFalse(sql.contains("COALESCE"))
        assertFalse(sql.contains("CAST("))
        assertFalse(sql.contains("IFNULL"))
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
}
