SELECT
    dp."TransId",
    dp."CardCode" AS "ShortName",
    p."DueDate"
FROM
    ODPI dp
    INNER JOIN DPI6 p ON p."DocEntry" = dp."DocEntry"
WHERE
    dp."U_venda_futura" = :idContrato
    AND dp."CANCELED" = 'N'
    AND dp."CardCode" = :cardCode
    AND p."Status" = 'O'
    AND p."InsTotal" <> 0
    AND p."DueDate" <= :dataLimite
