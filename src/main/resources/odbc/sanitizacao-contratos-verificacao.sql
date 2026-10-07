SELECT 'ESTORNO' AS "Tipo", j."TransId" AS "Id", j."RefDate" AS "Data", o."RefDate" AS "Original"
FROM "OJDT" j JOIN "OJDT" o ON o."TransId" = :transId
WHERE j."StornoToTr" = o."TransId" OR j."TransId" = o."StornoToTr"
UNION ALL
SELECT 'APROPRIACAO', i."DocEntry", i."DocDate", o."DocDate"
FROM "OINV" i JOIN "OINV" o ON o."DocEntry" IN (:apropriacoes)
WHERE i."CANCELED" = 'C' AND (
    EXISTS (SELECT 1 FROM "INV1" l WHERE l."DocEntry" = i."DocEntry" AND l."BaseType" = 13 AND l."BaseEntry" = o."DocEntry")
    OR EXISTS (SELECT 1 FROM "OJDT" j WHERE j."TransId" = i."TransId" AND j."StornoToTr" = o."TransId")
    OR EXISTS (SELECT 1 FROM "OJDT" jo WHERE jo."TransId" = o."TransId" AND jo."StornoToTr" = i."TransId")
)
UNION ALL
SELECT 'PENDENCIA', "DocEntry", "DocDate", NULL FROM "OINV"
WHERE "DocEntry" IN (:apropriacoes) AND "CANCELED" <> 'Y'
UNION ALL
SELECT 'PENDENCIA', "ReconNum", "ReconDate", NULL FROM "OITR"
WHERE "ReconNum" IN (:reconciliacoes) AND "Canceled" <> 'Y'
UNION ALL
SELECT 'PENDENCIA', j."TransId", j."RefDate", NULL FROM "OJDT" j
WHERE j."TransId" = :transId AND EXISTS (
    SELECT 1 FROM "JDT1" l WHERE l."TransId" = j."TransId"
    AND (ABS(l."BalDueDeb") > 0.000001 OR ABS(l."BalDueCred") > 0.000001)
)
