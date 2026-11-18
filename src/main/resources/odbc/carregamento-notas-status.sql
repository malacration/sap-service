-- Notas (OINV) e entregas de NF de venda futura (ODLN) geradas pela ordem de carregamento, com o
-- status da NF-e na TaxPlus ("Process"/"ProcessStatus"; 4 = Autorizado). Sem linha na "Process"
-- = ainda nao foi para a SEFAZ. "Mensagem" = ultimo historico da TaxPlus ("ProcessHist", ligado pelo
-- BatchId, que existe mesmo sem chave de acesso): o motivo da rejeicao ou do erro interno.
-- Boleto da BankPlus (IV_IB_BillOfExchange, DocType 13): "BoletosAtivos" sem os cancelados (status
-- IV_IB_BoeStatus "Cancelado"/"Aguardando Cancelamento") e "ParcelasSemBoleto" = parcelas (INV6) que
-- nunca tiveram boleto e ainda estao em aberto (INV6 "Status" 'O'; paga = 'C') - cancelado conta como ja
-- teve, igual a CarregamentoBoletoService. NF cancelada/estorno ("CANCELED" <> 'N') fica zerada: os
-- endpoints de boleto nao a processam. A tela usa os dois para mostrar "Gerar boletos" e "Baixar
-- boletos". Entrega (ODLN) nao tem boleto: zeros.
SELECT N."DocEntry", 13 AS "DocType", N."DocNum", N."Serial", P."KeyNfe", P."StatusId", S."Description" AS "Status",
    (SELECT H."Hist" FROM "ProcessHist" H WHERE H."Id" = (SELECT MAX(X."Id") FROM "ProcessHist" X WHERE X."BatchId" = P."BatchId")) AS "Mensagem",
    N."PeyMethod" AS "FormaPagamento",
    CASE WHEN N."CANCELED" = 'N' THEN (SELECT COUNT(*) FROM "IV_IB_BillOfExchange" B LEFT JOIN "IV_IB_BoeStatus" BS ON BS."ID" = B."Status"
        WHERE B."DocType" = 13 AND B."DocEntry" = N."DocEntry" AND LOWER(IFNULL(BS."Description", '')) NOT LIKE '%cancel%') ELSE 0 END AS "BoletosAtivos",
    CASE WHEN N."CANCELED" = 'N' THEN (SELECT COUNT(*) FROM "INV6" I WHERE I."DocEntry" = N."DocEntry" AND I."Status" = 'O' AND NOT EXISTS (
        SELECT 1 FROM "IV_IB_BillOfExchange" B WHERE B."DocType" = 13 AND B."DocEntry" = I."DocEntry" AND B."InstallmentID" = I."InstlmntID")) ELSE 0 END AS "ParcelasSemBoleto"
FROM "OINV" N
LEFT JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = 13
LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
WHERE N."U_faturadoOrdemCarregamento" = :ordem
UNION ALL
SELECT N."DocEntry", 15 AS "DocType", N."DocNum", N."Serial", P."KeyNfe", P."StatusId", S."Description" AS "Status",
    (SELECT H."Hist" FROM "ProcessHist" H WHERE H."Id" = (SELECT MAX(X."Id") FROM "ProcessHist" X WHERE X."BatchId" = P."BatchId")) AS "Mensagem",
    N."PeyMethod" AS "FormaPagamento", 0 AS "BoletosAtivos", 0 AS "ParcelasSemBoleto"
FROM "ODLN" N
LEFT JOIN "Process" P ON P."DocEntry" = N."DocEntry" AND P."DocType" = 15
LEFT JOIN "ProcessStatus" S ON S."ID" = P."StatusId"
WHERE N."U_faturadoOrdemCarregamento" = :ordem
