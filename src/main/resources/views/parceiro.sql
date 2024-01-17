SELECT
    CRD7."CardCode",
    CRD7."TaxId0",
    CRD7."TaxId4"
FROM
    CRD7
    INNER JOIN OCRD ON CRD7."CardCode" = OCRD."CardCode"
WHERE
    CRD7."TaxId0" = :valor or CRD7."TaxId4" = :valor
    AND OCRD."CardType" = :type



