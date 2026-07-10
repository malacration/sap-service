-- ReconNum das reconciliações internas ATIVAS que ligam DOIS documentos específicos
-- (A e B), cada um identificado por SrcObjTyp + SrcObjAbs. Usado para cancelar apenas a
-- conciliação entre a nota de saída e o lançamento de reclassificação, sem tocar em outras
-- conciliações da nota (ex.: pagamentos).
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
