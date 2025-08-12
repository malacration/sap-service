SELECT
    o."U_itemCode",
    SUM(o."U_quantidade") AS total_quantidade
FROM
    "@ORD_CARREGAMENTO" d
INNER JOIN
    "@ORD_CRG_LINHA" o ON d."DocEntry" = o."DocEntry"
WHERE
    d."U_Status" = 'Aberto'
    AND o."U_itemCode" = :ItemCode
GROUP BY
    o."U_itemCode"