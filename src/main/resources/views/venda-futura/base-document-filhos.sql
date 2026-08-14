SELECT 17 AS "ObjType", t."DocEntry" AS "DocEntry"
FROM RDR1 l INNER JOIN ORDR t ON t."DocEntry" = l."DocEntry"
WHERE l."BaseType" = :baseType AND l."BaseEntry" = :baseEntry AND t."CANCELED" = 'N'
GROUP BY t."DocEntry"

UNION ALL

SELECT 13 AS "ObjType", t."DocEntry" AS "DocEntry"
FROM INV1 l INNER JOIN OINV t ON t."DocEntry" = l."DocEntry"
WHERE l."BaseType" = :baseType AND l."BaseEntry" = :baseEntry AND t."CANCELED" = 'N'
GROUP BY t."DocEntry"

UNION ALL

SELECT 14 AS "ObjType", t."DocEntry" AS "DocEntry"
FROM RIN1 l INNER JOIN ORIN t ON t."DocEntry" = l."DocEntry"
WHERE l."BaseType" = :baseType AND l."BaseEntry" = :baseEntry AND t."CANCELED" = 'N'
GROUP BY t."DocEntry"