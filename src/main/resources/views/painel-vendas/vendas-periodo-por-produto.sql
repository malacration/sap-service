SELECT
    L."ItemCode",
    L."Dscription" AS "Description",
    SUM(L."Quantity") AS "Quantidade",
    SUM(L."LineTotal") AS "Total"
FROM OINV NS
    INNER JOIN INV1 L ON L."DocEntry" = NS."DocEntry"
WHERE
    NS."DocDate" >= :startDate
    AND NS."DocDate" <= :finalDate
    AND NS."CANCELED" = 'N'
    AND (NS."SlpCode" = :vendedor OR NS."SlpCode" < :superVendedor)
GROUP BY
    L."ItemCode",
    L."Dscription"
ORDER BY
    L."ItemCode"
