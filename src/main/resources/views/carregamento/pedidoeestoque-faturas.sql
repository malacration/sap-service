SELECT
    o."DocEntry",
    l."DocEntry" AS "U_ORD_CARREGAMENTO",
    i."ItemCode",
    l."U_qtdRetirada" AS "Retirada",
    i."Weight1" AS "PesoLinha",
    i."Quantity" AS "QtdFatura",
    1 AS "docEntryQuantity"
FROM "@ORD_CRG_LINHA" l
INNER JOIN "INV1" i ON i."DocEntry" = l."U_docEntryFatura" AND i."LineNum" = l."U_baseLine"
INNER JOIN "OINV" o ON o."DocEntry" = i."DocEntry"
WHERE l."DocEntry" = :ordCarregamento
    AND l."U_baseType" = 13
