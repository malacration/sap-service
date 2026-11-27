-- Listagem de notas fiscais de entrada (OPCH) para o Nota Agil importar como fonte alternativa ao Qive.
-- Notas nao canceladas das filiais :filiais (BPLId) lancadas (DocDate) entre :dataInicial e :dataFinal.
-- CNPJ/CPF do fornecedor vem da extensao fiscal do documento (PCH12), como gravado na nota.
-- Retencao: categoria I (Fatura) ja foi descontada do DocTotal; categoria P (Pagamento) nao.
-- AnexosPdf: quantos PDFs ha no anexo da nota (ATC1); o par XML + PDF e o padrao das filiais.
-- Fornecedor e numero vao no SQL para valer antes do limite de linhas (o sap-rovema reaplica os dois).
-- :fornecedor (so digitos, 11 ou 14; '' = todos) segue a regra do modelo: CNPJ = digitos de TaxId0 quando
-- sao 14; senao CPF = digitos de TaxId4 quando sao 11. Sem regex (o parser do sap-odbc nao aceita
-- REPLACE_REGEXPR): "digitos de X = :fornecedor" e X ter exatamente LENGTH(:fornecedor) digitos (soma das
-- contagens de cada digito 0-9) E X LIKE :fornecedorPadrao ('%d1%d2%...%dn%', os digitos em ordem).
-- :numero = Serial (-1 = todos).
-- Somente leitura, via sap-odbc.
SELECT
    N."DocEntry" AS "DocEntry",
    N."DocNum" AS "DocNum",
    N."Serial" AS "Serial",
    N."SeriesStr" AS "SeriesStr",
    N."Model" AS "Model",
    N."U_ChaveAcesso" AS "U_ChaveAcesso",
    TO_VARCHAR(N."TaxDate", 'YYYY-MM-DD') AS "TaxDate",
    TO_VARCHAR(N."DocDate", 'YYYY-MM-DD') AS "DocDate",
    N."BPLId" AS "BPLId",
    N."BPLName" AS "BPLName",
    N."CardCode" AS "CardCode",
    N."CardName" AS "CardName",
    T."TaxId0" AS "CnpjFornecedor",
    T."TaxId4" AS "CpfFornecedor",
    N."DocTotal" AS "DocTotal",
    N."DocCur" AS "DocCur",
    N."AtcEntry" AS "AtcEntry",
    (SELECT SUM(W."WTAmnt") FROM PCH5 W WHERE W."AbsEntry" = N."DocEntry" AND W."Category" = 'P') AS "RetencaoPagamento",
    (SELECT SUM(W."WTAmnt") FROM PCH5 W WHERE W."AbsEntry" = N."DocEntry" AND W."Category" = 'I') AS "RetencaoFatura",
    (SELECT COUNT(*) FROM ATC1 A WHERE A."AbsEntry" = N."AtcEntry" AND LOWER(A."FileExt") = 'pdf') AS "AnexosPdf"
FROM OPCH N
LEFT JOIN PCH12 T ON T."DocEntry" = N."DocEntry"
WHERE N."CANCELED" = 'N'
  AND N."BPLId" IN (:filiais)
  AND N."DocDate" >= :dataInicial
  AND N."DocDate" <= :dataFinal
  AND (:fornecedor = ''
       OR (LENGTH(:fornecedor) = 14
           AND (10 * LENGTH(IFNULL(T."TaxId0", ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '0', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '1', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '2', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '3', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '4', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '5', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '6', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '7', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '8', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '9', ''))) = 14
           AND IFNULL(T."TaxId0", '') LIKE :fornecedorPadrao)
       OR (LENGTH(:fornecedor) = 11
           AND (10 * LENGTH(IFNULL(T."TaxId0", ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '0', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '1', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '2', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '3', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '4', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '5', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '6', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '7', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '8', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId0", ''), '9', ''))) <> 14
           AND (10 * LENGTH(IFNULL(T."TaxId4", ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '0', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '1', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '2', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '3', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '4', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '5', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '6', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '7', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '8', ''))
            - LENGTH(REPLACE(IFNULL(T."TaxId4", ''), '9', ''))) = 11
           AND IFNULL(T."TaxId4", '') LIKE :fornecedorPadrao))
  AND (:numero = -1 OR N."Serial" = :numero)
ORDER BY N."DocDate" DESC, N."DocEntry" DESC
