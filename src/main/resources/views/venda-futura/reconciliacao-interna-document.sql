SELECT
	r."ReconNum"
FROM
	"ITR1" l
	INNER JOIN "OITR" r on(r."ReconNum" = l."ReconNum")
WHERE
	l."SrcObjTyp" = :objType
	AND l."SrcObjAbs" = :docEntry
	AND r."Canceled" = 'N'