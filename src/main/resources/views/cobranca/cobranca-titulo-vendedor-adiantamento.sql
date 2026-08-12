SELECT
    T0."SlpCode"
FROM ODPI T0
    INNER JOIN DPI6 P ON P."DocEntry" = T0."DocEntry"
WHERE
    T0."DocEntry" = :docEntry
    AND P."InstlmntID" = :instlmntId
