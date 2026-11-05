-- Face = valor do titulo informado pelo banco (ValuePaid), o mesmo que o BankPlus lanca em
-- Duplicatas Descontadas; pode diferir da parcela no SAP (ValorDaParcela), ex.: 466,67 x 478,29.
-- Um retorno do BankPlus com os boletos da ocorrencia de desconto (72, modo Descontado), uma linha
-- por boleto, com o que o LCM precisa: contas do contrato, rateio do retorno e filial do contrato.
-- Mesmas colunas Lcm/LcmBankPlus da lista (lancamento-retorno-lista.sql). MesmoArquivo: outro
-- retorno com o mesmo arquivo, que somaria o desconto duas vezes.
SELECT r."ID" AS "Retorno", r."Description" AS "Arquivo", r."ReturnDateFile" AS "DataArquivo", r."Status" AS "StatusRetorno",
       c."ID" AS "Contrato", c."TransferorCode" AS "Convenio", c."CompanyID" AS "Filial", b."BPLName" AS "NomeFilial",
       c."AccountDiscount" AS "ContaDesconto", c."AccountIOF" AS "ContaIof", c."AccountTax" AS "ContaJuros",
       c."AccountAccounting" AS "ContaClientes",
       CASE WHEN r."EnableCenterCost" = 1 THEN r."OcrCode" END AS "Rateio",
       CASE WHEN r."EnableCenterCost" = 1 THEN r."OcrCode2" END AS "Rateio2",
       (SELECT MAX(j."TransId") FROM "OJDT" j
         WHERE j."TransCode" = 'DDES' AND j."Ref1" = c."TransferorCode" || '-' || TO_VARCHAR(r."ID")
           AND j."StornoToTr" IS NULL
           AND NOT EXISTS (SELECT 1 FROM "OJDT" x WHERE x."StornoToTr" = j."TransId")) AS "Lcm",
       (SELECT MAX(e."LcmDocEntry") FROM "IV_IB_ReturnBankAccountingEntry" e
         WHERE e."ReturnOfBank" = r."ID"
           AND EXISTS (SELECT 1 FROM "JDT1" l WHERE l."TransId" = e."LcmDocEntry" AND l."Account" IN (c."AccountDiscount", c."AccountIOF", c."AccountTax"))
           AND NOT EXISTS (SELECT 1 FROM "OJDT" x WHERE x."StornoToTr" = e."LcmDocEntry")) AS "LcmBankPlus",
       (SELECT MIN(x."ID") FROM "IV_IB_ReturnOfBank" x WHERE x."Description" = r."Description" AND x."ID" <> r."ID") AS "MesmoArquivo",
       o."OurNumber" AS "NossoNumero", o."Document" AS "Documento", o."CardCode" AS "Cliente", o."CardName" AS "NomeCliente",
       o."OccurrenceDate" AS "DataOcorrencia", COALESCE(o."VencimentoBoleto", bo."DueDate") AS "Vencimento",
       o."ValuePaid" AS "Face", o."ValueIOF" AS "Iof", o."ValueTax" AS "Juros", o."Divergence" AS "Divergencia", bo."Situation" AS "Situacao"
FROM "IV_IB_ReturnOfBank" r
JOIN "IV_IB_ReturnBankOcurrence" o ON o."ReturnOfBank" = r."ID" AND o."OccurrenceCode" = '72' AND o."ModeId" = 2
JOIN "IV_IB_ContractBank" c ON c."ID" = r."ContractBank"
LEFT JOIN "OBPL" b ON b."BPLId" = c."CompanyID"
LEFT JOIN "IV_IB_BillOfExchange" bo ON bo."ID" = o."BillOfExchange"
WHERE r."ID" = :retorno
ORDER BY o."OurNumber"
