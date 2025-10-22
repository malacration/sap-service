SELECT c."U_Localidade", d."Name"
FROM RDR1 a
INNER JOIN ORDR b ON a."DocEntry" = b."DocEntry" 
INNER JOIN CRD1 c ON c."CardCode" = b."CardCode" AND c."AdresType" = 'B'
INNER JOIN "@RO_LOCAIS" d ON d."Code" = c."U_Localidade"
WHERE a."DocEntry" = :DocEntry