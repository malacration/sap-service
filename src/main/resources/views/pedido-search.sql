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
    r."U_LocalidadeS",
    c."BPLId" AS "BPL_IDAssignedToInvoice",
    l."Name",
    w."OnHand",
    d."UomCode",
    d."Weight1",
    grupo."BaseQty", AS "KgConversao"
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
LEFT JOIN "UGP1" grupo ON grupo."UgpEntry" = 4 AND grupo."UomEntry" = d."UomEntry"
WHERE
    c."DocDate" >= :startDate
    AND c."DocDate" <= :finalDate
    AND r."U_LocalidadeS" = :localidade
    AND c."BPLId" = :filial
    AND c."DocStatus" = 'O'