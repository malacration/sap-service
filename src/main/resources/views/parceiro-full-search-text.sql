SELECT
    "OCRD"."CardCode",
    "OCRD"."CardName",
    "CRD7"."TaxId0",
    "CRD7"."TaxId4",
    "OCRD"."SlpCode",
    "OCRD"."CreditLine",
    "OCRD"."Balance"
FROM
	"OCRD"
    LEFT JOIN "CRD7" ON ("OCRD"."CardCode" = "CRD7"."CardCode" AND "CRD7"."Address" = '')
WHERE
	"OCRD"."CardType" = 'C'
	AND ("OCRD"."SlpCode" = :vendedor or "OCRD"."SlpCode" = -1 or "OCRD"."SlpCode" < :superVendedor)
    AND ("CRD7"."TaxId0" like :valor
    OR "CRD7"."TaxId4" like :valor
    OR "OCRD"."CardCode" like :valor
    OR "OCRD"."CardName" like :valor)