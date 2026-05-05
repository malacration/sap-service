SELECT
"Code","Name"
FROM "@RO_LOCAIS"
WHERE "Code" LIKE :search
   OR "Name" LIKE :search
