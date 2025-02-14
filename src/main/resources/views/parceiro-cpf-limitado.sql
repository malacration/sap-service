SELECT
    "CardCode"
FROM
    OCRD
WHERE
    "LicTradNum" LIKE :valor
    AND "CardCode" LIKE 'FOR%'
