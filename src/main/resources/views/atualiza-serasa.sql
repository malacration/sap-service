SELECT
	r."CardCode"
FROM
	OWDD a
	INNER JOIN ODRF r ON r."DocEntry" = a."DraftEntry"
	INNER JOIN OCRD bp ON bp."CardCode" = r."CardCode"
WHERE
	a."WtmCode" = 26
	AND a."Status" = 'W'
	AND a."ProcesStat" = 'W'
	AND a."DocDate" > '2024-01-01 00:00:00.000'
	AND (bp."U_dataSerasa" < :data OR bp."U_dataSerasa" IS NULL)