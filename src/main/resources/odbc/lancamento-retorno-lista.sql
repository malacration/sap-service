-- Face = valor do titulo informado pelo banco (ValuePaid), o mesmo que o BankPlus lanca em
-- Duplicatas Descontadas; pode diferir da parcela no SAP (ValorDaParcela), ex.: 466,67 x 478,29.
-- Retornos processados no BankPlus com ocorrencia de desconto (72, modo Descontado) no periodo.
-- Conta todos os boletos da 72, inclusive os que o BankPlus nao processou porque a API ja tinha
-- liquidado (Divergence "Boleto com status Liquidado nao pode ser processado"): sao esses que
-- faltam no LCM dele.
-- SemSituacao: boletos da 72 que o BankPlus deixou fora da situacao de descontada (15 Carteira
-- Descontada, 19 Descontada - Assets); acontece com o boleto que a API liquidou antes do arquivo.
-- Lcm: o nosso (TransCode DDES, Ref1 convenio-retorno); o estorno copia Ref1 e TransCode, por isso
-- fica de fora o LCM estornado e o proprio estorno (StornoToTr preenchido). LcmBankPlus: o que o proprio BankPlus
-- gerou para o retorno com linha na conta de desconto, IOF ou juros do contrato (so a de desconto
-- nao basta: ela ja mudou e LCM antigo esta em outra conta). Estornados nao contam.
SELECT r."ID" AS "Retorno", r."Description" AS "Arquivo", r."ReturnDateFile" AS "DataArquivo",
       c."ID" AS "Contrato", c."TransferorCode" AS "Convenio", c."CompanyID" AS "Filial", b."BPLName" AS "NomeFilial",
       COUNT(*) AS "Boletos",
       SUM(CASE WHEN o."Divergence" LIKE '%Liquidado%' THEN 1 ELSE 0 END) AS "JaPagos",
       SUM(CASE WHEN COALESCE(bo."Situation", 0) NOT IN (15, 19) THEN 1 ELSE 0 END) AS "SemSituacao",
       SUM(o."ValuePaid") AS "Face", SUM(o."ValueIOF") AS "Iof", SUM(o."ValueTax") AS "Juros",
       (SELECT MAX(j."TransId") FROM "OJDT" j
         WHERE j."TransCode" = 'DDES' AND j."Ref1" = c."TransferorCode" || '-' || TO_VARCHAR(r."ID")
           AND j."StornoToTr" IS NULL
           AND NOT EXISTS (SELECT 1 FROM "OJDT" x WHERE x."StornoToTr" = j."TransId")) AS "Lcm",
       (SELECT MAX(e."LcmDocEntry") FROM "IV_IB_ReturnBankAccountingEntry" e
         WHERE e."ReturnOfBank" = r."ID"
           AND EXISTS (SELECT 1 FROM "JDT1" l WHERE l."TransId" = e."LcmDocEntry" AND l."Account" IN (c."AccountDiscount", c."AccountIOF", c."AccountTax"))
           AND NOT EXISTS (SELECT 1 FROM "OJDT" x WHERE x."StornoToTr" = e."LcmDocEntry")) AS "LcmBankPlus"
FROM "IV_IB_ReturnOfBank" r
JOIN "IV_IB_ReturnBankOcurrence" o ON o."ReturnOfBank" = r."ID" AND o."OccurrenceCode" = '72' AND o."ModeId" = 2
JOIN "IV_IB_ContractBank" c ON c."ID" = r."ContractBank"
LEFT JOIN "OBPL" b ON b."BPLId" = c."CompanyID"
LEFT JOIN "IV_IB_BillOfExchange" bo ON bo."ID" = o."BillOfExchange"
WHERE r."Status" = 3 AND r."ReturnDateFile" BETWEEN :de AND :ate
GROUP BY r."ID", r."Description", r."ReturnDateFile", c."ID", c."TransferorCode", c."CompanyID", b."BPLName",
         c."AccountDiscount", c."AccountIOF", c."AccountTax"
ORDER BY r."ReturnDateFile" DESC, r."ID" DESC
