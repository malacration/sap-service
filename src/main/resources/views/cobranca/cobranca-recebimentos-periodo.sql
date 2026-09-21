SELECT
    PG."DocEntry", PG."InstId", PG."SumApplied"
FROM RCT2 PG
    INNER JOIN ORCT PR ON PR."DocEntry" = PG."DocNum" AND (PR."Canceled" = 'N' OR PR."Canceled" IS NULL)
    INNER JOIN OINV NS ON NS."DocEntry" = PG."DocEntry"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = PG."DocEntry" AND C."U_InstlmntID" = PG."InstId"
WHERE
    PG."InvType" = 13
    AND PR."DocDate" >= :dataPagamentoDe
    AND PR."DocDate" <= :dataPagamentoAte
    AND (NS."BPLId"    = :filial   OR NS."BPLId"    < :filialIsFilter)
    AND (NS."SlpCode"  = :vendedor OR NS."SlpCode"  < :vendedorIsFilter)
    AND (NS."CardCode" = :cliente  OR NS."CardCode" < :clienteIsFilter)
    AND (
        PG."DocEntry" < :acaoAntesPagamentoIsFilter
        OR (
            C."Code" IS NOT NULL
            AND EXISTS (
                SELECT 1 FROM "@COB_TITULO_L" H WHERE H."Code" = C."Code" AND H."U_Data" <= PR."DocDate"
            )
        )
    )
