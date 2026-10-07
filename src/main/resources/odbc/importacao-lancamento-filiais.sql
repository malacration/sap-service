-- Filiais citadas no arquivo de importacao de lancamentos (coluna 0 do CSV = BPLID).
SELECT "BPLId", "BPLName", "Disabled"
FROM "OBPL"
WHERE "BPLId" IN (:filiais)
