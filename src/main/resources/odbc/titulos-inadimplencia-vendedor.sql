-- Relatorio semanal de inadimplencia por vendedor: parcelas abertas e vencidas ate :data.
-- Dois tipos de titulo:
--   NF = parcela da nota fiscal (OINV/INV6);
--   AD = boleto de adiantamento de contrato de venda futura (ODPI/DPI6). Em venda futura quem
--        vira boleto para o cliente e o adiantamento; a nota de entrega so abate o que ja foi pago,
--        entao sem este braco o boleto vencido nunca aparecia no e-mail.
-- Parcela de valor negativo nao e divida (ex.: NF de bonificacao, CFOP 6910, cujo total fica negativo) e fica de fora.
-- Mesmas filiais nos dois bracos. Adiantamento de pedido comum (sem contrato) fica de fora de proposito.
SELECT
    NS."BPLName"   AS "BPLName",
    NS."CardCode"  AS "CardCode",
    NS."CardName"  AS "CardName",
    V."SlpName"    AS "SlpName",
    'NF'           AS "Tipo",
    NS."Serial"    AS "Serial",
    P."InstlmntID" AS "InstlmntID",
    P."InsTotal"   AS "InsTotal",
    P."DueDate"    AS "DueDate",
    NS."DocTotal"  AS "DocTotal",
    P."PaidToDate" AS "PaidToDate",
    C."U_Status"   AS "U_StatusCobranca",
    C."U_Cobrador" AS "U_AgenteCobrador",
    VF."DocNum"    AS "Contrato"
FROM OINV NS
    INNER JOIN INV6 P ON P."DocEntry" = NS."DocEntry"
    LEFT JOIN "@AR_CONTRATO_FUTURO" VF ON VF."DocEntry" = NS."U_venda_futura"
    INNER JOIN OSLP V ON V."SlpCode" = NS."SlpCode"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'NF' AND C."U_DocEntry" = NS."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    NS."DocStatus" IN ('O','D')
    AND NS."BPLId" IN (2,4,11,17,18,12)
    AND P."InsTotal" > 0
    AND P."DueDate" <= :data
    AND P."Status" = 'O'
    AND V."SlpCode" = :slpCode
UNION ALL
SELECT
    T0."BPLName"   AS "BPLName",
    T0."CardCode"  AS "CardCode",
    T0."CardName"  AS "CardName",
    V."SlpName"    AS "SlpName",
    'AD'           AS "Tipo",
    T0."DocNum"    AS "Serial",
    P."InstlmntID" AS "InstlmntID",
    P."InsTotal"   AS "InsTotal",
    P."DueDate"    AS "DueDate",
    T0."DocTotal"  AS "DocTotal",
    P."PaidToDate" AS "PaidToDate",
    C."U_Status"   AS "U_StatusCobranca",
    C."U_Cobrador" AS "U_AgenteCobrador",
    VF."DocNum"    AS "Contrato"
FROM ODPI T0
    INNER JOIN DPI6 P ON P."DocEntry" = T0."DocEntry"
    INNER JOIN "@AR_CONTRATO_FUTURO" VF ON VF."DocEntry" = T0."U_venda_futura"
    INNER JOIN OSLP V ON V."SlpCode" = T0."SlpCode"
    LEFT JOIN "@COB_TITULO" C
         ON C."U_Tipo" = 'AD' AND C."U_DocEntry" = T0."DocEntry" AND C."U_InstlmntID" = P."InstlmntID"
WHERE
    T0."CANCELED" = 'N'
    AND T0."DocStatus" = 'O'
    AND IFNULL(T0."U_TX_DocEntryRef", 0) = 0
    AND T0."BPLId" IN (2,4,11,17,18,12)
    AND P."InsTotal" > 0
    AND P."DueDate" <= :data
    AND P."Status" = 'O'
    AND V."SlpCode" = :slpCode
ORDER BY "DueDate"
