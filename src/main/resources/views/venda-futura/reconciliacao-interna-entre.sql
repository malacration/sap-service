SELECT DISTINCT
	r."ReconNum"
FROM
	"OITR" r
	INNER JOIN "ITR1" a ON (a."ReconNum" = r."ReconNum")
	INNER JOIN "ITR1" b ON (b."ReconNum" = r."ReconNum")
WHERE
	r."Canceled" = 'N'
	AND a."SrcObjTyp" = :objTypeA AND a."SrcObjAbs" = :docEntryA
	AND b."SrcObjTyp" = :objTypeB AND b."SrcObjAbs" = :docEntryB
