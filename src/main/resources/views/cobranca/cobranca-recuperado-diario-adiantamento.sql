SELECT
    r."DocDate",
    l."SumApplied" AS "Recuperado",
    T0."DocEntry"
FROM RCT2 l
    INNER JOIN ORCT r ON r."DocEntry" = l."DocNum"
    INNER JOIN ODPI T0 ON T0."DocEntry" = l."DocEntry"
    INNER JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = l."DocEntry" AND C."U_InstlmntID" = l."InstId"
WHERE
    (r."Canceled" = 'N' OR r."Canceled" IS NULL)
    AND l."InvType" = 203
    AND r."DocDate" >= :de
    AND r."DocDate" <= :ate
    AND EXISTS(SELECT 1 FROM "@COB_TITULO_L" H WHERE H."Code" = C."Code" AND H."U_Data" <= r."DocDate")
    AND (T0."BPLId"   = :filial   OR T0."BPLId"   < :filialIsFilter)
    AND (T0."SlpCode" = :vendedor OR T0."SlpCode" < :vendedorIsFilter)
    AND T0."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
ORDER BY
    r."DocDate"
