SELECT
    l."DocEntry",
    h."DocNum",
    h."DocDate",
    l."ItemCode",
    MAX(l."unitMsr") AS "UnidadeCompra",
    SUM(l."Quantity") AS "Quantidade",
    SUM(l."LineTotal") AS "LineTotal",
    SUM(l."InvQty") AS "InvQty"
FROM "PCH1" l
INNER JOIN "OPCH" h ON h."DocEntry" = l."DocEntry"
WHERE
    l."ItemCode" = :item
    AND l."InvQty" > 0
    AND l."DocEntry" IN (SELECT MAX(o."DocEntry")
                         FROM "OPCH" o
                         INNER JOIN "PCH1" x ON x."DocEntry" = o."DocEntry"
                         WHERE x."ItemCode" = :item
                           AND x."InvQty" > 0
                           AND o."CANCELED" = 'N')
GROUP BY l."DocEntry", h."DocNum", h."DocDate", l."ItemCode"
