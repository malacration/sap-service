-- Frete (despesa adicional, INV3) e quantidade total de cada NF de venda futura, para a tela
-- mostrar o frete proporcional de cada retirada - a mesma conta da entrega (EntregaFaturaReserva).
-- Pelo sap-odbc porque o SQLQueries do Service Layer nao aceita SUM no SELECT.
SELECT o."DocEntry",
    IFNULL((SELECT SUM(f."LineTotal") FROM "INV3" f WHERE f."DocEntry" = o."DocEntry"), 0) AS "Frete",
    IFNULL((SELECT SUM(f."DrawnTotal") FROM "INV3" f WHERE f."DocEntry" = o."DocEntry"), 0) AS "FreteBaixado",
    (SELECT SUM(i."Quantity") FROM "INV1" i WHERE i."DocEntry" = o."DocEntry") AS "Quantidade"
FROM "OINV" o
WHERE o."DocEntry" IN (:docEntries)
