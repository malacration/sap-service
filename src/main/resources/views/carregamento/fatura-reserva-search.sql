SELECT
    o."DocEntry",
    o."DocNum",
    o."CardCode",
    o."CardName",
    o."DocDate",
    i."ItemCode",
    i."Dscription",
    i."OpenQty",
    i."Quantity" AS "QtdFatura",
    r."U_LocalidadeS",
    o."BPLId" AS "BPL_IDAssignedToInvoice",
    d."Name",
    r."CityS",
    r."StateS",
    w."OnHand",
    i."UomCode",
    i."Weight1" AS "PesoLinha",
    l."U_qtdRetirada" AS "Retirada",
    c."DocEntry" AS "OrdemAberta",
    w."IsCommited",
    w."OnOrder",
    b."DflWhs",
    i."WhsCode" AS "WarehouseCode",
    i."LineNum" AS "BaseLine",
    0 AS "DistribSum",
    e."SlpName",
    e."Telephone",
    e."Mobil",
    13 AS "BaseType"
FROM "OINV" o
INNER JOIN "INV1" i ON i."DocEntry" = o."DocEntry"
INNER JOIN "OBPL" b ON b."BPLId" = o."BPLId"
INNER JOIN "OITW" w ON w."ItemCode" = i."ItemCode" AND w."WhsCode" = i."WhsCode"
LEFT JOIN "INV12" r ON r."DocEntry" = o."DocEntry"
LEFT JOIN "@RO_LOCAIS" d ON d."Code" = r."U_LocalidadeS"
LEFT JOIN "OSLP" e ON e."SlpCode" = o."SlpCode"
LEFT JOIN "@ORD_CRG_LINHA" l ON l."U_docEntryFatura" = i."DocEntry" AND l."U_baseLine" = i."LineNum" AND l."U_baseType" = 13
LEFT JOIN "@ORD_CARREGAMENTO" c ON c."DocEntry" = l."DocEntry" AND c."U_Status" = 'Aberto'
WHERE
    o."isIns" = 'Y'
    AND o."CANCELED" = 'N'
    AND i."OpenQty" > 0
    AND o."DocDate" >= :startDate
    AND o."DocDate" <= :finalDate
    AND o."BPLId" = :filial
    AND (r."U_LocalidadeS" LIKE :localidade OR IFNULL(r."U_LocalidadeS", '') = '')
    AND (o."SlpCode" = :vendedor OR o."SlpCode" > :vendedorDisable)
