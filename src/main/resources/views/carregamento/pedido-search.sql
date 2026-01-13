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
    r."U_LocalidadeS",
    c."BPLId" AS "BPL_IDAssignedToInvoice",
    l."Name",
    w."OnHand",
    d."UomCode",
    d."Weight1",
    grupo."BaseQty" AS "KgConversao",
    w."IsCommited",
    w."OnOrder",
    b."DflWhs",
    x."U_Status",
    d."PriceBefDi" AS "UnitPrice",
    d."WhsCode" AS "WarehouseCode",
    d."Usage",
    d."TaxCode",
    d."OcrCode" AS "CostingCode",
    d."OcrCode2" AS "CostingCode2",
    d."LineNum" AS "BaseLine",
    d."U_preco_negociado" AS "PrecoNegociado",
    d."U_preco_base" AS "PrecoBase",
    c."Comments" AS "Comentario",
    d."DistribSum" AS "FretePorLinha",
    c."AtcEntry" AS "AttachmentEntry",
    d."DistribSum",
    e."SlpName",
    e."Telephone",
    e."Mobil"
FROM
    "ORDR" c
JOIN "RDR12" r ON c."DocEntry" = r."DocEntry"
JOIN "RDR1" d ON c."DocEntry" = d."DocEntry"
JOIN "OITM" o ON d."ItemCode" = o."ItemCode"
JOIN "OITW" w ON o."ItemCode" = w."ItemCode" AND d."WhsCode" = w."WhsCode"
JOIN "OBPL" b ON c."BPLId" = b."BPLId"
JOIN "@RO_LOCAIS" l ON r."U_LocalidadeS" = l."Code"
LEFT JOIN "UGP1" grupo ON grupo."UgpEntry" = 4 AND grupo."UomEntry" = d."UomEntry"
LEFT JOIN "@ORD_CRG_LINHA" m ON d."DocEntry" = m."U_orderDocEntry"
LEFT JOIN "@ORD_CARREGAMENTO" x ON m."DocEntry" = x."DocEntry"
LEFT JOIN "OSLP" e ON e."SlpCode" = c."SlpCode"
WHERE
    c."DocDate" >= :startDate
    AND c."DocDate" <= :finalDate
    AND r."U_LocalidadeS" = :localidade
    AND c."BPLId" = :filial
    AND c."DocStatus" = 'O'
    AND (x."U_Status" = 'Cancelado' OR x."U_Status" IS NULL)
    AND d."U_ORD_CARREGAMENTO" IS NULL
    AND d."Usage" = 9