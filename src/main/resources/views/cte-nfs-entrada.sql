SELECT
    n."DocEntry",
    SUM(l."LineTotal") AS "ValorNf"
FROM "OPCH" n
INNER JOIN "PCH1" l ON l."DocEntry" = n."DocEntry"
WHERE
    n."CANCELED" = 'N'
    AND n."DocEntry" IN (SELECT r."DocEntry"
                         FROM "PCH21" r
                         WHERE r."RefDocEntr" = :cte
                           AND r."RefObjType" = 18)
GROUP BY n."DocEntry"
