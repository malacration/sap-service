SELECT
    T0."DocEntry", T0."DocNum",
    VF."DocNum" AS "ContratoDocNum",
    T0."BPLId", T0."BPLName", T0."CardCode", T0."CardName",
    CL."Phone1" AS "Telefone", CL."Cellular" AS "Celular",
    T0."DocDate", T0."DocTotal",
    V."SlpCode", V."SlpName",
    P."InstlmntID", P."InsTotal", P."PaidToDate", P."DueDate", P."Status" AS "StatusParcela",
    C."U_Status", C."U_Cobrador", C."U_Acao", C."U_Situacao",
    C."U_Ocorrencia", C."U_Observacao", C."U_DataAcao", C."U_DataPromessa",
    PR."DocDate" AS "DataPagamento", PG."SumApplied" AS "ValorPago", PR."Comments" AS "ObservacaoPagamento"
FROM ODPI T0
    INNER JOIN DPI6 P ON P."DocEntry" = T0."DocEntry"
    LEFT JOIN OSLP V ON V."SlpCode" = T0."SlpCode"
    LEFT JOIN OCRD CL ON CL."CardCode" = T0."CardCode"
    LEFT JOIN "@AR_CONTRATO_FUTURO" VF ON VF."DocEntry" = T0."U_venda_futura"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = T0."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
    LEFT JOIN RCT2 PG
         ON PG."DocEntry" = T0."DocEntry" AND PG."InstId" = P."InstlmntID" AND PG."InvType" = 203
         AND EXISTS (
             SELECT 1 FROM ORCT PRX WHERE PRX."DocEntry" = PG."DocNum" AND (PRX."Canceled" = 'N' OR PRX."Canceled" IS NULL)
         )
    LEFT JOIN ORCT PR
         ON PR."DocEntry" = PG."DocNum"
WHERE
    T0."CANCELED" = 'N'
    AND P."InsTotal" <> 0
    AND (
         (P."Status" = 'O'
          AND P."DueDate" <= :data)
      OR C."Code" IS NOT NULL
    )
    AND (P."Status"    = :statusParcela OR P."Status" < :statusParcelaIsFilter)
    AND P."DueDate" >= :vencimentoDe
    AND P."DueDate" <= :vencimentoAte
    AND T0."DocDate" >= :lancamentoDe
    AND T0."DocDate" <= :lancamentoAte
    AND (C."U_Status"    = :status   OR T0."DocEntry" < :statusIsFilter)
    AND (C."U_Status"    LIKE :statusPrefixo   OR T0."DocEntry" < :statusPrefixoIsFilter)
    AND (C."U_Cobrador"  = :cobrador OR T0."DocEntry" < :cobradorIsFilter)
    AND (C."U_Cobrador"  LIKE :cobradorPrefixo OR T0."DocEntry" < :cobradorPrefixoIsFilter)
    AND (C."U_Situacao"  = :situacao OR T0."DocEntry" < :situacaoIsFilter)
    AND (C."U_Situacao"  LIKE :situacaoPrefixo OR T0."DocEntry" < :situacaoPrefixoIsFilter)
    AND (C."Code" IS NULL OR T0."DocEntry" < :semAcompanhamentoIsFilter)
    AND (
        T0."DocEntry" < :comAcompanhamentoIsFilter
        OR EXISTS (
            SELECT 1 FROM RCT2 PGX
                INNER JOIN ORCT PRX ON PRX."DocEntry" = PGX."DocNum" AND (PRX."Canceled" = 'N' OR PRX."Canceled" IS NULL)
                INNER JOIN "@COB_TITULO" CX ON CX."U_Tipo" = 'AD' AND CX."U_DocEntry" = PGX."DocEntry" AND CX."U_InstlmntID" = PGX."InstId"
                INNER JOIN "@COB_TITULO_L" HX ON HX."Code" = CX."Code" AND HX."U_Data" <= PRX."DocDate"
            WHERE PGX."DocEntry" = T0."DocEntry" AND PGX."InstId" = P."InstlmntID" AND PGX."InvType" = 203
              AND PRX."DocDate" >= :dataPagamentoDe AND PRX."DocDate" <= :dataPagamentoAte
        )
    )
    AND (C."U_DataPromessa" <= :promessaVencidaAte OR T0."DocEntry" < :promessaVencidaIsFilter)
    AND (T0."DocDate" <> P."DueDate" OR T0."DocEntry" < :ocultarAvistaIsFilter)
    AND (PR."DocDate" >= :dataPagamentoDe OR T0."DocEntry" < :dataPagamentoDeIsFilter)
    AND (PR."DocDate" <= :dataPagamentoAte OR T0."DocEntry" < :dataPagamentoAteIsFilter)
    AND (T0."BPLId"    = :filial   OR T0."BPLId"    < :filialIsFilter)
    AND (T0."SlpCode"  = :vendedor OR T0."SlpCode"  < :vendedorIsFilter)
    AND (T0."CardCode" = :cliente  OR T0."CardCode" < :clienteIsFilter)
    AND T0."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
    AND (
        PG."DocEntry" IS NULL
        OR NOT EXISTS (
            SELECT 1 FROM RCT2 PG2
                INNER JOIN ORCT PR2 ON PR2."DocEntry" = PG2."DocNum" AND (PR2."Canceled" = 'N' OR PR2."Canceled" IS NULL)
            WHERE PG2."DocEntry" = PG."DocEntry" AND PG2."InstId" = PG."InstId" AND PG2."InvType" = 203
              AND PR2."DocDate" >= :dataPagamentoDe
              AND PR2."DocDate" <= :dataPagamentoAte
              AND (PR2."DocDate" > PR."DocDate"
                   OR (PR2."DocDate" = PR."DocDate" AND PG2."DocNum" > PG."DocNum"))
        )
    )
ORDER BY P."DueDate", T0."DocNum"
