SELECT
    NS."SlpCode", NS."CardCode"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
WHERE
    NS."DocEntry" = :docEntry
    AND P."InstlmntID" = :instlmntId
