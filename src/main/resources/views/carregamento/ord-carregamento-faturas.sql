SELECT
    o."DocEntry",
    o."DocNum",
    o."CardCode",
    o."CardName",
    i."ItemCode",
    i."Dscription",
    l."U_qtdRetirada" AS "Retirada",
    i."UomCode",
    b."DflWhs",
    l."DocEntry" AS "U_ORD_CARREGAMENTO",
    o."Address2",
    o."Comments",
    i."Weight1" AS "PesoLinha",
    i."Quantity" AS "QtdFatura",
    d."Name",
    r."CityS",
    r."StateS",
    e."SlpName",
    0 AS "DistribSum",
    e."Telephone",
    e."Mobil",
    i."LineNum" AS "BaseLine",
    i."WhsCode" AS "WarehouseCode",
    13 AS "BaseType"
FROM "@ORD_CRG_LINHA" l
    JOIN "INV1" i ON i."DocEntry" = l."U_docEntryFatura" AND i."LineNum" = l."U_baseLine"
    JOIN "OINV" o ON o."DocEntry" = i."DocEntry"
    JOIN "OBPL" b ON o."BPLId" = b."BPLId"
    LEFT JOIN "INV12" r ON r."DocEntry" = o."DocEntry"
    LEFT JOIN "@RO_LOCAIS" d ON d."Code" = r."U_LocalidadeS"
    LEFT JOIN "OSLP" e ON e."SlpCode" = o."SlpCode"
    WHERE l."DocEntry" = :U_ORD_CARREGAMENTO
    AND l."U_baseType" = 13
