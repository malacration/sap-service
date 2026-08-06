SELECT
    NS."BPLId", NS."BPLName",
    sum(P."InsTotal")   AS "Total",
    sum(P."PaidToDate") AS "Pago",
    count(P."InstlmntID") AS "Parcelas"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = NS."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    NS."CANCELED" = 'N'
    AND P."InsTotal" <> 0
    AND P."Status" = 'O'
    AND P."DueDate" <= :data
    AND C."Code" IS NULL
    AND (NS."BPLId"   = :filial   OR NS."BPLId"   < :filialIsFilter)
    AND (NS."SlpCode" = :vendedor OR NS."SlpCode" < :vendedorIsFilter)
GROUP BY
    NS."BPLId", NS."BPLName"
