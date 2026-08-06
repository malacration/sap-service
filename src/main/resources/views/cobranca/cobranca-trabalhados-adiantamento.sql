SELECT
    H."U_Usuario",
    count(DISTINCT H."Code") AS "Titulos"
FROM "@COB_TITULO_L" H
    INNER JOIN "@COB_TITULO" C ON C."Code" = H."Code"
    INNER JOIN ODPI T0 ON T0."DocEntry" = C."U_DocEntry"
WHERE
    C."U_Tipo" = 'AD'
    AND H."U_Data" >= :de
    AND H."U_Data" <= :ate
    AND (T0."BPLId"   = :filial   OR T0."BPLId"   < :filialIsFilter)
    AND (T0."SlpCode" = :vendedor OR T0."SlpCode" < :vendedorIsFilter)
GROUP BY
    H."U_Usuario"
