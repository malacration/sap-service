SELECT
	r."DocNum",
	l."InstId"
FROM
	RCT2 l
	INNER JOIN ORCT r ON r."DocEntry"  = l."DocNum"
WHERE
	l."DocEntry" = :docEntryInvoice
	AND (r."Canceled" = 'N' OR r."Canceled" IS NULL)