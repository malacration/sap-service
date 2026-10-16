SELECT
    c."DocEntry",
    SUM(l."LineTotal") AS "FreteBruto"
FROM "OPCH" c
INNER JOIN "PCH1" l ON l."DocEntry" = c."DocEntry"
WHERE
    c."CANCELED" = 'N'
    AND c."DocEntry" IN (SELECT r."RefDocEntr"
                         FROM "PCH21" r
                         WHERE r."DocEntry" = :docEntry
                           AND r."RefObjType" = 18
                           AND r."RefDocEntr" IS NOT NULL)
GROUP BY c."DocEntry"
