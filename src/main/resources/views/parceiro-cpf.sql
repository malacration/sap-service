SELECT
    "CardCode",
    "TaxId0",
    "TaxId4"
FROM
    CRD7
WHERE
    ("TaxId0" LIKE :valor || '%' OR "TaxId4" LIKE :valor || '%')
    AND "CardCode" LIKE 'FOR%'
