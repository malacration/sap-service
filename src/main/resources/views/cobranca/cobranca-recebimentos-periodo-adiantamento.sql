SELECT
    PG."DocEntry", PG."InstId", PG."SumApplied"
FROM RCT2 PG
    INNER JOIN ORCT PR ON PR."DocEntry" = PG."DocNum" AND (PR."Canceled" = 'N' OR PR."Canceled" IS NULL)
    INNER JOIN ODPI T0 ON T0."DocEntry" = PG."DocEntry"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = PG."DocEntry" AND C."U_InstlmntID" = PG."InstId"
WHERE
    PG."InvType" = 203
    AND PR."DocDate" >= :dataPagamentoDe
    AND PR."DocDate" <= :dataPagamentoAte
    AND (T0."BPLId"    = :filial   OR T0."BPLId"    < :filialIsFilter)
    AND (T0."SlpCode"  = :vendedor OR T0."SlpCode"  < :vendedorIsFilter)
    AND (T0."CardCode" = :cliente  OR T0."CardCode" < :clienteIsFilter)
    AND (
        PG."DocEntry" < :acaoAntesPagamentoIsFilter
        OR (
            C."Code" IS NOT NULL
            AND EXISTS (
                SELECT 1 FROM "@COB_TITULO_L" H WHERE H."Code" = C."Code" AND H."U_Data" <= PR."DocDate"
            )
        )
    )
