SELECT
	inv."DocEntry",
	inv."ObjType",
	p."KeyNfe"
FROM OINV inv
LEFT JOIN "Process" p ON p."DocType" = inv."ObjType" AND p."DocEntry" = inv."DocEntry"
WHERE inv."DocEntry" = 25991