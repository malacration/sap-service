SELECT
	sum(linha."DrawnSum") AS soma
FROM
	INV9 as linha
	INNER JOIN OINV nota ON (linha."DocEntry" = nota."DocEntry")
WHERE
	linha."BaseAbs" = :docEntry
	AND nota."DocStatus" = 'C'
	AND nota."CANCELED" = 'N'