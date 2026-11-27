-- Conferencia de pagamento de nota de entrada (contas a pagar, Nota Agil / SoftExpert FXP-NA).
-- Fornecedores (OCRD.CardType = 'S') cujo CNPJ em CRD7.TaxId0 (de qualquer endereco do parceiro)
-- e :cnpj (14 digitos). O cadastro pode ter o CNPJ com ou sem mascara: compara so os digitos.
-- Somente leitura, via sap-odbc (nada e criado no SAP, ao contrario das views do Service Layer).
SELECT DISTINCT
    P."CardCode" AS "CardCode"
FROM CRD7 D
INNER JOIN OCRD P ON P."CardCode" = D."CardCode"
WHERE P."CardType" = 'S'
  AND REPLACE(REPLACE(REPLACE(REPLACE(D."TaxId0", '.', ''), '/', ''), '-', ''), ' ', '') = :cnpj
