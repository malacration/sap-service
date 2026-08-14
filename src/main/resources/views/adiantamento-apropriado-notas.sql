SELECT DISTINCT
	nota."DocEntry" AS "DocEntry"
FROM
	INV9 as linha
	INNER JOIN OINV nota ON (linha."DocEntry" = nota."DocEntry")
WHERE
	linha."BaseAbs" = :docEntry
	AND nota."CANCELED" = 'N'