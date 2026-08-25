SELECT
    L."Code" AS "CodLocalidade",
    sum(D."Quantity") AS "Quantidade"
FROM OINV NS
    INNER JOIN INV12 EE ON EE."DocEntry" = NS."DocEntry"
    INNER JOIN "@RO_LOCAIS" L ON L."Code" = EE."U_LocalidadeS"
    INNER JOIN INV1 D ON D."DocEntry" = NS."DocEntry"
WHERE
    NS."CANCELED" = 'N'
    AND NS."DocDate" >= :startDate
    AND NS."DocDate" <= :finalDate
    AND (NS."BPLId" = :filial OR NS."BPLId" < :filialIsFilter)
    AND EXISTS (
        SELECT 1 FROM INV3 E
        WHERE E."DocEntry" = NS."DocEntry" AND E."ExpnsCode" = 1 AND E."LineTotal" > 0
    )
GROUP BY
    L."Code"
