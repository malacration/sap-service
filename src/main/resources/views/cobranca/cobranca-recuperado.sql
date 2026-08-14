SELECT
    H."U_Cobrador",
    NS."BPLId", NS."BPLName",
    sum(l."SumApplied") AS "Recuperado",
    count(DISTINCT NS."DocEntry") AS "Documentos"
FROM RCT2 l
    INNER JOIN ORCT r ON r."DocEntry" = l."DocNum"
    INNER JOIN OINV NS ON NS."DocEntry" = l."DocEntry"
    INNER JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = l."DocEntry" AND C."U_InstlmntID" = l."InstId"
    INNER JOIN "@COB_TITULO_L" H
         ON H."Code" = C."Code" AND H."U_Data" <= r."DocDate"
WHERE
    (r."Canceled" = 'N' OR r."Canceled" IS NULL)
    AND l."InvType" = 13
    AND r."DocDate" >= :de
    AND r."DocDate" <= :ate
    AND NOT EXISTS (
        SELECT 1 FROM "@COB_TITULO_L" H2
        WHERE H2."Code" = H."Code" AND H2."U_Data" <= r."DocDate"
          AND (H2."U_Data" > H."U_Data" OR (H2."U_Data" = H."U_Data" AND H2."LineId" > H."LineId"))
    )
    AND (NS."BPLId"   = :filial   OR NS."BPLId"   < :filialIsFilter)
    AND (NS."SlpCode" = :vendedor OR NS."SlpCode" < :vendedorIsFilter)
    AND NS."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
GROUP BY
    H."U_Cobrador", NS."BPLId", NS."BPLName"
