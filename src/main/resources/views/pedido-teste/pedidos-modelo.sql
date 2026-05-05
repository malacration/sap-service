SELECT DISTINCT
    c."DocEntry"
FROM "ORDR" c
JOIN "RDR12" r ON c."DocEntry" = r."DocEntry"
JOIN "RDR1" d ON c."DocEntry" = d."DocEntry"
WHERE
    c."DocStatus" = 'O'
    AND c."CANCELED" = 'N'
    AND r."U_LocalidadeS" = :localidade
    AND (c."BPLId" = :filial OR c."BPLId" < :filialIsFilter)
    AND c."DocDate" >= :dataInicial
    AND c."DocDate" <= :dataFinal
    AND d."Usage" = 9
