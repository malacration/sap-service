SELECT
    T0."CardCode" AS "CardCode",
    T0."CardName" AS "CardName",
    T7."TaxId0" AS "Cnpj",
    T7."TaxId1" AS "InscricaoEstadual",
    T1."Street" AS "Endereco",
    T1."ZipCode" AS "Cep",
    T0."Phone1" AS "Telefone",
    T0."U_sysfeed_status" AS "SysfeedStatus"
FROM OCRD T0
LEFT JOIN CRD7 T7 ON T7."CardCode" = T0."CardCode" AND T7."Address" = ''
LEFT JOIN CRD1 T1 ON T1."CardCode" = T0."CardCode" AND T1."AdresType" = 'B'
WHERE T0."CardType" = 'S'
  AND T0."CardCode" LIKE 'FOR%'
  AND T0."CardCode" >= 'FOR0000032'
  AND T0."CardName" IS NOT NULL
  AND T0."CardName" <> ''
  AND (T0."U_sysfeed_status" IS NULL OR T0."U_sysfeed_status" = '' OR T0."U_sysfeed_status" = 'PENDENTE' OR T0."U_sysfeed_status" = 'ERRO')
ORDER BY T0."CardCode" ASC
