-- Conferencia de pagamento de nota de entrada (contas a pagar, Nota Agil / SoftExpert FXP-NA).
-- Notas fiscais de entrada (OPCH) NAO canceladas com Serial = :numero de UM fornecedor (:cardCode).
-- CANCELED: 'N' normal, 'Y' cancelada, 'C' documento de cancelamento: so 'N' conta.
-- Model = ONFM.AbsEntry (texto) do modelo fiscal; U_ChaveAcesso = chave de acesso gravada pelo
-- TaxPlus (44 digitos NFe/CTe, 50 digitos NFS-e nacional). SeriesStr e a serie fiscal (3 chars).
-- A filial (BPLId) e o modelo sao filtrados no sap-rovema.
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
WHERE N."Serial" = :numero
  AND N."CardCode" = :cardCode
  AND N."CANCELED" = 'N'
