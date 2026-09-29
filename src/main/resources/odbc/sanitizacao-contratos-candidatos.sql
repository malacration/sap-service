WITH "Origens" AS (
    SELECT j."TransId", i."DocEntry", i."DocNum", i."U_venda_futura" AS "Contrato",
           i."BPLId", i."CardCode", i."CardName", i."DocTotal", i."CANCELED"
    FROM "OJDT" j
    JOIN "OINV" i ON TO_NVARCHAR(i."DocNum") = j."Ref1"
      AND i."U_venda_futura" > 0 AND i."U_entrega_vf" = '1' AND i."CANCELED" IN ('N', 'Y')
    WHERE j."TransCode" = 'VFEC' AND j."TransType" = 30
      AND COALESCE(j."StornoToTr", 0) = 0
      AND NOT EXISTS (SELECT 1 FROM "OJDT" e WHERE e."StornoToTr" = j."TransId")
      AND j."TransId" > :apos AND (:transId = 0 OR j."TransId" = :transId)
      AND EXISTS (SELECT 1 FROM "JDT1" l WHERE l."TransId" = j."TransId"
          AND l."ShortName" = i."CardCode" AND l."BPLId" = i."BPLId")
), "Devolucoes" AS (
    SELECT DISTINCT o."DocEntry" AS "Origem", d."DocEntry", d."DocNum", d."DocTotal", d."U_conciliar_automatico"
    FROM "Origens" o
    JOIN "ORIN" d ON d."CANCELED" = 'N' AND d."CardCode" = o."CardCode" AND d."BPLId" = o."BPLId"
    WHERE EXISTS (SELECT 1 FROM "RIN1" r WHERE r."DocEntry" = d."DocEntry"
        AND r."BaseType" = 13 AND r."BaseEntry" = o."DocEntry")
       OR d."U_TX_DocEntryRef" = o."DocEntry"
       OR EXISTS (SELECT 1 FROM "ITR1" a JOIN "OITR" h ON h."ReconNum" = a."ReconNum" AND h."Canceled" = 'N'
           JOIN "ITR1" b ON b."ReconNum" = a."ReconNum" AND b."SrcObjTyp" = 14 AND b."SrcObjAbs" = d."DocEntry"
           WHERE a."SrcObjTyp" = 13 AND a."SrcObjAbs" = o."DocEntry")
), "Pagina" AS (
    SELECT DISTINCT o."TransId"
    FROM "Origens" o
    WHERE o."CANCELED" = 'Y' OR EXISTS (SELECT 1 FROM "Devolucoes" d WHERE d."Origem" = o."DocEntry")
    ORDER BY o."TransId"
    LIMIT 200
)
SELECT o.*,
       (SELECT COUNT(*) FROM "OJDT" x WHERE x."Ref1" = TO_NVARCHAR(o."DocNum")
           AND x."TransCode" IN ('VFET', 'VFEC') AND COALESCE(x."StornoToTr", 0) = 0
           AND NOT EXISTS (SELECT 1 FROM "OJDT" e WHERE e."StornoToTr" = x."TransId")
           AND EXISTS (SELECT 1 FROM "JDT1" l WHERE l."TransId" = x."TransId"
               AND l."ShortName" = o."CardCode" AND l."BPLId" = o."BPLId")) AS "ReclassificacoesOrigem",
       d."DocEntry" AS "DevolucaoEntry", d."DocNum" AS "DevolucaoNum",
       d."DocTotal" AS "DevolucaoTotal", d."U_conciliar_automatico" AS "DevolucaoAutomatica",
       CASE WHEN NOT EXISTS (
           SELECT 1 FROM "INV1" l WHERE l."DocEntry" = o."DocEntry" AND l."Quantity" > 0
           AND l."Quantity" > COALESCE((SELECT SUM(r."Quantity") FROM "RIN1" r
               JOIN "ORIN" h ON h."DocEntry" = r."DocEntry" AND h."CANCELED" = 'N'
               WHERE r."BaseType" = 13 AND r."BaseEntry" = l."DocEntry" AND r."BaseLine" = l."LineNum"), 0) + 0.000001
       ) AND EXISTS (SELECT 1 FROM "INV1" l WHERE l."DocEntry" = o."DocEntry" AND l."Quantity" > 0)
       THEN 1 ELSE 0 END AS "DevolucaoIntegral",
       (SELECT COUNT(*) FROM "OJDT" v WHERE v."TransCode" = 'VFDV'
           AND COALESCE(v."StornoToTr", 0) = 0
           AND NOT EXISTS (SELECT 1 FROM "OJDT" e WHERE e."StornoToTr" = v."TransId")
           AND EXISTS (SELECT 1 FROM "Devolucoes" dv WHERE dv."Origem" = o."DocEntry" AND TO_NVARCHAR(dv."DocNum") = v."Ref1")
       ) AS "EstornosDevolucao"
FROM "Origens" o JOIN "Pagina" p ON p."TransId" = o."TransId"
LEFT JOIN "Devolucoes" d ON d."Origem" = o."DocEntry"
ORDER BY o."TransId", o."DocEntry", d."DocEntry"
