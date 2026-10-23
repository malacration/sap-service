SELECT
	forma."PayMethCod",
	forma."Descript" as "Description"
FROM
	OPYM as forma
WHERE
	forma."U_envia_forca" = '1'
ORDER BY
	forma."Descript"