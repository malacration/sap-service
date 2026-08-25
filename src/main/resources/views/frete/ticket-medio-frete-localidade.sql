SELECT
    L."Code" AS "CodLocalidade",
    L."Name" AS "Localidade",
    count(DISTINCT NS."DocEntry") AS "Notas",
    sum(E."LineTotal") AS "TotalFrete"
FROM OINV NS
    INNER JOIN INV12 EE ON EE."DocEntry" = NS."DocEntry"
    INNER JOIN "@RO_LOCAIS" L ON L."Code" = EE."U_LocalidadeS"
    INNER JOIN INV3 E ON E."DocEntry" = NS."DocEntry" AND E."ExpnsCode" = 1 AND E."LineTotal" > 0
WHERE
    NS."CANCELED" = 'N'
    AND NS."DocDate" >= :startDate
    AND NS."DocDate" <= :finalDate
    AND (NS."BPLId" = :filial OR NS."BPLId" < :filialIsFilter)
GROUP BY
    L."Code", L."Name"
