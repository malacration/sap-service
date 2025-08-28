SELECT
    o."DocEntry",
    r."U_ORD_CARREGAMENTO2",
    r."ItemCode",
    r."Quantity",
    r."Weight1",
    1 AS "docEntryQuantity"
FROM ORDR o
INNER JOIN RDR1 r ON o."DocEntry"= r."DocEntry"
WHERE r."U_ORD_CARREGAMENTO2" = :ordCarregamento
