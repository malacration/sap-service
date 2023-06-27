SELECT
	t."DocEntry"
FROM
    OVPM t
    LEFT JOIN VPM2 line ON t."DocEntry" = line."DocNum"
WHERE
	line."DocEntry" = :DocEntry