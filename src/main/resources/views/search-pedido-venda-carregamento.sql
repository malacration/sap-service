SELECT DISTINCT
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
    r."U_Localidade",
    o."SWeight1" AS "Weight1",
    c."BPLId" as "BPL_IDAssignedToInvoice",
    l."Name",
    w."OnHand",
    d."Quantity",
    b."DflWhs"

FROM
    "ORDR" c
INNER JOIN "CRD1" r ON c."CardCode" = r."CardCode"
INNER JOIN "RDR1" d ON c."DocEntry" = d."DocEntry"
INNER JOIN "OITM" o ON d."ItemCode" = o."ItemCode"
INNER JOIN "OITW" w ON o."ItemCode" = w."ItemCode"
INNER JOIN "OBPL" b ON c."BPLId" = b."BPLId"
INNER JOIN "@RO_LOCAIS" l ON r."U_Localidade" = l."Code"
WHERE
    c."DocDate" >= :startDate
    AND c."DocDate" <= :finalDate
    AND r."U_Localidade" = :localidade
    AND c."BPLId" = :filial