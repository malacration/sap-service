SELECT
    T0."DocEntry" AS "DocEntry",
    T0."DocNum" AS "DocNum",
    T0."ItemCode" AS "ItemCode",
    T0."PlannedQty" AS "Quantidade",
    T0."PostDate" AS "DataEntradaOP",
    T0."DueDate" AS "DataEntregaProducao",
    T0."Comments" AS "DescricaoOrdemProducao",
    F."U_LbrOne_Id" AS "CodFormula",
    T0."U_LbrOne_Batelada" AS "QuantBat",
    T0."U_sysfeed_status" AS "SysfeedStatus"
FROM OWOR T0
LEFT JOIN OITT F ON F."Code" = T0."ItemCode"
WHERE T0."Status" = 'R'
  AND T0."PostDate" >= :startDate
  AND F."U_LbrOne_Id" IS NOT NULL
  AND T0."Type" = 'S'
  AND (T0."U_sysfeed_status" IS NULL OR T0."U_sysfeed_status" = '' OR T0."U_sysfeed_status" = 'PENDENTE')
ORDER BY T0."DocEntry" DESC
