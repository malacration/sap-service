SELECT
    T0."DocEntry" AS "DocEntry",
    T1."LineNum" AS "LineNum",
    T0."DocNum" AS "DocNum",
    T0."CardCode" AS "CardCode",
    T0."Serial" AS "Serial",
    T1."ItemCode" AS "ItemCode",
    T1."Quantity" AS "Quantity",
    I."U_LbrOne_Id" AS "CodProd",
    T0."U_sysfeed_status" AS "SysfeedStatus"
FROM OPCH T0
INNER JOIN PCH1 T1 ON T1."DocEntry" = T0."DocEntry"
INNER JOIN OITM I ON I."ItemCode" = T1."ItemCode"
WHERE T0."CANCELED" = 'N'
  AND T0."BPLId" = 2
  AND T1."Usage" = :usage
  AND T0."DocDate" >= :startDate
  AND (T0."U_sysfeed_status" IS NULL OR T0."U_sysfeed_status" = '' OR T0."U_sysfeed_status" = 'PENDENTE' OR T0."U_sysfeed_status" = 'ERRO' OR T0."U_sysfeed_status" = 'PARCIAL')
ORDER BY T0."DocEntry" DESC, T1."LineNum" ASC
