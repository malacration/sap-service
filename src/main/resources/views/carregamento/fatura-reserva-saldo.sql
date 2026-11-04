SELECT
    i."DocEntry",
    i."LineNum" AS "BaseLine",
    i."ItemCode",
    o."CardCode",
    i."OpenQty",
    l."U_qtdRetirada" AS "Retirada",
    c."DocEntry" AS "OrdemAberta"
FROM "INV1" i
INNER JOIN "OINV" o ON o."DocEntry" = i."DocEntry"
LEFT JOIN "@ORD_CRG_LINHA" l ON l."U_docEntryFatura" = i."DocEntry" AND l."U_baseLine" = i."LineNum" AND l."U_baseType" = 13
LEFT JOIN "@ORD_CARREGAMENTO" c ON c."DocEntry" = l."DocEntry" AND c."U_Status" = 'Aberto' AND c."DocEntry" <> :ordem
WHERE o."DocEntry" = :docEntry
    AND o."isIns" = 'Y'
    AND o."CANCELED" = 'N'
