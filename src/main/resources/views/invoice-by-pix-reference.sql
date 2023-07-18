SELECT DISTINCT
	inv."DocEntry"
FROM
	INV6 inst
	LEFT JOIN OINV inv on(inv."DocEntry" = inst."DocEntry")
where
    inst."U_pix_reference" = :reference