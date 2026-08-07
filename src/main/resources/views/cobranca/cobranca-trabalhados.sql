SELECT
    H."U_Usuario",
    count(DISTINCT H."Code") AS "Titulos"
FROM "@COB_TITULO_L" H
    INNER JOIN "@COB_TITULO" C ON C."Code" = H."Code"
    INNER JOIN OINV NS ON NS."DocEntry" = C."U_DocEntry"
WHERE
    C."U_Tipo" = 'NF'
    AND H."U_Data" >= :de
    AND H."U_Data" <= :ate
    AND (NS."BPLId"   = :filial   OR NS."BPLId"   < :filialIsFilter)
    AND (NS."SlpCode" = :vendedor OR NS."SlpCode" < :vendedorIsFilter)
    AND NS."CardCode" NOT IN (SELECT "DflCust" FROM OBPL WHERE "DflCust" IS NOT NULL)
GROUP BY
    H."U_Usuario"
