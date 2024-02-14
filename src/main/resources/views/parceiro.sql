SELECT
    "CardCode"
FROM
    CRD7
WHERE
    ("TaxId0" = :valor or "TaxId4" = :valor)
    AND "CardCode" like 'CLI%'