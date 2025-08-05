SELECT
    "ORIN"."DocEntry"
FROM
    "ORIN"
    INNER JOIN "RIN1" ON "ORIN"."DocEntry" = "RIN1"."DocEntry"
WHERE
	"RIN1"."BaseType" = 203 AND
	"RIN1"."BaseEntry" = :docEntry
