SELECT
    "CardCode"
FROM
    CRD7
WHERE
    ("TaxId0" like :valor or "TaxId4" like :valor)
    AND "CardCode" like 'FOR%'