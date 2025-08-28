SELECT
c."DocNum",
r."U_ORD_CARREGAMENTO2"
FROM "ORDR" c
INNER JOIN "RDR1" r ON c."DocEntry" = r."DocEntry"
WHERE c."DocNum" = :DocNum