SELECT
	documento."CardCode"
FROM
	CRD7 as documento
	INNER JOIN OCRD as parceiro ON parceiro."CardCode" = documento."CardCode"
WHERE
	parceiro."CardType" = 'C'
	AND (documento."TaxId0" = :comMascara OR documento."TaxId4" = :comMascara
		OR documento."TaxId0" = :semMascara OR documento."TaxId4" = :semMascara)