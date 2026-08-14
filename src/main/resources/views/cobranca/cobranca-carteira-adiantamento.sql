SELECT
    T0."BPLId", T0."BPLName",
    C."U_Status",
    sum(P."InsTotal")   AS "Total",
    sum(P."PaidToDate") AS "Pago",
    count(P."InstlmntID") AS "Parcelas"
FROM ODPI T0
    INNER JOIN DPI6 P ON P."DocEntry" = T0."DocEntry"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = T0."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    T0."CANCELED" = 'N'
    AND P."InsTotal" <> 0
    AND P."Status" = 'O'
    AND P."DueDate" >= :vencimentoDe
    AND P."DueDate" <= :vencimentoAte
    AND (T0."BPLId"   = :filial   OR T0."BPLId"   < :filialIsFilter)
    AND (T0."SlpCode" = :vendedor OR T0."SlpCode" < :vendedorIsFilter)
    AND T0."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
GROUP BY
    T0."BPLId", T0."BPLName", C."U_Status"
