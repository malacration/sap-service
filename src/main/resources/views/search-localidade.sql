SELECT DISTINCT
c."U_Localidade",
r."Name"
FROM CRD1 c
INNER JOIN "@RO_LOCAIS" r
ON c."U_Localidade" = r."Code"