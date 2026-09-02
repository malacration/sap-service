SELECT
	'item' AS "tipo",
	"OITM"."ItemCode" AS "codigo",
	"OITM"."ItemName" AS "nome"
FROM "OITM"
WHERE
	"OITM"."ItemCode" LIKE :prefixo
	AND IFNULL("OITM"."ItemName", '') <> ''
	AND "OITM"."ItemName" <> UPPER("OITM"."ItemName")
UNION ALL
SELECT
	'localidade',
	"@RO_LOCAIS"."Code",
	"@RO_LOCAIS"."Name"
FROM "@RO_LOCAIS"
WHERE
	IFNULL("@RO_LOCAIS"."Name", '') <> ''
	AND "@RO_LOCAIS"."Name" <> UPPER("@RO_LOCAIS"."Name")
UNION ALL
SELECT
	'cliente',
	"OCRD"."CardCode",
	"OCRD"."CardName"
FROM "OCRD"
WHERE
	IFNULL("OCRD"."CardName", '') <> ''
	AND "OCRD"."CardName" <> UPPER("OCRD"."CardName")
