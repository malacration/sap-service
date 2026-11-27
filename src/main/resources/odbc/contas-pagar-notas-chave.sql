-- Conferencia de pagamento de nota de entrada (contas a pagar, Nota Agil / SoftExpert FXP-NA).
-- Notas fiscais de entrada (OPCH) NAO canceladas cuja chave de acesso e :chave.
-- U_ChaveAcesso e o campo oficial do TaxPlus (preenchido pela importacao DF-e ou pelo usuario).
-- Nunca usar a tabela "DocReceived" do TaxPlus por DocEntry: o vinculo dela fica desatualizado.
-- Filial e fornecedor sao filtrados no sap-rovema (a chave ja identifica o documento).
SELECT
    N."DocEntry" AS "DocEntry",
    N."DocNum" AS "DocNum",
    N."Serial" AS "Serial",
    N."SeriesStr" AS "SeriesStr",
    N."CardCode" AS "CardCode",
    N."BPLId" AS "BPLId",
    TO_VARCHAR(N."DocDate", 'YYYY-MM-DD') AS "DocDate",
    N."CANCELED" AS "CANCELED",
    N."Model" AS "Model",
    N."U_ChaveAcesso" AS "U_ChaveAcesso"
FROM OPCH N
WHERE N."U_ChaveAcesso" = :chave
  AND N."CANCELED" = 'N'
