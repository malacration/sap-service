SELECT
    "CardCode",
    "TaxId0",
    "TaxId4"
FROM
    CRD7
    INNER JOIN OCRD ON CRD7."CardCode" = OCRD."CardCode"
WHERE
    "TaxId0" = :valor or "TaxId4" = :valor
    AND OCRD."CardType" = :type



