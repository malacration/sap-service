SELECT
    c."DocEntry",
    c."DocNum",
    c."CardCode",
    c."CardName",
    c."DocDate",
    c."DocTotal",
    d."ItemCode",
    d."Dscription",
    d."Quantity",
    o."BuyUnitMsr",
    r."U_LocalidadeS",
    o."SWeight1" AS "Weight1",
    c."BPLId" AS "BPL_IDAssignedToInvoice",
    l."Name",
    w."OnHand",
    w."IsCommited",
    w."OnOrder",
    b."DflWhs"
FROM
    "ORDR" c
INNER JOIN "RDR12" r ON c."DocEntry" = r."DocEntry"
INNER JOIN "RDR1" d ON c."DocEntry" = d."DocEntry"
INNER JOIN "OITM" o ON d."ItemCode" = o."ItemCode"
INNER JOIN "OITW" w ON o."ItemCode" = w."ItemCode"
AND d."WhsCode" = w."WhsCode"
INNER JOIN "OBPL" b ON c."BPLId" = b."BPLId"
INNER JOIN "@RO_LOCAIS" l ON r."U_LocalidadeS" = l."Code"
WHERE
    c."DocDate" >= :startDate
    AND c."DocDate" <= :finalDate
    AND r."U_LocalidadeS" = :localidade
    AND c."BPLId" = :filial