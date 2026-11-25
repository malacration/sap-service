-- Cessionarias ativas (UDT @NFL_CESSIONARIA) para o "Comunicado de Cessao de Titulo" do lote.
-- Code = CNPJ so com digitos. Sem parametros.
SELECT "Code" AS "Codigo", "Name" AS "Nome", "U_Cnpj" AS "Cnpj", "U_Telefone" AS "Telefone", "U_NomePdf" AS "NomePdf"
FROM "@NFL_CESSIONARIA"
WHERE "U_Ativo" = 'Y'
ORDER BY "Name"
