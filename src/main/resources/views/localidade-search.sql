SELECT
    r."Code",
    r."Name"
FROM
    "@RO_LOCAIS" r
WHERE
    r."Code" LIKE :valor
    OR r."Name" LIKE :valor