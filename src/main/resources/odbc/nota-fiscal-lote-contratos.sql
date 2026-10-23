-- Contratos de venda futura (UDO AR_CONTRATO_FUTURO) para o PDF "Controle de Pedido" do lote:
-- cabecalho, itens (@AR_CF_LINHA) e CPF/CNPJ do parceiro (CRD7 do cadastro, linha sem
-- endereco; TaxId4 (CPF) antes de TaxId0, como o CpfCnpj do sap-service), numa ida so para o
-- lote inteiro. Uma linha por item; contrato sem item vem com os
-- campos do item nulos.
SELECT
    C."DocEntry" AS "DocEntry",
    C."DocNum" AS "DocNum",
    C."U_cardCode" AS "CardCode",
    C."U_cardName" AS "CardName",
    TO_VARCHAR(C."U_dataCriacao", 'YYYY-MM-DD') AS "DataCriacao",
    C."U_valorFrete" AS "ValorFrete",
    L."LineId" AS "LineId",
    L."U_description" AS "Descricao",
    L."U_quantity" AS "Quantidade",
    L."U_precoNegociado" AS "PrecoNegociado",
    (SELECT MAX(CASE WHEN IFNULL(T."TaxId4", '') <> '' THEN T."TaxId4" ELSE T."TaxId0" END)
     FROM CRD7 T
     WHERE T."CardCode" = C."U_cardCode" AND IFNULL(T."Address", '') = '') AS "CpfCnpj"
FROM "@AR_CONTRATO_FUTURO" C
LEFT JOIN "@AR_CF_LINHA" L ON L."DocEntry" = C."DocEntry"
WHERE C."DocEntry" IN (:contratos)
ORDER BY C."DocEntry", L."LineId"
