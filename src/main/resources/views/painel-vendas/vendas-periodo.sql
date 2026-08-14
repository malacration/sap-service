SELECT
    NS."DocEntry",
    NS."DocNum",
    NS."CardCode",
    NS."CardName",
    NS."DocDate",
    NS."DocTotal"
FROM OINV NS
WHERE
    NS."DocDate" >= :startDate
    AND NS."DocDate" <= :finalDate
    AND NS."CANCELED" = 'N'
    AND (NS."SlpCode" = :vendedor OR NS."SlpCode" < :superVendedor)
ORDER BY
    NS."DocDate"
