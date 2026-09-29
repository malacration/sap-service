SELECT DISTINCT
    "ORIN"."DocEntry"
FROM
    "ORIN"
    INNER JOIN "RIN1" ON "ORIN"."DocEntry" = "RIN1"."DocEntry"
WHERE
	"ORIN"."CANCELED" = 'N' AND
	"RIN1"."BaseType" = 203 AND
	"RIN1"."BaseEntry" = :docEntry
