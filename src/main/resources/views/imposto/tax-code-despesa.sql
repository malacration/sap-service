SELECT D."DocEntry", D."LineNum", D."ExpnsCode", D."TaxCode"
FROM QUT13 D
WHERE D."DocEntry" = :docEntry
	AND D."ObjType" = :objType
	AND D."ExpnsCode" = :expnsCode

UNION ALL

SELECT D."DocEntry", D."LineNum", D."ExpnsCode", D."TaxCode"
FROM RDR13 D
WHERE D."DocEntry" = :docEntry
	AND D."ObjType" = :objType
	AND D."ExpnsCode" = :expnsCode

UNION ALL

SELECT D."DocEntry", D."LineNum", D."ExpnsCode", D."TaxCode"
FROM INV13 D
WHERE D."DocEntry" = :docEntry
	AND D."ObjType" = :objType
	AND D."ExpnsCode" = :expnsCode

UNION ALL

SELECT D."DocEntry", D."LineNum", D."ExpnsCode", D."TaxCode"
FROM RIN13 D
WHERE D."DocEntry" = :docEntry
	AND D."ObjType" = :objType
	AND D."ExpnsCode" = :expnsCode
