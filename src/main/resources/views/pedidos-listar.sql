SELECT
    c."DocEntry",
    c."DocNum",
    c."CardCode",
    c."CardName",
    c."TotalExpns" AS "ValorFrete",
    c."DocTotal",
    c."DocDate",
    c."DocStatus" AS "DocumentStatus",
    e."SlpName",
    b."BPLName",
    b."BPLId",
    'oOrders' as "DocObjectCode"
FROM
    "ORDR" c
    LEFT JOIN "OSLP" e ON e."SlpCode" = c."SlpCode"
    JOIN "OBPL" b ON c."BPLId" = b."BPLId"
WHERE
    (c."SlpCode" = :vendedor OR c."SlpCode" < :superVendedor)
    AND (c."DocStatus" = :status OR c."DocStatus" < :statusIsFilter)
    AND (c."BPLId" = :filial OR c."BPLId" < :filialIsFilter)
    AND (c."CardCode" = :cliente OR c."CardCode" < :clienteIsFilter)
    AND c."DocDate" >= :data
ORDER BY c."DocEntry" DESC
