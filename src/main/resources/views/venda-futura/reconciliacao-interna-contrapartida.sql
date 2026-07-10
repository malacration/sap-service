SELECT DISTINCT
	other."SrcObjTyp" AS "SrcObjTyp"
FROM
	"ITR1" me
	INNER JOIN "OITR" r ON (r."ReconNum" = me."ReconNum")
	INNER JOIN "ITR1" other ON (other."ReconNum" = me."ReconNum")
WHERE
	me."SrcObjTyp" = :objType
	AND me."SrcObjAbs" = :docEntry
	AND r."Canceled" = 'N'
	AND NOT (other."SrcObjTyp" = :objType AND other."SrcObjAbs" = :docEntry)
