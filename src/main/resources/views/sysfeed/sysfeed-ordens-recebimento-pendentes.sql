SELECT
    T0."DocEntry" AS "DocEntry",
    T1."LineNum" AS "LineNum",
    T0."DocNum" AS "DocNum",
    T0."CardCode" AS "CardCode",
    T0."Serial" AS "Serial",
    T1."ItemCode" AS "ItemCode",
    T1."InvQty" AS "Quantity",
    B."BatchNum" AS "NrLoteCodigoRecebimento",
    L."ExpDate" AS "DataValidade",
    L."PrdDate" AS "DataFabricacao",
    T0."DocDate" AS "DataRegistro",
    T12."Vehicle" AS "Placa",
    I."U_LbrOne_Id" AS "CodProd",
    T0."U_sysfeed_status" AS "SysfeedStatus"
FROM OPCH T0
INNER JOIN PCH1 T1 ON T1."DocEntry" = T0."DocEntry"
INNER JOIN OITM I ON I."ItemCode" = T1."ItemCode"
LEFT JOIN IBT1 B ON B."BaseType" = 18 AND B."BaseEntry" = T0."DocEntry" AND B."BaseLinNum" = T1."LineNum" AND B."ItemCode" = T1."ItemCode"
LEFT JOIN OIBT L ON L."ItemCode" = B."ItemCode" AND L."BatchNum" = B."BatchNum"
LEFT JOIN PCH12 T12 ON T12."DocEntry" = T0."DocEntry"
WHERE T0."CANCELED" = 'N'
  AND T0."BPLId" = 2
  AND T1."Usage" = :usage
  AND T0."DocDate" >= :startDate
  AND I."U_LbrOne_Id" IS NOT NULL
  AND (T0."U_sysfeed_status" IS NULL OR T0."U_sysfeed_status" = '' OR T0."U_sysfeed_status" = 'PENDENTE')
  AND T0."U_LbrOne_DtIntegracao" IS NULL
ORDER BY T0."DocEntry" DESC, T1."LineNum" ASC
