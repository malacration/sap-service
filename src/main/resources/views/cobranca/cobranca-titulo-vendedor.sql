SELECT
    NS."SlpCode"
FROM OINV NS
WHERE
    NS."DocEntry" = :docEntry
